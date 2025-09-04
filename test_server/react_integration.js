// Device Validation for React App
// Copy this code into your React component

import React, { useState, useEffect } from 'react';

const DeviceValidation = () => {
    const [status, setStatus] = useState('loading'); // 'loading', 'authorized', 'denied', 'error'
    const [deviceData, setDeviceData] = useState(null);
    const [error, setError] = useState(null);

    const validateDevice = async () => {
        setStatus('loading');
        setError(null);
        
        try {
            const response = await fetch('/api/device_whitelist.php', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ action: 'validate' })
            });
            
            // Check if response is JSON
            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Server returned HTML instead of JSON. Check PHP script for errors.');
            }
            
            const data = await response.json();
            setDeviceData(data);
            
            if (data.access_granted) {
                setStatus('authorized');
            } else {
                setStatus('denied');
            }
            
        } catch (err) {
            setError(err.message);
            setStatus('error');
        }
    };

    useEffect(() => {
        validateDevice();
    }, []);

    // Loading State
    if (status === 'loading') {
        return (
            <div className="device-validation loading">
                <div className="icon">🔍</div>
                <h2>Checking Device Authorization</h2>
                <p>Validating device access...</p>
            </div>
        );
    }

    // Success State
    if (status === 'authorized') {
        return (
            <div className="device-validation success">
                <div className="icon">✅</div>
                <h1>Access Granted</h1>
                <p><strong>Device Authorized Successfully</strong></p>
                <div className="device-info">
                    <h3>Device Information</h3>
                    <p><strong>Device ID:</strong></p>
                    <div className="device-id">{deviceData?.device_id || 'Unknown'}</div>
                    {deviceData?.device_info?.location && (
                        <p><strong>Location:</strong> {deviceData.device_info.location}</p>
                    )}
                    {deviceData?.device_info?.name && (
                        <p><strong>Name:</strong> {deviceData.device_info.name}</p>
                    )}
                </div>
                <button className="retry-btn" onClick={validateDevice}>
                    🔄 Check Again
                </button>
            </div>
        );
    }

    // Denied State
    if (status === 'denied') {
        return (
            <div className="device-validation denied">
                <div className="icon">❌</div>
                <h1>Access Denied</h1>
                <p><strong>Device Not Authorized</strong></p>
                <div className="device-info">
                    <h3>Device Information</h3>
                    <p><strong>Your Device ID:</strong></p>
                    <div className="device-id">{deviceData?.device_id || 'Unable to detect'}</div>
                    <p className="admin-note">
                        <strong>For Administrators:</strong><br/>
                        Add the above Device ID to the whitelist to authorize this device.
                    </p>
                </div>
                <button className="retry-btn" onClick={validateDevice}>
                    🔄 Try Again
                </button>
            </div>
        );
    }

    // Error State
    if (status === 'error') {
        return (
            <div className="device-validation error">
                <div className="icon">⚠️</div>
                <h1>Validation Error</h1>
                <p><strong>Unable to check device authorization</strong></p>
                <div className="device-info">
                    <h3>Error Details</h3>
                    <p className="error-message">{error}</p>
                    <p className="troubleshoot">
                        <strong>Common causes:</strong><br/>
                        • PHP script has syntax errors<br/>
                        • Server configuration issues<br/>
                        • Network connectivity problems
                    </p>
                </div>
                <button className="retry-btn" onClick={validateDevice}>
                    🔄 Retry
                </button>
            </div>
        );
    }

    return null;
};

// CSS styles for your React app
const styles = `
.device-validation {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background: white;
    border-radius: 20px;
    padding: 40px;
    box-shadow: 0 20px 40px rgba(0,0,0,0.1);
    max-width: 500px;
    width: 100%;
    text-align: center;
    margin: 20px auto;
}

.device-validation .icon {
    font-size: 4em;
    margin-bottom: 20px;
}

.device-validation.loading p {
    color: #666;
    font-size: 18px;
}

.device-validation.success h1 {
    color: #28a745;
    font-size: 2.5em;
    margin-bottom: 20px;
}

.device-validation.denied h1,
.device-validation.error h1 {
    color: #dc3545;
    font-size: 2.5em;
    margin-bottom: 20px;
}

.device-info {
    background: #f8f9fa;
    border-radius: 10px;
    padding: 20px;
    margin: 20px 0;
    border-left: 4px solid #007bff;
}

.device-info h3 {
    margin-top: 0;
    color: #495057;
}

.device-id {
    font-family: 'Courier New', monospace;
    background: #e9ecef;
    padding: 10px;
    border-radius: 5px;
    font-size: 14px;
    word-break: break-all;
}

.retry-btn {
    background: #007bff;
    color: white;
    border: none;
    padding: 12px 30px;
    border-radius: 25px;
    font-size: 16px;
    cursor: pointer;
    margin-top: 20px;
}

.retry-btn:hover {
    background: #0056b3;
}

.admin-note,
.troubleshoot {
    color: #6c757d;
    font-size: 14px;
    margin-top: 15px;
}

.error-message {
    color: #dc3545;
}
`;

export default DeviceValidation;