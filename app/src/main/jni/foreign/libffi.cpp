//
// Created by root on 7/27/23.
//
#include <android/log.h>
#include "utils.h"

extern "C" {
#include "../common.h"
#include <ffi.h>
#include <stdlib.h>
}

static jclass class_type;
static jclass class_allocator;
static jclass class_resource;

#define EQUAL_TO_NULL(ENV, NEEDLE) (NEEDLE == nullptr || env->IsSameObject(NEEDLE, nullptr))

#define LOAD_CLASS_TYPE(ENV, EXPR) \
FIND_CLASS(ENV, type, "net/fred/lua/foreign/types/Type"); \
IF_NULL_RETURN(class_type, EXPR)

#define LOAD_CLASS_ALLOCATOR(ENV, EXPR) \
FIND_CLASS(ENV, allocator, "net/fred/lua/foreign/allocator/IAllocator"); \
IF_NULL_RETURN(class_allocator, EXPR)

#define LOAD_CLASS_RESOURCE(ENV, EXPR) \
FIND_CLASS(ENV, resource, "net/fred/lua/foreign/Resource"); \
IF_NULL_RETURN(class_resource, EXPR)

static void *get_type_ffi_pointer(JNIEnv *env, jobject type) {
    if (EQUAL_TO_NULL(env, type)) {
        return &ffi_type_void;
    }
    LOAD_CLASS_TYPE(env, nullptr);

    static jmethodID method_type_get_pointer;
    FIND_INSTANCE_METHOD(env, type, get_pointer, "getFFIPointer",
                         "()Lnet/fred/lua/foreign/Pointer;");
    IF_NULL_RETURN(method_type_get_pointer, nullptr);

    return pointer_get_from(env, env->CallObjectMethod(type, method_type_get_pointer));
}

static int
do_prep_cif(JNIEnv *env, void *_cif, jobject allocator, jobject return_type, jobjectArray _params) {
    auto *returnType = static_cast<ffi_type *>(get_type_ffi_pointer(env, return_type));

    static jmethodID method_allocator_allocateMemory;
    static jmethodID method_resource_getBasePointer;
    LOAD_CLASS_ALLOCATOR(env, -2);
    LOAD_CLASS_RESOURCE(env, -2);
    FIND_INSTANCE_METHOD(env, allocator, allocateMemory, "allocateMemory",
                         "(J)Lnet/fred/lua/foreign/Resource;");
    IF_NULL_RETURN(method_allocator_allocateMemory, -2);
    FIND_INSTANCE_METHOD(env, resource, getBasePointer, "getBasePointer",
                         "()Lnet/fred/lua/foreign/Pointer;");
    IF_NULL_RETURN(method_resource_getBasePointer, -2);

    //solve params
    ffi_type **params = nullptr;
    size_t params_len = 0;
    if (!(EQUAL_TO_NULL(env, _params))) { // has params
        params_len = env->GetArrayLength(_params);
        if (params_len != 0) { // has data
            // Allocate Memory For Params
            {
                jobject resource = env->CallObjectMethod(allocator,
                                                         method_allocator_allocateMemory,
                                                         static_cast<jlong>(sizeof(ffi_type *) *
                                                                            params_len));
                jobject basePointer = env->CallObjectMethod(resource, method_resource_getBasePointer);
                params = reinterpret_cast<ffi_type **>(pointer_get_from(env, basePointer));
                IF_NULL_RETURN(params, -2);
                env->DeleteLocalRef(resource);
                env->DeleteLocalRef(basePointer);
            }
            for (jsize i = 0; i < params_len; ++i) {
                jobject curr = env->GetObjectArrayElement(_params, i);

                void *curr_ptr = get_type_ffi_pointer(env, curr);
                IF_NULL_RETURN(curr_ptr, -2);
                env->DeleteLocalRef(curr);

                params[i] = (ffi_type *) curr_ptr;
            }
        }
    }
    __android_log_print(ANDROID_LOG_INFO, "Caller", "cif: %ld, params_len: %d, returnType: %d, parmas: %d", _cif, params_len, returnType, params);
    return ffi_prep_cif(reinterpret_cast<ffi_cif *>(_cif), FFI_DEFAULT_ABI, params_len, returnType,
                        params);
}

extern "C"
JNIEXPORT jint JNICALL
Java_net_fred_lua_foreign_core_ffi_FunctionDescriptor_prep_1cif(JNIEnv *env, jobject thiz,
                                                                jobject _cif, jobject scope,
                                                                jobject return_type,
                                                                jobjectArray params) {
    GET_POINTER_PARAM(env, cif, _cif, -1);
    return do_prep_cif(env, cif, scope, return_type, params);
}
extern "C"
JNIEXPORT jobject JNICALL
Java_net_fred_lua_foreign_core_ffi_FunctionCaller_ffi_1call(JNIEnv *env, jobject thiz,
                                                            jobject accessor, jobject allocator,
                                                            jobject _cif, jobject func_address,
                                                            jobjectArray _typed_params,
                                                            jobjectArray _params,
                                                            jobject _return_type) {
    LOAD_CLASS_TYPE(env, nullptr);
    static jmethodID method_type_getSize;
    FIND_INSTANCE_METHOD(env, type, getSize, "getSize", "(Ljava/lang/Object;)I");
    IF_NULL_RETURN(method_type_getSize, nullptr);

    void **params_ptr_index = nullptr;
    jsize length, data_length = 0;

    // Deal with params
    if (!EQUAL_TO_NULL(env, _typed_params) && (length = env->GetArrayLength(_typed_params))) {
        // Eval total size
        for (jsize i = 0; i < length; ++i) {
            jobject curr_type = env->GetObjectArrayElement(_typed_params, i);
            jobject curr_data = env->GetObjectArrayElement(_params, i);
            data_length += env->CallIntMethod(curr_type, method_type_getSize, curr_data);
            env->DeleteLocalRef(curr_data);
            env->DeleteLocalRef(curr_type);
        }

        char *params_data = reinterpret_cast<char *>(alloca(data_length));
        params_ptr_index = reinterpret_cast<void **>(alloca(sizeof(void *) * length + 1));
        jint off = 0;
        static jmethodID method_type_write;
        FIND_INSTANCE_METHOD(env, type, write, "write",
                             "(Lnet/fred/lua/foreign/MemoryAccessor;Lnet/fred/lua/foreign/Pointer;Ljava/lang/Object;)V");
        IF_NULL_RETURN(method_type_write, nullptr);

        for (jsize i = 0; i < length; ++i) {
            jobject typed = env->GetObjectArrayElement(_typed_params, i);
            jobject param = env->GetObjectArrayElement(_params, i);

            void *dest_ptr = params_data + off;
            params_ptr_index[i] = dest_ptr;

            jobject dest = pointer_create(env, dest_ptr);
            env->CallVoidMethod(typed, method_type_write, accessor, dest, param);
            env->DeleteLocalRef(dest);

            off += env->CallIntMethod(typed, method_type_getSize, param);

            env->DeleteLocalRef(typed);
            env->DeleteLocalRef(param);
        }
    }

    void *returned_data = nullptr;
    int rsize = 0;
    if (!EQUAL_TO_NULL(env, _return_type) && (rsize = env->CallIntMethod(_return_type, method_type_getSize, nullptr)) != 0) {
        returned_data = alloca(rsize);
    }

    void *cif = alloca(sizeof(ffi_cif));
    if (EQUAL_TO_NULL(env, _cif)) {
        if (do_prep_cif(env, cif, allocator, _return_type, _typed_params) != FFI_OK) {
            return nullptr;
        }
    } else {
        cif = pointer_get_from(env, _cif);
        IF_NULL_RETURN(cif, nullptr);
    }

    GET_POINTER_PARAM(env, func_addr, func_address, nullptr);
    //real call
    ffi_call((ffi_cif *) cif, (void (*)()) func_addr, returned_data, params_ptr_index);

    if (rsize != 0) {
        static jmethodID method_type_read;
        FIND_INSTANCE_METHOD(env, type, read, "read",
                             "(Lnet/fred/lua/foreign/allocator/IAllocator;Lnet/fred/lua/foreign/MemoryAccessor;Lnet/fred/lua/foreign/Pointer;)Ljava/lang/Object;");
        IF_NULL_RETURN(method_type_read, nullptr);

        jobject _ret_ptr = pointer_create(env, returned_data);
        IF_NULL_RETURN(_ret_ptr, nullptr);

        return env->CallObjectMethod(_return_type, method_type_read, allocator, accessor,
                                     _ret_ptr);
    }
    return nullptr;
}