APP_ABI := armeabi-v7a arm64-v8a x86 x86_64
APP_PATH := $(call my-dir)
include $(APP_PATH)/lua/Android.mk
include $(APP_PATH)/bridge/Android.mk
