package com.example.nt_clock_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.nt_clock_app.ui.theme.NT_Clock_AppTheme
import kotlinx.coroutines.delay

@Composable
fun TimerDisplayOnlyScreen() {
    val context = LocalContext.current

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(key1 = Unit) {
        createTimerFinishedNotificationChannel(context)
    }

    var timeInput by remember { mutableStateOf(value = "") }
    var remainingSeconds by remember { mutableIntStateOf(value = 0) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(timeMillis = 1000)
            remainingSeconds--

            if (remainingSeconds == 0) {
                isRunning = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        showTimerFinishedNotification(context)
                    } else {
                        requestPermissionLauncher.launch(
                            input = Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                } else {
                    showTimerFinishedNotification(context)
                }
            }
        }
    }

    val displayTime = if (!isRunning) {
        formatTimeString(timeInput)
    } else {
        formatTimeFromSeconds(sec = remainingSeconds)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(top = 150.dp),
            text = displayTime,
            fontSize = 80.sp,
            fontFamily = NimbusSans1,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(30.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                KeypadButton("1") { if (!isRunning) timeInput = addDigit(timeInput, "1") }
                KeypadButton("2") { if (!isRunning) timeInput = addDigit(timeInput, "2") }
                KeypadButton("3") { if (!isRunning) timeInput = addDigit(timeInput, "3") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                KeypadButton("4") { if (!isRunning) timeInput = addDigit(timeInput, "4") }
                KeypadButton("5") { if (!isRunning) timeInput = addDigit(timeInput, "5") }
                KeypadButton("6") { if (!isRunning) timeInput = addDigit(timeInput, "6") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                KeypadButton("7") { if (!isRunning) timeInput = addDigit(timeInput, "7") }
                KeypadButton("8") { if (!isRunning) timeInput = addDigit(timeInput, "8") }
                KeypadButton("9") { if (!isRunning) timeInput = addDigit(timeInput, "9") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Button(
                    onClick = { if (!isRunning) timeInput = "" },
                    modifier = Modifier.width(80.dp).height(60.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Clear",
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                KeypadButton("0") { if (!isRunning) timeInput = addDigit(timeInput, "0") }
                Button(
                    onClick = { if (!isRunning) timeInput = deleteDigit(timeInput) },
                    modifier = Modifier.width(80.dp).height(60.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Del",
                        fontSize = 25.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (!isRunning && timeInput.isNotEmpty()) {
                        remainingSeconds = convertToSeconds(timeInput)
                        if (remainingSeconds > 0) isRunning = true
                    }
                },
                modifier = Modifier.width(150.dp).height(70.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (isRunning) "Running" else "Start",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

fun addDigit(old: String, d: String): String =
    if (old.length >= 6) old else old + d

fun deleteDigit(old: String): String =
    if (old.isEmpty()) "" else old.dropLast(1)

fun formatTimeString(input: String): String {
    val padded = input.padStart(6, '0')
    val h = padded.substring(0, 2)
    val m = padded.substring(2, 4)
    val s = padded.substring(4, 6)
    return "$h:$m:$s"
}

fun formatTimeFromSeconds(sec: Int): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

fun convertToSeconds(input: String): Int {
    val padded = input.padStart(6, '0')
    val h = padded.substring(0, 2).toInt()
    val m = padded.substring(2, 4).toInt()
    val s = padded.substring(4, 6).toInt()
    return h * 3600 + m * 60 + s
}

fun createTimerFinishedNotificationChannel(context: Context) {
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
}

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun showTimerFinishedNotification(context: Context) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val notification = NotificationCompat.Builder(context, "timer_finished_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("タイマー終了")
        .setContentText("設定したタイマーが 0 になりました。")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    NotificationManagerCompat.from(context).notify(1001, notification)
}

@Composable
fun KeypadButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(80.dp).height(60.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = text,
            fontSize = 35.sp
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewTimerMenu1() {
    NT_Clock_AppTheme {
        TimerDisplayOnlyScreen()
    }
}