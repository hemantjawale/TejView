package com.example.tejview.services

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

/**
 * Bluetooth Low Energy manager for connecting to external wearables,
 * smartwatches, and health monitoring devices.
 *
 * Handles scanning, connecting, and reading real-time data streams
 * from devices that expose standard health-related GATT services:
 * - Heart Rate Service (0x180D)
 * - Blood Pressure Service (0x1810)
 * - Health Thermometer (0x1809)
 * - Pulse Oximeter (fictional custom UUID for demo)
 */
class BleHealthManager(private val context: Context) {

    companion object {
        private const val TAG = "BleHealthManager"

        // Standard Bluetooth Health GATT Service UUIDs
        val HEART_RATE_SERVICE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

        val BLOOD_PRESSURE_SERVICE: UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb")
        val BLOOD_PRESSURE_MEASUREMENT: UUID = UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb")

        val SPO2_SERVICE: UUID = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb")
        val SPO2_MEASUREMENT: UUID = UUID.fromString("00002a5e-0000-1000-8000-00805f9b34fb")

        // Client Characteristic Configuration Descriptor
        val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var scanner: BluetoothLeScanner? = null

    // State flows for reactive data
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _heartRateData = MutableStateFlow(0)
    val heartRateData: StateFlow<Int> = _heartRateData

    private val _spo2Data = MutableStateFlow(0)
    val spo2Data: StateFlow<Int> = _spo2Data

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices

    enum class ConnectionState {
        DISCONNECTED, SCANNING, CONNECTING, CONNECTED, DISCONNECTING
    }

    /**
     * Start scanning for BLE health devices
     */
    fun startScan() {
        if (!hasBluetoothPermissions()) {
            Log.w(TAG, "Bluetooth permissions not granted")
            return
        }

        if (bluetoothAdapter?.isEnabled != true) {
            Log.w(TAG, "Bluetooth not enabled")
            return
        }

        _connectionState.value = ConnectionState.SCANNING
        scanner = bluetoothAdapter?.bluetoothLeScanner

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(HEART_RATE_SERVICE))
                .build()
        )

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        try {
            scanner?.startScan(filters, settings, scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "Scan failed: ${e.message}")
        }
    }

    /**
     * Stop BLE scanning
     */
    fun stopScan() {
        try {
            scanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "Stop scan failed: ${e.message}")
        }
        if (_connectionState.value == ConnectionState.SCANNING) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    /**
     * Connect to a specific BLE device
     */
    fun connect(device: BluetoothDevice) {
        _connectionState.value = ConnectionState.CONNECTING
        try {
            bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } catch (e: SecurityException) {
            Log.e(TAG, "Connect failed: ${e.message}")
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    /**
     * Disconnect from current device
     */
    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTING
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (e: SecurityException) {
            Log.e(TAG, "Disconnect failed: ${e.message}")
        }
        bluetoothGatt = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    // ========== BLE Callbacks ==========

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val currentList = _discoveredDevices.value.toMutableList()
            if (!currentList.any { it.address == device.address }) {
                currentList.add(device)
                _discoveredDevices.value = currentList
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code: $errorCode")
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.CONNECTED
                    try {
                        gatt.discoverServices()
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Discover services failed: ${e.message}")
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    try {
                        gatt.close()
                    } catch (e: SecurityException) {
                        Log.e(TAG, "Close failed: ${e.message}")
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Subscribe to heart rate notifications
                enableNotifications(gatt, HEART_RATE_SERVICE, HEART_RATE_MEASUREMENT)
                // Subscribe to SpO2 notifications
                enableNotifications(gatt, SPO2_SERVICE, SPO2_MEASUREMENT)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            when (characteristic.uuid) {
                HEART_RATE_MEASUREMENT -> {
                    // Parse heart rate per Bluetooth spec
                    val flags = value[0].toInt()
                    val isUint16 = (flags and 0x01) != 0
                    val heartRate = if (isUint16) {
                        ((value[2].toInt() and 0xFF) shl 8) or (value[1].toInt() and 0xFF)
                    } else {
                        value[1].toInt() and 0xFF
                    }
                    _heartRateData.value = heartRate
                }
                SPO2_MEASUREMENT -> {
                    // Parse SpO2 value
                    if (value.size >= 2) {
                        _spo2Data.value = value[1].toInt() and 0xFF
                    }
                }
            }
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, serviceUuid: UUID, charUuid: UUID) {
        val service = gatt.getService(serviceUuid) ?: return
        val characteristic = service.getCharacteristic(charUuid) ?: return

        try {
            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(CCCD)
            descriptor?.let {
                gatt.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Enable notifications failed: ${e.message}")
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopScan()
        disconnect()
    }
}
