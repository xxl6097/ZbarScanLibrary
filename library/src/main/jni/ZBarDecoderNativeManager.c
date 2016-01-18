//
/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2016-01-18 13:25
 * Description:  
 */
//

/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife 
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: ZBarDecoderNativeManager.c
 * Create: 2016/1/18 13:25
 */
#include <string.h>
#include <stdlib.h>
#include <android/log.h>

#include "zbar.h"
#include "ZBarDecoderNativeManager.h"

/*
 * Class:     uuxia_het_zbar_library_coder_ZBarDecoderNativeManager
 * Method:    decodeRaw
 * Signature: ([BII)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_uuxia_het_zbar_library_coder_ZBarDecoderNativeManager_decodeRaw
        (JNIEnv *env, jobject obj, jbyteArray img, jint width, jint height)
{
    zbar_image_scanner_t *scanner;
    zbar_image_t *zimage;
    zbar_image_t *zgrayimage;

    jbyte *pixbuf;
    jstring s = NULL;

    zbar_set_verbosity(10); // XXX

    pixbuf = (*env)->GetByteArrayElements(env, img, 0);

    zimage = zbar_image_create();
    if (zimage == NULL) {
        (*env)->ReleaseByteArrayElements(env, img, pixbuf, 0);
        return NULL;
    }

    zbar_image_set_format(zimage, *(unsigned long *) "Y800");
    zbar_image_set_size(zimage, width, height);
    zbar_image_set_data(zimage, pixbuf, (*env)->GetArrayLength(env, img),
                        zbar_image_free_data);

    zgrayimage = zbar_image_convert(zimage, *(unsigned long *) "Y800");
    if (zgrayimage == NULL) {
        (*env)->ReleaseByteArrayElements(env, img, pixbuf, 0);
        return NULL;
    }

    zbar_image_destroy(zimage);

    scanner = zbar_image_scanner_create();
    zbar_image_scanner_set_config(scanner, 0, ZBAR_CFG_ENABLE, 1);
    zbar_scan_image(scanner, zgrayimage);

    const zbar_symbol_t *sym;

    sym = zbar_image_first_symbol(zgrayimage);
    if (sym != NULL) {
        const char *sym_data;
        sym_data = zbar_symbol_get_data(sym);
        __android_log_print(ANDROID_LOG_DEBUG, "zbar", "url: %s", sym_data);
        s = (*env)->NewStringUTF(env, sym_data);
    }

    (*env)->ReleaseByteArrayElements(env, img, pixbuf, 0);
    return s;
}

/*
 * Class:     uuxia_het_zbar_library_coder_ZBarDecoderNativeManager
 * Method:    decodeCrop
 * Signature: ([BIIIIII)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_uuxia_het_zbar_library_coder_ZBarDecoderNativeManager_decodeCrop
        (JNIEnv *env, jobject obj, jbyteArray img, jint width, jint height,
         jint x, jint y, jint cropw, jint croph)
{
    zbar_image_scanner_t *scanner;
    zbar_image_t *zimage;
    zbar_image_t *zgrayimage;

    jbyte *pixbuf;
    jstring s = NULL;

    zbar_set_verbosity(10); // XXX

    pixbuf = (*env)->GetByteArrayElements(env, img, 0);

    zimage = zbar_image_create();
    if (zimage == NULL) {
        (*env)->ReleaseByteArrayElements(env, img, pixbuf, 0);
        return NULL;
    }

    zbar_image_set_format(zimage, *(unsigned long *) "Y800");
    zbar_image_set_size(zimage, width, height);
    zbar_image_set_data(zimage, pixbuf, (*env)->GetArrayLength(env, img),
                        zbar_image_free_data);

    zbar_image_set_crop(zimage, x, y, cropw, croph);

    zgrayimage = zbar_image_convert(zimage, *(unsigned long *) "Y800");
    if (zgrayimage == NULL) {
        (*env)->ReleaseByteArrayElements(env, img, pixbuf, 0);
        return NULL;
    }

    zbar_image_destroy(zimage);

    scanner = zbar_image_scanner_create();
    zbar_image_scanner_set_config(scanner, 0, ZBAR_CFG_ENABLE, 1);
    zbar_scan_image(scanner, zgrayimage);

    const zbar_symbol_t *sym;

    sym = zbar_image_first_symbol(zgrayimage);
    if (sym != NULL) {
        const char *sym_data;
        sym_data = zbar_symbol_get_data(sym);
        __android_log_print(ANDROID_LOG_DEBUG, "zbar", "url: %s", sym_data);
        s = (*env)->NewStringUTF(env, sym_data);
    }

    (*env)->ReleaseByteArrayElements(env, img, pixbuf, 0);
    return s;
}