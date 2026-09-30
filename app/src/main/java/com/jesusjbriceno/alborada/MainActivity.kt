package com.jesusjbriceno.alborada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesusjbriceno.alborada.ui.theme.AlboradaTheme
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlboradaTheme {
                HomeScreen()
            }
        }
    }
}

/** Placeholder home screen shown until the alarm features land. */
@Composable
fun HomeScreen() {
    Scaffold { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SunIcon(modifier = Modifier.size(96.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** A simple sun: circle plus rays, drawn instead of pulling in the heavy icons artifact. */
@Composable
fun SunIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height * 0.42f
        val radius = size.minDimension * 0.24f

        drawCircle(color = color, radius = radius, center = Offset(cx, cy))

        val rayStart = radius * 1.55f
        val rayEnd = radius * 2.0f
        val strokeWidth = radius * 0.28f
        val rayColor = color.copy(alpha = 0.75f)
        for (i in 0 until 8) {
            val angle = i * Math.PI / 4.0
            val start =
                Offset(
                    (cx + rayStart * cos(angle)).toFloat(),
                    (cy + rayStart * sin(angle)).toFloat(),
                )
            val end =
                Offset(
                    (cx + rayEnd * cos(angle)).toFloat(),
                    (cy + rayEnd * sin(angle)).toFloat(),
                )
            drawLine(
                color = rayColor,
                start = start,
                end = end,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AlboradaTheme {
        HomeScreen()
    }
}
