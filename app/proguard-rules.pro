# R8 / ProGuard optimization & obfuscation rules for AnimaCura_SK
# Preserves stack trace line numbers with mapping.txt uploaded inside AAB for Play Console.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database & Entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Keep classes/members explicitly annotated with @Keep
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Android framework XML PullParser used for live RSS feeds
-dontwarn org.xmlpull.v1.**
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Coroutines & OkHttp / Conscrypt optional JVM warnings
-dontwarn sun.misc.Unsafe
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
