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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

enum class ConnectMode { BLUETOOTH, LAN }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PGameScreen(
    nav: NavHostController,
    gameVm: GameViewModel,
    p2pVm: P2PGameViewModel
) {
    val ctx = LocalContext.current

    val permissionList = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    var showPermDialog by remember { mutableStateOf(false) }
    var granted by remember {
        mutableStateOf(
            BluetoothP2P.hasAllPermissions(ctx, permissionList)
        )
    }
    var status by remember { mutableStateOf("Not connected") }

    var showJoinPicker by remember { mutableStateOf(false) }
    var bonded by remember { mutableStateOf(BluetoothP2P.bondedDevices(ctx)) }

    var mode by remember {
        mutableStateOf(
            if (BluetoothP2P.adapter(ctx) == null)
                ConnectMode.LAN
            else
                ConnectMode.BLUETOOTH
        )
    }

    var joinIp by remember { mutableStateOf("") }
    val myIp = remember { NetP2P.localIp(ctx) }
    var isJoining by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var choiceLocked by remember { mutableStateOf(false) }
    var choiceMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        P2PSession.connected.collect { ok ->
            if (ok) {
                isJoining = false
                status = "Connected"
            }
        }
    }

    LaunchedEffect(Unit) {
        P2PSession.incoming.collect { msg ->
            when {
                msg == "LOCK:YOU_FIRST" -> {
                    choiceLocked = true
                    choiceMessage = "You chose to go first as X."
                }
                msg == "LOCK:YOU_SECOND" -> {
                    choiceLocked = true
                    choiceMessage = "You chose to go second as O."
                }
                msg == "LOCK:REMOTE_FIRST" -> {
                    choiceLocked = true
                    choiceMessage = "Your opponent chose to go first as X."
                }
                msg == "LOCK:REMOTE_SECOND" -> {
                    choiceLocked = true
                    choiceMessage = "Your opponent chose to go second as O."
                }
                msg == "READY" -> {
                    val starter = P2PSession.chosenStarter()
                    val sideMe = P2PSession.chosenLocalSide()
                    val newSettings = GameSettings(
                        opponent = Opponent.HUMAN_BT,
                        difficulty = Difficulty.EASY,
                        starter = starter,
                        localSide = sideMe
                    )
                    gameVm.applySettings(newSettings)
                    nav.navigate(AppRoute.MAIN.name)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        P2PSession.errors.collect { msg ->
            isJoining = false
            errorText = msg
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result.values.all { it }
        showPermDialog = false
        if (granted) {
            bonded = BluetoothP2P.bondedDevices(ctx)
        }
    }

    val bg = Brush.linearGradient(
        0f to Color(0xFFFEF3C7),
        0.5f to Color(0xFFE9D5FF),
        1f to Color(0xFFBAE6FD)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = { nav.goBack() }) { Text("Back") }
                }
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Two Players",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == ConnectMode.BLUETOOTH,
                        onClick = { mode = ConnectMode.BLUETOOTH },
                        label = { Text("Bluetooth") }
                    )
                    FilterChip(
                        selected = mode == ConnectMode.LAN,
                        onClick = { mode = ConnectMode.LAN },
                        label = { Text("Local Network (Emulator)") }
                    )
                }

                Spacer(Modifier.height(18.dp))
                Text(status)
                Spacer(Modifier.height(24.dp))

                Text("Who goes first?", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!choiceLocked) {
                                P2PSession.claimMeFirst()
                                choiceLocked = true
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isJoining && !choiceLocked
                    ) {
                        Text("Me")
                    }
                    Button(
                        onClick = {
                            if (!choiceLocked) {
                                P2PSession.claimOpponentFirst()
                                choiceLocked = true
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isJoining && !choiceLocked
                    ) {
                        Text("Opponent")
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (choiceMessage.isNotEmpty()) {
                    Text(
                        text = choiceMessage,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(24.dp))

                if (mode == ConnectMode.BLUETOOTH) {
                    if (!granted) {
                        Button(
                            onClick = { showPermDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Enable Bluetooth Permissions") }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(
                                onClick = {
                                    status = "Hosting…"
                                    P2PSession.host(ctx)
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Host") }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    bonded = BluetoothP2P.bondedDevices(ctx)
                                    showJoinPicker = true
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Join") }
                        }
                    }
                } else {
                    Text("Your IP (host shares this): $myIp")
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            status = "Hosting on $myIp…"
                            P2PSession.hostLan()
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isJoining
                    ) { Text("Host") }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = joinIp,
                        onValueChange = { joinIp = it },
                        label = { Text("Host IP") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isJoining
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val ip = joinIp.trim()
                            val ipv4 = Regex("""\b\d{1,3}(\.\d{1,3}){3}\b""")
                            if (
                                ip.isEmpty() ||
                                !ipv4.matches(ip) ||
                                ip == "127.0.0.1" ||
                                ip == "0.0.0.0"
                            ) {
                                errorText =
                                    "Enter a valid IPv4 address on your Wi-Fi (e.g. 192.168.x.y)."
                            } else {
                                isJoining = true
                                status = "Joining $ip…"
                                P2PSession.joinLan(ip)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isJoining
                    ) {
                        Text(if (isJoining) "Joining…" else "Join")
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (choiceLocked) {
                            P2PSession.finalizeAndSync()
                        } else {
                            errorText = "Choose who goes first before continuing."
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue to Game")
                }
            }
        }
    }

    if (showPermDialog && !granted && mode == ConnectMode.BLUETOOTH) {
        AlertDialog(
            onDismissRequest = { showPermDialog = false },
            title = { Text("Bluetooth permission required") },
            text = { Text("Allow Mesere tic tac toe to connect to nearby bluetooth devices") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermDialog = false
                        launcher.launch(permissionList)
                    }
                ) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = { showPermDialog = false }) {
                    Text("Cancel")
                }
            }
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
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
            confirmButton = {
                TextButton(onClick = { showJoinPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (errorText != null) {
        AlertDialog(
            onDismissRequest = { errorText = null },
            title = { Text("Connection problem") },
            text = { Text(errorText!!) },
            confirmButton = {
                TextButton(onClick = { errorText = null }) { Text("OK") }
            }
        )
    }
}
