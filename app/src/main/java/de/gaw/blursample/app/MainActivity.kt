package de.gaw.blursample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import de.gaw.blursample.app.blur.Blurred
import de.gaw.blursample.app.ui.theme.BlurSampleTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BlurSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        var sliderValue by remember { mutableFloatStateOf(0f) }
                        val blurRadius = with(LocalDensity.current) { 48.dp * sliderValue }
                        Blurred(
                            modifier = Modifier.padding(innerPadding),
                            radius = blurRadius,
                        ) {
                            val isLarge by produceState(initialValue = false) {
                                while (true) {
                                    value = !value
                                    delay(1500)
                                }
                            }
                            val size by animateDpAsState(
                                targetValue = if (isLarge) 128.dp else 64.dp,
                                label = "size-transition",
                            )
                            val sizePx = with(LocalDensity.current) { size.toPx() }
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(Color.Red, sizePx)
                            }
                        }
                        Slider(
                            modifier = Modifier
                                .padding(32.dp)
                                .statusBarsPadding(),
                            value = sliderValue, onValueChange = { sliderValue = it },
                        )
                    }
                }
            }
        }
    }
}