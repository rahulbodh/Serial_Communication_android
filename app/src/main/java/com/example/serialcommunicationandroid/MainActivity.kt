package com.example.serialcommunicationandroid

import android.annotation.SuppressLint
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
import com.example.serialcommunicationandroid.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private var usbDevice: UsbDevice? = null
    private lateinit var usbManager: UsbManager
    private lateinit var usbReceiver: BroadcastReceiver

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initUsb()

        binding.retry.setOnClickListener {
            initUsb()
        }


    }

    private fun initUsb() {
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        // Retrieve connected USB devices
        val deviceList = usbManager.deviceList
        if (deviceList.isNotEmpty()) {
            // Select the first device in the list
            usbDevice = deviceList.values.first()
            Log.d("SerialDevice", "Selected USB Device: ${usbDevice?.deviceName}")
            binding.text.text = "Selected USB Device: ${usbDevice?.deviceName} \n"
            // Prepare PendingIntent for permission request
            val permissionIntent = PendingIntent.getBroadcast(
                this, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
            )

            // Define the broadcast receiver
            usbReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (ACTION_USB_PERMISSION == intent.action) {
                        synchronized(this) {
                            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, true)) {
                                device?.let {
                                    Log.d("SerialDevice", "Permission granted for device: ${it.deviceName}")
                                    binding.text.append("Permission granted for device: ${it.deviceName} \n")
                                    // Initialize communication with the USB device here
                                }
                            } else {
                                Log.d("SerialDevice", "Permission denied for device: $device")
                                binding.text.append("Permission denied for device: $device \n")
                            }
                        }
                    }
                }
            }

            // Register the receiver and request permission for the device
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(usbReceiver, filter, RECEIVER_EXPORTED)

            usbDevice?.let {
                usbManager.requestPermission(it, permissionIntent)
            } ?: run {
                Log.e("SerialDevice", "No valid USB device found to request permission.")
                binding.text.append("No valid USB device found to request permission. \n")
            }
        } else {
            Log.e("SerialDevice", "No USB devices connected.")
            binding.text.append("No USB devices connected. \n")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::usbReceiver.isInitialized) {
            unregisterReceiver(usbReceiver)
        }
    }
}
