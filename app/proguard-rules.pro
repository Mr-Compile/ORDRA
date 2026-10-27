# Ordra release rules — extend when a release build needs them.

# Ordra backup codec — entities are serialized to JSON by field name
-keep class com.ordra.data.local.entity.** { *; }
-keep class com.ordra.data.backup.** { *; }
-keepclassmembers class * extends java.lang.Enum { <fields>; public static **[] values(); }
