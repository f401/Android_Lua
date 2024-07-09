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

-printmapping out.mapping

-keepclasseswithmembers class * {
    native <methods>;
}

-keep public class * extends net.fred.lua.common.activity.BaseActivity {}
-keep public class * extends android.app.Application {}
-keep public class * extends android.view.View {}

-keepclasseswithmembers interface net.fred.lua.foreign.types.Type {
    net.fred.lua.foreign.Pointer getFFIPointer();
    void write(net.fred.lua.foreign.MemoryAccessor, net.fred.lua.foreign.Pointer, java.lang.Object);
    java.lang.Object read(net.fred.lua.foreign.allocator.IAllocator, net.fred.lua.foreign.MemoryAccessor, net.fred.lua.foreign.Pointer);
    int getSize(java.lang.Object);
}
-keepclasseswithmembers class net.fred.lua.foreign.Pointer {
    static net.fred.lua.foreign.Pointer from(long);
    long get();
}
-keep class net.fred.lua.foreign.NativeMethodException {}