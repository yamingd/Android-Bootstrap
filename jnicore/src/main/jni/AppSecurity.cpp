#include "com_argo_sdk_core_AppSecurity.h"
#include <cstddef>
#include <stdlib.h>
#include <stdio.h>
#include <sys/time.h>
#include <openssl/rand.h>
#include <openssl/aes.h>
#include <openssl/sha.h>
#include <openssl/md5.h>
#include <openssl/evp.h>
#include <openssl/err.h>
#include <iostream>
#include <string>

#include "logs.h"
#include "base64.h"

static unsigned char salt[] = "&vKUnrpaCvL9gH&!";
static unsigned char iv[] =  "&i973144334676&!";
static const unsigned char digits[] = "0123456789abcdef";
static const unsigned char zeroPadding[] = "adf6916edc853858a8a03432f1bd99c3";

void md5String(JNIEnv *env, const char *string, char outputBuffer[33]);
void sha256String(const char *string, char outputBuffer[65]);

JNIEXPORT void JNICALL Java_com_argo_sdk_core_AppSecurity_init
        (JNIEnv *env, jobject obj, jstring seed){

    const char* csecret = env->GetStringUTFChars(seed, 0);

    char hash[65];
    md5String(env, csecret, hash);

    for (int i = 0; i < 16; ++i) {
        salt[i] = hash[i * 2];
    }

    //LOGD("AppSecurity init salt %s", salt);

    for (int i = 0; i < 16; ++i) {
        iv[i] = hash[i * 2 + 1];
    }

    //LOGD("AppSecurity init iv %s", iv);
}

JNIEXPORT jbyteArray JNICALL Java_com_argo_sdk_core_AppSecurity_signSalt
  (JNIEnv *env, jobject obj, jbyteArray bytes){

    jbyte* arrayBody = env->GetByteArrayElements(bytes, 0);
    jsize len  = env->GetArrayLength(bytes);
    jbyteArray data = env->NewByteArray(64);
    if (data == NULL) {
        return NULL; //  out of memory error thrown
    }
    jbyte *buf = env->GetByteArrayElements(data, 0);
    for(int i=0; i<64;i++){
        buf[i] = arrayBody[i + 10];
    }
    env->SetByteArrayRegion(data, 0, 64, buf);
    return data;

  }


JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_authHeader
  (JNIEnv *env, jobject obj, jstring sid, jstring secret, jstring userId){

    const char* csecret = env->GetStringUTFChars(secret, 0);
    const char* csid = env->GetStringUTFChars(sid, 0);
    const char* cuserId = env->GetStringUTFChars(userId, 0);

    char hash[65];
    md5String(env, csecret, hash);

    struct timeval tv;
    gettimeofday(&tv, NULL);

    std::string input_str(cuserId);
    std::string b64userId;
    Base64Encode(cuserId, &b64userId);

    std::string buf;
    buf.append(std::to_string(tv.tv_sec));
    buf.append("|");
    buf.append(hash, strlen(hash));
    buf.append("|");
    buf.append(csid, strlen(csid));
    buf.append("|");
    buf.append(b64userId);

    memset(hash, 0, 65);
    sha256String(buf.c_str(), hash);

    buf.clear();

    buf.append(b64userId);
    buf.append("|");
    buf.append(std::to_string(tv.tv_sec));
    buf.append("|");
    buf.append(hash, strlen(hash));

    env->ReleaseStringUTFChars(sid, csid);
    env->ReleaseStringUTFChars(secret, csecret);
    env->ReleaseStringUTFChars(userId, cuserId);

    //LOGI("auth result \n%s", buf.c_str());

    jstring ret = env->NewStringUTF(buf.c_str());

    buf.clear();
    input_str.clear();
    b64userId.clear();

    return ret;
  }


void md5String(JNIEnv *env, const char *string, char outputBuffer[33])
{

	//const char *string = env->GetStringUTFChars(str, 0);

	unsigned char hash[MD5_DIGEST_LENGTH];

    MD5_CTX md5ctx;
    MD5_Init(&md5ctx);
    MD5_Update(&md5ctx, string, strlen(string));
    MD5_Final(hash, &md5ctx);

	int i = 0;
	for (i = 0; i < MD5_DIGEST_LENGTH; i++) {
		sprintf(outputBuffer + (i * 2), "%02x", hash[i]);
	}
    outputBuffer[32] = 0;

    //释放出从java端接收的字符串
    //env->ReleaseStringUTFChars(str, string);

	//LOGI("md5String result \n%s", outputBuffer);
}

