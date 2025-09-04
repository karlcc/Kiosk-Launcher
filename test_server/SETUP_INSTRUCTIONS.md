# Device Validation Setup Instructions

## Quick Setup:

### 1. Upload Files to Your Server:
- `simple_validation.html` - Simple test page (2 cases: pass/fail)
- `device_whitelist.php` - Validation API (enhanced with error handling)

### 2. Add Your Device ID to Whitelist:
1. Load `simple_validation.html` in your kiosk app
2. You'll see "Access Denied" with your Device ID displayed
3. Copy that Device ID
4. Edit `device_whitelist.php` and add your device ID like this:

```php
$whitelisted_devices = [
    'YOUR_DEVICE_ID_HERE' => ['name' => 'My Kiosk Device', 'location' => 'Office'],
    // ... other devices
];
```

### 3. Test Both Cases:
- **FAIL case**: Load page before adding device ID → "Access Denied" 
- **PASS case**: Load page after adding device ID → "Access Granted"

## For Your React App:

### Simple JavaScript validation function (copy this into your React component):
```javascript
const [deviceStatus, setDeviceStatus] = useState('loading'); // 'loading', 'authorized', 'denied', 'error'
const [deviceData, setDeviceData] = useState(null);

async function validateDevice() {
    setDeviceStatus('loading');
    
    try {
        const response = await fetch('device_whitelist.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'validate' })
        });
        
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            throw new Error('Server returned HTML instead of JSON');
        }
        
        const data = await response.json();
        setDeviceData(data);
        
        if (data.access_granted) {
            setDeviceStatus('authorized');
            // SUCCESS: Show your app content
        } else {
            setDeviceStatus('denied');
            // FAIL: Show "contact admin" message + device ID
        }
        
    } catch (error) {
        setDeviceStatus('error');
        console.error('Validation error:', error.message);
    }
}

// Call validateDevice() when your app starts
useEffect(() => {
    validateDevice();
}, []);
```

## Files Included:
- ✅ `simple_validation.html` - Clean test page with 2 states
- ✅ `device_whitelist.php` - API with proper error handling  
- ✅ `SETUP_INSTRUCTIONS.md` - This setup guide
- ✅ PHP error handling ensures JSON responses
- ✅ Selective request interception (no more HTML source display)

## Expected Results:
- **Authorized Device**: Green checkmark, "Access Granted" 
- **Unknown Device**: Red X, "Access Denied" + Device ID for admin
- **Server Error**: Warning icon, error details for troubleshooting