package com.example.nt_clock_app

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nt_clock_app.ui.theme.NT_Clock_AppTheme
import kotlinx.coroutines.delay

@Composable
fun StopwatchMainScreen1(innerPadding: PaddingValues) {
    var elapsedTime by remember { mutableLongStateOf(value = 0L) }
    var isRunning by remember { mutableStateOf(value = false) }
    val formattedTime = formatTime(timeMs = elapsedTime)

    val lapTimes = remember {
        mutableStateListOf<Long>()
    }

    LaunchedEffect(key1 = isRunning) {
        var lastTime = SystemClock.elapsedRealtime()

        while (isRunning) {
            delay(timeMillis = 10)

            val currentTime = SystemClock.elapsedRealtime()
            elapsedTime += currentTime - lastTime
            lastTime = currentTime
        }
    }

    TimeTextAndButtons1(
        text = formattedTime,
        isRunning = isRunning,
        onStartStop = { isRunning = !isRunning },
        onReset = {
            lapTimes.clear()
            elapsedTime = 0L
        },
        onRap = { lapTimes.add(0, elapsedTime) },
        lapTimes = lapTimes,
        modifier = Modifier.padding(paddingValues = innerPadding)
    )
}

@SuppressLint("DefaultLocale")
fun formatTime(timeMs: Long): String {
    val totalCs = timeMs / 10
    val minutes = totalCs / 6000
    val seconds = (totalCs % 6000) / 100
    val centiseconds = totalCs % 100

    return String.format("%02d.%02d.%02d", minutes, seconds, centiseconds)
}

@Composable
fun TimeTextAndButtons1(
    text: String,
    isRunning: Boolean,
    lapTimes: List<Long>,
    onStartStop: () -> Unit,
    onReset: () -> Unit,
    onRap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 80.sp,
            fontFamily = NimbusSans1,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
        )
        Spacer(modifier = Modifier.size(100.dp))
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            onClick = onStartStop,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = if (isRunning) "Stop" else "Start",
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            onClick = {
                if (!isRunning) {
                    onReset()
                } else {
                    onRap()
                }
            },
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = if (!isRunning) "Reset" else "Rap",
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium
            )
        }
        lapTimes.forEachIndexed { index, lapTime ->
            Text(
                text = "Lap ${lapTimes.size - index}: ${formatTime(lapTime)}",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 25.sp,
                fontFamily = NimbusSans1,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun Preview_Mobile_SM() {
    NT_Clock_AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            StopwatchMainScreen1(
                innerPadding = PaddingValues(top = 150.dp)
            )
        }
    }
}
