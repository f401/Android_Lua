LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
include $(FFI)

LOCAL_MODULE := ffi
LOCAL_CFLAGS += -Wall -O3 -fexceptions -DHAVE_CONFIG_H -DPIC -fPIC -I. -Isrc $(FFI_INCLUDES) 

ifeq ($(TARGET_ARCH), arm)
LOCAL_TGT_FILES := src/arm/sysv.S \
		   src/arm/ffi.c 
endif

ifeq ($(TARGET_ARCH), arm64)
LOCAL_TGT_FILES := src/aarch64/ffi.c \
		   src/aarch64/sysv.S 
endif

ifeq ($(TARGET_ARCH), x86_64)
LOCAL_TGT_FILES := src/x86/ffi64.c  \
		   src/x86/ffiw64.c \
		   src/x86/win64.S  \
		   src/x86/unix64.S 
endif

ifeq ($(TARGET_ARCH), x86)
LOCAL_TGT_FILES := src/x86/ffi.c \
		   src/x86/sysv.S
endif

ifeq ($(LOCAL_TGT_FILES),)
  $(error doesn't support for ffi in $(TARGET_ARCH))
endif

$(info "TARGET: $(TARGET_ARCH)")
$(info "  LOCAL_SRC_FILES: $(LOCAL_TGT_FILES)") 
$(info "  FLAGS: $(LOCAL_CFLAGS)")

LOCAL_SRC_FILES := src/closures.c src/java_raw_api.c src/prep_cif.c src/raw_api.c src/tramp.c src/types.c $(LOCAL_TGT_FILES)

LOCAL_LDLIBS += -ldl -L$(SYSROOT)/usr/lib -llog
#LOCAL_ARM_MODE := arm
include $(BUILD_SHARED_LIBRARY) 
