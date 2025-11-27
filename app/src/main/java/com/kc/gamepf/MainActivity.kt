package com.kc.gamepf

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var performanceMonitor: PerformanceMonitor
    private lateinit var networkOptimizer: NetworkOptimizer
    private lateinit var startServiceBtn: Button
    private lateinit var showToolkitBtn: Button
    private lateinit var turboModeSwitch: Switch
    private lateinit var networkBoostSwitch: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeComponents()
        setupEventListeners()
        startBackgroundServices()
    }
    
    private fun initializeComponents() {
        performanceMonitor = PerformanceMonitor(this)
        networkOptimizer = NetworkOptimizer(this)
        startServiceBtn = findViewById(R.id.startServiceBtn)
        showToolkitBtn = findViewById(R.id.showToolkitBtn)
        turboModeSwitch = findViewById(R.id.turboModeSwitch)
        networkBoostSwitch = findViewById(R.id.networkBoostSwitch)
    }
    
    private fun setupEventListeners() {
        startServiceBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                performanceMonitor.startMonitoring()
                networkOptimizer.optimizeNetwork()
            }
        }
        
        showToolkitBtn.setOnClickListener {
            FloatingGameToolkit.show(this)
        }
        
        turboModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            performanceMonitor.setTurboMode(isChecked)
        }
        
        networkBoostSwitch.setOnCheckedChangeListener { _, isChecked ->
            networkOptimizer.setNetworkBoost(isChecked)
        }
    }
    
    private fun startBackgroundServices() {
        val serviceIntent = Intent(this, GameLauncherService::class.java)
        startForegroundService(serviceIntent)
    }
    
    override fun onResume() {
        super.onResume()
        performanceMonitor.resumeMonitoring()
    }
    
    override fun onPause() {
        super.onPause()
        performanceMonitor.pauseMonitoring()
    }
    
    companion object {
        init {
            System.loadLibrary("kc-gamepf")
        }
    }
}
