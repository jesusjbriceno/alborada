package com.jesusjbriceno.alborada.alarm

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.jesusjbriceno.alborada.R
import com.jesusjbriceno.alborada.ui.theme.AlboradaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Full-screen fake sunrise that runs [Alarm.anticipationMinutes] before the
 * alarm: the screen color and window brightness ramp through dawn phases while
 * the bundled tone creeps in from silence to full volume (crescendo). At the
 * alarm time it turns into the ringing state with a stop action.
 */
class AlarmActivity : ComponentActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarmStartEpoch =
            intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_START_EPOCH, System.currentTimeMillis())
        val anticipationMinutes =
            intent.getIntExtra(AlarmReceiver.EXTRA_ANTICIPATION_MINUTES, 15)
        val label = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: ""

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AlboradaTheme(darkTheme = true) {
                val holder =
                    remember {
                        SunriseStateHolder(
                            sunriseStartEpoch = alarmStartEpoch - anticipationMinutes * 60_000L,
                            alarmStartEpoch = alarmStartEpoch,
                            label = label,
                        )
                    }
                LaunchedEffect(Unit) {
                    while (isActive) {
                        holder.tick()
                        // Full volume once ringing (also covers anticipation = 0).
                        player?.volume =
                            if (holder.ringing) 1f else SunriseRenderer.toneVolume(holder.progress)
                        // Screen brightness follows the dawn too (window attribute).
                        window.attributes =
                            window.attributes.apply {
                                screenBrightness = SunriseRenderer.brightness(holder.progress)
                            }
                        delay(100L)
                    }
                }
                SunriseScreen(
                    holder = holder,
                    onStop = { stopAndClose() },
                )
            }
        }

        player =
            ExoPlayer.Builder(this).build().apply {
                setMediaItem(MediaItem.fromUri("android.resource://$packageName/${R.raw.sunrise_tone}"))
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                prepare()
                play()
            }
    }

    private fun stopAndClose() {
        player?.release()
        player = null
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}

/** Mutable sunrise state driven by wall-clock time, not frame counting. */
class SunriseStateHolder(
    private val sunriseStartEpoch: Long,
    private val alarmStartEpoch: Long,
    val label: String,
) {
    private val totalMillis = (alarmStartEpoch - sunriseStartEpoch).coerceAtLeast(1L)
    private val totalMinutes = (totalMillis / 60_000L).coerceAtLeast(1)

    var progress by mutableFloatStateOf(0f)
        private set
    var ringing by mutableStateOf(false)
        private set

    fun tick() {
        val now = System.currentTimeMillis()
        progress = SunriseRenderer.progress(now - sunriseStartEpoch, totalMillis)
        ringing = now >= alarmStartEpoch
    }

    val minutesText: String get() = totalMinutes.toString()
}

@Composable
private fun SunriseScreen(
    holder: SunriseStateHolder,
    onStop: () -> Unit,
) {
    val background = SunriseRenderer.phaseColor(holder.progress)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = background)
            // Rising sun: from low horizon to upper area as progress grows.
            val cx = size.width / 2f
            val cy = size.height * (0.78f - 0.55f * holder.progress)
            val radius = size.minDimension * 0.12f
            drawCircle(color = Color(0xFFFFF1C9), radius = radius, center = Offset(cx, cy))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (holder.ringing) {
                Text(
                    text = stringResource(R.string.ringing_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (holder.label.isNotBlank()) {
                    Text(
                        text = holder.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Text(
                    text = stringResource(R.string.ringing_anticipation, holder.minutesText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                )
            } else {
                Text(
                    text = stringResource(R.string.sunrise_running),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Button(
                onClick = onStop,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text(stringResource(R.string.ringing_stop))
            }
        }
    }
}
