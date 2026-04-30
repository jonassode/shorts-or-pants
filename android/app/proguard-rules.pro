# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the default configuration in the Android SDK.

# Keep WebView related classes
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
