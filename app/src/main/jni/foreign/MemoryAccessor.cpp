#include <string.h>
#include <android/log.h>
#include "MemoryAccessor.h"
#include "utils.h"


static bool supportedCritical;

// net.fred.lua.foreign.CriticalNativeTest.test
// When arg0 and arg1 are zero, that means out machine supports CriticalNative
// arg3 is from the stack, it should be unexpect
static void Java_nativeTestMethod(jint arg0, jint arg1, jint arg2) {
    supportedCritical = arg0 == 0 && arg1 == 0 && arg2 != 0;
}

static bool tryCriticalNative(JNIEnv* env) {
    const static JNINativeMethod methods[] = {
        { "test", "(II)V", (void *) &Java_nativeTestMethod }
    };
    jclass testClass = env->FindClass("net/fred/lua/foreign/CriticalNativeTest");
    jmethodID runTest = env->GetStaticMethodID(testClass, "runTest", "()V");
    
    env->RegisterNatives(testClass, methods, 1);
    env->CallStaticVoidMethod(testClass, runTest);
    env->UnregisterNatives(testClass);
    return supportedCritical;
}

//Macro is a good thing.
#define DEF_PUT_FUNCS(TYPE)                                                   \
DEFINE_CRITICAL_FUNCTION_WITH_NON_CRITICAL(putJava##TYPE, {                   \
    critial_putJava##TYPE(handle, value);                                     \
}, static void, jlong handle, TYPE value) {                                   \
    memcpy((void *) handle, &value, sizeof(TYPE));                            \
}

DEF_PUT_FUNCS(jbyte);
DEF_PUT_FUNCS(jint);
DEF_PUT_FUNCS(jshort);
DEF_PUT_FUNCS(jchar);
DEF_PUT_FUNCS(jlong);

static void PutString(JNIEnv *env, jclass clazz, jobject handle, jstring string) {
    const char *str = env->GetStringUTFChars(string, JNI_FALSE);
    size_t string_length = env->GetStringUTFLength(string);
    GET_POINTER_PARAM(env, dst, handle,);
    memcpy(dst, str, string_length + 1);
    env->ReleaseStringUTFChars(string, str);
}

static void putJavaPointer(JNIEnv *env, jclass clazz, jobject _dest, jobject _source) {
    GET_POINTER_PARAM(env, source, _source,);
    GET_POINTER_PARAM(env, dst, _dest,);
    memcpy(dst, &source, sizeof(void *));
}

#undef DEF_PUT_FUNCS

#define DEF_PEEK_FUNCS(TYPE)                                               \
static j##TYPE peekJava##TYPE(JNIEnv *env, jclass clazz, jobject ptr) {    \
    j##TYPE result = 0;                                                    \
    GET_POINTER_PARAM(env, dst, ptr, 0);                                   \
    memcpy(&result, dst, sizeof(result));                                  \
    return result;                                                         \
}                                                                          \
static j##TYPE peekJava##TYPE(JNIEnv *env, jclass clazz, jobject ptr)

DEF_PEEK_FUNCS(byte);
DEF_PEEK_FUNCS(int);
DEF_PEEK_FUNCS(short);
DEF_PEEK_FUNCS(char);
DEF_PEEK_FUNCS(long);

static jobject peekJavaPointer(JNIEnv *env, jclass clazz, jobject dst) {
    GET_POINTER_PARAM(env, dest, dst, nullptr);
    void *result;
    memcpy(&result, dest, sizeof(void *));
    return pointer_create(env, result);
}

static jstring ReadString(JNIEnv *env, jclass clazz, jobject dest) {
    GET_POINTER_PARAM(env, dst, dest, nullptr);
    return env->NewStringUTF((char *) dst);
}

#undef DEF_PEEK_FUNCS

