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
import com.osamaalek.kiosklauncher.R
import com.osamaalek.kiosklauncher.util.DisplayUtil

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

        setupWebView()
        setupGestureDetector()
        setupTransparentStatusBar()

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
                // Inject edge-to-edge viewport after page loads
                injectEdgeToEdgeViewport()
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

    private fun setupTransparentStatusBar() {
        // Force transparent status bar using WindowManager flags
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        
        // Extend WebView to full screen edges
        val layoutParams = webView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        layoutParams.topMargin = 0
        layoutParams.bottomMargin = 0
        webView.layoutParams = layoutParams
        
        // Remove any padding that blocks content
        webView.setPadding(0, 0, 0, 0)
        
        // Inject viewport meta tag for edge-to-edge rendering
        injectEdgeToEdgeViewport()
    }
    
    private fun injectEdgeToEdgeViewport() {
        // Inject CSS and viewport meta tag via JavaScript to force edge-to-edge rendering
        webView.evaluateJavascript("""
            (function() {
                // Remove existing viewport meta if present
                var existingViewport = document.querySelector('meta[name="viewport"]');
                if (existingViewport) {
                    existingViewport.remove();
                }
                
                // Add edge-to-edge viewport meta tag
                var viewport = document.createElement('meta');
                viewport.name = 'viewport';
                viewport.content = 'width=device-width, initial-scale=1.0, viewport-fit=cover';
                document.head.appendChild(viewport);
                
                // Add CSS to handle safe area insets
                var style = document.createElement('style');
                style.textContent = `
                    body {
                        margin: 0;
                        padding: 0;
                        padding-top: env(safe-area-inset-top, 0px);
                        padding-bottom: env(safe-area-inset-bottom, 0px);
                    }
                    * { 
                        -webkit-box-sizing: border-box;
                        -moz-box-sizing: border-box;
                        box-sizing: border-box;
                    }
                `;
                document.head.appendChild(style);
            })();
        """, null)
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