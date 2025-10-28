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

    private var chosenLocked = false
    private var starterPlayer: Player = Player.X
    private var localPlayerSide: Player = Player.X

    @SuppressLint("MissingPermission")
    fun host(ctx: Context) {
        amHost = true
        chosenLocked = false
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
        chosenLocked = false
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
                    handleIncomingLine(line)
                }
            } catch (_: Throwable) { }
            _connected.value = false
        }
        _connected.value = true
    }

    fun hostLan() {
        amHost = true
        chosenLocked = false
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
        chosenLocked = false
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
        writer = NetP2P.ioLoop(s) { handleIncomingLine(it) }
        _connected.value = true
    }

    private fun handleIncomingLine(line: String) {
        if (line.startsWith("CLAIM:")) {
            if (!chosenLocked) {
                when (line.removePrefix("CLAIM:")) {
                    "ME_FIRST" -> {
                        starterPlayer = Player.X
                        localPlayerSide = if (amHost == true) Player.O else Player.O
                        chosenLocked = true
                        _incoming.tryEmit("LOCK:REMOTE_FIRST")
                    }
                    "OPP_FIRST" -> {
                        starterPlayer = Player.X
                        localPlayerSide = if (amHost == true) Player.X else Player.X
                        chosenLocked = true
                        _incoming.tryEmit("LOCK:REMOTE_SECOND")
                    }
                }
            }
            return
        }
        if (line.startsWith("SYNC:")) {
            val parts = line.split(":")
            val starterStr = parts.getOrNull(1)
            val localSideStr = parts.getOrNull(2)
            val st = if (starterStr == "X") Player.X else Player.O
            val ls = if (localSideStr == "X") Player.X else Player.O
            starterPlayer = st
            localPlayerSide = ls
            chosenLocked = true
            _incoming.tryEmit("READY")
            return
        }
        _incoming.tryEmit(line)
    }

    fun claimMeFirst() {
        if (chosenLocked) return
        chosenLocked = true
        starterPlayer = Player.X
        localPlayerSide = Player.X
        send("CLAIM:OPP_FIRST")
        _incoming.tryEmit("LOCK:YOU_FIRST")
    }

    fun claimOpponentFirst() {
        if (chosenLocked) return
        chosenLocked = true
        starterPlayer = Player.X
        localPlayerSide = Player.O
        send("CLAIM:ME_FIRST")
        _incoming.tryEmit("LOCK:YOU_SECOND")
    }

    fun finalizeAndSync() {
        val starterStr = if (starterPlayer == Player.X) "X" else "O"
        val localStr = if (localPlayerSide == Player.X) "X" else "O"
        val syncMsg = "SYNC:$starterStr:$localStr"
        send(syncMsg)
        _incoming.tryEmit("READY")
    }

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
        chosenLocked = false
    }

    fun chosenStarter(): Player = starterPlayer
    fun chosenLocalSide(): Player = localPlayerSide
}
