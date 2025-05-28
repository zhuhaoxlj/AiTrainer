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

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# 特别保留TypeToken的泛型信息
-keep,allowobfuscation class * extends com.google.gson.reflect.TypeToken

# Keep model classes used for JSON parsing
-keep class com.lindum.mlkit.vision.entity.** { *; }
-keep class com.lindum.mlkit.vision.posedetector.** { *; }
-keep class com.lindum.mlkit.vision.view.** { *; }

# 保留结果相关类
-keep class com.lindum.mlkit.vision.result.** { *; }
-keep class com.lindum.mlkit.vision.result.TrainingResultActivity { *; }
-keep class com.lindum.mlkit.vision.result.ResultEntity { *; }
-keep class com.lindum.mlkit.vision.result.JSONResultEntity { *; }
-keep class com.lindum.mlkit.vision.result.ResultListAdapter { *; }
-keep class com.lindum.mlkit.vision.result.WrongPoseListAdapter { *; }
-keep class com.lindum.mlkit.vision.result.DotTimeLineAdapter { *; }
-keep class com.lindum.mlkit.vision.result.ImageDetails { *; }

# 保留实体类
-keep class com.lindum.mlkit.vision.entity.WrongPoseModel { *; }
-keep class com.lindum.mlkit.vision.utils.TemporaryDataHolder { *; }
-keep class com.lindum.mlkit.vision.utils.FileUtils { *; }

# Keep classes with @Keep annotation
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# 保留assets文件夹中的所有文件
-keepdirectories assets/
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# 确保不混淆assets中的文件
-keep class com.lindum.mlkit.vision.viewmodels.** { *; }

# 保留Fragment和Activity
-keep class com.lindum.mlkit.vision.fragments.** { *; }
-keep class com.lindum.mlkit.vision.MainActivity { *; }
-keep class com.lindum.mlkit.vision.CameraXLivePreviewActivity { *; }

# 保留序列化字段
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile 