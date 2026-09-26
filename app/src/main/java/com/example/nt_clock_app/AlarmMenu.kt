package com.example.nt_clock_app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nt_clock_app.ui.theme.NT_Clock_AppTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardAM(innerPadding: PaddingValues) {
    val context = LocalContext.current
    var alarms by remember { mutableStateOf<List<AlarmItem>>(value = emptyList()) }
    val showTimePicker = remember { mutableStateOf(value = false) }

    LaunchedEffect(Unit) {
        alarms = loadAlarms1(context)
    }

    fun updateAlarms(updatedAlarms: List<AlarmItem>) {
        alarms = updatedAlarms
        saveAlarms1(context, updatedAlarms)
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues = innerPadding),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showTimePicker.value = true
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AlarmMenuContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding),
                alarms = alarms,
                onToggleEnabled = { alarmId, enabled ->
                    val alarm = alarms.find { it.id == alarmId }

                    if (alarm != null) {
                        if (enabled) {
                            scheduleAlarm(
                                context = context,
                                hour = alarm.hour,
                                minute = alarm.minute,
                                requestCode = alarm.id
                            )
                        } else {
                            cancelAlarm(
                                context = context,
                                requestCode = alarm.id
                            )
                        }

                        updateAlarms(
                            updatedAlarms = alarms.map { alarmItem ->
                                if (alarmItem.id == alarmId) {
                                    alarmItem.copy(enabled = enabled)
                                } else {
                                    alarmItem
                                }
                            }
                        )
                    }
                },
                onDelete = { alarmId ->
                    cancelAlarm(
                        context = context,
                        requestCode = alarmId
                    )

                    updateAlarms(
                        updatedAlarms = alarms.filterNot { it.id == alarmId }
                    )
                },
                onEditAlarmName = { newName ->
                    updateAlarms(
                        updatedAlarms = alarms.map { alarm ->
                            alarm.copy(alarmName = newName)
                        }
                    )
                }
            )
        }

        val nextId = (alarms.maxOfOrNull { it.id } ?: 0) + 1

        AnimatedVisibility(
            visible = showTimePicker.value,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            MainTimePicker1(
                onConfirm = { timePickerState ->
                    val newAlarm = AlarmItem(
                        id = nextId,
                        hour = timePickerState.hour,
                        minute = timePickerState.minute,
                        enabled = true,
                        alarmName = "My Alarm $nextId"
                    )

                    updateAlarms(
                        updatedAlarms = alarms + newAlarm
                    )

                    scheduleAlarm(
                        context = context,
                        hour = newAlarm.hour,
                        minute = newAlarm.minute,
                        requestCode = newAlarm.id
                    )

                    showTimePicker.value = false
                },
                onDismiss = {
                    showTimePicker.value = false
                }
            )
        }
    }
}

fun cancelAlarm(
    context: Context,
    requestCode: Int
) {
    val intent = Intent(context, AlarmReceiver::class.java)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
}

@Composable
fun AlarmMenuContent(
    modifier: Modifier = Modifier,
    alarms: List<AlarmItem>,
    onToggleEnabled: (Int, Boolean) -> Unit,
    onDelete: (Int) -> Unit,
    onEditAlarmName: (String) -> Unit
) {
    val showEditAlarmNameMenu = remember { mutableStateOf(value = false) }

    if (alarms.isEmpty()) {
        Box(
            modifier = modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No alarms")
        }
        return
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        items(items = alarms, key = { it.id }) { alarm ->
            AlarmCard(
                alarm = alarm,
                onToggleEnabled = { enabled -> onToggleEnabled(alarm.id, enabled) },
                onDelete = { onDelete(alarm.id) },
                onEditAlarmName = {showEditAlarmNameMenu.value = true}
            )
        }
    }

    if (showEditAlarmNameMenu.value) {
        EditAlarmName1(
            onDismiss = { showEditAlarmNameMenu.value = false },
            onConfirm = { newAlarmName ->
                onEditAlarmName(newAlarmName)
                showEditAlarmNameMenu.value = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTimePicker1(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true
    )

    TimePickerDialog1(
        onConfirm = { onConfirm(timePickerState) },
        onDismiss = { onDismiss() }
    ) {
        TimePicker(state = timePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog1(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("Confirm")
            }
        },
        text = { content() }
    )
}


@Composable
fun EditAlarmName1(
    onDismiss: () -> Unit, onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(value = "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit alarm name...") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(text = "Name") },
                singleLine = false
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text(text = "Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Dismiss") }
        }
    )
}

@Composable
fun AlarmCard(
    alarm: AlarmItem,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEditAlarmName: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = alarm.alarmName,
            )
            Spacer(modifier = Modifier)
            Row {
                Column {
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = "%02d:%02d".format(alarm.hour, alarm.minute),
                        fontSize = 50.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Switch(
                        checked = alarm.enabled,
                        onCheckedChange = onToggleEnabled
                    )
                    Row {
                        FilledIconButton(
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            onClick = onEditAlarmName,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                        FilledIconButton(
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            onClick = onDelete,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete alarm"
                            )
                        }
                    }
                }
            }
        }
    }
}

fun scheduleAlarm(
    context: Context,
    hour: Int,
    minute: Int,
    requestCode: Int
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(
                    calendar.timeInMillis,
                    pendingIntent
                ),
                pendingIntent
            )
        }
    } else {
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                calendar.timeInMillis,
                pendingIntent
            ),
            pendingIntent
        )
    }
}

@Preview(showSystemUi = true, name = "Smartphone Size", widthDp = 400, heightDp = 800)
@Composable
fun Preview_Mobile_AM() {
    NT_Clock_AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AlarmMenuContent(
                modifier = Modifier.fillMaxSize(),
                alarms = listOf(
                    AlarmItem(id = 1, hour = 7, minute = 30, enabled = true, alarmName = "My Alarm 1"),
                    AlarmItem(id = 2, hour = 8, minute = 0, enabled = false, alarmName = "My Alarm 2")
                ),
                onToggleEnabled = { _, _ -> },
                onDelete = {},
                onEditAlarmName = {}
            )
        }
    }
}
