# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
#
# This file is no longer maintained and is not used by new (2.2+) versions of the
# Android plugin for Gradle. Instead, the Android plugin for Gradle generates the
# default rules at build time and stores them in the build directory.

# Optimizations: If you don't want to optimize, use the
# proguard-android.txt configuration file instead of this one, which
# turns off the optimization flags.  Adding optimization introduces
# certain risks, since for example not all optimizations performed by
# ProGuard works on all versions of Dalvik.  The following flags turn
# off various optimizations known to have issues, but the list may not
# be complete or up to date. (The "arithmetic" optimization can be
# used if you are only targeting Android 2.0 or later.)  Make sure you
# test thoroughly if you go this route.
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# The remainder of this file is identical to the non-optimized version
# of the Proguard configuration file (except that the other file has
# flags to turn off optimization).

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

-keepattributes *Annotation*
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

# Understand the @Keep support annotation.
-keep class android.support.annotation.Keep

-keep @android.support.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}
############
#-libraryjars ../mtranskey/libs/mtk_v4.6.0.31.jar

-printmapping map.txt
-printseeds seed.txt
-printusage usage.txt
-printconfiguration config3.txt

-keep public class * { public protected *; }
-keep class org.apache.commons.**
-keep interface org.apache.commons.**
-keep class org.apache.commons.**{
public *;
}

-keep class org.apache.http.**
-keep interface org.apache.http.**
-keep class org.apache.http.** {
public *;
}

-keep class org.tukaani.**
-keep interface org.tukaani.**
-keep class org.tukaani.** {
public *;
}
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**

#Databinding
-dontwarn android.databinding.**
-keep class android.databinding.** { *; }

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
-dontwarn org.apache.commons.compress.compressors.xz.XZCompressorInputStream
-dontwarn org.apache.commons.compress.compressors.xz.XZCompressorOutputStream

#-keep class android.graphics.**
#-keep class com.bumptech.glide.load.resource.bitmap.VideoDecoder
#-keep interface com.bumptech.glide.load.resource.bitmap.VideoDecoder

-keep class com.bumptech.glide.**
-keep interface com.bumptech.glide.**
-keep class com.bumptech.glide.** {
public *;
}

-keep class com.softsecurity.transkey.**
-keep interface com.softsecurity.transkey.**
-keep class com.softsecurity.transkey.** {
public *;
}
-keep class com.softsecurity.transkey.KeypadView.reArrangeKey { *; }


-dontwarn com.softsecurity.transkey.**
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8

# ·Î±×º½
-verbose

