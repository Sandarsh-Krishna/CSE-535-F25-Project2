// file: app/src/main/java/edu/asu/cse535/meseretictactoe/BluetoothP2P.kt
package edu.asu.cse535.meseretictactoe

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.Executors

object BluetoothP2P {
    // ----------------------
    // Constants / utilities
    // ----------------------
    private const val TAG = "BluetoothP2P"

    // Use YOUR app UUID so both sides match
    val APP_UUID: UUID = UUID.fromString("c4b2b2d4-7c09-4f9f-9c1c-0f3c2a3f43c9")
    const val SERVICE_NAME = "MesereTTT"

    fun adapter(ctx: Context): BluetoothAdapter? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }

    fun hasAllPermissions(ctx: Context, permissions: Array<String>): Boolean =
        permissions.all { p ->
            ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED
        }

    fun hasConnectPermission(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED
        else true

    fun hasScanPermission(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED
        else true

    @SuppressLint("MissingPermission") // guarded by hasConnectPermission
    fun bondedDevices(ctx: Context): List<Pair<String, String>> {
        if (!hasConnectPermission(ctx)) return emptyList()
        val ad = adapter(ctx) ?: return emptyList()
        return ad.bondedDevices
            .map { it.address to (it.name ?: it.address) }
            .sortedBy { it.second }
    }

    fun requiredRuntimePermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        else
            emptyArray()

    fun ensurePermissions(activity: Activity, requestCode: Int = 1001) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity, requiredRuntimePermissions(), requestCode)
        }
    }

    // ----------------------
    // Internal state
    // ----------------------
    @Volatile private var serverSocket: BluetoothServerSocket? = null
    @Volatile private var socket: BluetoothSocket? = null
    @Volatile private var input: InputStream? = null
    @Volatile private var output: OutputStream? = null

    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val gson = Gson()

    @Volatile private var onMessageCb: ((String) -> Unit)? = null
    @Volatile private var onConnectedCb: (() -> Unit)? = null
    @Volatile private var onErrorCb: ((String) -> Unit)? = null

    // ----------------------
    // Public API (host/client/send/close)
    // ----------------------

    /** Set callbacks used by host/client. Optional, you can also pass them in start/connect. */
    fun setCallbacks(
        onMessage: (String) -> Unit,
        onConnected: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        onMessageCb = onMessage
        onConnectedCb = onConnected
        onErrorCb = onError
    }

    /** True if a socket is currently connected. */
    fun isConnected(): Boolean = socket?.isConnected == true

    /** Start as HOST (server). Returns false if missing permission / no adapter. */
    @SuppressLint("MissingPermission") // guarded by hasConnectPermission
    fun startServer(
        ctx: Context,
        onMessage: (String) -> Unit = { s -> onMessageCb?.invoke(s) },
        onConnected: (() -> Unit)? = onConnectedCb,
        onError: ((String) -> Unit)? = onErrorCb,
        insecure: Boolean = true
    ): Boolean {
        if (!hasConnectPermission(ctx)) {
            onError?.invoke("Missing BLUETOOTH_CONNECT permission")
            return false
        }
        val ad = adapter(ctx) ?: run {
            onError?.invoke("Bluetooth not supported")
            return false
        }

        close() // ensure clean slate

        try {
            serverSocket = if (insecure) {
                ad.listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, APP_UUID)
            } else {
                ad.listenUsingRfcommWithServiceRecord(SERVICE_NAME, APP_UUID)
            }

            Thread {
                try {
                    Log.d(TAG, "Server: waiting for client...")
                    val s = serverSocket?.accept() ?: throw IllegalStateException("accept() returned null")
                    socket = s
                    input = s.inputStream
                    output = s.outputStream
                    Log.d(TAG, "Server: client connected ${s.remoteDevice?.address}")
                    onConnected?.invoke()
                    startReader(onMessage)
                } catch (e: Exception) {
                    onError?.invoke("Server error: ${e.message}")
                    Log.e(TAG, "Server error", e)
                    close()
                } finally {
                    try { serverSocket?.close() } catch (_: Exception) {}
                    serverSocket = null
                }
            }.start()

            return true
        } catch (e: Exception) {
            onError?.invoke("Server start failed: ${e.message}")
            Log.e(TAG, "listenUsingRfcomm failed", e)
            return false
        }
    }

    /** Connect as CLIENT to a known MAC address. Returns false on immediate pre-check failure. */
    @SuppressLint("MissingPermission") // guarded by hasConnectPermission
    fun connectTo(
        ctx: Context,
        macAddress: String,
        onMessage: (String) -> Unit = { s -> onMessageCb?.invoke(s) },
        onConnected: (() -> Unit)? = onConnectedCb,
        onError: ((String) -> Unit)? = onErrorCb,
        insecure: Boolean = true
    ): Boolean {
        if (!hasConnectPermission(ctx)) {
            onError?.invoke("Missing BLUETOOTH_CONNECT permission")
            return false
        }
        val ad = adapter(ctx) ?: run {
            onError?.invoke("Bluetooth not supported")
            return false
        }

        close() // ensure clean slate

        Thread {
            try {
                val device: BluetoothDevice = ad.getRemoteDevice(macAddress)
                val s: BluetoothSocket = if (insecure) {
                    device.createInsecureRfcommSocketToServiceRecord(APP_UUID)
                } else {
                    device.createRfcommSocketToServiceRecord(APP_UUID)
                }
                ad.cancelDiscovery()
                Log.d(TAG, "Client: connecting to $macAddress …")
                s.connect()
                socket = s
                input = s.inputStream
                output = s.outputStream
                Log.d(TAG, "Client: connected")
                onConnected?.invoke()
                startReader(onMessage)
            } catch (e: SecurityException) {
                onError?.invoke("BT SecurityException: ${e.message}")
                Log.e(TAG, "Client security", e)
                close()
            } catch (e: Exception) {
                onError?.invoke("Client connect failed: ${e.message}")
                Log.e(TAG, "Client connect failed", e)
                close()
            }
        }.start()

        return true
    }

    /** Send raw bytes; returns false if not connected. */
    fun sendRaw(bytes: ByteArray): Boolean {
        val out = output ?: return false
        return try {
            synchronized(this) {
                out.write(bytes)
                out.flush()
            }
            true
        } catch (e: Exception) {
            onErrorCb?.invoke("Send failed: ${e.message}")
            Log.e(TAG, "sendRaw", e)
            false
        }
    }

    /** Convenience: send text / JSON string. */
    fun sendText(text: String): Boolean = sendRaw(text.toByteArray())

    /** Convenience: serialize with Gson and send. (Requires Gson dependency) */
    fun sendObject(obj: Any): Boolean = sendText(gson.toJson(obj))

    /** Close everything. Safe to call multiple times. */
    fun close() {
        try { input?.close() } catch (_: Exception) {}
        try { output?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        try { serverSocket?.close() } catch (_: Exception) {}
        input = null; output = null; socket = null; serverSocket = null
    }

    // ----------------------
    // Internal: reader loop
    // ----------------------
    private fun startReader(onMessage: (String) -> Unit) {
        val ins = input ?: return
        ioExecutor.execute {
            val buffer = ByteArray(1024)
            while (true) {
                try {
                    val n = ins.read(buffer)
                    if (n <= 0) break
                    val msg = String(buffer, 0, n)
                    Log.d(TAG, "recv: $msg")
                    onMessage(msg)
                } catch (e: Exception) {
                    Log.e(TAG, "read loop ended: ${e.message}")
                    break
                }
            }
            close()
        }
    }
}
