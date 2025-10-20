package com.example.bluetoothjsontester

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val tag = "BT_JSON_UI"
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var listView: ListView
    private lateinit var tvStatus: TextView
    private lateinit var tvLog: TextView
    private lateinit var etJson: EditText
    private lateinit var deviceAdapter: ArrayAdapter<String>
    private val deviceList = mutableListOf<BluetoothDevice>()

    private var connection: BluetoothConnection? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        listView = findViewById(R.id.listDevices)
        tvStatus = findViewById(R.id.tvStatus)
        tvLog = findViewById(R.id.tvLog)
        etJson = findViewById(R.id.etJson)
        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView.adapter = deviceAdapter

        // Permissions
        checkAndRequestBluetoothPermissions()

        // --- Button Handlers ---
        findViewById<Button>(R.id.btnEnable).setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Request BLUETOOTH_CONNECT permission
                    return@setOnClickListener
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Bluetooth already enabled", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnScan).setOnClickListener {
            if (hasBluetoothPermission()) {
                startDiscovery()
            } else {
                requestBluetoothPermission()
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val device = deviceList[position]
            connectTo(device.address)
        }

        findViewById<Button>(R.id.btnHost).setOnClickListener {
            startServer()
        }

        findViewById<Button>(R.id.btnSendJson).setOnClickListener {
            val json = etJson.text.toString()
            if (json.isNotEmpty()) {
                connection?.sendRaw(json.toByteArray())
                appendLog("Sent JSON: $json")
            } else {
                Toast.makeText(this, "Enter JSON to send", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    // Bluetooth Scanning & Permissions
    // --------------------------------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermission()
            return
        }

        deviceAdapter.clear()
        deviceList.clear()
        appendLog("Scanning for devices...")

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        try {
            bluetoothAdapter.startDiscovery()
        } catch (e: SecurityException) {
            appendLog("Permission denied: ${e.message}")
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                try {
                    @Suppress("DEPRECATION")
                    val device:
                            BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && device.name != null) {
                        deviceList.add(device)
                        deviceAdapter.add("${device.name} (${device.address})")
                    }
                } catch (e: SecurityException) {
                    appendLog("SecurityException while discovering: ${e.message}")
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    // Bluetooth Server / Client Setup
    // --------------------------------------------------------------------------------------------

    private fun startServer() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermission()
            return
        }

        val server = BluetoothServer(
            onConnected = { conn ->
                connection = conn
                runOnUiThread {
                    tvStatus.text = "Connected as Server"
                    appendLog("Server: Client connected.")
                }
            },
            onMessageReceived = { message ->
                runOnUiThread {
                    appendLog("Received: $message")
                }
            }
        )
        server.start()
        appendLog("Server started, waiting for client...")
    }

    private fun connectTo(mac: String) {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermission()
            return
        }

        val client = BluetoothClient(mac) { conn ->
            connection = conn
            runOnUiThread {
                tvStatus.text = "Connected to $mac"
                appendLog("Connected to server: $mac")
            }
        }
        client.connect()
        appendLog("Connecting to $mac ...")
    }

    // --------------------------------------------------------------------------------------------
    // Permissions Handling
    // --------------------------------------------------------------------------------------------

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkAndRequestBluetoothPermissions() {
        if (!hasBluetoothPermission()) {
            requestBluetoothPermission()
        }
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                1001
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Bluetooth permissions are required to scan and connect",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    // Utility
    // --------------------------------------------------------------------------------------------

    private fun appendLog(text: String) {
        runOnUiThread {
            tvLog.append("\n$text")
            Log.d(tag, text)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (_: Exception) {
        }
    }
}
