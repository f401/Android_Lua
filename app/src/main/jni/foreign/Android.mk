LOCAL_PATH := $(call my-dir)

include $(FFI)

include $(CLEAR_VARS)
LOCAL_SRC_FILES        := foreignFuncs.cpp foreignVals.c utils.c libffi.cpp
LOCAL_MODULE           := foreign
LOCAL_SHARED_LIBRARIES := libffi
LOCAL_LDLIBS           += -ldl
LOCAL_CFLAGS           += $(FFI_INCLUDES)
LOCAL_C_INCLUDES       += $(FFI_C_INCLUDES)
include $(BUILD_SHARED_LIBRARY)