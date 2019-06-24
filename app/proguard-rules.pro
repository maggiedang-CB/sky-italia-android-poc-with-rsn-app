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
-dontwarn com.google.**
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn retrofit.**
-dontwarn okio.*
-dontwarn org.joda.**
-dontwarn com.comscore.**
-dontwarn com.urbanairship.push.fcm.**

-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontobfuscate
-dontoptimize
-dontpreverify
-keepattributes
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-keep enum ** { *; }
-keep class !org.apache.**, !com.google.**, !android.support.v4.**, !android.support.v4.app.** { *; }
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-keep class com.comscore.** { *; }