void sha256String(const char *string, char outputBuffer[65])
{
    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    SHA256_Update(&sha256, string, strlen(string));
    SHA256_Final(hash, &sha256);
    int i = 0;
    for(i = 0; i < SHA256_DIGEST_LENGTH; i++)
    {
        sprintf(outputBuffer + (i * 2), "%02x", hash[i]);
    }
    outputBuffer[64] = 0;

    //LOGI("sha256String result \n%s", (char *) outputBuffer);
}

JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_genDeviceToken
  (JNIEnv *env, jobject obj, jstring deviceId, jstring appName){

    return env->NewStringUTF( "NULL.");

  }

JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_genSessionId
   (JNIEnv *env, jobject obj, jstring sid, jstring secret){

     return env->NewStringUTF( "NULL.");

   }


JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_signRequest
   (JNIEnv *env, jobject obj, jstring sid, jstring secret, jstring url, jstring userId){

    //LOGD("@@@signRequest begin");

    // The data that we're going to hash
    const char* csid = env->GetStringUTFChars(sid, 0);
    const char* csecret = env->GetStringUTFChars(secret, 0);
    const char* curl = env->GetStringUTFChars(url, 0);
    const char* cuserId = env->GetStringUTFChars(userId, 0);

    //struct timeval tv;
    //gettimeofday(&tv, NULL);

    std::string buf;

    // 产生随机码
    //buf.append(std::to_string(tv.tv_sec));
    //buf.append("|");
    //buf.append(std::to_string(tv.tv_usec));
    //buf.append("|");
    //buf.append(csecret);

    RAND_seed(salt, 16);
    unsigned char randcs[5];

    RAND_bytes(randcs, 5);

    char hash[65];
    char sign[75];

    //memset(hash, 0, 65);
    //md5String(env, buf.c_str(), hash);
    //LOGD("signRequest key %d \n %s", strlen(hash), hash);

    for (int i = 0; i < 5; i++ ){
      sprintf(sign + (i * 2), "%02x", randcs[i]);
    }

    //LOGD("signRequest prefix \n%s", sign);

    memset(hash, 0, 65);
    md5String(env, sign, hash);

    //LOGD("signRequest hash \n%s", hash);

    buf.clear();
    buf.append(hash);
    buf.append("|");
    buf.append(cuserId);
    buf.append("|");
    buf.append(curl);
    buf.append("|");
    buf.append(csecret);
    buf.append("|");
    buf.append(csid);

    memset(hash, 0, 65);
    sha256String(buf.c_str(), hash);

    //LOGD("signRequest data %d \n %s", buf.length(), buf.c_str());
    //LOGD("signRequest sha256 0 %d \n %s", strlen(hash), hash);
    buf.clear();
    //LOGD("signRequest sha256 1 %d \n %s", strlen(hash), hash);

    // 输出最终结果
    sprintf(sign + 10, "%s", hash);

    //LOGD("signRequest result \n%s", sign);

    //LOGD("@@@signRequest end");

    env->ReleaseStringUTFChars(sid, csid);
    env->ReleaseStringUTFChars(secret, csecret);
    env->ReleaseStringUTFChars(url, curl);
    env->ReleaseStringUTFChars(userId, cuserId);

    return env->NewStringUTF(sign);

   }

int hextoDigit(char c){
    for(int i=0; i<16; i++){
        if(digits[i] == c){
            return i;
        }
    }
    return -1;
}

JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_encrypt
  (JNIEnv *env, jobject obj, jstring plain){
    // http://www.open-open.com/lib/view/open1383611713040.html

    const char* plaincs = env->GetStringUTFChars(plain, 0);
    int len0 = strlen(plaincs);

    int diff = AES_BLOCK_SIZE - len0 % AES_BLOCK_SIZE;
    int newsize = len0;
    if(diff > 0){
        newsize = len0 + diff;
    }

    unsigned char inBuffer[newsize];
    memcpy(inBuffer, plaincs, len0);
    for(int i=0; i<diff; i++){
        inBuffer[i + len0] = 0x00;
    }

    int inLen = newsize;
    int outLen, tmplen;

    unsigned char outBuffer[newsize + AES_BLOCK_SIZE];
    memset(outBuffer, 0, newsize + AES_BLOCK_SIZE);

    //LOGD("EVP encrypt prepare. inLen=%d, in=%s", inLen, inBuffer);

    EVP_CIPHER_CTX ctx;
    EVP_CIPHER_CTX_init(&ctx);

    EVP_CipherInit_ex(&ctx, EVP_aes_128_cbc(), NULL, NULL, NULL, 0);
    OPENSSL_assert(EVP_CIPHER_CTX_key_length(&ctx) == 16);
    OPENSSL_assert(EVP_CIPHER_CTX_iv_length(&ctx) == 16);

    EVP_EncryptInit_ex(&ctx, EVP_aes_128_cbc(), NULL, salt, iv);
    EVP_CIPHER_CTX_set_padding(&ctx, 0);

    //LOGD("encrypt init.");

    if(!EVP_EncryptUpdate(&ctx, outBuffer, &outLen, (const unsigned char*)inBuffer, inLen))
    {
        return NULL;
    }

//    LOGD("encrypt update. outLen=%d", outLen);
//    for(int i=0; i < outLen; i++){
//        LOGD("encrypt result. %d", (int)outBuffer[i]);
//    }
//    LOGD("encrypt update. %s", outBuffer);

    if(!EVP_EncryptFinal_ex(&ctx, outBuffer + outLen, &tmplen))
    {
        return NULL;
    }

    outLen += tmplen;

    EVP_CIPHER_CTX_cleanup(&ctx);

//    LOGD("encrypt final. outLen=%d", outLen);
//    LOGD("encrypt final. %s", outBuffer);


    char* hex = (char*)malloc(sizeof(char) * outLen * 2);
    for(int i = 0; i < outLen; i++)
    {
        sprintf(hex + (i * 2), "%02x", outBuffer[i]);
    }

    env->ReleaseStringUTFChars(plain, plaincs);

    //LOGD("encrypt hex result \n%s", hex);

    return env->NewStringUTF(hex);
  }


JNIEXPORT jstring JNICALL Java_com_argo_sdk_core_AppSecurity_decrypt
  (JNIEnv *env, jobject obj, jcharArray data){

    jchar* hex = env->GetCharArrayElements(data, 0);
    jint jbslen = env->GetArrayLength(data);

    int inLen = jbslen / 2;

    unsigned char* inBuffer;
    inBuffer = (unsigned char*)malloc(sizeof(char) * inLen);
    //memset(inBuffer, 0, inLen);

    int i = 0, j=0;
    for(i=0, j=0; i<inLen; i++){
        int f = hextoDigit((char)hex[j]) << 4;
        //LOGD("decrypt source. %d", (int)hex[j]);

        j++;
        f = f | hextoDigit((char)hex[j]);
        //LOGD("decrypt source. %d", (int)hex[j]);
        j++;
        unsigned char c = (f & 0xFF);
        if (c < 0){
            c += 256;
        }
        //LOGD("decrypt source0. %d", (int)c);
        inBuffer[i] = c;
    }


//    for(int i=0; i<inLen; i++){
//        LOGD("decrypt in source. %d", (int)inBuffer[i]);
//    }

    env->ReleaseCharArrayElements(data, hex, 0);

    //unsigned char iv[128];
    unsigned char outBuffer[inLen+16];

    //memset(iv, 0, 128);

    int outLen, tmplen;

    //LOGD("EVP decrypt init. %d", inLen);

    EVP_CIPHER_CTX ctx;
    EVP_CIPHER_CTX_init(&ctx);

    EVP_CipherInit_ex(&ctx, EVP_aes_128_cbc(), NULL, NULL, NULL, 0);
    OPENSSL_assert(EVP_CIPHER_CTX_key_length(&ctx) == 16);
    OPENSSL_assert(EVP_CIPHER_CTX_iv_length(&ctx) == 16);

    EVP_DecryptInit_ex(&ctx, EVP_aes_128_cbc(), NULL, salt, iv);
    EVP_CIPHER_CTX_set_padding(&ctx, 0);

    //LOGD("decrypt update.");

    if(!EVP_DecryptUpdate(&ctx, outBuffer, &outLen, (const unsigned char*)inBuffer, inLen))
    {
        LOGE("EVP_DecryptUpdate error.");
        return NULL;
    }

    //LOGD("EVP_DecryptUpdate final. outLen=%d", outLen);

    if(!EVP_DecryptFinal_ex(&ctx, outBuffer + outLen, &tmplen))
    {
        LOGE("EVP_DecryptFinal_ex error.");
        return NULL;
    }

    outLen += tmplen;

    EVP_CIPHER_CTX_cleanup(&ctx);

    outBuffer[outLen] = '\0';

    //LOGD("decrypt cleanup. %d", outLen);

    //LOGD("decrypt result \n%s", outBuffer);

    return env->NewStringUTF((const char*)outBuffer);

  }

  JNIEXPORT void JNICALL Java_com_argo_sdk_core_AppSecurity_clean
    (JNIEnv *env, jobject obj){

    EVP_cleanup();
    CRYPTO_cleanup_all_ex_data();
    ERR_free_strings();

 }