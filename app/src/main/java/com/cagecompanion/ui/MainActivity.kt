package com.cagecompanion.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cagecompanion.CageCompanionApp
import com.cagecompanion.R
import com.cagecompanion.data.CageColor
import com.cagecompanion.data.CageStatus
import com.cagecompanion.service.CageUpdateService
import com.cagecompanion.ui.theme.CageCompanionTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CageCompanionTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as CageCompanionApp
    val scope = rememberCoroutineScope()

    var isConfigured by remember { mutableStateOf<Boolean?>(null) }
    var cageStatus by remember { mutableStateOf<CageStatus?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check if configured on start and start service
    LaunchedEffect(Unit) {
        isConfigured = app.preferencesManager.isNightscoutConfigured()
        if (isConfigured == true) {
            // Load cached CAGE if available
            val cachedTime = app.preferencesManager.getLastCageTimeSync()
            if (cachedTime > 0) {
                val yellowThreshold = app.preferencesManager.getYellowThresholdMinutesSync()
                val redThreshold = app.preferencesManager.getRedThresholdMinutesSync()
                cageStatus = CageStatus.fromTimestamp(cachedTime, yellowThreshold, redThreshold)
            }

            // Start foreground service for continuous updates
            val serviceIntent = Intent(context, CageUpdateService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    // Update UI every minute (recalculate age from cached timestamp)
    LaunchedEffect(cageStatus?.cageTimeMs) {
        if (cageStatus != null) {
            while (true) {
                delay(60_000L) // Wait 1 minute
                val cachedTime = app.preferencesManager.getLastCageTimeSync()
                if (cachedTime > 0) {
                    val yellowThreshold = app.preferencesManager.getYellowThresholdMinutesSync()
                    val redThreshold = app.preferencesManager.getRedThresholdMinutesSync()
                    cageStatus = CageStatus.fromTimestamp(cachedTime, yellowThreshold, redThreshold)
                }
            }
        }
    }

    // Refresh function
    fun refresh() {
        scope.launch {
            isLoading = true
            errorMessage = null

            val result = app.nightscoutApi.getLatestCage()
            result.onSuccess { timestamp ->
                if (timestamp != null) {
                    app.preferencesManager.setCageTime(timestamp)
                    val yellowThreshold = app.preferencesManager.getYellowThresholdMinutesSync()
                    val redThreshold = app.preferencesManager.getRedThresholdMinutesSync()
                    cageStatus = CageStatus.fromTimestamp(timestamp, yellowThreshold, redThreshold)

                    // Service will update notification automatically
                } else {
                    cageStatus = null
                    errorMessage = context.getString(R.string.cage_no_data)
                }
            }.onFailure { e ->
                errorMessage = e.message ?: context.getString(R.string.cage_error)
            }

            isLoading = false
        }
    }

    // Cannula changed function
    fun cannulaChanged() {
        scope.launch {
            isLoading = true

            val result = app.nightscoutApi.uploadCannulaChange()
            result.onSuccess {
                Toast.makeText(context, R.string.cannula_change_success, Toast.LENGTH_SHORT).show()
                // Refresh to get the new CAGE
                refresh()
            }.onFailure { e ->
                Toast.makeText(context, R.string.cannula_change_error, Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { refresh() }, enabled = !isLoading && isConfigured == true) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                    IconButton(onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                isConfigured == null -> {
                    CircularProgressIndicator()
                }
                isConfigured == false -> {
                    NotConfiguredCard()
                }
                else -> {
                    CageDisplayCard(
                        cageStatus = cageStatus,
                        isLoading = isLoading,
                        errorMessage = errorMessage
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    CannulaChangedButton(
                        onClick = { cannulaChanged() },
                        enabled = !isLoading
                    )
                }
            }
        }
    }
}

@Composable
fun NotConfiguredCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.cage_not_configured),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.nightscout_settings),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CageDisplayCard(
    cageStatus: CageStatus?,
    isLoading: Boolean,
    errorMessage: String?
) {
    val backgroundColor = when (cageStatus?.color) {
        CageColor.GREEN -> Color(0xFF4CAF50)
        CageColor.YELLOW -> Color(0xFFFFC107)
        CageColor.RED -> Color(0xFFF44336)
        null -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when (cageStatus?.color) {
        CageColor.YELLOW -> Color.Black
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .size(250.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = textColor)
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                cageStatus == null -> {
                    Text(
                        text = stringResource(R.string.cage_no_data),
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.cage_title),
                            color = textColor.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cageStatus.formattedAge,
                            color = textColor,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CannulaChangedButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = stringResource(R.string.cannula_changed),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
