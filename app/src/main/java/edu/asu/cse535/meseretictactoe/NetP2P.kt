package edu.asu.cse535.meseretictactoe

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket

object NetP2P {
    const val PORT = 53535
    private const val CONNECT_TIMEOUT_MS = 3000


    fun host(onSocket: (Socket) -> Unit, onError: (Throwable) -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ServerSocket(PORT).use { server ->
                    val s = server.accept()
                    onSocket(s)
                }
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }


    fun join(ip: String, onSocket: (Socket) -> Unit, onError: (Throwable) -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addr = InetSocketAddress(InetAddress.getByName(ip), PORT)
                val s = Socket()
                s.connect(addr, CONNECT_TIMEOUT_MS)
                onSocket(s)
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }


    fun ioLoop(socket: Socket, onLine: (String) -> Unit): PrintWriter {
        val out = PrintWriter(socket.getOutputStream(), true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (true) {
                    val line = reader.readLine() ?: break
                    onLine(line)
                }
            } catch (_: Throwable) {
                // done
            } finally {
                try { socket.close() } catch (_: Throwable) {}
            }
        }
        return out
    }


    @SuppressLint("MissingPermission")
    fun localIp(context: Context): String {

        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val lp = cm.getLinkProperties(cm.activeNetwork)
            val ip = lp?.linkAddresses
                ?.firstOrNull { it.address is Inet4Address && !it.address.isLoopbackAddress }
                ?.address?.hostAddress
            if (!ip.isNullOrBlank()) return ip
        } catch (_: Throwable) {

        }

        
        return try {
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
                ?.hostAddress ?: "0.0.0.0"
        } catch (_: Throwable) {
            "0.0.0.0"
        }
    }
}
