package com.kc.gamepf

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PerformanceMonitor(private val context: Context) {
    
    private external fun nativeEnableTurboMode(enable: Boolean)
    private external fun nativeSetCPUBoost(level: Int)
    private external fun nativeSetGPUBoost(level: Int)
    private external fun nativeGetCPUTemperature(): Float
    private external fun nativeGetBatteryTemperature(): Float
    
    private val monitoringScope = CoroutineScope(Dispatchers.Default)
    private var monitoringJob: Job? = null
    
    private val _performanceStats = MutableStateFlow(PerformanceStats())
    val performanceStats: StateFlow<PerformanceStats> = _performanceStats
    
    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo: StateFlow<SystemInfo> = _systemInfo
    
    fun startMonitoring() {
        stopMonitoring()
        monitoringJob = monitoringScope.launch {
            while (true) {
                updatePerformanceStats()
                updateSystemInfo()
                delay(1000)
            }
        }
    }
    
    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }
    
    fun pauseMonitoring() {
        monitoringJob?.cancel()
    }
    
    fun resumeMonitoring() {
        if (monitoringJob?.isActive != true) {
            startMonitoring()
        }
    }
    
    fun setTurboMode(enable: Boolean) {
        nativeEnableTurboMode(enable)
        if (enable) {
            nativeSetCPUBoost(2)
            nativeSetGPUBoost(2)
        } else {
            nativeSetCPUBoost(0)
            nativeSetGPUBoost(0)
        }
    }
    
    private fun updatePerformanceStats() {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val stats = PerformanceStats(
            fps = calculateCurrentFPS(),
            cpuUsage = getCPUUsage(),
            memoryUsage = getMemoryUsage(),
            gpuUsage = getGPUUsage(),
            batteryTemperature = nativeGetBatteryTemperature(),
            cpuTemperature = nativeGetCPUTemperature(),
            availableMemory = memoryInfo.availMem / (1024 * 1024),
            totalMemory = memoryInfo.totalMem / (1024 * 1024),
            timestamp = System.currentTimeMillis()
        )
        
        _performanceStats.value = stats
    }
    
    private fun updateSystemInfo() {
        val info = SystemInfo(
            deviceModel = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            maxCPUFreq = getMaxCPUFrequency(),
            gpuModel = getGPUModel(),
            isTurboSupported = true
        )
        
        _systemInfo.value = info
    }
    
    private fun calculateCurrentFPS(): Int {
        return (50 + Math.random() * 70).toInt()
    }
    
    private fun getCPUUsage(): Float {
        return (10 + Math.random() * 80).toFloat()
    }
    
    private fun getMemoryUsage(): Float {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        return (usedMemory.toFloat() / maxMemory.toFloat()) * 100
    }
    
    private fun getGPUUsage(): Float {
        return (20 + Math.random() * 60).toFloat()
    }
    
    private fun getMaxCPUFrequency(): Long {
        return 2400000L
    }
    
    private fun getGPUModel(): String {
        return "Adreno 660"
    }
    
    data class PerformanceStats(
        val fps: Int = 0,
        val cpuUsage: Float = 0f,
        val memoryUsage: Float = 0f,
        val gpuUsage: Float = 0f,
        val batteryTemperature: Float = 0f,
        val cpuTemperature: Float = 0f,
        val availableMemory: Long = 0,
        val totalMemory: Long = 0,
        val timestamp: Long = 0
    )
    
    data class SystemInfo(
        val deviceModel: String = "",
        val androidVersion: String = "",
        val cpuCores: Int = 0,
        val maxCPUFreq: Long = 0,
        val gpuModel: String = "",
        val isTurboSupported: Boolean = false
    )
}
