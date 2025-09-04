# 🔐 Hybrid Security System - Setup Instructions

## Overview

This system provides **enterprise-grade security** for kiosk applications using a hybrid approach:

1. **Initial Device Validation**: Cryptographic signature verification (HMAC-SHA256)
2. **Session-Based APIs**: JWT tokens for all React.js API calls
3. **Automatic Token Management**: WebView handles authentication seamlessly

## Architecture

```
📱 Android Kiosk App                 🖥️ Your Server
┌─────────────────────┐             ┌─────────────────────┐
│  WebView loads      │──── Signed ──▶│  device_whitelist   │
│  React app          │   Request     │  .php validates     │
│                     │◀──── JWT ─────│  & issues token     │
│  React makes API    │   Token       │                     │
│  calls with tokens  │◀─────────────▶│  api_validate.php   │
│  (auto-injected)    │               │  api_data.php       │
└─────────────────────┘               └─────────────────────┘
```

## Quick Setup

### 1. Deploy Server Files

Upload these files to your web server:

```
your-server/
├── device_whitelist.php    # Main validation endpoint
├── api_validate.php        # Session token validation
├── api_data.php           # Example protected API
├── react_example.html     # Working React.js example
├── REACT_INTEGRATION_GUIDE.md
└── SECURITY_IMPLEMENTATION.md
```

### 2. Add Your Device to Whitelist

1. **Enable device validation** in your Android kiosk app (long-press config → Device Validation: ON)

2. **Load your React app** in the kiosk - you'll see "Access Denied" with your device ID

3. **Add device ID to whitelist** in `device_whitelist.php`:

```php
$whitelist = array(
    'your_device_id_here' => array(
        'name' => 'My Kiosk Device', 
        'location' => 'Office Reception'
    ),
    // Add more devices as needed
);
```

4. **Reload app** - should now show "Access Granted" with session token

### 3. Integrate with Your React.js App

See `REACT_INTEGRATION_GUIDE.md` for complete integration details, or use this quick template:

```jsx
import React, { useState, useEffect } from 'react';

const App = () => {
    const [authStatus, setAuthStatus] = useState('loading');
    const [deviceInfo, setDeviceInfo] = useState(null);
    const [apiData, setApiData] = useState(null);

    // Check authentication on startup
    useEffect(() => {
        checkAuth();
    }, []);

    const checkAuth = async () => {
        try {
            const response = await fetch('/api_validate.php');
            const data = await response.json();
            
            if (data.authenticated) {
                setAuthStatus('authenticated');
                setDeviceInfo(data.device_info);
                loadApiData();
            } else {
                setAuthStatus('denied');
            }
        } catch (error) {
            setAuthStatus('error');
        }
    };

    const loadApiData = async () => {
        try {
            // Session token is automatically injected by WebView
            const response = await fetch('/api_data.php');
            const data = await response.json();
            setApiData(data);
        } catch (error) {
            console.error('API call failed:', error);
        }
    };

    if (authStatus === 'loading') {
        return <div>🔐 Authenticating device...</div>;
    }

    if (authStatus === 'denied') {
        return (
            <div>
                <h1>🚫 Access Denied</h1>
                <p>Device not authorized. Contact administrator.</p>
            </div>
        );
    }

    if (authStatus === 'error') {
        return <div>❌ Authentication error. Please refresh.</div>;
    }

    return (
        <div>
            <h1>✅ Welcome to Kiosk App</h1>
            <p>Device: {deviceInfo?.device_name}</p>
            <p>Location: {deviceInfo?.device_location}</p>
            {apiData && (
                <div>
                    <h2>Dashboard Data</h2>
                    <pre>{JSON.stringify(apiData, null, 2)}</pre>
                </div>
            )}
        </div>
    );
};

export default App;
```

## Security Features

### ✅ **What's Protected:**

