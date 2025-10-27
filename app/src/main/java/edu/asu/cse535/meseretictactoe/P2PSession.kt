package edu.asu.cse535.meseretictactoe

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket


object P2PSession {
    private var btSocket: BluetoothSocket? = null
    private var netSocket: Socket? = null
    private var writer: PrintWriter? = null


    var amHost: Boolean? = null
        private set

    private val _connected = MutableStateFlow(false)
    val connected = _connected.asStateFlow()

    private val _incoming = MutableSharedFlow<String>(extraBufferCapacity = 32)
    val incoming = _incoming.asSharedFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val errors = _errors.asSharedFlow()

    // ---------- Bluetooth ----------

    @SuppressLint("MissingPermission")
    fun host(ctx: Context) {
        amHost = true // I'm hosting, I will be X
        val ad = BluetoothP2P.adapter(ctx) ?: return
        if (!BluetoothP2P.hasConnectPermission(ctx)) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val server: BluetoothServerSocket =
                    ad.listenUsingRfcommWithServiceRecord(
                        BluetoothP2P.SERVICE_NAME,
                        BluetoothP2P.APP_UUID
                    )
                val s = server.accept()
                server.close()
                onBtSocketReady(s)
            } catch (t: Throwable) {
                _connected.value = false
                _errors.tryEmit("Bluetooth hosting failed: ${t.message ?: "Unknown error"}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun join(ctx: Context, address: String) {
        amHost = false // I'm joining, I will be O
        val ad = BluetoothP2P.adapter(ctx) ?: return
        if (!BluetoothP2P.hasConnectPermission(ctx)) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val device: BluetoothDevice = ad.getRemoteDevice(address)
                val s = device.createRfcommSocketToServiceRecord(BluetoothP2P.APP_UUID)
                if (BluetoothP2P.hasScanPermission(ctx)) {
                    try { ad.cancelDiscovery() } catch (_: Throwable) {}
                }
                s.connect()
                onBtSocketReady(s)
            } catch (t: Throwable) {
                _connected.value = false
                _errors.tryEmit("Bluetooth connect failed: ${t.message ?: "Unknown error"}")
            }
        }
    }

    private fun onBtSocketReady(s: BluetoothSocket) {
        btSocket = s
        writer = PrintWriter(s.outputStream, true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rd = BufferedReader(InputStreamReader(s.inputStream))
                while (true) {
                    val line = rd.readLine() ?: break
                    _incoming.tryEmit(line)
                }
            } catch (_: Throwable) { }
            _connected.value = false
        }
        _connected.value = true
    }

    // ---------- LAN ----------

    fun hostLan() {
        amHost = true
        NetP2P.host(
            onSocket = { s -> onNetSocketReady(s) },
            onError = { t ->
                _connected.value = false
                _errors.tryEmit("Hosting failed: ${t.message ?: "Unknown error"}")
            }
        )
    }

    fun joinLan(ip: String) {
        amHost = false
        NetP2P.join(
            ip = ip,
            onSocket = { s -> onNetSocketReady(s) },
            onError = { _ ->
                _connected.value = false
                _errors.tryEmit("Cannot connect to $ip")
            }
        )
    }

    private fun onNetSocketReady(s: Socket) {
        netSocket = s
        writer = NetP2P.ioLoop(s) { _incoming.tryEmit(it) }
        _connected.value = true
    }

    // ---------- Messaging ----------

    fun send(line: String) {
        writer?.println(line)
    }

    @Suppress("unused")
    fun close() {
        try { writer?.flush() } catch (_: Throwable) {}
        try { btSocket?.close() } catch (_: Throwable) {}
        try { netSocket?.close() } catch (_: Throwable) {}
        btSocket = null
        netSocket = null
        writer = null
        amHost = null
        _connected.value = false
    }
}
