package edu.asu.cse535.meseretictactoe

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collectLatest

enum class ConnectMode { BLUETOOTH, LAN }

@Composable
private fun PurplePrimaryButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .clickable(enabled) {
                if (enabled) onClick()
            },
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4C3A8C)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF5B4BB8),
                            Color(0xFF4A3797)
                        )
                    ),
                    shape
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant
    val bgBrush =
        if (selected) {
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surface
                )
            )
        }

    Surface(
        modifier = Modifier
            .clickable { onClick() },
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .background(bgBrush, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val indicatorColor =
                if (selected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            val indicatorBorderColor =
                if (selected) MaterialTheme.colorScheme.primary
                else borderColor

            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        indicatorColor,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, indicatorBorderColor),
                        RoundedCornerShape(4.dp)
                    )
            )

            Text(
                text,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FirstTurnChip(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant
    val bgBrush =
        if (selected) {
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surface
                )
            )
        }

    Surface(
        modifier = Modifier
            .clickable(enabled = enabled) {
                if (enabled) onClick()
            },
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .background(bgBrush, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val indicatorColor =
                if (selected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            val indicatorBorderColor =
                if (selected) MaterialTheme.colorScheme.primary
                else borderColor

            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        indicatorColor,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, indicatorBorderColor),
                        RoundedCornerShape(4.dp)
                    )
            )

            Text(
                text,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PGameScreen(
    nav: NavHostController,
    gameVm: GameViewModel
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
    var bonded by remember { mutableStateOf(listOf<Pair<String, String>>()) }

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

    var choiceLocked by remember { mutableStateOf(false) }
    var choiceMessage by remember { mutableStateOf("") }

    var isConnected by remember { mutableStateOf(false) }
    var showNotConnectedDialog by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(granted) {
        if (granted) {
            bonded = BluetoothP2P.bondedDevices(ctx)
        }
    }

    LaunchedEffect(Unit) {
        P2PSession.connected.collectLatest { ok ->
            isJoining = false
            isConnected = ok
            status = if (ok) "Connected" else "Not connected"
        }
    }

    LaunchedEffect(Unit) {
        P2PSession.incoming.collectLatest { msg ->
            when {
                msg.startsWith("LOCKSET:") -> {
                    val parts = msg.split(":")
                    val starterStr = parts.getOrNull(1) ?: "X"
                    val hostStr = parts.getOrNull(2) ?: "X"
                    val joinerStr = parts.getOrNull(3) ?: "O"

                    P2PSession.applyLocksetFromRemote(
                        starterStr = starterStr,
                        hostStr = hostStr,
                        joinerStr = joinerStr
                    )

                    choiceLocked = true

                    val starter = P2PSession.chosenStarter()
                    val mine = P2PSession.chosenLocalSide()
                    val amStarting = (starter == mine)

                    choiceMessage =
                        if (amStarting) {
                            "You will go first as ${mine.name}."
                        } else {
                            "Your opponent will go first as ${starter.name}. You will go second as ${mine.name}."
                        }
                }

                msg.startsWith("SYNC:") -> {
                    val parts = msg.split(":")
                    val starterSide =
                        if (parts.getOrNull(1) == "X") Player.X else Player.O
                    val hostSideStr = parts.getOrNull(2) ?: "X"
                    val joinerSideStr = parts.getOrNull(3) ?: "O"

                    val localSide =
                        if (P2PSession.amHost == true) {
                            if (hostSideStr == "X") Player.X else Player.O
                        } else {
                            if (joinerSideStr == "X") Player.X else Player.O
                        }

                    val newSettings = GameSettings(
                        opponent = Opponent.HUMAN_BT,
                        difficulty = Difficulty.EASY,
                        starter = starterSide,
                        localSide = localSide
                    )
                    gameVm.applySettings(newSettings)
                    gameVm.reset()
                }

                msg == "READY" -> {
                    nav.navigate(AppRoute.MAIN.name)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        P2PSession.errors.collectLatest { msg ->
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
                    TextButton(onClick = { nav.popBackStack() }) { Text("Back") }
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
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Two Players",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    SelectChip(
                        text = "Bluetooth",
                        selected = (mode == ConnectMode.BLUETOOTH)
                    ) {
                        mode = ConnectMode.BLUETOOTH
                    }

                    SelectChip(
                        text = "Local Network (Emulator)",
                        selected = (mode == ConnectMode.LAN)
                    ) {
                        mode = ConnectMode.LAN
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(status)

                Spacer(Modifier.height(24.dp))

                Text("Who goes first?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    FirstTurnChip(
                        text = "Me",
                        selected = choiceLocked && P2PSession.chosenStarter() == P2PSession.chosenLocalSide(),
                        enabled = !choiceLocked
                    ) {
                        if (!choiceLocked) {
                            choiceLocked = true
                            P2PSession.claimLocalFirst()

                            val starter = P2PSession.chosenStarter()
                            val mine = P2PSession.chosenLocalSide()
                            val amStarting = (starter == mine)
                            choiceMessage =
                                if (amStarting) {
                                    "You will go first as ${mine.name}."
                                } else {
                                    "Your opponent will go first as ${starter.name}. You will go second as ${mine.name}."
                                }
                        }
                    }

                    FirstTurnChip(
                        text = "Opponent",
                        selected = choiceLocked && P2PSession.chosenStarter() != P2PSession.chosenLocalSide(),
                        enabled = !choiceLocked
                    ) {
                        if (!choiceLocked) {
                            choiceLocked = true
                            P2PSession.claimRemoteFirst()

                            val starter = P2PSession.chosenStarter()
                            val mine = P2PSession.chosenLocalSide()
                            val amStarting = (starter == mine)
                            choiceMessage =
                                if (amStarting) {
                                    "You will go first as ${mine.name}."
                                } else {
                                    "Your opponent will go first as ${starter.name}. You will go second as ${mine.name}."
                                }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (choiceMessage.isNotEmpty()) {
                    Text(
                        text = choiceMessage,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(28.dp))

                if (mode == ConnectMode.BLUETOOTH) {
                    if (!granted) {
                        PurplePrimaryButton(
                            label = "Enable Bluetooth Permissions",
                            enabled = true
                        ) {
                            showPermDialog = true
                        }
                    } else {
                        PurplePrimaryButton(
                            label = "Host",
                            enabled = !isJoining
                        ) {
                            status = "Hosting…"
                            P2PSession.host(ctx)
                        }

                        Spacer(Modifier.height(16.dp))

                        PurplePrimaryButton(
                            label = "Join",
                            enabled = !isJoining
                        ) {
                            bonded = BluetoothP2P.bondedDevices(ctx)
                            showJoinPicker = true
                        }
                    }
                } else {
                    Text("Your IP: $myIp")
                    Spacer(Modifier.height(16.dp))

                    PurplePrimaryButton(
                        label = "Host",
                        enabled = !isJoining
                    ) {
                        status = "Hosting on $myIp…"
                        P2PSession.hostLan()
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = joinIp,
                        onValueChange = { joinIp = it },
                        label = { Text("Host IP") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isJoining
                    )

                    Spacer(Modifier.height(16.dp))

                    PurplePrimaryButton(
                        label = if (isJoining) "Joining…" else "Join",
                        enabled = !isJoining
                    ) {
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
                    }
                }

                Spacer(Modifier.height(32.dp))

                PurplePrimaryButton(
                    label = "Start Game",
                    enabled = true
                ) {
                    if (choiceLocked && isConnected) {
                        P2PSession.finalizeAndSync()
                    } else {
                        showNotConnectedDialog = true
                    }
                }
            }
        }
    }

    if (showPermDialog && !granted && mode == ConnectMode.BLUETOOTH) {
        AlertDialog(
            onDismissRequest = { showPermDialog = false },
            title = { Text("Bluetooth permission required") },
            text = { Text("Allow Misere tic tac toe to connect to nearby bluetooth devices") },
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

    if (showNotConnectedDialog) {
        AlertDialog(
            onDismissRequest = { showNotConnectedDialog = false },
            title = { Text("Not Yet Connected") },
            text = { Text("Make sure both players are connected and who goes first is selected.") },
            confirmButton = {
                TextButton(
                    onClick = { showNotConnectedDialog = false }
                ) { Text("OK") }
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