const static JNINativeMethod nonCritial_memoryAccessorMethods[] = {
        {"nativePutByte",     "(JB)V",                 (void *) &nonCritial_putJavajbyte},
        {"nativePutChar",     "(JC)V",                 (void *) &nonCritial_putJavajchar},
        {"nativePutShort",    "(JS)V",                 (void *) &nonCritial_putJavajbyte},
        {"nativePutInt",      "(JI)V",                 (void *) &nonCritial_putJavajint},
        {"nativePutLong",     "(JJ)V",                 (void *) &nonCritial_putJavajlong},
        {"putPointerUnchecked",  "(Lnet/fred/lua/foreign/Pointer;Lnet/fred/lua/foreign/Pointer;)V",
                                                                                       (void *) &putJavaPointer},

        {"peekByteUnchecked",    "(Lnet/fred/lua/foreign/Pointer;)B",                  (void *) &peekJavabyte},
        {"peekCharUnchecked",    "(Lnet/fred/lua/foreign/Pointer;)C",                  (void *) &peekJavachar},
        {"peekShortUnchecked",   "(Lnet/fred/lua/foreign/Pointer;)S",                  (void *) &peekJavashort},
        {"peekIntUnchecked",     "(Lnet/fred/lua/foreign/Pointer;)I",                  (void *) &peekJavaint},
        {"peekLongUnchecked",    "(Lnet/fred/lua/foreign/Pointer;)J",                  (void *) &peekJavalong},
        {"peekPointerUnchecked", "(Lnet/fred/lua/foreign/Pointer;)Lnet/fred/lua/foreign/Pointer;",
                                                                                       (void *) &peekJavaPointer},

        {"putStringUnchecked",   "(Lnet/fred/lua/foreign/Pointer;Ljava/lang/String;)V",
                                                                                       (void *) &PutString},
        {"peekStringUnchecked",  "(Lnet/fred/lua/foreign/Pointer;)Ljava/lang/String;", (void *) &ReadString},
};

const static JNINativeMethod critial_memoryAccessorMethods[] = {
        {"nativePutByte",     "(JB)V",                 (void *) &critial_putJavajbyte},
        {"nativePutChar",     "(JC)V",                 (void *) &critial_putJavajchar},
        {"nativePutShort",    "(JS)V",                 (void *) &critial_putJavajbyte},
        {"nativePutInt",      "(JI)V",                 (void *) &critial_putJavajint},
        {"nativePutLong",     "(JJ)V",                 (void *) &critial_putJavajlong},
        {"putPointerUnchecked",  "(Lnet/fred/lua/foreign/Pointer;Lnet/fred/lua/foreign/Pointer;)V",
                                                                                       (void *) &putJavaPointer},

        {"peekByteUnchecked",    "(Lnet/fred/lua/foreign/Pointer;)B",                  (void *) &peekJavabyte},
        {"peekCharUnchecked",    "(Lnet/fred/lua/foreign/Pointer;)C",                  (void *) &peekJavachar},
        {"peekShortUnchecked",   "(Lnet/fred/lua/foreign/Pointer;)S",                  (void *) &peekJavashort},
        {"peekIntUnchecked",     "(Lnet/fred/lua/foreign/Pointer;)I",                  (void *) &peekJavaint},
        {"peekLongUnchecked",    "(Lnet/fred/lua/foreign/Pointer;)J",                  (void *) &peekJavalong},
        {"peekPointerUnchecked", "(Lnet/fred/lua/foreign/Pointer;)Lnet/fred/lua/foreign/Pointer;",
                                                                                       (void *) &peekJavaPointer},

        {"putStringUnchecked",   "(Lnet/fred/lua/foreign/Pointer;Ljava/lang/String;)V",
                                                                                       (void *) &PutString},
        {"peekStringUnchecked",  "(Lnet/fred/lua/foreign/Pointer;)Ljava/lang/String;", (void *) &ReadString},
};

extern "C" int registerMemoryAccessorFunctions(JNIEnv *env) {
    bool critical = tryCriticalNative(env);
    __android_log_print(ANDROID_LOG_INFO, "NativeRegister", "CriticalNative is %d", critical);
    jclass mem_acc = env->FindClass("net/fred/lua/foreign/internal/MemoryAccessor");
    if (critical) {
        return
            env->RegisterNatives(mem_acc,
                             critial_memoryAccessorMethods,
                             sizeof(critial_memoryAccessorMethods) / sizeof(critial_memoryAccessorMethods[0]));
    } 
    return env->RegisterNatives(mem_acc,
                             nonCritial_memoryAccessorMethods,
                             sizeof(nonCritial_memoryAccessorMethods) / sizeof(nonCritial_memoryAccessorMethods[0]));
}
