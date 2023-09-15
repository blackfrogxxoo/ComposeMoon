package me.wxc.composemoon

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.BounceInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.wxc.composemoon.ui.theme.ComposeMoonTheme
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeMoonTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Starry(modifier = Modifier.fillMaxSize())
                    Column {
                        DragMoon(modifier = Modifier.requiredSize(300f.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DragMoon(modifier: Modifier) {
    val drag = remember {
        mutableStateOf(Drag())
    }
    val startOffset = remember {
        mutableStateOf(Offset(0f, 0f))
    }
    val endAnimator = remember {
        mutableStateOf(ValueAnimator.ofFloat(0f, 1f))
    }
    val modifierState = remember {
        mutableStateOf(modifier.pointerInput(Any()) {
            detectDragGestures(
                onDragStart = {
                    startOffset.value = it
                },
                onDragEnd = {
                    endAnimator.value = ValueAnimator.ofFloat(drag.value.strength, 0f).apply {
                        interpolator = BounceInterpolator()
                        duration = 500
                        addUpdateListener {
                            drag.value = drag.value.copy(
                                strength = it.animatedValue as Float
                            )
                        }
                        start()
                    }
                },
                onDrag = { change, dragAmount ->
                    val relateStart = startOffset.value.copy(
                        startOffset.value.x - size.width / 2,
                        startOffset.value.y - size.height / 2
                    )
                    if (relateStart.getDistanceSquared() > (size.width.toDouble() / 2).pow(2.0)
                    ) return@detectDragGestures
                    val relate = change.position.copy(
                        change.position.x - size.width / 2,
                        change.position.y - size.height / 2
                    )
                    val index = 90 + (atan2(relate.y, relate.x) / (2 * PI / 360)).toInt()
                    drag.value = Drag(
                        strength = (relate.getDistance() - relateStart.getDistance()) / 500f,
                        index = index
                    )
                }
            )
        })
    }
    Moon(drag = drag.value, modifierState.value)
}

@Composable
fun Moon(drag: Drag, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val baseRadius = size.width / 3
        val path = Path()
        val pointCount = 360
        val effectCount = if (drag.strength < 0) {
            pointCount / 8
        } else {
            pointCount / 12
        }
        val dragIndex = drag.index
        val fromIndex = dragIndex - pointCount / 2
        val toIndex = fromIndex + pointCount
        for (i in fromIndex..toIndex) {
            val strength = drag.strength
            val x0 = (i - dragIndex) / effectCount.toDouble()
            val p = strength * Math.E.pow(-x0 * x0)
            val radius = (baseRadius + baseRadius * p).toFloat()
            val x = (cx + sin(i * 2 * PI / pointCount) * radius).toFloat()
            val y = (cy - cos(i * 2 * PI / pointCount) * radius).toFloat()
            when (i) {
                fromIndex -> path.moveTo(x, y)
                toIndex -> path.close()
                else -> path.lineTo(x, y)
            }
        }
        drawPath(path = path, color = Color.LightGray)
    }
}

data class Drag(
    val strength: Float = 0f,
    val index: Int = 0
)

@Composable
fun Starry(modifier: Modifier) {
    val drew = remember {
        mutableStateOf(listOf<Star>())
    }
    Canvas(modifier = modifier) {
        if (drew.value.isEmpty()) {
            val count = 40
            (0..count).map {
                val x = (0..size.width.toInt()).random()
                val y = (0..size.height.toInt()).random()
                val size = (1..6).random().dp
                Star(
                    position = Offset(x.toFloat(), y.toFloat()),
                    size = size
                )
            }.apply {
                drew.value = this
            }
        }
        drew.value.forEach {
            drawCircle(color = Color.LightGray, radius = it.size.value / 2, center = it.position)
        }
    }
}

data class Star(
    val position: Offset,
    val size: Dp
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeMoonTheme {
        Moon(
            drag = Drag(),
            modifier = Modifier
                .width(300.dp)
                .height(300.dp)
        )
    }
}