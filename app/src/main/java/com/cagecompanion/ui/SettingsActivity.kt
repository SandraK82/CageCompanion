package com.cagecompanion.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.cagecompanion.CageCompanionApp
import com.cagecompanion.R
import com.cagecompanion.ui.theme.CageCompanionTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CageCompanionTheme {
                SettingsScreen(onNavigateBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as CageCompanionApp
    val scope = rememberCoroutineScope()

    // Nightscout settings
    var nightscoutUrl by remember { mutableStateOf("") }
    var apiSecret by remember { mutableStateOf("") }
    var isTestingConnection by remember { mutableStateOf(false) }

    // Threshold settings
    var yellowDays by remember { mutableStateOf("2") }
    var yellowHours by remember { mutableStateOf("0") }
    var yellowMinutes by remember { mutableStateOf("0") }
    var redDays by remember { mutableStateOf("2") }
    var redHours by remember { mutableStateOf("12") }
    var redMinutes by remember { mutableStateOf("0") }

    // Notification settings
    var notificationEnabled by remember { mutableStateOf(true) }

    // Load current settings
    LaunchedEffect(Unit) {
        nightscoutUrl = app.preferencesManager.nightscoutUrl.first()
        apiSecret = app.preferencesManager.nightscoutApiSecret.first()
        notificationEnabled = app.preferencesManager.notificationEnabled.first()

        val yellowThreshold = app.preferencesManager.yellowThresholdMinutes.first()
        yellowDays = (yellowThreshold / (24 * 60)).toString()
        yellowHours = ((yellowThreshold % (24 * 60)) / 60).toString()
        yellowMinutes = (yellowThreshold % 60).toString()

        val redThreshold = app.preferencesManager.redThresholdMinutes.first()
        redDays = (redThreshold / (24 * 60)).toString()
        redHours = ((redThreshold % (24 * 60)) / 60).toString()
        redMinutes = (redThreshold % 60).toString()
    }

    // Save settings
    fun saveSettings() {
        scope.launch {
            app.preferencesManager.setNightscoutUrl(nightscoutUrl)
            app.preferencesManager.setNightscoutApiSecret(apiSecret)
            app.preferencesManager.setNotificationEnabled(notificationEnabled)

            app.preferencesManager.setYellowThreshold(
                yellowDays.toIntOrNull() ?: 2,
                yellowHours.toIntOrNull() ?: 0,
                yellowMinutes.toIntOrNull() ?: 0
            )
            app.preferencesManager.setRedThreshold(
                redDays.toIntOrNull() ?: 2,
                redHours.toIntOrNull() ?: 12,
                redMinutes.toIntOrNull() ?: 0
            )

            Toast.makeText(context, R.string.save, Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    // Test connection
    fun testConnection() {
        scope.launch {
            isTestingConnection = true
            // Save URL temporarily for testing
            app.preferencesManager.setNightscoutUrl(nightscoutUrl)
            app.preferencesManager.setNightscoutApiSecret(apiSecret)

            val result = app.nightscoutApi.testConnection()
            result.onSuccess {
                Toast.makeText(context, R.string.connection_success, Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, R.string.connection_failed, Toast.LENGTH_SHORT).show()
            }
            isTestingConnection = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = { saveSettings() }) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nightscout Settings Section
            Text(
                text = stringResource(R.string.nightscout_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = nightscoutUrl,
                onValueChange = { nightscoutUrl = it },
                label = { Text(stringResource(R.string.nightscout_url)) },
                placeholder = { Text(stringResource(R.string.nightscout_url_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = apiSecret,
                onValueChange = { apiSecret = it },
                label = { Text(stringResource(R.string.nightscout_api_secret)) },
                placeholder = { Text(stringResource(R.string.nightscout_api_secret_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Button(
                onClick = { testConnection() },
                enabled = nightscoutUrl.isNotBlank() && !isTestingConnection,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTestingConnection) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(R.string.test_connection))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Threshold Settings Section
            Text(
                text = stringResource(R.string.threshold_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Yellow Threshold
            Text(
                text = stringResource(R.string.yellow_threshold),
                style = MaterialTheme.typography.bodyMedium
            )
            ThresholdInput(
                days = yellowDays,
                hours = yellowHours,
                minutes = yellowMinutes,
                onDaysChange = { yellowDays = it },
                onHoursChange = { yellowHours = it },
                onMinutesChange = { yellowMinutes = it }
            )

            // Red Threshold
            Text(
                text = stringResource(R.string.red_threshold),
                style = MaterialTheme.typography.bodyMedium
            )
            ThresholdInput(
                days = redDays,
                hours = redHours,
                minutes = redMinutes,
                onDaysChange = { redDays = it },
                onHoursChange = { redHours = it },
                onMinutesChange = { redMinutes = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Notification Settings Section
            Text(
                text = stringResource(R.string.notification_settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.enable_notification))
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it }
                )
            }
        }
    }
}

@Composable
fun ThresholdInput(
    days: String,
    hours: String,
    minutes: String,
    onDaysChange: (String) -> Unit,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = days,
            onValueChange = { onDaysChange(it.filter { c -> c.isDigit() }) },
            label = { Text(stringResource(R.string.days)) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        OutlinedTextField(
            value = hours,
            onValueChange = { onHoursChange(it.filter { c -> c.isDigit() }) },
            label = { Text(stringResource(R.string.hours)) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        OutlinedTextField(
            value = minutes,
            onValueChange = { onMinutesChange(it.filter { c -> c.isDigit() }) },
            label = { Text(stringResource(R.string.minutes)) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}
