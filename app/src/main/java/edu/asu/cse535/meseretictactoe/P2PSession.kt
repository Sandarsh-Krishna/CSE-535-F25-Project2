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

    private val _incoming = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incoming = _incoming.asSharedFlow()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val errors = _errors.asSharedFlow()

    private var starterSide: Player = Player.X
    private var hostSide: Player = Player.X
    private var joinerSide: Player = Player.O
    private var mySide: Player = Player.X

    @SuppressLint("MissingPermission")
    fun host(ctx: Context) {
        amHost = true
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
        amHost = false
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
        writer = NetP2P.ioLoop(s) { line ->
            _incoming.tryEmit(line)
        }
        _connected.value = true
    }

    fun claimLocalFirst() {
        if (amHost == true) {
            starterSide = Player.X
            hostSide = Player.X
            joinerSide = Player.O
            mySide = Player.X
        } else {
            starterSide = Player.X
            hostSide = Player.X
            joinerSide = Player.O
            mySide = Player.O
        }
        val msg = "LOCKSET:${starterSide.name}:${hostSide.name}:${joinerSide.name}"
        send(msg)
    }

    fun claimRemoteFirst() {
        if (amHost == true) {
            starterSide = Player.O
            hostSide = Player.O
            joinerSide = Player.X
            mySide = Player.O
        } else {
            starterSide = Player.O
            hostSide = Player.O
            joinerSide = Player.X
            mySide = Player.X
        }
        val msg = "LOCKSET:${starterSide.name}:${hostSide.name}:${joinerSide.name}"
        send(msg)
    }

    fun applyLocksetFromRemote(starterStr: String, hostStr: String, joinerStr: String) {
        starterSide = if (starterStr == "X") Player.X else Player.O
        hostSide = if (hostStr == "X") Player.X else Player.O
        joinerSide = if (joinerStr == "X") Player.X else Player.O
        mySide = if (amHost == true) hostSide else joinerSide
    }

    fun finalizeAndSync() {
        mySide = if (amHost == true) hostSide else joinerSide
        val starterChar = starterSide.name
        val hostChar = hostSide.name
        val joinerChar = joinerSide.name
        val syncLine = "SYNC:$starterChar:$hostChar:$joinerChar"
        send(syncLine)
        send("READY")
        _incoming.tryEmit(syncLine)
        _incoming.tryEmit("READY")
    }

    fun chosenStarter(): Player = starterSide
    fun chosenLocalSide(): Player = mySide

    fun send(line: String) {
        writer?.println(line)
    }

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
