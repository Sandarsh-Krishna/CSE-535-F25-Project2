package com.example.bluetoothjsontester

import android.util.Log
import com.google.gson.Gson
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Executors

/**
 * Handles two-way Bluetooth communication once connected.
 */
class BluetoothConnection(
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
    private val onMessageReceived: (String) -> Unit // Callback for UI updates
) {

    private val gson = Gson()
    private val executor = Executors.newSingleThreadExecutor()
    private val TAG = "BluetoothConnection"

    init {
        listenForIncomingData()
    }

    /**
     * Listens continuously for incoming messages from the InputStream.
     * Runs on a background thread.
     */
    private fun listenForIncomingData() {
        executor.execute {
            val buffer = ByteArray(1024)
            while (true) {
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

    /**
     * Sends a byte array directly over the output stream.
     * Example: sendRaw("{\"key\":\"value\"}".toByteArray())
     */
    fun sendRaw(data: ByteArray) {
        try {
            outputStream.write(data)
            outputStream.flush()
            Log.d(TAG, "Sent raw bytes: ${String(data)}")
        } catch (e: Exception) {
            Log.e(TAG, "Send failed: ${e.message}")
        }
    }

    /**
     * Optional convenience method for sending JSON strings.
     */
    fun sendJson(jsonString: String) {
        sendRaw(jsonString.toByteArray())
    }

    /**
     * Example convenience method to send a structured JSON object.
     * (You can customize this to your game/test data model)
     */
    fun sendObject(obj: Any) {
        val json = gson.toJson(obj)
        sendRaw(json.toByteArray())
    }

    /**
     * Cleanly closes the connection.
     */
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
