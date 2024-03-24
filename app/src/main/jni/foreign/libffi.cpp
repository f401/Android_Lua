//
// Created by root on 7/27/23.
//
#include "utils.h"

extern "C" {
#include "../common.h"
#include <ffi.h>
#include <stdlib.h>
}

static jclass class_type;

#define EQUAL_TO_NULL(ENV, NEEDLE) (NEEDLE == nullptr || env->IsSameObject(NEEDLE, nullptr))

#define LOAD_CLASS_TYPE(ENV, EXPR) \
FIND_CLASS(ENV, type, "net/fred/lua/foreign/types/Type"); \
IF_NULL_RETURN(class_type, EXPR)

static void *get_type_ffi_pointer(JNIEnv *env, jobject type) {
    LOAD_CLASS_TYPE(env, nullptr);

    static jmethodID method_type_get_pointer;
    FIND_INSTANCE_METHOD(env, type, get_pointer, "getFFIPointer",
                         "()Lnet/fred/lua/foreign/Pointer;");
    IF_NULL_RETURN(method_type_get_pointer, nullptr);

    return pointer_get_from(env, env->CallObjectMethod(type, method_type_get_pointer));
}

static jint
prep_cif(JNIEnv *env, jobject thiz, void *_cif, jobject return_type, jobjectArray _params);

extern "C"
JNIEXPORT jint JNICALL
Java_net_fred_lua_foreign_ffi_FunctionDescriber_prep_1cif(JNIEnv *env, jobject thiz,
                                                          jobject _cif,
                                                          jobject return_type,
                                                          jobjectArray _params) {
    GET_POINTER_PARAM(env, cif, _cif, -1);
    LOAD_CLASS_TYPE(env, -1);
    return prep_cif(env, thiz, cif, return_type, _params);
}

static jint
prep_cif(JNIEnv *env, jobject thiz, void *_cif, jobject return_type, jobjectArray _params) {
    auto *returnType = static_cast<ffi_type *>(get_type_ffi_pointer(env, return_type));
    IF_NULL_RETURN(returnType, -1);

    static jmethodID method_desc_request;
    jclass class_desc = env->GetObjectClass(thiz);
    FIND_INSTANCE_METHOD(env, desc, request, "requestMemory", "(J)J");
    IF_NULL_RETURN(method_desc_request, -1);

    // Obtain a pointer to store CIF (already allocated by the Java layer).
    auto *cif = (ffi_cif *) _cif;

    //solve params
    ffi_type **params = nullptr;
    size_t params_len = 0;
    if (!(EQUAL_TO_NULL(env, _params))) { //has params
        params_len = env->GetArrayLength(_params);
        if (params_len != 0) { //has data
            params = jlong_to_ptr(env->CallLongMethod(thiz, method_desc_request,
                                                      (jlong) params_len * sizeof(void *)),
                                  ffi_type *); // Allocate space for storage pointers.
            for (jsize i = 0; i < params_len; ++i) {
                jobject curr = env->GetObjectArrayElement(_params, i);

                void *curr_ptr = get_type_ffi_pointer(env, curr);
                IF_NULL_RETURN(curr_ptr, -2);
                env->DeleteLocalRef(curr);

                params[i] = (ffi_type *) curr_ptr;
            }
        }
    }
    return ffi_prep_cif(cif, FFI_DEFAULT_ABI, params_len, returnType, params);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_net_fred_lua_foreign_ffi_FunctionCaller_ffi_1call(JNIEnv *env, jobject thiz,
                                                       jobject mem_accessor,
                                                       jobject mem_allocator,
                                                       jobject func_describer,
                                                       jobject _cif,
                                                       jobject _func_address,
                                                       jobjectArray _typed_params,// describer.getParams()
                                                       jobjectArray _params,
                                                       jobject return_type) {
    LOAD_CLASS_TYPE(env, nullptr);
    jclass class_functionCaller = env->GetObjectClass(thiz);
    static jmethodID method_functionCaller_evalTotalSize, method_type_getSize;
    FIND_INSTANCE_METHOD(env, type, getSize, "getSize", "(Ljava/lang/Object;)I");
    IF_NULL_RETURN(method_type_getSize, nullptr);

    FIND_INSTANCE_METHOD(env, functionCaller, evalTotalSize, "evalParamsTotalSize",
                         "([Ljava/lang/Object;)J");
    IF_NULL_RETURN(method_functionCaller_evalTotalSize, nullptr);

    void **params_ptr = nullptr;
    jsize length;
    //assign params data
    if (!(EQUAL_TO_NULL(env, _params) || (length = env->GetArrayLength(_typed_params)) == 0)) {
        // Save pointers that truly store data
        char *params_data = (char *) alloca(
                env->CallLongMethod(thiz, method_functionCaller_evalTotalSize, _params));
        params_ptr = (void **) alloca(sizeof(void *) * length); //lazy init
        jint off = 0;

        LOAD_CLASS_TYPE(env, nullptr);
        static jmethodID method_type_write;
        FIND_INSTANCE_METHOD(env, type, write, "write",
                             "(Lnet/fred/lua/foreign/internal/MemoryAccessor;Lnet/fred/lua/foreign/Pointer;Ljava/lang/Object;)V");
        IF_NULL_RETURN(method_type_write, nullptr);

        for (jsize i = 0; i < length; ++i) {
            jobject typed = env->GetObjectArrayElement(_typed_params, i);
            jobject param = env->GetObjectArrayElement(_params, i);

            void *dest_ptr = params_data + off;
            params_ptr[i] = dest_ptr;

            jobject dest = pointer_create(env, dest_ptr);
            env->CallVoidMethod(typed, method_type_write, mem_accessor, dest, param);
            env->DeleteLocalRef(dest);

            off += env->CallIntMethod(typed, method_type_getSize, param);

            env->DeleteLocalRef(typed);
            env->DeleteLocalRef(param);
        }
    }
    void *return_segment = nullptr;
    int rsize;
    if ((rsize = env->CallIntMethod(return_type, method_type_getSize, nullptr)) != 0) {
        return_segment = alloca(rsize);
    }
    void *cif = alloca(sizeof(ffi_cif));
    if (EQUAL_TO_NULL(env, _cif)) {
        if (prep_cif(env, func_describer, cif, return_type, _typed_params) != FFI_OK) {
            return nullptr;
        }
    } else {
        GET_POINTER_PARAM(env, __cif, _cif, nullptr);
        cif = __cif;
    }

    GET_POINTER_PARAM(env, func_addr, _func_address, nullptr);
    //real call
    ffi_call((ffi_cif *) cif, (void (*)()) func_addr, return_segment, params_ptr);

    if (rsize != 0) {
        static jmethodID method_type_read;
        FIND_INSTANCE_METHOD(env, type, read, "read",
                             "(Lnet/fred/lua/foreign/allocator/IAllocator;Lnet/fred/lua/foreign/internal/MemoryAccessor;Lnet/fred/lua/foreign/Pointer;)Ljava/lang/Object;");
        IF_NULL_RETURN(method_type_read, nullptr);

        jobject _ret_ptr = pointer_create(env, return_segment);
        IF_NULL_RETURN(_ret_ptr, nullptr);

        return env->CallObjectMethod(return_type, method_type_read, mem_allocator, mem_accessor,
                                     _ret_ptr);
    }
    return nullptr;
}