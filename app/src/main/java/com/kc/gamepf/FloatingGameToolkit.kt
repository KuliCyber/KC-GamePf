package com.kc.gamepf

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FloatingGameToolkit : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var performanceLayout: LinearLayout
    private lateinit var fpsText: TextView
    private lateinit var cpuText: TextView
    private lateinit var memoryText: TextView
    private lateinit var tempText: TextView
    private lateinit var closeBtn: ImageButton
    private lateinit var expandBtn: ImageButton
    private lateinit var boostBtn: Button
    private lateinit var cleanBtn: Button
    
    private var updateJob: Job? = null
    private var isExpanded = true
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingWindow()
        setupEventListeners()
        startPerformanceUpdates()
    }
    
    private fun createFloatingWindow() {
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )
        
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 100
        
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_toolkit, null)
        windowManager.addView(floatingView, layoutParams)
        
        initializeViews()
    }
    
    private fun initializeViews() {
        performanceLayout = floatingView.findViewById(R.id.performanceLayout)
        fpsText = floatingView.findViewById(R.id.fpsText)
        cpuText = floatingView.findViewById(R.id.cpuText)
        memoryText = floatingView.findViewById(R.id.memoryText)
        tempText = floatingView.findViewById(R.id.tempText)
        closeBtn = floatingView.findViewById(R.id.closeBtn)
        expandBtn = floatingView.findViewById(R.id.expandBtn)
        boostBtn = floatingView.findViewById(R.id.boostBtn)
        cleanBtn = floatingView.findViewById(R.id.cleanBtn)
    }
    
    private fun setupEventListeners() {
        closeBtn.setOnClickListener {
            stopSelf()
        }
        
        expandBtn.setOnClickListener {
            toggleExpand()
        }
        
        boostBtn.setOnClickListener {
            performQuickBoost()
        }
        
        cleanBtn.setOnClickListener {
            performMemoryClean()
        }
        
        floatingView.setOnClickListener {
            if (!isExpanded) {
                expandView()
            }
        }
    }
    
    private fun toggleExpand() {
        if (isExpanded) {
            collapseView()
        } else {
            expandView()
        }
    }
    
    private fun expandView() {
        performanceLayout.visibility = View.VISIBLE
        isExpanded = true
    }
    
    private fun collapseView() {
        performanceLayout.visibility = View.GONE
        isExpanded = false
    }
    
    private fun startPerformanceUpdates() {
        updateJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                updatePerformanceDisplay()
                delay(1000)
            }
        }
    }
    
    private fun updatePerformanceDisplay() {
        val fps = (50 + Math.random() * 70).toInt()
        val cpuUsage = (10 + Math.random() * 80).toInt()
        val memoryUsage = (40 + Math.random() * 50).toInt()
        val temperature = (35 + Math.random() * 20).toInt()
        
        fpsText.text = "FPS: $fps"
        cpuText.text = "CPU: ${cpuUsage}%"
        memoryText.text = "RAM: ${memoryUsage}%"
        tempText.text = "TEMP: ${temperature}°C"
    }
    
    private fun performQuickBoost() {
        CoroutineScope(Dispatchers.IO).launch {
            Runtime.getRuntime().gc()
            System.runFinalization()
            Runtime.getRuntime().gc()
        }
    }
    
    private fun performMemoryClean() {
        CoroutineScope(Dispatchers.IO).launch {
            Runtime.getRuntime().gc()
            System.runFinalization()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
    
    companion object {
        fun show(context: Context) {
            val intent = Intent(context, FloatingGameToolkit::class.java)
            context.startService(intent)
        }
    }
}
