package edu.asu.cse535.mesereticttactoe.bluetooth

import android.util.Log
import com.google.gson.Gson
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors

class BluetoothConnection(
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
    private val onMessageReceived: (String) -> Unit
) {
    private val gson = Gson()
    private val executor = Executors.newSingleThreadExecutor()
    private val TAG = "BluetoothConnection"

    init { listenForIncomingData() }

    private fun listenForIncomingData() {
        executor.execute {
            val buffer = ByteArray(1024)
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        Log.d(TAG, "Received: $message")
                        onMessageReceived(message)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Connection closed or failed: ${e.message}")
                    break
                }
            }
        }
    }

    fun sendRaw(data: ByteArray) {
        try {
            outputStream.write(data)
            outputStream.flush()
            Log.d(TAG, "Sent raw bytes: ${String(data)}")
        } catch (e: Exception) {
            Log.e(TAG, "Send failed: ${e.message}")
        }
    }

    fun sendJson(jsonString: String) = sendRaw(jsonString.toByteArray())

    fun sendObject(obj: Any) = sendRaw(gson.toJson(obj).toByteArray())

    fun close() {
        try {
            inputStream.close()
            outputStream.close()
            executor.shutdownNow()
            Log.d(TAG, "BluetoothConnection closed.")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing connection: ${e.message}")
        }
    }
}
