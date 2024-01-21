#ifndef _MEMACCESS_
#define _MEMACCESS_ 1

#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif

int registerMemoryAccessorFunctions(JNIEnv *env);

#ifdef __cplusplus
} // extern "C"
#endif
 
#endif // #ifndef _MEMACCESS_ 