- **Initial device validation**: HMAC-SHA256 cryptographic signatures
- **All API calls**: JWT session token authentication  
- **Request tampering**: Any modification invalidates signatures
- **Replay attacks**: Timestamp + nonce prevent request reuse
- **Session hijacking**: Tokens expire and rotate automatically

### 🛡️ **Attack Resistance:**

| Attack Vector | Protection Level |
|---------------|------------------|
| Device ID spoofing | 🟢 Cryptographically prevented |
| API call forgery | 🟢 JWT tokens required |
| Network interception | 🟢 Signed requests/responses |
| Session replay | 🟢 Timestamp validation |
| Browser-based attacks | 🟢 CORS + WebView only |

## API Endpoints Reference

### Device Validation (Automatic)
- **URL**: `/device_whitelist.php`
- **Method**: Handled automatically by WebView
- **Authentication**: Cryptographic signatures (HMAC-SHA256)
- **Response**: Device authorization + session token

### Session Validation
- **URL**: `/api_validate.php`
- **Method**: GET/POST
- **Authentication**: `Authorization: Bearer <token>` (auto-injected)
- **Use**: Verify session status, get device info

### Protected APIs
- **URL**: `/api_data.php` (example)
- **Method**: GET/POST
- **Authentication**: Session token (auto-injected by WebView)
- **Use**: Your application's API endpoints

## Testing & Troubleshooting

### Test the System

1. **Load `react_example.html`** in your kiosk app
2. **Should see device authentication** in progress
3. **If denied**: Check device ID is in whitelist
4. **If authenticated**: See dashboard with session info

### Common Issues

**"Access Denied" - Device not whitelisted:**
```bash
# Check server logs
tail -f device_access.log security_events.log

# Add device ID to whitelist in device_whitelist.php
```

**"Session Expired" - Token issues:**
```bash
# Tokens expire after 24 hours by default
# App should automatically re-authenticate
# Check server logs for token validation errors
```

**"API Error" - Network issues:**
```bash
# Check API endpoints are accessible
# Verify CORS settings for your domain
# Check server PHP error logs
```

### Log Files

- **`device_access.log`**: Successful authentications
- **`security_events.log`**: Security events (JSON format)
- **Server error logs**: PHP errors and API issues

## Production Deployment

### Security Checklist

- [ ] **Device IDs added** to production whitelist
- [ ] **CORS configured** for your production domain  
- [ ] **HTTPS enforced** for all communication
- [ ] **JWT secret changed** from default value
- [ ] **Session expiry** set appropriately (24h recommended)
- [ ] **Log monitoring** enabled for security events
- [ ] **Backup strategy** for device whitelist data

### Performance Optimization

- [ ] **Database storage** for large device whitelists (instead of PHP array)
- [ ] **Redis/Memcached** for session token caching
- [ ] **Load balancing** for high-traffic deployments
- [ ] **CDN configuration** for static assets

### Monitoring

- [ ] **Security event alerts** for failed authentications
- [ ] **Session token usage** analytics
- [ ] **API performance** monitoring
- [ ] **Device connectivity** status tracking

## Advanced Configuration

### Custom Session Duration
```php
// In device_whitelist.php
define('SESSION_TOKEN_EXPIRY', 12 * 60 * 60); // 12 hours
```

### Custom JWT Claims
```php
// Add custom claims to JWT tokens
$payload = [
    'iss' => 'your-organization',
    'sub' => $device_id,
    'custom_role' => 'kiosk_user',
    'permissions' => ['read', 'dashboard'],
    'location_code' => 'NYC-01'
];
```

### Multiple API Endpoints
Create additional protected endpoints following the `api_data.php` pattern:

```php
// api_reports.php, api_settings.php, etc.
$user_session = validateSessionToken(); // Reusable function
// Your API logic here...
```

## Support

- **Integration Guide**: `REACT_INTEGRATION_GUIDE.md`
- **Security Details**: `SECURITY_IMPLEMENTATION.md` 
- **Example App**: `react_example.html`

This hybrid security system provides **enterprise-grade protection** while maintaining full **React.js compatibility** for modern web applications.