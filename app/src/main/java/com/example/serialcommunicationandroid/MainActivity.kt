package com.example.serialcommunicationandroid

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private  val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private  var usbDevice: UsbDevice ?= null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager


        val deviceList = usbManager.getDeviceList()

        for (device in deviceList) {

            usbDevice = deviceList.values.first()
            Log.d("SerialDevice", "Found USB Device: ${usbDevice?.deviceName}")
            val deviceManufacturer = device.value.manufacturerName
            val deviceClass = device.value.deviceClass
            val deviceSubclass = device.value.deviceSubclass
            val deviceProtocol = device.value.deviceProtocol
            val deviceVendorId = device.value.interfaceCount
            val deviceProductId = device.value.productId
            Log.d("SerialDevice", "Device: $deviceManufacturer $deviceClass $deviceSubclass $deviceProtocol $deviceVendorId $deviceProductId")
        }




         val usbReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                if (ACTION_USB_PERMISSION == intent.action) {
                    synchronized(this) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            usbDevice?.apply {
                                // call method to set up device communication
                            }
                        } else {
                            Log.d("SerialDevice", "permission denied for device $usbDevice")
                        }
                    }
                }
            }
        }

        val permissionIntent = PendingIntent.getBroadcast(
            this, 0,
            Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
        )
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter, RECEIVER_EXPORTED)

        usbManager.requestPermission(usbDevice,permissionIntent)

    }
}