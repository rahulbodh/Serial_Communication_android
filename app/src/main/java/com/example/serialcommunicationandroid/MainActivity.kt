package com.example.serialcommunicationandroid

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val manager = getSystemService(Context.USB_SERVICE) as UsbManager

        val deviceList = manager.getDeviceList()

        for (device in deviceList) {
            val deviceManufacturer = device.value.manufacturerName
            val deviceClass = device.value.deviceClass
            val deviceSubclass = device.value.deviceSubclass
            val deviceProtocol = device.value.deviceProtocol
            val deviceVendorId = device.value.interfaceCount
            val deviceProductId = device.value.productId
            Log.d("SerialDevice", "Device: $deviceManufacturer $deviceClass $deviceSubclass $deviceProtocol $deviceVendorId $deviceProductId")
        }


    }
}