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
Java_com_ron_zedboardtestapp_PLContentProvider_capture(JNIEnv* env, jobject thiz, jboolean is3DCapture)
{
    is_data_ready = 0;

    //TODO: initiate commands to activate the PL
    if (is3DCapture) {
        //Do 3D Capture (Wait Customer's command)
    } else {
        //Do 2D Capture (Wait Customer's command)
    }

    return (jint)0;
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

    //TBR: test only
//    return (*env)->NewStringUTF(env, file_name);

    if (savePLContent(file_name) == -1) {
        __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Unable to create file");
        return (*env)->NewStringUTF(env, "");
    }

    is_data_ready = 0;   // Data is saved to file , clear data ready flag
    return (*env)->NewStringUTF(env, file_name);
}

JNIEXPORT jlong JNICALL
Java_com_ron_zedboardtestapp_PLContentProvider_readRegister(JNIEnv* env, jobject thiz, jlong address)
{
    unsigned long mmap_start;
    unsigned int addr, value;
    void *page;

    //TBR: for testing
//    return (jlong) 0xBAAAAAAD;

    int fd = openPL();
    if (fd < 0) {
        return (jlong) -1;
    }

    addr = (unsigned long) address;
    mmap_start = addr & ~(PAGE_SIZE - 1);

    page = mmap(0, PAGE_SIZE, PROT_READ | PROT_WRITE,
                       MAP_SHARED, fd, mmap_start);

    if (page == MAP_FAILED) {
        __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Unable to map /dev/mem");
        return (jlong) -1;
    }

    unsigned int *x = (unsigned int *) (((unsigned) page) + (addr & 4095));
    value = *x;

    __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Read %08x: %08x", addr, value);

    close(fd);
    return (jlong) value;

}

JNIEXPORT jint JNICALL
Java_com_ron_zedboardtestapp_PLContentProvider_writeRegister(JNIEnv* env, jobject thiz, jlong address, jlong value)
{
    unsigned long mmap_start;
    unsigned int addr;
    unsigned int nvalue = (unsigned int) value;
    void *page;

    //TBR: for testing
//    __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Write %08x: %08x", (long)address, (long)value);
//    return (jint) 0;

    int fd = openPL();
    if (fd < 0) {
        return (jint) -1;
    }

    addr = (unsigned long) address;
    mmap_start = addr & ~(PAGE_SIZE - 1);

    page = mmap(0, PAGE_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd, mmap_start);

    __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "mmap_start=%08x, page=%08x", mmap_start, page);

    if (page == MAP_FAILED) {
        __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Unable to map /dev/mem");
        return (jint) -1;
    }

    unsigned int *x = (unsigned int *) (((unsigned) page) + (addr & 4095));
    *x = nvalue;

    __android_log_print(ANDROID_LOG_DEBUG, "pl-jni", "Write %08x: %08x", addr, nvalue);

    close(fd);
    return 0;
}

JNIEXPORT jboolean JNICALL
Java_com_ron_zedboardtestapp_PLContentProvider_isDataReady(JNIEnv *env, jobject instance) {
    // TODO: Wait customer provide method to check data readiness

    //TBR: for testing
    is_data_ready = 1;

    if (is_data_ready)
        return JNI_TRUE;
    else
        return JNI_FALSE;

}