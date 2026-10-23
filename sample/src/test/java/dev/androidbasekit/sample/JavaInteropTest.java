package dev.androidbasekit.sample;

import dev.androidbasekit.sample.view.adapter.DemoAdapter;
import dev.androidbasekit.sample.model.api.DemoApi;
import dev.androidbasekit.sample.model.entity.Photo;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.room.Room;
import dev.androidbasekit.common.*;
import dev.androidbasekit.image.ImageLoader;
import dev.androidbasekit.network.*;
import dev.androidbasekit.storage.*;
import dev.androidbasekit.ui.*;
import dev.androidbasekit.sample.databinding.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

/**
 * JavaInteropTest 回归测试集。通过测试方法验证对应模块行为、错误与边界条件；测试结果见验收说明。
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Application.class)
public class JavaInteropTest {
    /** 工厂式 Activity，页面仅传静态 inflate 方法，由 Base 统一创建布局。 */
    public static final class FactoryActivity extends BaseActivity<ActivityResultBinding> {
        public FactoryActivity() { super(ActivityResultBinding::inflate); }
        @Override protected void onBindingCreated(Bundle state) { }
        public ActivityResultBinding bindingForTest() { return getBinding(); }
    }

    /** 具备公开无参构造的工厂式 Fragment，验证系统重建和 Binding 的视图作用域。 */
    public static final class FactoryFragment extends BaseFragment<FragmentDetailsBinding> {
        public FactoryFragment() { super(FragmentDetailsBinding::inflate); }
        @Override protected void onBindingCreated(Bundle state) { getBinding().title.setText("factory"); }
        public FragmentDetailsBinding bindingForTest() { return getBinding(); }
    }

    /**
     * Java 互操作测试 Activity。验证 Kotlin 基类的 Binding 初始化。
     */
    public static final class TestActivity extends BaseActivity<ActivityResultBinding> {
        @Override protected ActivityResultBinding inflateBinding(LayoutInflater inflater) { return ActivityResultBinding.inflate(inflater); }
        @Override protected void onBindingCreated(Bundle state) { }
    }
    /**
     * Java 互操作测试 Fragment。验证视图销毁后的 Binding 释放。
     */
    public static final class TestFragment extends BaseFragment<FragmentDetailsBinding> {
        @Override protected FragmentDetailsBinding inflateBinding(LayoutInflater inflater, ViewGroup parent) { return FragmentDetailsBinding.inflate(inflater, parent, false); }
        @Override protected void onBindingCreated(Bundle state) { getBinding().title.setText("test"); }
        public void requireBinding() { getBinding(); }
    }
    @Test public void staticApiAndDefaultConstructorsAreCallable() {
        MemoryCache<String> cache = new MemoryCache<>(); cache.put("k", "v", 0);
        assertEquals("v", cache.get("k"));
        ApiResult<String> result = ApiResult.success("ok"); assertTrue(result.isSuccess()); assertEquals("ok", result.getOrNull());
        NetworkConfig config = new NetworkConfig("https://example.com/"); assertEquals(30, config.getTimeoutSeconds());
        assertEquals(PageStatus.CONTENT, PageState.content().getStatus());
        assertNotNull(new DemoAdapter(item -> {}));
    }
    @Test public void javaActivityAndFragmentUseKotlinBindingLifecycle() {
        try (var controller = Robolectric.buildActivity(TestActivity.class).setup()) {
            TestActivity activity = controller.get();
            TestFragment fragment = new TestFragment();
            activity.getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commitNow();
            fragment.requireBinding();
            activity.getSupportFragmentManager().beginTransaction().remove(fragment).commitNow();
            try { fragment.requireBinding(); fail("Destroyed binding retained"); } catch (IllegalStateException expected) { }
        }
    }

    @Test public void bindingFactorySurvivesRecreationAndReleasesOldView() {
        try (var controller = Robolectric.buildActivity(FactoryActivity.class).setup()) {
            FactoryActivity activity = controller.get();
            ActivityResultBinding originalActivityBinding = activity.bindingForTest();
            FactoryFragment fragment = new FactoryFragment();
            activity.getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, fragment, "factory").commitNow();
            FragmentDetailsBinding originalBinding = fragment.bindingForTest();
            assertSame(activity.findViewById(android.R.id.content), originalBinding.getRoot().getParent());
            controller.recreate();
            FactoryActivity recreated = controller.get();
            recreated.getSupportFragmentManager().executePendingTransactions();
            FactoryFragment restored = (FactoryFragment) recreated.getSupportFragmentManager().findFragmentByTag("factory");
            assertNotNull(restored);
            assertNotSame(originalActivityBinding, recreated.bindingForTest());
            assertNotSame(originalBinding, restored.bindingForTest());
            assertEquals("factory", restored.bindingForTest().title.getText().toString());
            try { fragment.bindingForTest(); fail("Old binding retained after recreate"); }
            catch (IllegalStateException expected) { }
            recreated.getSupportFragmentManager().beginTransaction().remove(restored).commitNow();
            try { restored.bindingForTest(); fail("Destroyed binding retained"); }
            catch (IllegalStateException expected) { }
        }
    }
    @Test public void lifecycleCancelsJavaCallbackRequest() throws Exception {
        try (var controller = Robolectric.buildActivity(TestActivity.class).setup();
             var server = new okhttp3.mockwebserver.MockWebServer()) {
            server.enqueue(new okhttp3.mockwebserver.MockResponse().setBody("[]").setBodyDelay(10, java.util.concurrent.TimeUnit.SECONDS));
            DemoApi api = new NetworkClient(new NetworkConfig(server.url("/").toString())).create(DemoApi.class);
            retrofit2.Call<java.util.List<Photo>> call = api.photosCall(1);
            JavaRequests.enqueue(controller.get(), call, new ResultCallback<>() {
                public void onSuccess(java.util.List<Photo> data) { fail("Callback after destroy"); }
                public void onError(AppError error) { fail("Cancellation mapped to error"); }
            });
            controller.pause().stop().destroy();
            assertTrue(call.isCanceled());
        }
    }
    @Test public void imageClearWorksAfterActivityDestroyed() {
        var controller = Robolectric.buildActivity(TestActivity.class).setup();
        android.widget.ImageView image = new android.widget.ImageView(controller.get());
        controller.pause().stop().destroy();
        ImageLoader.clear(image);
    }

    /**
     * Java 响应包裹测试接口。验证 BaseRequest 对 BaseResponse 的解包。
     */
    public interface EnvelopeApi {
        @retrofit2.http.GET("profile") retrofit2.Call<BaseResponse<Photo>> profile();
    }
    @Test public void javaBaseRequestUnwrapsEnvelopeWithConfiguredCode() throws Exception {
        try (var controller = Robolectric.buildActivity(TestActivity.class).setup();
             var server = new okhttp3.mockwebserver.MockWebServer()) {
            server.enqueue(new okhttp3.mockwebserver.MockResponse().setBody(
                "{\"code\":200,\"message\":\"ok\",\"data\":{\"id\":1,\"title\":\"remote\",\"thumbnailUrl\":\"\"}}"));
            EnvelopeApi api = new NetworkClient(new NetworkConfig(server.url("/").toString())).create(EnvelopeApi.class);
            BaseRequest<Photo> request = BaseRequest.forOwner(controller.get());
            request.enqueueResponse(api.profile(), 200);
            long deadline = System.currentTimeMillis() + 5000;
            while (request.getState().getValue().getStatus() == RequestStatus.RUNNING && System.currentTimeMillis() < deadline) {
                org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
                Thread.sleep(10);
            }
            assertEquals(RequestStatus.SUCCESS, request.getState().getValue().getStatus());
            assertEquals("remote", request.getState().getValue().getData().getTitle());
        }
    }
    @Test public void javaBaseRequestCancelsCallWithOwner() throws Exception {
        try (var controller = Robolectric.buildActivity(TestActivity.class).setup();
             var server = new okhttp3.mockwebserver.MockWebServer()) {
            server.enqueue(new okhttp3.mockwebserver.MockResponse().setBody("[]").setBodyDelay(10, java.util.concurrent.TimeUnit.SECONDS));
            DemoApi api = new NetworkClient(new NetworkConfig(server.url("/").toString())).create(DemoApi.class);
            BaseRequest<java.util.List<Photo>> request = BaseRequest.forOwner(controller.get());
            retrofit2.Call<java.util.List<Photo>> call = api.photosCall(1);
            request.enqueue(call); controller.pause().stop().destroy();
            assertTrue(call.isCanceled());
            assertEquals(RequestStatus.CANCELLED, request.getState().getValue().getStatus());
            assertNull(request.getState().getValue().getError());
        }
    }

}
