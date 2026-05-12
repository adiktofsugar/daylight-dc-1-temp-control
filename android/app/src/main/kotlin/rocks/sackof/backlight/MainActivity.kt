package rocks.sackof.backlight

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlin.math.roundToInt

private object Palette {
    val Bg = Color(0xFF1A1A18)
    val Card = Color(0xFF242422)
    val Border = Color(0xFF3A3A36)
    val Fg = Color(0xFFE8E4DC)
    val Dim = Color(0xFF8A8880)
    val Amber = Color(0xFFE8A040)
    val Cool = Color(0xFFB8C4D0)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
        setContent { App(onClose = { finish() }) }
    }
}

@Composable
private fun App(onClose: () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Palette.Bg,
            surface = Palette.Card,
            primary = Palette.Amber,
        )
    ) {
        Surface(
            color = Palette.Bg,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            Content(onClose)
        }
    }
}

@Composable
private fun Content(onClose: () -> Unit) {
    val max = remember { BacklightController.readState() }
    val initialTemp = run {
        val total = (max.main + max.amber).toFloat()
        if (total <= 0f) 0.3f else max.amber / total
    }
    val initialBright = run {
        val cap = max.mainMax + max.amberMax
        if (cap <= 0) 0.5f else ((max.main + max.amber).toFloat() / cap).coerceIn(0f, 1f)
    }

    var temp by remember { mutableStateOf(initialTemp) }
    var bright by remember { mutableStateOf(initialBright) }

    LaunchedEffect(Unit) {
        snapshotFlow { temp to bright }
            .conflate()
            .flowOn(Dispatchers.IO)
            .collect { (t, b) -> BacklightController.apply(t, b, max) }
    }

    Column(
        modifier = Modifier
            .background(Palette.Bg)
            .padding(20.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "Backlight",
            color = Palette.Fg,
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
        )

        LabeledSlider(
            label = "Temperature",
            value = temp,
            valueText = when {
                temp < 0.05f -> "Cool"
                temp > 0.95f -> "Amber"
                else -> "${(temp * 100).roundToInt()}%"
            },
            trackEnd = Palette.Amber,
            trackStart = Palette.Cool,
            onChange = { temp = it },
        )

        LabeledSlider(
            label = "Brightness",
            value = bright,
            valueText = "${(bright * 100).roundToInt()}%",
            trackEnd = Palette.Fg,
            trackStart = Palette.Border,
            onChange = { bright = it },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PresetChip("Off") { temp = 0f; bright = 0f }
            PresetChip("Night") { temp = 1f; bright = 0.05f }
            PresetChip("Warm") { temp = 0.7f; bright = 0.6f }
            PresetChip("Day") { temp = 0.15f; bright = 1f }
        }

        Spacer(Modifier.height(4.dp))

        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Done", color = Palette.Fg)
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueText: String,
    trackStart: Color,
    trackEnd: Color,
    onChange: (Float) -> Unit,
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label.uppercase(), color = Palette.Dim, fontSize = 11.sp)
            Text(valueText, color = Palette.Fg, fontSize = 13.sp)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            colors = SliderDefaults.colors(
                thumbColor = trackEnd,
                activeTrackColor = trackEnd,
                inactiveTrackColor = trackStart.copy(alpha = 0.35f),
            ),
        )
    }
}

@Composable
private fun RowScope.PresetChip(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Palette.Card,
            contentColor = Palette.Fg,
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.weight(1f),
    ) { Text(label, fontSize = 11.sp) }
}
