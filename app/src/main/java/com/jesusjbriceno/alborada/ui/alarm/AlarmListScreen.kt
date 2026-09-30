package com.jesusjbriceno.alborada.ui.alarm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jesusjbriceno.alborada.R
import com.jesusjbriceno.alborada.alarm.AlarmListViewModel
import com.jesusjbriceno.alborada.data.local.Alarm
import com.jesusjbriceno.alborada.domain.AlarmDays

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(viewModel: AlarmListViewModel = viewModel()) {
    val alarms by viewModel.alarms.collectAsState()
    val canScheduleExact by viewModel.canScheduleExact.collectAsState()

    var editing by remember { mutableStateOf<Alarm?>(null) }
    var creating by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { }

    // Android 13+: ask for notification permission once at startup.
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Re-check the exact-alarm permission when returning from settings.
    LifecycleResumeEffect(viewModel) {
        viewModel.refreshExactPermission()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { creating = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_alarm))
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (!canScheduleExact) {
                ExactAlarmDeniedBanner(
                    onOpenSettings = { context.openAppAlarmSettings() },
                )
            }

            if (alarms.isEmpty()) {
                EmptyAlarms(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { viewModel.toggle(alarm) },
                            onClickEdit = { editing = alarm },
                        )
                    }
                }
            }
        }
    }

    if (creating) {
        AlarmEditorDialog(
            alarm = null,
            onDismiss = { creating = false },
            onSave = { hour, minute, days, label, anticipation ->
                viewModel.upsert(
                    id = 0L,
                    hour = hour,
                    minute = minute,
                    days = days,
                    label = label,
                    anticipationMinutes = anticipation,
                )
                creating = false
            },
        )
    }

    editing?.let { alarm ->
        AlarmEditorDialog(
            alarm = alarm,
            onDismiss = { editing = null },
            onSave = { hour, minute, days, label, anticipation ->
                viewModel.upsert(
                    id = alarm.id,
                    hour = hour,
                    minute = minute,
                    days = days,
                    label = label,
                    anticipationMinutes = anticipation,
                )
                editing = null
            },
            onDelete = {
                viewModel.delete(alarm)
                editing = null
            },
        )
    }
}

@Composable
private fun ExactAlarmDeniedBanner(onOpenSettings: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(12.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onOpenSettings),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.exact_alarm_denied_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun EmptyAlarms(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.alarm_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    onToggle: () -> Unit,
    onClickEdit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClickEdit),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.timeText(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = alarm.daysText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (alarm.anticipationMinutes > 0) {
                    Text(
                        text = stringResource(
                            R.string.alarm_anticipation_format,
                            alarm.anticipationMinutes,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Switch(
                checked = alarm.enabled,
                onCheckedChange = { onToggle() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditorDialog(
    alarm: Alarm?,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, days: Set<Int>, label: String, anticipationMinutes: Int) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val timePickerState =
        rememberTimePickerState(
            initialHour = alarm?.hour ?: 7,
            initialMinute = alarm?.minute ?: 0,
            is24Hour = true,
        )
    var selectedDays by remember {
        mutableStateOf(
            if (alarm == null) emptySet() else AlarmDays.fromBitmask(alarm.daysBitmask),
        )
    }
    var label by remember { mutableStateOf(alarm?.label ?: "") }
    var anticipation by remember {
        mutableFloatStateOf(
            (alarm?.anticipationMinutes ?: Alarm.DEFAULT_ANTICIPATION_MINUTES).toFloat(),
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (alarm == null) R.string.add_alarm else R.string.edit_alarm)) },
        text = {
            Column {
                TimePicker(state = timePickerState)
                Spacer(modifier = Modifier.height(12.dp))
                DayChipRow(selectedDays = selectedDays, onToggleDay = { day ->
                    selectedDays =
                        if (day in selectedDays) selectedDays - day else selectedDays + day
                })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.label_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.alarm_anticipation_label))
                    Text(
                        text = stringResource(
                            R.string.alarm_anticipation_format,
                            anticipation.toInt(),
                        ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Slider(
                    value = anticipation,
                    onValueChange = { anticipation = it },
                    valueRange = Alarm.MIN_ANTICIPATION_MINUTES.toFloat()..
                        Alarm.MAX_ANTICIPATION_MINUTES.toFloat(),
                    steps = 11, // 5-minute steps: 0, 5, ..., 60
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    timePickerState.hour,
                    timePickerState.minute,
                    selectedDays,
                    label.trim(),
                    anticipation.toInt(),
                )
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.delete_alarm))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        },
    )
}

@Composable
private fun DayChipRow(
    selectedDays: Set<Int>,
    onToggleDay: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        AlarmDays.ALL.forEach { day ->
            FilterChip(
                selected = day in selectedDays,
                onClick = { onToggleDay(day) },
                label = { Text(day.shortLabel()) },
            )
        }
    }
}

private fun Alarm.timeText(): String = "%02d:%02d".format(hour, minute)

private fun Alarm.daysText(): String {
    val days = AlarmDays.fromBitmask(daysBitmask)
    if (days.isEmpty()) return ""
    if (days.size == 7) return "Todos los días"
    return days.sorted().joinToString(" ") { it.shortLabel() }
}

private fun Int.shortLabel(): String =
    when (this) {
        AlarmDays.SUNDAY -> "D"
        AlarmDays.MONDAY -> "L"
        AlarmDays.TUESDAY -> "M"
        AlarmDays.WEDNESDAY -> "X"
        AlarmDays.THURSDAY -> "J"
        AlarmDays.FRIDAY -> "V"
        AlarmDays.SATURDAY -> "S"
        else -> "?"
    }

private fun Context.openAppAlarmSettings() {
    val intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
        }
    runCatching { startActivity(intent) }
        .onFailure { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)) }
}
