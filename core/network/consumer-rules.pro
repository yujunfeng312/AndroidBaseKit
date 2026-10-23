# Library-specific rules only; avoid keeping the entire framework.

-keepclassmembers,allowobfuscation class dev.androidbasekit.network.BaseResponse {
    @com.google.gson.annotations.SerializedName <fields>;
}
