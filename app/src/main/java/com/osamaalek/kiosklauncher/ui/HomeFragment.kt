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
        fixBlackBarIssue()

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

        webView.webViewClient = WebViewClient()
        
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

    private fun fixBlackBarIssue() {
        // Use direct layout manipulation to extend WebView to screen edges
        val layoutParams = webView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        
        // Get status bar height to extend WebView under it
        val statusBarHeight = getStatusBarHeight()
        layoutParams.topMargin = -statusBarHeight
        webView.layoutParams = layoutParams
        
        // Add top padding to WebView content to avoid content being hidden under status bar
        webView.setPadding(0, statusBarHeight, 0, 0)
        webView.clipToPadding = false
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            // Fallback for devices where status bar height can't be determined
            (24 * resources.displayMetrics.density).toInt()
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