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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.geometry.Offset
enum class ConnectMode { BLUETOOTH, LAN }

@Composable
private fun PurplePrimaryButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .clickable(enabled) {
                if (enabled) onClick()
            },
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334C3A8C)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(listOf(Luxe.accent1, Luxe.accent2)),
                    shape

                )
                .padding(horizontal = 22.dp, vertical = 12.dp),
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
private fun SelectChipLikeFirstScreen(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val bgBrush =
        if (selected) Brush.horizontalGradient(listOf(Luxe.accent1.copy(alpha = 0.92f), Luxe.accent2.copy(alpha = 0.92f)))
        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))

    val borderColor = if (selected) Luxe.accent2 else Luxe.chipOutline
    val textColor = if (selected) Color.White else Luxe.textPrimary

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
                    .size(12.dp)
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        RoundedCornerShape(999.dp)
                    )
                    .background(if (selected) Color.White.copy(alpha = 0.35f) else Color.Transparent, RoundedCornerShape(999.dp))
            )

            Text(text, color = textColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SmallChoiceChipMeOpponent(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val bgBrush =
        if (selected) Brush.horizontalGradient(listOf(Luxe.accent1.copy(alpha = 0.92f), Luxe.accent2.copy(alpha = 0.92f)))
        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))

    val borderColor = if (selected) Luxe.accent2 else Luxe.chipOutline
    val textColor = if (selected) Color.White else Luxe.textPrimary

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

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        RoundedCornerShape(999.dp)
                    )
                    .background(if (selected) Color.White.copy(alpha = 0.35f) else Color.Transparent, RoundedCornerShape(999.dp))
            )

            Text(text, color = textColor, fontWeight = FontWeight.Medium)

        }
    }
}

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

    var choiceLocked by remember { mutableStateOf(false) }
    var choiceMessage by remember { mutableStateOf("") }

    var isConnected by remember { mutableStateOf(false) }

    var showNotConnectedDialog by remember { mutableStateOf(false) }

    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        P2PSession.connected.collect { ok ->
            isJoining = false
            isConnected = ok
            status = if (ok) "Connected" else "Not connected"
        }
    }

    LaunchedEffect(Unit) {
        P2PSession.incoming.collect { msg ->
            when (msg) {
                "LOCK:YOU_FIRST" -> {
                    choiceLocked = true
                    choiceMessage = "You chose to go first as X."
                }
                "LOCK:YOU_SECOND" -> {
                    choiceLocked = true
                    choiceMessage = "You chose to go second as O."
                }
                "LOCK:REMOTE_FIRST" -> {
                    choiceLocked = true
                    choiceMessage = "Your opponent chose to go first as X."
                }
                "LOCK:REMOTE_SECOND" -> {
                    choiceLocked = true
                    choiceMessage = "Your opponent chose to go second as O."
                }
                "READY" -> {
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
        colors = listOf(Luxe.bgStart, Luxe.bgEnd),
        start = Offset(0f, 0f),
        end = Offset(1200f, 2200f)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Scaffold(
            topBar = {

                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "P2P Match",
                            color = Luxe.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = { nav.goBack() }) {
                            Text(
                                "Back",
                                color = Luxe.textPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { pad ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)

            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Luxe.glass,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Luxe.glassBorder),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .fillMaxWidth()
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
                            SelectChipLikeFirstScreen(
                                text = "Bluetooth",
                                selected = (mode == ConnectMode.BLUETOOTH)
                            ) {
                                mode = ConnectMode.BLUETOOTH
                            }

                            SelectChipLikeFirstScreen(
                                text = "Local Network",
                                selected = (mode == ConnectMode.LAN)
                            ) {
                                mode = ConnectMode.LAN
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(status, color = Luxe.textMuted)

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
                            SmallChoiceChipMeOpponent(
                                text = "Me",
                                selected = choiceLocked && choiceMessage.startsWith("You chose to go first"),
                                enabled = !choiceLocked
                            ) {
                                if (!choiceLocked) {
                                    P2PSession.claimMeFirst()
                                    choiceLocked = true
                                }
                            }

                            SmallChoiceChipMeOpponent(
                                text = "Opponent",
                                selected = choiceLocked && choiceMessage.startsWith("You chose to go second"),
                                enabled = !choiceLocked
                            ) {
                                if (!choiceLocked) {
                                    P2PSession.claimOpponentFirst()
                                    choiceLocked = true
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        if (choiceMessage.isNotEmpty()) {
                            Text(
                                text = choiceMessage,
                                color = Luxe.textMuted,
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
                            Text("Your IP: $myIp", color = Luxe.textMuted)
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
                                label = { Text("Host IP", color = Luxe.textMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isJoining,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Luxe.accent2,
                                    unfocusedBorderColor = Luxe.tileBorder,
                                    focusedTextColor = Luxe.textPrimary,
                                    unfocusedTextColor = Luxe.textPrimary,
                                    cursorColor = Luxe.accent2
                                )
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
        }
    }

    if (showPermDialog && !granted && mode == ConnectMode.BLUETOOTH) {
        AlertDialog(
            onDismissRequest = { showPermDialog = false },
            title = { Text("Bluetooth permission required", color = Luxe.textPrimary) },
            text = { Text("Allow Mesere tic tac toe to connect to nearby bluetooth devices", color = Luxe.textMuted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermDialog = false
                        launcher.launch(permissionList)
                    }
                ) { Text("Allow", color = Luxe.accent2) }
            },
            dismissButton = {
                TextButton(onClick = { showPermDialog = false }) { Text("Cancel", color = Luxe.textMuted) }
            }
        )
    }

    if (showJoinPicker) {
        AlertDialog(
            onDismissRequest = { showJoinPicker = false },
            title = { Text("Select paired device", color = Luxe.textPrimary) },
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
                                headlineContent = { Text(dev.second, color = Luxe.textPrimary) },
                                supportingContent = { Text(dev.first, color = Luxe.textMuted) },
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
                TextButton(onClick = { showJoinPicker = false }) { Text("Close", color = Luxe.accent2) }
            }
        )
    }

    if (showNotConnectedDialog) {
        AlertDialog(
            onDismissRequest = { showNotConnectedDialog = false },
            title = { Text("Not Yet Connected", color = Luxe.textPrimary) },
            text = { Text("Make sure both players are connected and who goes first is selected.", color = Luxe.textMuted) },
            confirmButton = {
                TextButton(onClick = { showNotConnectedDialog = false }) { Text("OK", color = Luxe.accent2) }
            }
        )
    }

    if (errorText != null) {
        AlertDialog(
            onDismissRequest = { errorText = null },
            title = { Text("Connection problem", color = Luxe.textPrimary) },
            text = { Text(errorText!!, color = Luxe.textMuted) },
            confirmButton = {
                TextButton(onClick = { errorText = null }) { Text("OK", color = Luxe.accent2) }
            }
        )
    }
}
