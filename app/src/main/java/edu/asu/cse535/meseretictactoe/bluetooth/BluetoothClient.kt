package edu.asu.cse535.mesereticttactoe.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothClient(
    private val macAddress: String,
    private val onConnected: (BluetoothConnection) -> Unit
) {
    private val TAG = "BluetoothClient"
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    fun connect() {
        Thread {
            try {
                val device: BluetoothDevice = adapter?.getRemoteDevice(macAddress)
                    ?: throw IllegalStateException("No Bluetooth adapter")
                val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

                adapter?.cancelDiscovery()
                Log.d(TAG, "Connecting to ${device.name} (${device.address})")
                socket.connect()
                Log.d(TAG, "Connected")

                val connection = BluetoothConnection(
                    socket.inputStream,
                    socket.outputStream
                ) { msg -> Log.d(TAG, "From server: $msg") }

                onConnected(connection)
            } catch (e: Exception) {
                Log.e(TAG, "Client connection failed: ${e.message}")
            }
        }.start()
    }
}
