package dev.androidbasekit.sample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Looper;
import androidx.fragment.app.FragmentActivity;
import dev.androidbasekit.system.permission.CameraPermissionManager;
import dev.androidbasekit.system.permission.PermissionManager;
import dev.androidbasekit.system.permission.PermissionOptions;
import dev.androidbasekit.system.permission.PermissionPrompt;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

/** 原生权限入口、Activity Result 结果分发、弹框和页面生命周期的集成验证。 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Application.class)
public class PermissionManagerTest {
    public static class Host extends FragmentActivity {
        public void deliverSettingsResult(int requestCode) { super.onActivityResult(requestCode, RESULT_CANCELED, null); }
    }

    @Before public void installSystemPermissionMetadata() {
        // Robolectric 不包含完整的系统权限表；与真机一致地声明相机为 dangerous。
        android.content.pm.PermissionInfo camera = new android.content.pm.PermissionInfo();
        camera.name = Manifest.permission.CAMERA;
        camera.protectionLevel = android.content.pm.PermissionInfo.PROTECTION_DANGEROUS;
        shadowOf(RuntimeEnvironment.getApplication().getPackageManager()).addPermissionInfo(camera);
    }

    private void cancelDialog() {
        ((AlertDialog) ShadowDialog.getLatestDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
    }

    @Test public void alreadyGrantedCompletesOnceAndAllowsAnotherRequest() {
        try (var controller = Robolectric.buildActivity(Host.class).setup()) {
            shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA);
            CameraPermissionManager manager = new CameraPermissionManager();
            AtomicInteger calls = new AtomicInteger();
            PermissionOptions options = new PermissionOptions()
                    .onGranted(granted -> { assertTrue(granted.contains(Manifest.permission.CAMERA)); calls.incrementAndGet(); })
                    .onDenied((granted, denied) -> fail("Already granted"));
            assertTrue(manager.isGranted(controller.get()));
            assertTrue(manager.request(controller.get(), options));
            assertEquals(1, calls.get());
            assertTrue(manager.request(controller.get(), options));
            assertEquals(2, calls.get());
        }
    }

    @Test public void explanationCanDeclineAndRejectsConcurrentManagers() {
        try (var controller = Robolectric.buildActivity(Host.class).setup()) {
            shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.CAMERA);
            CameraPermissionManager manager = new CameraPermissionManager();
            AtomicInteger deniedCalls = new AtomicInteger();
            PermissionOptions options = new PermissionOptions().explainBeforeRequest()
                    .onGranted(granted -> fail("Camera denied"))
                    .onDenied((granted, denied) -> {
                        assertTrue(granted.isEmpty());
                        assertEquals(Collections.singletonList(Manifest.permission.CAMERA), denied);
                        deniedCalls.incrementAndGet();
                    });
            assertTrue(manager.request(controller.get(), options));
            assertFalse(new CameraPermissionManager().request(controller.get()));
            cancelDialog();
            assertEquals(1, deniedCalls.get());
            assertTrue(manager.request(controller.get(), options));
            cancelDialog();
            assertEquals(2, deniedCalls.get());
        }
    }

    @Test public void destroyedFragmentViewDismissesPromptAndSuppressesStaleDecision() {
        try (var controller = Robolectric.buildActivity(Host.class).setup()) {
            shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.CAMERA);
            androidx.fragment.app.Fragment fragment = new androidx.fragment.app.Fragment(android.R.layout.simple_list_item_1);
            controller.get().getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment).commitNow();
            AtomicInteger calls = new AtomicInteger();
            PermissionPrompt[] prompt = new PermissionPrompt[1];
            CameraPermissionManager manager = new CameraPermissionManager();
            assertTrue(manager.request(fragment, new PermissionOptions().explainBeforeRequest()
                    .onExplain((decision, permissions, before) -> {
                        prompt[0] = decision;
                        decision.showDialog(new dev.androidbasekit.system.permission.PermissionDialog("Camera", "Continue", "Cancel"));
                    })
                    .onGranted(granted -> calls.incrementAndGet())
                    .onDenied((granted, denied) -> calls.incrementAndGet())));
            var dialog = ShadowDialog.getLatestDialog();
            controller.get().getSupportFragmentManager().beginTransaction().detach(fragment).commitNow();
            shadowOf(Looper.getMainLooper()).idle();
            assertNull(fragment.getView());
            assertFalse(dialog.isShowing());
            prompt[0].proceed();
            prompt[0].cancel();
            assertEquals(0, calls.get());
            assertTrue(manager.request(controller.get(), new PermissionOptions().explainBeforeRequest()));
            cancelDialog();
        }
    }

    @Test public void nativeActivityResultGrantCompletesAndReleasesGate() {
        try (var controller = Robolectric.buildActivity(Host.class).setup()) {
            var activity = controller.get();
            shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.CAMERA);
            AtomicInteger calls = new AtomicInteger();
            CameraPermissionManager manager = new CameraPermissionManager();
            assertTrue(manager.request(activity, new PermissionOptions().onGranted(granted -> calls.incrementAndGet())));
            var request = shadowOf(activity).getLastRequestedPermission();
            assertNotNull(request);
            assertArrayEquals(new String[] {Manifest.permission.CAMERA}, request.requestedPermissions);
            assertFalse(manager.request(activity));
            shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA);
            activity.onRequestPermissionsResult(request.requestCode, request.requestedPermissions,
                    new int[] {PackageManager.PERMISSION_GRANTED});
            shadowOf(Looper.getMainLooper()).idle();
            assertEquals(1, calls.get());
            assertTrue(manager.request(activity));
        }
    }

    @Test public void permanentDenialUsesNativeSettingsLauncherAndRechecksOnReturn() {
        try (var controller = Robolectric.buildActivity(Host.class).setup()) {
            var activity = controller.get();
            shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.CAMERA);
            AtomicInteger calls = new AtomicInteger();
            PermissionPrompt[] settingsPrompt = new PermissionPrompt[1];
            CameraPermissionManager manager = new CameraPermissionManager();
            assertTrue(manager.request(activity, new PermissionOptions()
                    .onGranted(granted -> calls.incrementAndGet())
                    .onForwardToSettings((prompt, permissions) -> settingsPrompt[0] = prompt)));
            var request = shadowOf(activity).getLastRequestedPermission();
            activity.onRequestPermissionsResult(request.requestCode, request.requestedPermissions,
                    new int[] {PackageManager.PERMISSION_DENIED});
            shadowOf(Looper.getMainLooper()).idle();
            assertNotNull(settingsPrompt[0]);
            settingsPrompt[0].proceed();
            var settings = shadowOf(activity).getNextStartedActivityForResult();
            assertEquals(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, settings.intent.getAction());
            assertEquals("package:" + activity.getPackageName(), settings.intent.getDataString());
            assertEquals(0, calls.get());
            shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA);
            activity.deliverSettingsResult(settings.requestCode);
            shadowOf(Looper.getMainLooper()).idle();
            assertEquals(1, calls.get());
            assertTrue(manager.request(activity));
        }
    }

    @Test public void viewDestructionDuringSystemRequestWaitsForResultAndDoesNotDeliver() {
        try (var controller = Robolectric.buildActivity(Host.class).setup()) {
            var activity = controller.get();
            shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.CAMERA);
            androidx.fragment.app.Fragment fragment = new androidx.fragment.app.Fragment(android.R.layout.simple_list_item_1);
            activity.getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commitNow();
            AtomicInteger calls = new AtomicInteger();
            CameraPermissionManager manager = new CameraPermissionManager();
            assertTrue(manager.request(fragment, new PermissionOptions()
                    .onGranted(granted -> calls.incrementAndGet())
                    .onDenied((granted, denied) -> calls.incrementAndGet())));
            var request = shadowOf(activity).getLastRequestedPermission();
            activity.getSupportFragmentManager().beginTransaction().detach(fragment).commitNow();
            shadowOf(Looper.getMainLooper()).idle();
            assertFalse(manager.request(activity));
            activity.onRequestPermissionsResult(request.requestCode, request.requestedPermissions,
                    new int[] {PackageManager.PERMISSION_DENIED});
            shadowOf(Looper.getMainLooper()).idle();
            assertEquals(0, calls.get());
            assertTrue(manager.request(activity, new PermissionOptions().explainBeforeRequest()));
            cancelDialog();
        }
    }

    @Test public void recreationConsumesOldResultWithoutCallingOldPageOrNewRequest() {
        try (var controller = Robolectric.buildActivity(Host.class).setup()) {
            shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.CAMERA);
            CameraPermissionManager manager = new CameraPermissionManager();
            AtomicInteger oldCalls = new AtomicInteger();
            assertTrue(manager.request(controller.get(), new PermissionOptions()
                    .onGranted(granted -> oldCalls.incrementAndGet())
                    .onDenied((granted, denied) -> oldCalls.incrementAndGet())));
            var request = shadowOf(controller.get()).getLastRequestedPermission();
            controller.recreate();
            assertEquals(0, oldCalls.get());
            assertFalse(manager.request(controller.get()));
            shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.CAMERA);
            controller.get().onRequestPermissionsResult(request.requestCode, request.requestedPermissions,
                    new int[] {PackageManager.PERMISSION_GRANTED});
            shadowOf(Looper.getMainLooper()).idle();
            assertEquals(0, oldCalls.get());
            AtomicInteger newCalls = new AtomicInteger();
            assertTrue(manager.request(controller.get(), new PermissionOptions().onGranted(granted -> newCalls.incrementAndGet())));
            assertEquals(1, newCalls.get());
        }
    }

    @Test public void undeclaredPermissionFailsBeforeStartingARequest() {
        try (var controller = Robolectric.buildActivity(Host.class).setup()) {
            assertThrows(IllegalArgumentException.class, () ->
                    new dev.androidbasekit.system.permission.MicrophonePermissionManager().request(controller.get()));
            assertTrue(new CameraPermissionManager().request(controller.get(), new PermissionOptions().explainBeforeRequest()));
            cancelDialog();
        }
    }

    @Test public void versionWithoutRequiredPermissionSucceedsAndStoppedHostIsRejected() {
        PermissionManager manager = new dev.androidbasekit.system.permission.NotificationPermissionManager();
        try (var controller = Robolectric.buildActivity(Host.class).setup()) {
            AtomicInteger calls = new AtomicInteger();
            assertTrue(manager.request(controller.get(), new PermissionOptions().onGranted(permissions -> calls.incrementAndGet())));
            assertEquals(1, calls.get());
            controller.pause().stop();
            assertThrows(IllegalStateException.class, () -> manager.request(controller.get()));
        }
    }
}
