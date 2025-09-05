# 🔐 Kiosk Launcher Authentication Integration Guide

## Overview

This guide covers integrating hybrid cryptographic authentication with your web applications. The system provides both **React.js** and **Vanilla JavaScript** examples for secure device validation and session management.

## 📁 Files Structure

```
test_server/
├── device_whitelist.php       # Main device validation endpoint
├── api_validate.php          # Session token validation API
├── api_data.php             # Protected data endpoint example
├── react_example.html       # React.js integration example
├── vanilla_example.html     # Vanilla JS integration example
└── *.md                     # Documentation files
```

## 🔄 Authentication Flow

### Step 1: Device Authentication
- **Endpoint**: `POST /device_whitelist.php`
- **WebView Integration**: Android WebView automatically adds cryptographic headers
- **Response**: Signed response with JWT session token

### Step 2: Session Validation
- **Endpoint**: `POST /api_validate.php` 
- **Headers**: `Authorization: Bearer <jwt_token>`
- **Response**: Device info and session validation

### Step 3: API Access
- **Protected Endpoints**: `/api_data.php`, `/api_*`
- **Authentication**: JWT session token from Step 1
- **WebView Integration**: Automatic token injection

## 🚀 Quick Start

### Option 1: React.js Integration

```html
<!-- Copy react_example.html as your starting point -->
<script type="text/babel">
    const { useState, useEffect } = React;
    
    const useAuth = () => {
        const [isAuthenticated, setIsAuthenticated] = useState(false);
        const [isLoading, setIsLoading] = useState(true);

        const checkAuth = async () => {
            // Step 1: Device validation
            const deviceResponse = await fetch('/device_whitelist.php', {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            });

            if (deviceResponse.ok) {
                const deviceData = await deviceResponse.json();
                const actualData = deviceData.data || deviceData; // Handle signed response
                
                if (actualData.access_granted && actualData.session_token) {
                    // Step 2: Session validation
                    const validateResponse = await fetch('/api_validate.php', {
                        method: 'POST',
                        headers: { 
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${actualData.session_token}`
                        }
                    });

                    if (validateResponse.ok) {
                        const validateData = await validateResponse.json();
                        if (validateData.authenticated === true) {
                            // WebView timing fix
                            setTimeout(() => {
                                setIsAuthenticated(true);
                            }, 100);
                        }
                    }
                }
            }
            setIsLoading(false);
        };

        useEffect(() => { checkAuth(); }, []);
        return { isAuthenticated, isLoading };
    };
</script>
```

### Option 2: Vanilla JavaScript Integration

```html
<!-- Copy vanilla_example.html as your starting point -->
<script>
    let appState = {
        isAuthenticated: false,
        isLoading: true
    };

    async function checkAuth() {
        try {
            // Step 1: Device validation
            const deviceResponse = await fetch('/device_whitelist.php', {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            });

            if (deviceResponse.ok) {
                const deviceData = await deviceResponse.json();
                const actualData = deviceData.data || deviceData; // Handle signed response
                
                if (actualData.access_granted && actualData.session_token) {
                    // Step 2: Session validation
                    const validateResponse = await fetch('/api_validate.php', {
                        method: 'POST',
                        headers: { 
                            'Authorization': `Bearer ${actualData.session_token}`
                        }
                    });

                    const validateData = await validateResponse.json();
                    if (validateData.authenticated === true) {
                        // WebView timing fix
                        setTimeout(() => {
                            appState.isAuthenticated = true;
                            render();
                        }, 100);
                    }
                }
            }
        } catch (error) {
            console.error('Auth failed:', error);
        }
        appState.isLoading = false;
        render();
    }
</script>
```

## 🔧 Configuration

### 1. Device Whitelist Setup

Edit `device_whitelist.php`:

```php
$whitelist = array(
    'your_device_id_here' => array(
        'name' => 'Device Name', 
        'location' => 'Device Location'
    ),
);
```

### 2. Security Configuration

Both files use these security constants:
```php
define('SHARED_SECRET', 'kiosk-device-validation-secret-2025-v1');
define('JWT_SECRET', 'kiosk-session-token-secret-2025-v1');
define('SESSION_TOKEN_EXPIRY', 24 * 60 * 60); // 24 hours
```

### 3. Android WebView Configuration

Your Android app should be configured with:
```kotlin
// MainActivity.kt - default URL
putString("webview_url", "https://your-domain.com/react_example.html")
putBoolean("device_validation_enabled", true)

// HomeFragment.kt - session token injection
if (url.contains("/api/") || url.contains("api_validate") || url.contains("api_data")) {
    return injectSessionToken(request, sessionToken)
}
```

## 🐛 Troubleshooting

### Common Issues

1. **Stuck on Loading Screen**
   - **Solution**: Use `setTimeout()` for state updates in WebView environments
   - **Code**: `setTimeout(() => setIsAuthenticated(true), 100)`

2. **Missing Authorization Header**
   - **Cause**: Session token not properly extracted from signed response
   - **Solution**: Use `actualData.session_token` instead of `deviceData.session_token`

3. **Device Not Whitelisted**
   - **Check**: Device ID in error screen
   - **Action**: Add device ID to `$whitelist` array in `device_whitelist.php`

### Debug Tools

- **System Test**: Visit `/system_test.php` for health check
- **Debug Console**: Both examples include real-time debug logging
- **Server Logs**: Check `device_access.log` and `security_events.log`

### WebView-Specific Considerations

1. **State Updates**: Always use `setTimeout()` for React state changes
2. **Response Parsing**: Handle signed response structure with nested `data` property
3. **Token Injection**: Pattern matching covers `/api/`, `api_validate`, `api_data`
4. **Error Handling**: Display device ID for failed authentication cases

## 📊 Security Features

- ✅ **HMAC-SHA256 Cryptographic Signatures**
- ✅ **JWT Session Token Management**  
- ✅ **Device Fingerprinting**
- ✅ **Replay Attack Prevention**
- ✅ **Comprehensive Security Logging**
- ✅ **Session Expiration Control**
- ✅ **Signed Response Validation**

## 🚀 Production Deployment

1. **Update Security Secrets**: Change `SHARED_SECRET` and `JWT_SECRET`
2. **Configure Device Whitelist**: Add production device IDs
3. **Enable HTTPS**: All communication should use SSL/TLS
4. **Log Monitoring**: Monitor `security_events.log` for threats
5. **Session Management**: Configure appropriate token expiry times
6. **Database Integration**: Replace array-based whitelist with database

## 📁 Example Files

- **`react_example.html`**: Complete React.js implementation with debug console
- **`vanilla_example.html`**: Pure JavaScript implementation with state management  
- **`SECURITY_IMPLEMENTATION.md`**: Detailed security architecture
- **`SETUP_INSTRUCTIONS.md`**: Step-by-step setup guide

---

## 🎯 Key Takeaway

Both examples provide the same authentication functionality - choose based on your frontend preference:
- **React.js**: Modern component-based architecture with hooks
- **Vanilla JS**: Pure JavaScript with manual DOM management

The authentication flow, security features, and WebView integration are identical in both implementations.