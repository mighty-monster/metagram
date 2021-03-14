# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile




#----------------------------------------------------------#

-dontnote
-verbose


-keepclassmembers class vp.operation.JSInterface {public *;}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface



-keepclasseswithmembers class vp.instapi.requests** { *;}

-keep class com.github.mikephil.charting.** { *; }

# Proguard configuration for Jackson 2.x (fasterxml package instead of codehaus package)
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepclasseswithmembernames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-dontwarn com.squareup.okhttp.**


# OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keepclasseswithmembernames class okhttp3.** { *; }
-keepclasseswithmembernames interface okhttp3.** { *; }
-dontwarn okhttp3.**

#-keep class instagram.requests.**
-dontwarn nava.instapi.requests.**
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

-dontwarn org.kxml2.io.**

-dontwarn okio.**
-keep class okio.** { *; }

-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.* { *; }
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-keep class com.gitonway.lee.niftymodaldialogeffects.lib.** { *; }

-keepclasseswithmembernames class com.michaelmuenzer.android.** {*;}

-keepclasseswithmembernames class com.viksaa.sssplash.lib.activity.** {*;}

-keep class net.sqlcipher.** {*;}

-keepclassmembers enum * { *; }

-keep class io.michaelrocks.** {*;}
-keep class java.lang.annotation.** {*;}

-keepclasseswithmembernames class org.angmarch.views.** {*;}

-keep class com.android.vending.billing

-keepclasseswithmembernames class com.github.amlcurran.showcaseview.** {*;}

-keepattributes Exceptions