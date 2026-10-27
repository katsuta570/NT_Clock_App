package com.example.nt_clock_app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.nt_clock_app.ui.theme.NT_Clock_AppTheme
import kotlinx.coroutines.delay

@Composable
fun TimerDisplayOnlyScreen(innerPadding: PaddingValues = PaddingValues()) {
    val context = LocalContext.current
    var timers by remember { mutableStateOf<List<TimerItem>>(emptyList()) }
    var showKeypad by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    fun updateTimers(updated: List<TimerItem>) { timers = updated; saveTimers(context, updated) }

    LaunchedEffect(Unit) {
        createTimerFinishedNotificationChannel(context)
        timers = loadTimers(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showKeypad || timers.isEmpty()) {
        TimerKeypad(
            Modifier.padding(innerPadding), (timers.maxOfOrNull { it.id } ?: 0) + 1,
            onCreate = { seconds, name ->
                val id = (timers.maxOfOrNull { it.id } ?: 0) + 1
                updateTimers(timers + TimerItem(id, name, seconds)); showKeypad = false
            },
            onCancel = if (timers.isNotEmpty()) ({ showKeypad = false }) else null
        )
        return
    }

    Scaffold(modifier = Modifier.padding(innerPadding), floatingActionButton = {
        FloatingActionButton(onClick = { showKeypad = true }) { Icon(Icons.Default.Add, "タイマーを追加") }
    }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(timers, key = { it.id }) { timer ->
                TimerCard(timer, onUpdate = { updated ->
                    updateTimers(timers.map { if (it.id == updated.id) updated else it })
                }, onFinished = { showTimerFinishedNotification(context, timer.name) })
            }
        }
    }
}

@Composable
private fun TimerCard(timer: TimerItem, onUpdate: (TimerItem) -> Unit, onFinished: () -> Unit) {
    var showNameEditor by remember { mutableStateOf(false) }
    LaunchedEffect(timer.isRunning, timer.remainingSeconds) {
        if (timer.isRunning && timer.remainingSeconds > 0) {
            delay(1_000)
            val remaining = timer.remainingSeconds - 1
            onUpdate(timer.copy(remainingSeconds = remaining, isRunning = remaining > 0))
            if (remaining == 0) onFinished()
        }
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp)) {
            Text(timer.name, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimerProgress(timer, Modifier.size(190.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FilledIconButton(onClick = {
                        if (timer.remainingSeconds > 0) onUpdate(timer.copy(isRunning = !timer.isRunning))
                    }, modifier = Modifier.size(56.dp)) {
                        Icon(if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (timer.isRunning) "一時停止" else "再生")
                    }
                    FilledIconButton(onClick = { showNameEditor = true }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.Edit, "タイマー名を編集")
                    }
                }
            }
        }
    }
    if (showNameEditor) TimerNameDialog(timer.name, { showNameEditor = false }) { name ->
        onUpdate(timer.copy(name = name.ifBlank { timer.name })); showNameEditor = false
    }
}

@Composable
private fun TimerProgress(timer: TimerItem, modifier: Modifier) {
    val progress = if (timer.initialSeconds == 0) 0f else timer.remainingSeconds.toFloat() / timer.initialSeconds
    val trackColor = MaterialTheme.colorScheme.surface
    val progressColor = MaterialTheme.colorScheme.primary
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
            drawArc(trackColor, -90f, 360f, false, style = stroke)
            drawArc(progressColor, -90f, 360f * progress, false, style = stroke)
        }
        Text(formatTimeFromSeconds(timer.remainingSeconds), fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TimerKeypad(modifier: Modifier, nextTimerNumber: Int, onCreate: (Int, String) -> Unit, onCancel: (() -> Unit)?) {
    var input by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(formatTimeString(input), fontSize = 52.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { digit -> KeypadButton(digit) { input = addDigit(input, digit) } }
            }; Spacer(Modifier.height(12.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            KeypadButton("C") { input = "" }; KeypadButton("0") { input = addDigit(input, "0") }; KeypadButton("⌫") { input = deleteDigit(input) }
        }
        Spacer(Modifier.height(28.dp))
        Button(onClick = { onCreate(convertToSeconds(input), "Timer $nextTimerNumber") }, enabled = convertToSeconds(input) > 0, modifier = Modifier.width(180.dp).height(56.dp)) {
            Text("タイマーを作成", fontSize = 18.sp)
        }
        if (onCancel != null) TextButton(onClick = onCancel) { Text("キャンセル") }
    }
}

@Composable
private fun TimerNameDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("タイマー名を変更") },
        text = { OutlinedTextField(name, { name = it }, singleLine = true) },
        confirmButton = { TextButton({ onConfirm(name) }) { Text("保存") } },
        dismissButton = { TextButton(onDismiss) { Text("キャンセル") } })
}

fun addDigit(old: String, digit: String) = if (old.length >= 6) old else old + digit
fun deleteDigit(old: String) = old.dropLast(1)
fun formatTimeString(input: String): String { val p = input.padStart(6, '0'); return "${p.substring(0, 2)}:${p.substring(2, 4)}:${p.substring(4, 6)}" }
fun formatTimeFromSeconds(seconds: Int): String { val h = seconds / 3600; val m = seconds % 3600 / 60; val s = seconds % 60; return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s) }
fun convertToSeconds(input: String): Int { val p = input.padStart(6, '0'); return p.substring(0, 2).toInt() * 3600 + p.substring(2, 4).toInt() * 60 + p.substring(4, 6).toInt() }

fun createTimerFinishedNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.getSystemService(NotificationManager::class.java).createNotificationChannel(
        NotificationChannel("timer_finished_channel", "タイマー完了", NotificationManager.IMPORTANCE_HIGH))
}
fun showTimerFinishedNotification(context: Context, timerName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
    val notification = NotificationCompat.Builder(context, "timer_finished_channel").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("タイマー終了").setContentText("$timerName が終了しました。").setPriority(NotificationCompat.PRIORITY_HIGH).build()
    NotificationManagerCompat.from(context).notify(timerName.hashCode(), notification)
}
@Composable fun KeypadButton(text: String, onClick: () -> Unit) { Button(onClick, Modifier.width(80.dp).height(56.dp)) { Text(text, fontSize = if (text.length == 1) 28.sp else 20.sp) } }
@Preview(showSystemUi = true) @Composable private fun PreviewTimerMenu() { NT_Clock_AppTheme { TimerDisplayOnlyScreen() } }
