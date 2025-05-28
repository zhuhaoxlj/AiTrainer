# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep fastjson classes that might be referenced
-dontwarn java.awt.**
-dontwarn javax.money.**
-dontwarn javax.ws.rs.**
-dontwarn org.glassfish.jersey.**
-dontwarn org.javamoney.moneta.**
-dontwarn org.joda.time.**
-dontwarn springfox.documentation.**
-dontwarn javax.servlet.**
-dontwarn org.springframework.**
-dontwarn retrofit2.**

# Keep the fastjson library classes
-keep class com.alibaba.fastjson.** { *; }
-keep class com.alibaba.fastjson.support.** { *; }
-keep class com.alibaba.fastjson.serializer.** { *; }
-keep class com.alibaba.fastjson.parser.** { *; }

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile 