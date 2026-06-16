package com.example.nt_clock_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
        AlarmMenuContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding),
            alarms = alarms,
            onToggleEnabled = { alarmId, enabled ->
                updateAlarms(
                    updatedAlarms = alarms.map { alarm ->
                        if (alarm.id == alarmId) alarm.copy(enabled = enabled) else alarm
                    }
                )
            },
            onDelete = { alarmId ->
                updateAlarms(updatedAlarms = alarms.filterNot { it.id == alarmId })
            },
            onEditAlarmName = { newName ->
                updateAlarms(
                    updatedAlarms = alarms.map { alarm ->
                        alarm.copy(alarmName = newName)
                    }
                )
            }
        )

        if (showTimePicker.value) {
            MyTimePicker1(
                onConfirm = { hour, minute ->
                    val nextId = (alarms.maxOfOrNull { it.id } ?: 0) + 1

                    updateAlarms(
                        updatedAlarms = alarms + AlarmItem(
                            id = nextId,
                            hour = hour,
                            minute = minute,
                            enabled = true,
                            alarmName = "Alarm"
                        )
                    )

                    showTimePicker.value = false
                },
                onCancel = {
                    showTimePicker.value = false
                }
            )
        }
    }
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
fun MyTimePicker1(
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onCancel: () -> Unit
) {
    val currentTime = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = true
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Max),
        ) {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth()) {

                FilledIconButton(
                    onClick = {
                        onConfirm(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                    },
                    shape = RoundedCornerShape(size = 12.dp),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                }

                Spacer(modifier = Modifier.weight(1f))

                FilledIconButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(size = 12.dp),
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                }
            }
        }
    }
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
                    Text(
                        text = "Monday ~ Friday",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
            }
        }
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
