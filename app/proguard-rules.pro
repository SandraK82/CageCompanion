# Add project specific ProGuard rules here.
# Keep Ktor and kotlinx.serialization classes
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.cagecompanion.**$$serializer { *; }
-keepclassmembers class com.cagecompanion.** {
    *** Companion;
}
-keepclasseswithmembers class com.cagecompanion.** {
    kotlinx.serialization.KSerializer serializer(...);
}
