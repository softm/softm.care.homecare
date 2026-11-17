-dontshrink
-dontoptimize
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*
-optimizationpasses 5
-allowaccessmodification

 # 로그봄
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

# my config
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

-keep class com.softsecurity.transkey.KeypadView.reArrangeKey
-keep interface com.softsecurity.transkey.KeypadView.reArrangeKey
-keep class com.softsecurity.transkey.KeypadView.reArrangeKey {
public *;
}
-keep class com.softsecurity.transkey.KeypadView.reArrangeKey { *; }


-dontwarn com.softsecurity.transkey.**
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8

