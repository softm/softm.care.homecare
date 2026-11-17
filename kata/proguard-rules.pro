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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-keep class net.softm.data.kata.dto.** { *; }
-keep class net.softm.data.kata.util.MapPoint { *; }
-keep class net.softm.data.kata.common.** { *; }
-keep class net.softm.data.kata.web.** { *; }
-keep class androidx.core.app.CoreComponentFactory { *; }

-keep class net.softm.data.kata.god.dto.** { *; }
-keep class net.softm.data.kata.god.common.** { *; }

-keep class com.google.firebase.** { *; }

-keep class com.kakao.sdk.**.model.* { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
-keep class androidx.work.** { *; }

-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type

#-keep class net.softm.data.kata.SoftmImageButton { *; }
-keepclassmembers class * {
    *** TAG;
}

-keep public class com.google.android.gms.ads.**{
   public *;
}

-keep public class com.google.ads.**{
   public *;
}

# https://github.com/square/okhttp/pull/6792
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.**
