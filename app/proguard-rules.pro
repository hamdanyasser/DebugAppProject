# DebugMaster - ProGuard Rules for Release Build
# These rules ensure proper code shrinking and obfuscation while keeping necessary classes

# ===================================
# General Android Settings
# ===================================

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*

# Keep generic signatures for reflection
-keepattributes Signature

# Keep exceptions for better error messages
-keepattributes Exceptions

# ===================================
# Room Database
# ===================================

# Keep all Room entities
-keep @androidx.room.Entity class * {
    *;
}

# Keep all Room DAOs
-keep @androidx.room.Dao class * {
    *;
}

# Keep Room database class
-keep class * extends androidx.room.RoomDatabase {
    *;
}

# Keep TypeConverters
-keep @androidx.room.TypeConverter class * {
    *;
}

# Keep model classes (entities)
-keep class com.example.debugappproject.model.** { *; }

# ===================================
# Gson (JSON Parsing)
# ===================================

# Gson uses generic type information stored in a class file when working with fields
-keepattributes Signature

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

# Keep all model classes that use Gson
-keep class com.example.debugappproject.model.** { *; }

# Prevent Gson from stripping generic type information
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# ===================================
# WorkManager
# ===================================

# Keep WorkManager Worker classes
-keep class * extends androidx.work.Worker {
    public <init>(...);
}

-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}

# Keep our notification worker
-keep class com.example.debugappproject.workers.** { *; }

# ===================================
# Firebase (Optional - if enabled)
# ===================================

# Firebase Authentication
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.Timestamp { *; }

# Keep our Firebase sync classes
-keep class com.example.debugappproject.sync.** { *; }
-keep class com.example.debugappproject.auth.** { *; }

# ===================================
# Navigation Component
# ===================================

-keep class androidx.navigation.fragment.NavHostFragment { *; }
-keepnames class * extends androidx.navigation.Navigator

# ===================================
# ViewBinding
# ===================================

# Keep ViewBinding classes
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** inflate(...);
    public static *** bind(***);
}

# ===================================
# App-Specific Classes
# ===================================

# Keep all Fragment classes
-keep public class * extends androidx.fragment.app.Fragment {
    public <init>(...);
}

# Keep all ViewModel classes
-keep public class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Keep all RecyclerView adapters
-keep public class * extends androidx.recyclerview.widget.RecyclerView$Adapter {
    public <init>(...);
}

# Keep all RecyclerView ViewHolders
-keep public class * extends androidx.recyclerview.widget.RecyclerView$ViewHolder {
    public <init>(...);
}

# Keep utility classes
-keep class com.example.debugappproject.util.** { *; }

# ===================================
# Debugging & Crash Reporting
# ===================================

# Keep native methods (JNI)
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom View constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===================================
# Remove Logging (Optional)
# ===================================

# Remove verbose and debug logs in release builds
# Uncomment these lines to strip out all Log.v() and Log.d() calls
#-assumenosideeffects class android.util.Log {
#    public static *** v(...);
#    public static *** d(...);
#}

# ===================================
# Optimization Settings
# ===================================

# Allow aggressive optimization
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Number of optimization passes
-optimizationpasses 5

# Don't preverify (speeds up build)
-dontpreverify

# Allow code obfuscation
-repackageclasses ''
-allowaccessmodification

# ===================================
# Google Play Billing
# ===================================

# Keep Billing classes
-keep class com.android.vending.billing.** { *; }
-keep class com.android.billingclient.** { *; }

# Keep our billing manager
-keep class com.example.debugappproject.billing.** { *; }

# ===================================
# Hilt Dependency Injection
# ===================================

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ===================================
# Warnings to Ignore
# ===================================

# Ignore warnings about missing classes (Firebase optional dependencies)
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Ignore warnings from other libraries
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**