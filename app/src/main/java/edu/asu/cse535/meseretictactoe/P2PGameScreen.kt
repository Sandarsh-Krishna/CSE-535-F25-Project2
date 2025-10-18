package edu.asu.cse535.meseretictactoe

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Suppress("UNUSED_PARAMETER")  // ✅ silence “vm is never used”
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PGameScreen(nav: NavHostController, vm: P2PGameViewModel) {
    val ctx = LocalContext.current

    val permissionList = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    var showPermDialog by remember { mutableStateOf(true) }
    var granted by remember { mutableStateOf(BluetoothP2P.hasAllPermissions(ctx, permissionList)) }
    var status by remember { mutableStateOf("Not connected") }
    var showJoinPicker by remember { mutableStateOf(false) }
    var bonded by remember { mutableStateOf(BluetoothP2P.bondedDevices(ctx)) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result.values.all { it }
        showPermDialog = !granted
        if (granted) bonded = BluetoothP2P.bondedDevices(ctx)
    }

    LaunchedEffect(Unit) {
        P2PSession.connected.collect { ok ->
            if (ok) {
                status = "Connected"
                nav.navigate(AppRoute.MAIN.name)
            }
        }
    }

    // Same gradient as GameScreen
    val bg = Brush.linearGradient(
        0f to Color(0xFFFEF3C7),
        0.5f to Color(0xFFE9D5FF),
        1f to Color(0xFFBAE6FD)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { TextButton(onClick = { nav.goBack() }) { Text("Back") } }
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(bg)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Bluetooth Match", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(status)

                Spacer(Modifier.height(24.dp))

                if (!granted) {
                    Button(onClick = { showPermDialog = true }, shape = RoundedCornerShape(16.dp)) { Text("Enable Bluetooth Permissions") }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = {
                            status = "Hosting…"
                            P2PSession.host(ctx)
                        }, shape = RoundedCornerShape(16.dp)) { Text("Host") }

                        Button(onClick = {
                            bonded = BluetoothP2P.bondedDevices(ctx)
                            showJoinPicker = true
                        }, shape = RoundedCornerShape(16.dp)) { Text("Join") }
                    }
                }
            }
        }
    }

    if (showPermDialog && !granted) {
        AlertDialog(
            onDismissRequest = { showPermDialog = false },
            title = { Text("Bluetooth permission required") },
            text = { Text("Allow Mesere tic tac toe to connect to nearby bluetooth devices") },
            confirmButton = {
                TextButton(onClick = {
                    showPermDialog = false
                    launcher.launch(permissionList)
                }) { Text("Allow") }
            },
            dismissButton = { TextButton(onClick = { showPermDialog = false }) { Text("Cancel") } }
        )
    }

    if (showJoinPicker) {
        AlertDialog(
            onDismissRequest = { showJoinPicker = false },
            title = { Text("Select paired device") },
            text = {
                if (bonded.isEmpty()) {
                    Text("No paired devices found. Pair in system Bluetooth settings first.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        items(bonded) { dev ->
                            ListItem(
                                headlineContent = { Text(dev.second) },
                                supportingContent = { Text(dev.first) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showJoinPicker = false
                                        status = "Connecting to ${dev.second}…"
                                        P2PSession.join(ctx, dev.first)
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showJoinPicker = false }) { Text("Close") } }
        )
    }
}
