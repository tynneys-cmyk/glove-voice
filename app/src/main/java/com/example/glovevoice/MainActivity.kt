
package com.example.glovevoice

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import java.io.IOException
import java.util.Locale
import java.util.UUID

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private val bluetoothAdapter: BluetoothAdapter? =
        BluetoothAdapter.getDefaultAdapter()

    private val uuid: UUID = UUID.fromString(
        "00001101-0000-1000-8000-00805F9B34FB"
    )

    private var socket: BluetoothSocket? = null
    private var tts: TextToSpeech? = null

    private lateinit var status: TextView
    private lateinit var received: TextView

    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
        }

        val title = TextView(this).apply {
            text = "GLOVE VOICE"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "Bluetooth disconnected"
            textSize = 18f
            gravity = Gravity.CENTER
        }

        val connectButton = Button(this).apply {
            text = "CONNECT ESP32"
            setOnClickListener {
                checkPermissionAndConnect()
            }
        }

        received = TextView(this).apply {
            text = "Waiting for glove..."
            textSize = 23f
            gravity = Gravity.CENTER
        }

        val disconnectButton = Button(this).apply {
            text = "DISCONNECT"
            setOnClickListener {
                disconnect()
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(connectButton)
        layout.addView(received)
        layout.addView(disconnectButton)

        setContentView(layout)
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
        }
    }

    private fun checkPermissionAndConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                permissionCode
            )
            return
        }

        showPairedDevices()
    }

    private fun showPairedDevices() {
        val adapter = bluetoothAdapter

        if (adapter == null) {
            status.text = "Bluetooth not supported"
            return
        }

        if (!adapter.isEnabled) {
            status.text = "Please enable Bluetooth"
            return
        }

        try {
            val devices = adapter.bondedDevices.toList()

            if (devices.isEmpty()) {
                status.text = "Pair your ESP32 in phone settings first"
                return
            }

            val names = devices.map {
                "${it.name ?: "Unknown device"} - ${it.address}"
            }

            AlertDialog.Builder(this)
                .setTitle("Select your ESP32")
                .setItems(names.toTypedArray()) { _, which ->
                    connectToDevice(devices[which])
                }
                .setNegativeButton("Cancel", null)
                .show()

        } catch (e: SecurityException) {
            status.text = "Bluetooth permission required"
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        status.text = "Connecting..."

        Thread {
            try {
                bluetoothAdapter?.cancelDiscovery()

                val newSocket = device.createRfcommSocketToServiceRecord(uuid)
                newSocket.connect()
                socket = newSocket

                runOnUiThread {
                    status.text = "Connected to ${device.name}"
                }

                listenForMessages(newSocket)

            } catch (e: IOException) {
                runOnUiThread {
                    status.text = "Connection failed. Try again."
                }
                disconnect()
            } catch (e: SecurityException) {
                runOnUiThread {
                    status.text = "Bluetooth permission denied"
                }
            }
        }.start()
    }

    private fun listenForMessages(btSocket: BluetoothSocket) {
        try {
            val reader = btSocket.inputStream.bufferedReader()

            while (btSocket.isConnected) {
                val message = reader.readLine() ?: break

                runOnUiThread {
                    received.text = message
                    tts?.speak(
                        message,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "glove_message"
                    )
                }
            }

        } catch (e: IOException) {
            runOnUiThread {
                status.text = "Bluetooth disconnected"
            }
        }
    }

    private fun disconnect() {
        try {
            socket?.close()
        } catch (_: IOException) {
        }

        socket = null
        status.text = "Bluetooth disconnected"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == permissionCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            showPairedDevices()
        }
    }

    override fun onDestroy() {
        disconnect()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
