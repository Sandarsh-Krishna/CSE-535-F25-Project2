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

object P2PSession {
    private var socket: BluetoothSocket? = null
    private var writer: PrintWriter? = null

    private val _connected = MutableStateFlow(false)
    val connected = _connected.asStateFlow()

    private val _incoming = MutableSharedFlow<String>(extraBufferCapacity = 32)
    val incoming = _incoming.asSharedFlow()

    /** Host waits for an incoming RFCOMM socket. Safe against missing permission. */
    @SuppressLint("MissingPermission") // guarded and try/catch SecurityException
    fun host(ctx: Context) {
        val ad = BluetoothP2P.adapter(ctx) ?: return
        if (!BluetoothP2P.hasConnectPermission(ctx)) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val server: BluetoothServerSocket =
                    ad.listenUsingRfcommWithServiceRecord(BluetoothP2P.SERVICE_NAME, BluetoothP2P.APP_UUID)
                val s = server.accept() // blocking wait
                server.close()
                onSocketReady(s)
            } catch (se: SecurityException) {
                _incoming.tryEmit("ERR:NOPERM_HOST")
                _connected.value = false
            } catch (_: Throwable) {
                _connected.value = false
            }
        }
    }

    /** Join connects to a specific bonded device address. Safe against missing permission. */
    @SuppressLint("MissingPermission") // guarded and try/catch SecurityException
    fun join(ctx: Context, address: String) {
        val ad = BluetoothP2P.adapter(ctx) ?: return
        if (!BluetoothP2P.hasConnectPermission(ctx)) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val device: BluetoothDevice = ad.getRemoteDevice(address)
                val s = device.createRfcommSocketToServiceRecord(BluetoothP2P.APP_UUID)
                ad.cancelDiscovery()
                s.connect()
                onSocketReady(s)
            } catch (se: SecurityException) {
                _incoming.tryEmit("ERR:NOPERM_JOIN")
                _connected.value = false
            } catch (_: Throwable) {
                _connected.value = false
            }
        }
    }

    private fun onSocketReady(s: BluetoothSocket) {
        socket = s
        writer = PrintWriter(s.outputStream, true)
        // reader loop
        CoroutineScope(Dispatchers.IO).launch {
            val rd = BufferedReader(InputStreamReader(s.inputStream))
            while (true) {
                val line = rd.readLine() ?: break
                _incoming.tryEmit(line)
            }
            _connected.value = false
        }
        _connected.value = true
    }

    fun send(line: String) { writer?.println(line) }

    @Suppress("unused")
    fun close() {
        try { writer?.flush() } catch (_: Throwable) {}
        try { socket?.close() } catch (_: Throwable) {}
        writer = null
        socket = null
        _connected.value = false
    }
}
