/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_argo_sdk_core_AppSecurity */

#ifndef _Included_com_argo_sdk_core_AppSecurity
#define _Included_com_argo_sdk_core_AppSecurity
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_inno_sdk_core_AppSecurity
 * Method:    init
 * Signature: ([B)[B
 */
JNIEXPORT void JNICALL Java_com_argo_sdk_core_AppSecurity_init
        (JNIEnv *, jobject, jstring);

/*
 * Class:     com_inno_sdk_core_AppSecurity
 * Method:    signSalt
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_argo_sdk_core_AppSecurity_signSalt
  (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     com_inno_sdk_core_AppSecurity
 * Method:    authHeader
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_authHeader
  (JNIEnv *, jobject, jstring, jstring, jstring);

/*
 * Class:     com_inno_sdk_core_AppSecurity
 * Method:    genDeviceToken
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_genDeviceToken
  (JNIEnv *, jobject, jstring, jstring);

/*
 * Class:     com_inno_sdk_core_AppSecurity
 * Method:    genSessionId
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_genSessionId
  (JNIEnv *, jobject, jstring, jstring);


/*
 * Class:     com_inno_sdk_core_AppSecurity
 * Method:    signRequest
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_signRequest
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring);


/*
 * Class:     com_inno_sdk_core_AppSecurity
 * Method:    encrypt
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_encrypt
  (JNIEnv *, jobject, jstring);


/*
 * Class:     com_inno_sdk_core_AppSecurity
 * Method:    decrypt
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_decrypt
  (JNIEnv *, jobject, jcharArray);


JNIEXPORT void JNICALL Java_com_argo_sdk_core_AppSecurity_clean
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
