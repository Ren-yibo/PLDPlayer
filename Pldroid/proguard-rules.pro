# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Studio1.5.0\sdk/tools/proguard/proguard-android.txt
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
#--------------- BEGIN: 七牛 ----------
#播放
-keep class com.qiniu.**{*;}
-keep class com.qiniu.**{public <init>();}
-ignorewarnings
#直播推送
-keep class com.qiniu.pili.droid.streaming.** { *; }
#--------------- END: 七牛 ----------