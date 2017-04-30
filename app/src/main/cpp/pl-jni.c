#include <string.h>
#include <jni.h>
#include <android/log.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <stdio.h>
#include <stdlib.h>

#define MAP_ADDRESS   (0x70e00000)       //<-- ron: Modify PL memory base address here
#define MAP_SIZE   (65536UL)     //<-- ron: Modify length of PL memory region here
#define MAP_MASK  (MAP_SIZE - 1)

static int is_data_ready = 0;

static int openPL() {
    int fd;

    if ((fd = open("/dev/mem", O_RDWR | O_SYNC)) == -1) {
        __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Failed to open /dev/mem");
        return -1;
    }

    return fd;
}

static int savePLContent(const char *file_name) {
    if (file_name == NULL) {
        return -1;
    }

    int fd = openPL();
    char *map_base;

    if (fd < 0) {
        return -1;
    }

    map_base = mmap(0, MAP_SIZE, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, MAP_ADDRESS);
    if (map_base == (void *) -1) {
        __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Failed to mmap /dev/mem");
        return -1;
    }

    FILE *fp = fopen(file_name, "wb");
    if (fp == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Failed to create file");
        return -1;
    }

    __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Created file %s", file_name);
    fwrite(map_base, sizeof(char), MAP_SIZE, fp);
    fclose(fp);

    close(fd);
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_ron_zedboardtestapp_PLContentProvider_capture(JNIEnv* env, jobject thiz)
{
    is_data_ready = 0;

    //TODO: initiate commands to activate the PL, wait until the memory content is ready


    is_data_ready = 1;

    return (jint)is_data_ready;
}

JNIEXPORT jstring JNICALL
Java_com_ron_zedboardtestapp_PLContentProvider_getFilePath(JNIEnv* env, jobject thiz, jstring postfix)
{
    if (is_data_ready != 1) {
        __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "PL data is not ready");
        return (*env)->NewStringUTF(env, "");
    }

    char file_name[128];
    const char *native_postfix = (*env)->GetStringUTFChars(env, postfix, JNI_FALSE);
    const char *external_storage = getenv("EXTERNAL_STORAGE");

    strcpy(file_name, external_storage);
    strcat(file_name, "/image-");
    strcat(file_name, native_postfix);
    (*env)->ReleaseStringUTFChars(env, postfix, native_postfix);

    if (savePLContent(file_name) == -1) {
        __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Unable to create file");
        return (*env)->NewStringUTF(env, "");
    }

    return (*env)->NewStringUTF(env, file_name);
}