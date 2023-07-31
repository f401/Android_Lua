APP_ABI := armeabi-v7a arm64-v8a x86_64
APP_CXXFLAGS := -std=c++17
APP_PATH := $(call my-dir)

FFI := $(APP_PATH)/libffi/libffiArchSelector.mk
BREAKPAD_INCLUDES := $(APP_PATH)/breakpad/src

include $(APP_PATH)/lua/Android.mk
include $(APP_PATH)/bridge/Android.mk
include $(APP_PATH)/libffi/Android.mk
include $(APP_PATH)/foreign/Android.mk
include $(APP_PATH)/breakpad/android/google_breakpad/Android.mk