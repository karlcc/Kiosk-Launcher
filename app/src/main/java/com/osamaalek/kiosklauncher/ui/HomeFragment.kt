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
import com.osamaalek.kiosklauncher.util.DeviceIdentifier
import com.osamaalek.kiosklauncher.util.DeviceInfo
import com.osamaalek.kiosklauncher.util.SecurityUtil
import com.osamaalek.kiosklauncher.util.SignedPayload
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.net.URL
import java.net.HttpURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader
import org.json.JSONObject
import org.json.JSONException
import android.content.Context

class HomeFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var imageButtonConfig: ImageButton
    private lateinit var sharedPreferences: SharedPreferences
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalOrientation: Int = 0
    private var isConfigButtonVisible: Boolean = true
    private lateinit var gestureDetector: GestureDetector
    private var sessionToken: String? = null

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
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                DebugLogger.log("Page started loading: $url")
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                request?.let { req ->
                    try {
                        val deviceInfo = DeviceIdentifier.getDeviceIdentifierWithInfo(requireContext())
                        val validationEnabled = sharedPreferences.getBoolean("device_validation_enabled", false)
                        
                        DebugLogger.log("=== REQUEST INTERCEPT DEBUG ===")
                        DebugLogger.log("URL: ${req.url}")
                        DebugLogger.log("Device ID: ${deviceInfo.deviceId}")
                        DebugLogger.log("Device Info: ${deviceInfo.deviceInfo}")
                        DebugLogger.log("Validation Enabled: $validationEnabled")
                        
                        if (validationEnabled) {
                            // Hybrid Security Approach: Sign device validation requests only
                            val url = req.url.toString()
                            
                            val requiresSignature = when {
                                url.contains("device_whitelist") -> true    // Initial device validation - MUST be signed
                                url.contains("api_validate") -> false       // Session token validation - no signature needed
                                url.contains("api_data") -> false           // Session-protected APIs - no signature needed
                                url.contains("/api/") -> false              // General API endpoints - use session tokens
                                url.endsWith(".php") -> false               // Other PHP files - use session tokens
                                url.endsWith(".html") -> false              // Static files
                                url.endsWith(".css") -> false               // Static files
                                url.endsWith(".js") -> false                // Static files
                                url.endsWith(".png") -> false               // Images
                                url.endsWith(".jpg") -> false               // Images
                                url.endsWith(".gif") -> false               // Images
                                else -> false                               // Default: no signature required
                            }
                            
                            DebugLogger.log("URL: $url")
                            DebugLogger.log("Requires cryptographic signature: $requiresSignature")
                            
                            if (requiresSignature) {
                                DebugLogger.log("Creating cryptographically signed request for device validation...")
                                return createRequestWithDeviceHeaders(req, deviceInfo)
                            } else {
                                DebugLogger.log("Using standard request - session tokens or static content")
                                // For API endpoints, inject session token if available
                                if (url.contains("/api/") && sessionToken != null) {
                                    return injectSessionToken(req, sessionToken!!)
                                }
                            }
                        } else {
                            DebugLogger.log("Device validation disabled - not adding headers")
                        }
                    } catch (e: Exception) {
                        DebugLogger.logError("Error intercepting request", e)
                    }
                }
                return super.shouldInterceptRequest(view, request)
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

    private fun createRequestWithDeviceHeaders(request: WebResourceRequest, deviceInfo: DeviceInfo): WebResourceResponse? {
        return try {
            val url = URL(request.url.toString())
            val connection = url.openConnection() as HttpURLConnection
            
            // Copy original request properties
            connection.requestMethod = request.method
            connection.connectTimeout = 10000 // 10 seconds
            connection.readTimeout = 15000 // 15 seconds
            
            // Copy original headers
            request.requestHeaders.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            
            // Create signed payload
            val signedPayload = SecurityUtil.createSignedPayload(
                deviceInfo.deviceId,
                deviceInfo.deviceInfo, 
                deviceInfo.appVersion
            )
            
            // Add signed device headers
            connection.setRequestProperty("X-Device-ID", signedPayload.deviceId)
            connection.setRequestProperty("X-Device-Info", signedPayload.deviceInfo)
            connection.setRequestProperty("X-App-Version", signedPayload.appVersion)
            connection.setRequestProperty("X-Timestamp", signedPayload.timestamp.toString())
            connection.setRequestProperty("X-Nonce", signedPayload.nonce)
            connection.setRequestProperty("X-Signature", signedPayload.signature)
            
            DebugLogger.log("Added signed device headers - ID: ${signedPayload.deviceId}")
            DebugLogger.log("Request signature: ${signedPayload.signature.take(20)}...")
            
            // Make the request
            connection.connect()
            
            val responseCode = connection.responseCode
            val contentType = connection.contentType ?: "application/json"
            val encoding = connection.contentEncoding ?: "utf-8"
            
            DebugLogger.log("Server response: $responseCode for ${request.url}")
            
            // Handle different response codes
            when (responseCode) {
                403 -> {
                    DebugLogger.log("Device not authorized - showing unauthorized page")
                    return createUnauthorizedResponse(deviceInfo.deviceId)
                }
                in 200..299 -> {
                    // Success - verify response signature before returning
                    val responseText = connection.inputStream.bufferedReader().readText()
                    
                    if (contentType.contains("application/json")) {
                        val verifiedResponse = verifyAndProcessResponse(responseText)
                        if (verifiedResponse != null) {
                            // Check if this response contains a session token (from device validation)
                            extractAndStoreSessionToken(verifiedResponse)
                            return WebResourceResponse(contentType, encoding, verifiedResponse.byteInputStream())
                        } else {
                            DebugLogger.log("Response signature verification failed")
                            return createSecurityErrorResponse()
                        }
                    } else {
                        // Non-JSON response (HTML, CSS, etc.) - return as-is
                        return WebResourceResponse(contentType, encoding, responseText.byteInputStream())
                    }
                }
                else -> {
                    // Other error codes
                    val errorText = try {
                        connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    } catch (e: Exception) {
                        "Network error: $responseCode"
                    }
                    DebugLogger.log("Server error $responseCode: $errorText")
                    return WebResourceResponse(contentType, encoding, errorText.byteInputStream())
                }
            }
            
        } catch (e: Exception) {
            DebugLogger.logError("Error creating signed request", e)
            return createNetworkErrorResponse(e.message ?: "Unknown network error")
        }
    }
    
    private fun verifyAndProcessResponse(responseText: String): String? {
        return try {
            val jsonResponse = JSONObject(responseText)
            
            // Check if response has signature data
            if (jsonResponse.has("signature") && jsonResponse.has("timestamp") && jsonResponse.has("nonce")) {
                val responseData = jsonResponse.getJSONObject("data").toString()
                val timestamp = jsonResponse.getLong("timestamp")
                val nonce = jsonResponse.getString("nonce")
                val signature = jsonResponse.getString("signature")
                
                if (SecurityUtil.verifyResponsePayload(responseData, timestamp, nonce, signature)) {
                    DebugLogger.log("Response signature verified successfully")
                    return jsonResponse.getJSONObject("data").toString()
                } else {
                    DebugLogger.log("Response signature verification failed")
                    return null
                }
            } else {
                // Response doesn't have signature - might be non-validation response
                DebugLogger.log("Response has no signature data - treating as unsigned response")
                return responseText
            }
        } catch (e: JSONException) {
            DebugLogger.logError("Error parsing JSON response", e)
            // If not JSON, return as-is (might be HTML or other content)
            return responseText
        } catch (e: Exception) {
            DebugLogger.logError("Error verifying response", e)
            return null
        }
    }
    
    private fun createUnauthorizedResponse(deviceId: String): WebResourceResponse {
        val unauthorizedHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Device Not Authorized</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; margin: 50px; background: #f8f9fa; }
                    .container { max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .error { color: #d32f2f; margin-bottom: 20px; }
                    .device-id { background: #f5f5f5; padding: 15px; margin: 20px 0; border-radius: 5px; font-family: monospace; word-break: break-all; }
                    .icon { font-size: 48px; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">🚫</div>
                    <h1 class="error">Access Denied</h1>
                    <p>This device is not authorized to access this application.</p>
                    <div class="device-id">
                        <strong>Device ID:</strong><br>$deviceId
                    </div>
                    <p><strong>Next Steps:</strong></p>
                    <p>Please contact your system administrator and provide the Device ID above to request access.</p>
                    <p><small>This page uses cryptographic device validation to ensure security.</small></p>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        return WebResourceResponse("text/html", "utf-8", unauthorizedHtml.byteInputStream())
    }
    
    private fun createSecurityErrorResponse(): WebResourceResponse {
        val errorHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Security Error</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; margin: 50px; background: #f8f9fa; }
                    .container { max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .error { color: #d32f2f; }
                    .icon { font-size: 48px; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">⚠️</div>
                    <h1 class="error">Security Verification Failed</h1>
                    <p>The server response could not be verified for security reasons.</p>
                    <p>Please try again or contact your system administrator if the problem persists.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        return WebResourceResponse("text/html", "utf-8", errorHtml.byteInputStream())
    }
    
    private fun createNetworkErrorResponse(errorMessage: String): WebResourceResponse {
        val errorHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Network Error</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; margin: 50px; background: #f8f9fa; }
                    .container { max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .error { color: #ff9800; }
                    .icon { font-size: 48px; margin-bottom: 20px; }
                    .details { background: #f5f5f5; padding: 10px; border-radius: 5px; font-family: monospace; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">🌐</div>
                    <h1 class="error">Network Error</h1>
                    <p>Unable to connect to the validation server.</p>
                    <div class="details">$errorMessage</div>
                    <p>Please check your internet connection and try again.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        return WebResourceResponse("text/html", "utf-8", errorHtml.byteInputStream())
    }
    
    private fun extractAndStoreSessionToken(responseJson: String) {
        try {
            val jsonResponse = JSONObject(responseJson)
            if (jsonResponse.has("session_token")) {
                sessionToken = jsonResponse.getString("session_token")
                val expiresAt = jsonResponse.optLong("token_expires_at", 0)
                
                // Store session token in SharedPreferences for persistence
                sharedPreferences.edit()
                    .putString("session_token", sessionToken)
                    .putLong("session_token_expires", expiresAt)
                    .apply()
                
                DebugLogger.log("Session token extracted and stored successfully")
                DebugLogger.log("Token expires at: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(expiresAt * 1000))}")
            }
        } catch (e: JSONException) {
            DebugLogger.logError("Error extracting session token from response", e)
        }
    }
    
    private fun injectSessionToken(request: WebResourceRequest, token: String): WebResourceResponse? {
        return try {
            val url = URL(request.url.toString())
            val connection = url.openConnection() as HttpURLConnection
            
            // Copy original request properties
            connection.requestMethod = request.method
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            
            // Copy original headers
            request.requestHeaders.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            
            // Inject Authorization header with session token
            connection.setRequestProperty("Authorization", "Bearer $token")
            
            DebugLogger.log("Injected session token into API request")
            DebugLogger.log("Token preview: ${token.take(20)}...")
            
            // Make the request
            connection.connect()
            
            val responseCode = connection.responseCode
            val contentType = connection.contentType ?: "application/json"
            val encoding = connection.contentEncoding ?: "utf-8"
            
            DebugLogger.log("API response code: $responseCode for ${request.url}")
            
            when (responseCode) {
                401 -> {
                    // Session token expired or invalid - clear it
                    sessionToken = null
                    sharedPreferences.edit()
                        .remove("session_token")
                        .remove("session_token_expires")
                        .apply()
                    
                    DebugLogger.log("Session token invalid/expired - cleared from storage")
                    return createSessionExpiredResponse()
                }
                in 200..299 -> {
                    val responseText = connection.inputStream.bufferedReader().readText()
                    return WebResourceResponse(contentType, encoding, responseText.byteInputStream())
                }
                else -> {
                    val errorText = try {
                        connection.errorStream?.bufferedReader()?.readText() ?: "API error: $responseCode"
                    } catch (e: Exception) {
                        "Network error: $responseCode"
                    }
                    return WebResourceResponse(contentType, encoding, errorText.byteInputStream())
                }
            }
            
        } catch (e: Exception) {
            DebugLogger.logError("Error injecting session token", e)
            null // Let WebView handle the original request
        }
    }
    
    private fun createSessionExpiredResponse(): WebResourceResponse {
        val expiredHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Session Expired</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: Arial, sans-serif; text-align: center; margin: 50px; background: #f8f9fa; }
                    .container { max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .warning { color: #ff9800; }
                    .icon { font-size: 48px; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">⏰</div>
                    <h1 class="warning">Session Expired</h1>
                    <p>Your session token has expired. The app will automatically re-authenticate.</p>
                    <p><small>Please refresh the page to continue.</small></p>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        return WebResourceResponse("text/html", "utf-8", expiredHtml.byteInputStream())
    }

    private fun toggleConfigButtonVisibility() {
        isConfigButtonVisible = !isConfigButtonVisible
        imageButtonConfig.visibility = if (isConfigButtonVisible) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        
        // Restore session token from SharedPreferences
        val storedToken = sharedPreferences.getString("session_token", null)
        val tokenExpires = sharedPreferences.getLong("session_token_expires", 0)
        
        if (storedToken != null && tokenExpires > System.currentTimeMillis() / 1000) {
            sessionToken = storedToken
            DebugLogger.log("Session token restored from storage")
        } else if (storedToken != null) {
            // Token expired - clear it
            sharedPreferences.edit()
                .remove("session_token")
                .remove("session_token_expires")
                .apply()
            DebugLogger.log("Stored session token expired - cleared from storage")
        }
        
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