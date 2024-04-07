package de.gaw.blursample.app.blur

import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children

/**
 * Draws its content blurred
 *
 * Uses render effects on Android 12 and above, RenderScript with bitmaps on lower Android levels
 */
@Composable
fun Blurred(
    modifier: Modifier = Modifier,
    radius: Dp = 32.dp,
    content: @Composable () -> Unit,
) {
//    if (isAtLeastAndroid12()) {
//        ModernBlurred(
//            modifier = modifier,
//            blurRadius = radius,
//            content = content,
//        )
//    } else {
        LegacyBlurred(
            modifier = modifier,
            blurRadius = radius,
            content = content,
        )
//    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.S)
private fun ModernBlurred(
    blurRadius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .blur(radius = blurRadius),
    ) {
        content()
    }
}

@Composable
private fun LegacyBlurred(
    blurRadius: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val blurRadiusPx = with(LocalDensity.current) { blurRadius.toPx() }
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ComposeBlurView(context)
        },
        update = { composeBlurView ->
            with(composeBlurView) {
                radius = blurRadiusPx
                setContent {
                    content()
                }
            }
        },
    )
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isAtLeastAndroid12() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Suppress("FunctionName")
private fun ComposeBlurView(context: Context) = BlurView(context).apply {
    addView(ComposeView(context))
}

private fun BlurView.setContent(content: @Composable () -> Unit) = children
    .filterIsInstance(ComposeView::class.java)
    .first()
    .setContent(content)