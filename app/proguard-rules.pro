# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/amine/apps/android-sdk-linux/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#About libraries
-keep class .R
-keep class **.R$* {
    <fields>;
}


##Retrofit
#-keep class com.google.gson.** { *; }
#-keep class com.google.inject.** { *; }
#-keep class org.apache.http.** { *; }
#-keep class org.apache.james.mime4j.** { *; }
#-keep class javax.inject.** { *; }
#-keep class retrofit.** { *; }
#-keepclassmembernames interface * {
#    @retrofit.http.* <methods>;
#}
#-keep class retrofit.** { *; }
#-keep class apps.amine.bou.readerforselfoss.api.selfoss.model.** { *; }
#-keepclassmembernames interface * {
#    @retrofit.http.* <methods>;
#}
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault


#Bottom bar lib
-dontwarn com.roughike.bottombar.**