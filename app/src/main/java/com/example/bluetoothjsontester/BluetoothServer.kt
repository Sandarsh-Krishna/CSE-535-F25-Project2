package com.example.bluetoothjsontester

import android.R
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.util.UUID

/**
 * Waits for incoming Bluetooth connection requests (Host/Server role).
 */
@SuppressLint("MissingPermission")
class BluetoothServer(
    private val onConnected: (BluetoothConnection) -> Unit,
    private val onMessageReceived: (String) -> Unit
) {

    private val TAG = "BluetoothServer"
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID
    private var serverSocket: BluetoothServerSocket? = null
    @Volatile private var isRunning = false

    fun start() {
        Thread {
            try {
                if (adapter == null) {
                    Log.e(TAG, "Bluetooth is not supported on this device")
                    return@Thread
                }
                isRunning = true
                serverSocket = adapter.listenUsingRfcommWithServiceRecord("BTServer", uuid)
                Log.d(TAG, "Bluetooth server started, waiting for client...")

                val socket: BluetoothSocket? = serverSocket?.accept()

                socket?.let {
                    Log.d(TAG, "Client connected: ${it.remoteDevice.name}")

                    val connection = BluetoothConnection(
                        it.inputStream,
                        it.outputStream,
                        onMessageReceived
                    )

                    onConnected(connection)
                }
            } catch (e: Exception) {
                if(isRunning) {
                    Log.e(TAG, "Server error: ${e.message}")
                }
            } finally {
                stop()
            }
        }.start()
    }

    fun stop() {
        try {
            isRunning = false
            serverSocket?.close()
            Log.d(TAG, "Bluetooth server stopped.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server: ${e.message}")
        }
    }
}
