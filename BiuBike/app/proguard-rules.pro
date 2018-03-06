# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/gaolei/Work/Software/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keep class com.iflytek.**{*;}

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 系统类不需要混淆
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keepattributes Signature
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-dontwarn com.alipay.android.phone.mrpc.core**
-keep class com.alipay.android.phone.mrpc.core.**{*;}

-dontwarn com.alipay.apmobilesecuritysdk.face**
-keep class com.alipay.apmobilesecuritysdk.face.**{*;}

#  百度导航的不需要混淆
-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}
-keep class com.sinovoice.** {*;}
-keep class pvi.com.** {*;}
-dontwarn com.baidu.**
-dontwarn vi.com.**
-dontwarn pvi.com.**





-dontwarn com.sinovoice**
-keep class com.sinovoice.** { *; }

# 百度地图相关不需要混淆
-dontwarn com.baidu**
-keep class com.baidu.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}

# gson工具不需要混淆
-dontwarn com.google.gson**
-keep class com.google.gson.**{*;}

-dontwarn com.nineoldandroids.**
-keep class com.nineoldandroids.**{*;}

-dontwarn org.apache.http**
-keep class org.apache.http.**{*;}

-dontwarn com.jcodecraeer.xrecyclerview.**
-keep class com.jcodecraeer.xrecyclerview.**{*;}
# 自定义控件不需要混淆
-dontwarn com.biubike.custom.**
-keep class com.biubike.custom.**{*;}


-printmapping mapping.txt #混淆后文件映射

#-keep public class com.cheweishi.android.R$*{
#    public static final int *;
#}

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
