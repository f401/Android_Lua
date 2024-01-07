LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

ifeq ($(TARGET_ARCH), arm)
        LOCAL_TGT_FILES := src/arm/sysv.S \
               src/arm/ffi.c 
        LOCAL_TGT_INCLUDES := $(LOCAL_PATH)/include/arm
        $(info Building on arch arm)
else ifeq ($(TARGET_ARCH), arm64)
        LOCAL_TGT_FILES := src/aarch64/ffi.c \
               src/aarch64/sysv.S 
        LOCAL_TGT_INCLUDES := $(LOCAL_PATH)/include/aarch64
        $(info Building on arch arm64)
else ifeq ($(TARGET_ARCH), x86_64)
        LOCAL_TGT_FILES := src/x86/ffi64.c  \
               src/x86/ffiw64.c \
               src/x86/win64.S  \
               src/x86/unix64.S 
        LOCAL_TGT_INCLUDES := $(LOCAL_PATH)/include/x86
        $(info Building on arch x86)
else ifeq ($(TARGET_ARCH), x86)
        LOCAL_TGT_FILES := src/x86/ffi.c \
               src/x86/sysv.S
        LOCAL_TGT_INCLUDES := $(LOCAL_PATH)/include/i686
        $(info Building on arch i686)
else
        $(error doesn't support for ffi in $(TARGET_ARCH))
endif

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include $(LOCAL_TGT_INCLUDES)

LOCAL_MODULE := ffi
LOCAL_SRC_FILES := src/closures.c src/java_raw_api.c src/prep_cif.c src/raw_api.c src/tramp.c src/types.c $(LOCAL_TGT_FILES)
LOCAL_LDLIBS += -ldl -L$(SYSROOT)/usr/lib -llog
LOCAL_CFLAGS := -Wall -O3 -fexceptions -DHAVE_CONFIG_H -DPIC -fPIC -I. -Isrc -I$(LOCAL_PATH)/include -I$(LOCAL_TGT_INCLUDES)

include $(BUILD_SHARED_LIBRARY) 
