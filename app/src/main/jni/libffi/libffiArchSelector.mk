LIBFFI_ROOT_DIR := $(APP_PATH)/libffi

FFI_ARCH__RUNNER := {          \
	case $(TARGET_ARCH) in \
	arm)                   \
		echo arm;;     \
	arm64)                 \
		echo aarch64;; \
	x86_64)                \
		echo x86;;     \
	x86)                   \
		echo i686;;    \
	esac                   \
}

FFI_ARCH := $(shell $(FFI_ARCH__RUNNER))
FFI_INCLUDES := -I$(LIBFFI_ROOT_DIR)/include \
		-I$(LIBFFI_ROOT_DIR)/include/$(FFI_ARCH)
FFI_C_INCLUDES := $(LIBFFI_ROOT_DIR)/include $(LIBFFI_ROOT_DIR)/include/$(FFI_ARCH)
