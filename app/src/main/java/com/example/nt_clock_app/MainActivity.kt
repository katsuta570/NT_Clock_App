package com.example.nt_clock_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nt_clock_app.ui.theme.NT_Clock_AppTheme
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Components1()
        }
    }
}

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    Alarm(route = "Alarm", label = "Alarm", Icons.Rounded.Alarm, contentDescription = "Alarm"),
    Clock(route = "Clock", label = "Clock", Icons.Rounded.Public, contentDescription = "Clock"),
    Stopwatch(route = "Stopwatch", label = "Stopwatch", Icons.Rounded.Timer, contentDescription = "Stopwatch"),
    Timer(route = "Timer", label = "Timer", Icons.Rounded.Timer, contentDescription = "Timer")
}

val NimbusSans1 = FontFamily(
    Font(resId = R.font.nimbussanl_reg, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(resId = R.font.nimbussanl_regita, weight = FontWeight.Normal, style = FontStyle.Italic),
    Font(resId = R.font.nimbussanl_bol, weight = FontWeight.Bold, style = FontStyle.Normal),
    Font(resId = R.font.nimbussanl_bolita, weight = FontWeight.Bold, style = FontStyle.Italic)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Components1() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = rememberTopAppBarState())
    val navController = rememberNavController()
    val startDestination = Destination.Clock
    var selectedDestination by rememberSaveable { mutableIntStateOf(value = startDestination.ordinal) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTitle = Destination.entries.find { it.route == currentRoute }?.label ?: "No Title"

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black
                ),
                title = {
                    Text(
                        text = currentTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                Destination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            navController.navigate(route = destination.route)
                            selectedDestination = index
                        },
                        label = { Text(destination.label) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Clock.route
        ) {
            composable(route = Destination.Clock.route) {
                ClockMenu1(innerPadding)
            }

            composable(route = Destination.Alarm.route) {
                CardAM(innerPadding)
            }

            composable(route = Destination.Timer.route) {
                TimerDisplayOnlyScreen()
            }

            composable(route = Destination.Stopwatch.route) {
                StopwatchMainScreen1(innerPadding)
            }
        }
    }
}

@Composable
fun ClockMenu1(innerPadding: PaddingValues) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(value = getCurrentTime()) }
    var currentDate by remember { mutableStateOf(value = getCurrentDate()) }
    var currentInstant by remember { mutableStateOf(value = Instant.now()) }
    var worldClocks by remember { mutableStateOf<List<WorldClockItem>>(emptyList()) }
    val showAddDialog = rememberSaveable { mutableStateOf(value = false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = Unit) {
        worldClocks = loadWorldClocks1(context)
    }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            val now = Instant.now()
            currentInstant = now
            currentTime = getCurrentTime()
            currentDate = getCurrentDate()

            val delayMillis = 1000 - (System.currentTimeMillis() % 1000)
            delay(timeMillis = delayMillis)
        }
    }

    fun updateWorldClocks(updatedWorldClocks: List<WorldClockItem>) {
        worldClocks = updatedWorldClocks
        saveWorldClocks1(context, updatedWorldClocks)
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues = innerPadding),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog.value = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add world clock"
                )
            }
        }
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = contentPadding)
                    .padding(top = 60.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = currentTime,
                    fontSize = 70.sp,
                    fontFamily = NimbusSans1,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = currentDate,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(40.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    if (worldClocks.isEmpty()) {
                        Text(text = "No world clocks")
                    } else {
                        worldClocks.forEach { worldClock ->
                            CardSample1(
                                clockLocation1 = worldClock.label,
                                clockTime1 = getCurrentTime(zoneId = worldClock.zoneId, instant = currentInstant),
                                onDelete = {
                                    updateWorldClocks(
                                        updatedWorldClocks = worldClocks.filterNot { it.id == worldClock.id }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog.value) {
            AddWorldClockDialog(
                onDismiss = { showAddDialog.value = false },
                onAddClock = { label, zoneId ->
                    val nextId = (worldClocks.maxOfOrNull { it.id } ?: 0) + 1
                    updateWorldClocks(
                        updatedWorldClocks = worldClocks + WorldClockItem(
                            id = nextId,
                            label = label,
                            zoneId = zoneId
                        )
                    )
                    showAddDialog.value = false
                }
            )
        }
    }
}

@Composable
private fun AddWorldClockDialog(
    onDismiss: () -> Unit,
    onAddClock: (String, String) -> Unit
) {
    val availableZoneIds = remember { ZoneId.getAvailableZoneIds().sorted() }
    var locationName by remember { mutableStateOf(value = "") }
    val zoneQuery = remember { mutableStateOf(value = "") }
    val selectedZoneId = remember { mutableStateOf(value = "") }
    val filteredZoneIds = remember(key1 = zoneQuery) {
        val query = zoneQuery.value.trim()
        if (query.isBlank()) {
            availableZoneIds.take(n = 8)
        } else {
            availableZoneIds
                .filter { zoneId -> zoneId.contains(other = query, ignoreCase = true) }
                .take(n = 8)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "世界時計を追加")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text(text = "表示名（任意）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = zoneQuery.value,
                    onValueChange = { query ->
                        zoneQuery.value = query
                        selectedZoneId.value = availableZoneIds.firstOrNull { zoneId ->
                            zoneId.equals(other = query.trim(), ignoreCase = true)
                        } ?: ""
                    },
                    label = { Text(text = "地域 / タイムゾーン") },
                    placeholder = { Text(text = "例: Asia/Tokyo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = 220.dp)
                ) {
                    filteredZoneIds.forEach { zoneId ->
                        TextButton(
                            onClick = {
                                selectedZoneId.value = zoneId
                                zoneQuery.value = zoneId
                                if (locationName.isBlank()) {
                                    locationName = defaultClockLabel(zoneId)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = zoneId, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedZoneId.value.isNotBlank()) {
                        onAddClock(
                            locationName.ifBlank { defaultClockLabel(selectedZoneId.value) },
                            selectedZoneId.value
                        )
                    }
                },
                enabled = selectedZoneId.value.isNotBlank()
            ) {
                Text(text = "追加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "キャンセル")
            }
        }
    )
}

fun defaultClockLabel(zoneId: String): String {
    return zoneId.substringAfterLast(delimiter = "/").replace(oldChar = '_', newChar = ' ')
}

fun getCurrentTime(zoneId: String, instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    return formatter.format(instant.atZone(ZoneId.of(zoneId)))
}

@Composable
fun CardSample1(
    clockLocation1: String,
    clockTime1: String,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(all = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = clockTime1,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = clockLocation1,
                fontSize = 20.sp
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete world clock"
                )
            }
        }
    }
}

fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    return sdf.format(Date())
}

@Preview(showSystemUi = true)
@Composable
fun PreviewMainActivity1() {
    NT_Clock_AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Components1()
        }
    }
}
