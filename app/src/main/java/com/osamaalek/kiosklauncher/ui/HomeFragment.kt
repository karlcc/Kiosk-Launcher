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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        setupTransparentStatusBar(v)

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

    private fun setupTransparentStatusBar(rootView: View) {
        DebugLogger.log("Setting up transparent status bar with proper inset handling")
        
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navigationBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            DebugLogger.log("Status bar insets - top: ${statusBarInsets.top}px, left: ${statusBarInsets.left}px")
            DebugLogger.log("Navigation bar insets - left: ${navigationBarInsets.left}px, bottom: ${navigationBarInsets.bottom}px, right: ${navigationBarInsets.right}px")
            
            try {
                // Use layout margins instead of padding to preserve WebView's touch area
                val webViewLayoutParams = webView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                webViewLayoutParams.topMargin = statusBarInsets.top
                webViewLayoutParams.leftMargin = navigationBarInsets.left
                webViewLayoutParams.bottomMargin = navigationBarInsets.bottom
                webViewLayoutParams.rightMargin = navigationBarInsets.right
                webView.layoutParams = webViewLayoutParams
                
                // Adjust config button position to account for insets
                val configButtonParams = imageButtonConfig.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                configButtonParams.topMargin = (8 + statusBarInsets.top)
                configButtonParams.leftMargin = (8 + navigationBarInsets.left)
                imageButtonConfig.layoutParams = configButtonParams
                
                DebugLogger.log("Layout margins applied successfully")
                
                // Inject CSS safe-area viewport for web content positioning
                injectSafeAreaViewport(statusBarInsets.top, navigationBarInsets.left, navigationBarInsets.bottom, navigationBarInsets.right)
                
            } catch (e: Exception) {
                DebugLogger.logError("Error applying inset margins", e)
            }
            
            insets
        }
    }
    
    private fun injectSafeAreaViewport(topInset: Int, leftInset: Int, bottomInset: Int, rightInset: Int) {
        DebugLogger.log("Injecting safe-area viewport: top=$topInset, left=$leftInset, bottom=$bottomInset, right=$rightInset")
        
        webView.evaluateJavascript("""
            (function() {
                console.log('KioskLauncher: Injecting safe-area viewport');
                
                // Remove existing viewport and styles
                var existingViewport = document.querySelector('meta[name="viewport"]');
                if (existingViewport) existingViewport.remove();
                
                var existingStyle = document.querySelector('#kiosk-safe-area-fix');
                if (existingStyle) existingStyle.remove();
                
                // Add proper viewport meta tag
                var viewport = document.createElement('meta');
                viewport.name = 'viewport';
                viewport.content = 'width=device-width, initial-scale=1.0, user-scalable=no, viewport-fit=cover';
                if (document.head) document.head.appendChild(viewport);
                
                // Add CSS using safe-area-inset properties for modern approach
                var style = document.createElement('style');
                style.id = 'kiosk-safe-area-fix';
                style.textContent = 
                    ':root { ' +
                    '  --safe-area-inset-top: ' + ${topInset} + 'px; ' +
                    '  --safe-area-inset-left: ' + ${leftInset} + 'px; ' +
                    '  --safe-area-inset-bottom: ' + ${bottomInset} + 'px; ' +
                    '  --safe-area-inset-right: ' + ${rightInset} + 'px; ' +
                    '} ' +
                    'body { ' +
                    '  margin: 0 !important; ' +
                    '  padding: var(--safe-area-inset-top) var(--safe-area-inset-right) var(--safe-area-inset-bottom) var(--safe-area-inset-left) !important; ' +
                    '  box-sizing: border-box !important; ' +
                    '  min-height: 100vh !important; ' +
                    '} ' +
                    'html { ' +
                    '  height: 100% !important; ' +
                    '  overflow-x: hidden !important; ' +
                    '}';
                    
                if (document.head) {
                    document.head.appendChild(style);
                } else {
                    document.addEventListener('DOMContentLoaded', function() {
                        document.head.appendChild(style);
                    });
                }
                
                console.log('KioskLauncher: Safe-area viewport injection complete');
            })();
        """) { result ->
            DebugLogger.log("JavaScript safe-area injection result: $result")
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