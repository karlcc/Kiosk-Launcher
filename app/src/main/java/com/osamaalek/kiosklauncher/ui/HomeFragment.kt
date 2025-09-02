package com.osamaalek.kiosklauncher.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.osamaalek.kiosklauncher.R
import com.osamaalek.kiosklauncher.util.DisplayUtil
import com.osamaalek.kiosklauncher.util.DebugLogger

class HomeFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var imageButtonConfig: ImageButton
    private lateinit var sharedPreferences: SharedPreferences
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalOrientation: Int = 0
    private var isConfigButtonVisible: Boolean = true
    private lateinit var gestureDetector: GestureDetector

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_home, container, false)

        webView = v.findViewById(R.id.webView)
        imageButtonConfig = v.findViewById(R.id.imageButton_config)

        sharedPreferences = requireContext().getSharedPreferences("kiosk_settings", Context.MODE_PRIVATE)

        // Initialize debug logging
        DebugLogger.init(requireContext())
        DebugLogger.log("HomeFragment onCreateView started")
        
        setupWebView()
        setupGestureDetector()
        setupStatusBarHiding()

        imageButtonConfig.setOnClickListener {
            PasswordDialog.showPasswordDialog(requireContext(), 
                onSuccess = {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, ConfigFragment()).commit()
                }
            )
        }

        // Set touch listener on parent container to catch touch events before WebView
        v.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false // Allow WebView to also handle the event
        }

        return v
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        
        // Enable media playback
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.setSupportMultipleWindows(true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                DebugLogger.log("Page finished loading: $url")
                // Inject fixed pixel viewport after page loads
                injectFixedPixelViewport()
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                DebugLogger.log("Page started loading: $url")
            }
        }
        
        // Custom WebChromeClient for fullscreen video support
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    onHideCustomView()
                    return
                }
                
                customView = view
                customViewCallback = callback
                
                // Hide the WebView and show fullscreen custom view
                webView.visibility = View.GONE
                imageButtonConfig.visibility = View.GONE
                
                // Add custom view to activity's root layout
                val decorView = requireActivity().window.decorView as FrameLayout
                decorView.addView(customView, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ))
                
                // Hide system UI for true fullscreen
                requireActivity().window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            }
            
            override fun onHideCustomView() {
                if (customView == null) {
                    return
                }
                
                // Remove custom view from activity's root layout
                val decorView = requireActivity().window.decorView as FrameLayout
                decorView.removeView(customView)
                
                // Show the WebView again
                webView.visibility = View.VISIBLE
                imageButtonConfig.visibility = View.VISIBLE
                
                customView = null
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
                
                // Restore system UI to user's configured display settings
                DisplayUtil.applyDisplaySettings(requireActivity(), requireContext())
            }
        }

        // Load URL from SharedPreferences
        val url = sharedPreferences.getString("webview_url", "https://www.google.com")
        url?.let { webView.loadUrl(it) }
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                toggleConfigButtonVisibility()
                return true
            }
        })
    }

    private fun setupStatusBarHiding() {
        DebugLogger.log("Setting up status bar hiding for Android ${Build.VERSION.SDK_INT}")
        
        // Log initial state
        DebugLogger.logStatusBarState(requireActivity())
        
        try {
            // Use FLAG_FULLSCREEN for reliable status bar hiding across all Android versions
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            
            DebugLogger.log("Applied FLAG_FULLSCREEN and FLAG_LAYOUT_NO_LIMITS")
            
            // Clear any conflicting flags
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            
            // Extend WebView to full screen
            val layoutParams = webView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            layoutParams.topMargin = 0
            layoutParams.bottomMargin = 0
            webView.layoutParams = layoutParams
            
            DebugLogger.log("WebView layout updated for fullscreen")
            
            // Remove any padding
            webView.setPadding(0, 0, 0, 0)
            
            // Schedule to override DisplayUtil after a delay
            Handler(Looper.getMainLooper()).postDelayed({
                overrideDisplayUtilSettings()
            }, 500)
            
        } catch (e: Exception) {
            DebugLogger.logError("Error in setupStatusBarHiding", e)
        }
    }
    
    private fun overrideDisplayUtilSettings() {
        try {
            DebugLogger.log("Overriding DisplayUtil settings")
            
            // Re-apply our fullscreen flags after DisplayUtil might have changed them
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            
            // Log state after override
            DebugLogger.logStatusBarState(requireActivity())
            DebugLogger.logWebViewState(webView)
            
            // Inject CSS with fixed pixel padding
            injectFixedPixelViewport()
            
        } catch (e: Exception) {
            DebugLogger.logError("Error in overrideDisplayUtilSettings", e)
        }
    }
    
    private fun injectFixedPixelViewport() {
        // Get status bar height for fixed pixel padding (Android 4.4+ compatible)
        val statusBarHeight = getStatusBarHeight()
        DebugLogger.log("Injecting fixed pixel viewport with statusBarHeight: ${statusBarHeight}px")
        
        webView.evaluateJavascript("""
            (function() {
                console.log('KioskLauncher: Injecting viewport fixes');
                
                // Remove existing viewport meta if present
                var existingViewport = document.querySelector('meta[name="viewport"]');
                if (existingViewport) {
                    existingViewport.remove();
                }
                
                // Add viewport meta tag for proper scaling
                var viewport = document.createElement('meta');
                viewport.name = 'viewport';
                viewport.content = 'width=device-width, initial-scale=1.0, user-scalable=no';
                if (document.head) {
                    document.head.appendChild(viewport);
                }
                
                // Add CSS with fixed pixel padding for status bar area
                var style = document.createElement('style');
                style.id = 'kiosk-status-bar-fix';
                style.textContent = 
                    'body { ' +
                    '  margin: 0 !important; ' +
                    '  padding: 0 !important; ' +
                    '  box-sizing: border-box !important; ' +
                    '} ' +
                    'html, body { ' +
                    '  height: 100% !important; ' +
                    '  overflow-x: hidden !important; ' +
                    '} ' +
                    '* { ' +
                    '  -webkit-box-sizing: border-box !important; ' +
                    '  -moz-box-sizing: border-box !important; ' +
                    '  box-sizing: border-box !important; ' +
                    '}';
                    
                if (document.head) {
                    document.head.appendChild(style);
                } else {
                    // Fallback if head is not ready
                    document.addEventListener('DOMContentLoaded', function() {
                        document.head.appendChild(style);
                    });
                }
                
                console.log('KioskLauncher: Viewport injection complete');
            })();
        """) { result ->
            DebugLogger.log("JavaScript viewport injection result: $result")
        }
    }
    
    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            val height = resources.getDimensionPixelSize(resourceId)
            DebugLogger.log("Status bar height from resources: ${height}px")
            height
        } else {
            // Fallback calculation based on density
            val fallbackHeight = (24 * resources.displayMetrics.density).toInt()
            DebugLogger.log("Status bar height fallback: ${fallbackHeight}px")
            fallbackHeight
        }
    }

    private fun toggleConfigButtonVisibility() {
        isConfigButtonVisible = !isConfigButtonVisible
        imageButtonConfig.visibility = if (isConfigButtonVisible) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        // Reload URL in case it was changed in config
        val url = sharedPreferences.getString("webview_url", "https://www.google.com")
        if (url != null && webView.url != url) {
            webView.loadUrl(url)
        }
    }

    fun onBackPressed(): Boolean {
        // Handle fullscreen video exit first
        if (customView != null) {
            webView.webChromeClient?.onHideCustomView()
            return true
        }
        
        // Then handle regular web navigation
        return if (webView.canGoBack()) {
            webView.goBack()
            true // Handled by fragment
        } else {
            false // Not handled, let activity handle it
        }
    }
}