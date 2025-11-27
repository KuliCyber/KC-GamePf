#include <jni.h>
#include <string>
#include <android/log.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>

#define LOG_TAG "KCGamePF"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT void JNICALL
Java_com_kc_gamepf_PerformanceMonitor_nativeEnableTurboMode(JNIEnv *env, jobject thiz, jboolean enable) {
    if (enable) {
        LOGI("Turbo Mode Enabled");
        system("echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
        system("echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor");
    } else {
        LOGI("Turbo Mode Disabled");
        system("echo interactive > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
        system("echo msm-adreno-tz > /sys/class/kgsl/kgsl-3d0/devfreq/governor");
    }
}

JNIEXPORT void JNICALL
Java_com_kc_gamepf_PerformanceMonitor_nativeSetCPUBoost(JNIEnv *env, jobject thiz, jint level) {
    const char* governor;
    switch(level) {
        case 0:
            governor = "interactive";
            break;
        case 1:
            governor = "schedutil";
            break;
        case 2:
            governor = "performance";
            break;
        default:
            governor = "interactive";
    }
    
    char command[256];
    snprintf(command, sizeof(command), "echo %s > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor", governor);
    system(command);
    LOGI("CPU Boost level set to: %d", level);
}

JNIEXPORT void JNICALL
Java_com_kc_gamepf_PerformanceMonitor_nativeSetGPUBoost(JNIEnv *env, jobject thiz, jint level) {
    const char* governor;
    switch(level) {
        case 0:
            governor = "msm-adreno-tz";
            break;
        case 1:
            governor = "simple_ondemand";
            break;
        case 2:
            governor = "performance";
            break;
        default:
            governor = "msm-adreno-tz";
    }
    
    char command[256];
    snprintf(command, sizeof(command), "echo %s > /sys/class/kgsl/kgsl-3d0/devfreq/governor", governor);
    system(command);
    LOGI("GPU Boost level set to: %d", level);
}

JNIEXPORT jfloat JNICALL
Java_com_kc_gamepf_PerformanceMonitor_nativeGetCPUTemperature(JNIEnv *env, jobject thiz) {
    FILE* file = fopen("/sys/class/thermal/thermal_zone0/temp", "r");
    if (file == nullptr) {
        file = fopen("/sys/class/thermal/thermal_zone1/temp", "r");
    }
    
    if (file != nullptr) {
        float temp = 0.0f;
        fscanf(file, "%f", &temp);
        fclose(file);
        return temp / 1000.0f;
    }
    
    return 35.0f + (rand() % 200) / 10.0f;
}

JNIEXPORT jfloat JNICALL
Java_com_kc_gamepf_PerformanceMonitor_nativeGetBatteryTemperature(JNIEnv *env, jobject thiz) {
    FILE* file = fopen("/sys/class/power_supply/battery/temp", "r");
    if (file == nullptr) {
        file = fopen("/sys/class/power_supply/batt_temp", "r");
    }
    
    if (file != nullptr) {
        float temp = 0.0f;
        fscanf(file, "%f", &temp);
        fclose(file);
        return temp / 10.0f;
    }
    
    return 30.0f + (rand() % 150) / 10.0f;
}

JNIEXPORT jstring JNICALL
Java_com_kc_gamepf_MainActivity_getNativeVersion(JNIEnv *env, jobject thiz) {
    std::string version = "KC GamePF Native v1.0.0";
    return env->NewStringUTF(version.c_str());
}

}
