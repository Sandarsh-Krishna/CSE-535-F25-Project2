package edu.asu.cse535.meseretictactoe

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.UUID

object BluetoothP2P {
    // Shared UUID for classic RFCOMM SPP
    val APP_UUID: UUID = UUID.fromString("c4b2b2d4-7c09-4f9f-9c1c-0f3c2a3f43c9")
    const val SERVICE_NAME = "MesereTTT"

    fun adapter(ctx: Context): BluetoothAdapter? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        } else {
            @Suppress("DEPRECATION") BluetoothAdapter.getDefaultAdapter()
        }

    fun hasAllPermissions(ctx: Context, permissions: Array<String>): Boolean =
        permissions.all { p ->
            ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED
        }

    fun hasConnectPermission(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        else true

    fun hasScanPermission(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        else true


    @SuppressLint("MissingPermission") // guarded by hasConnectPermission
    fun bondedDevices(ctx: Context): List<Pair<String, String>> {
        if (!hasConnectPermission(ctx)) return emptyList()
        val ad = adapter(ctx) ?: return emptyList()
        return ad.bondedDevices.map { it.address to (it.name ?: it.address) }.sortedBy { it.second }
    }
}
