package mx.virgo.app.ui.guest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

private const val LARGE_FONT_SCALE = 1.3f
private val CompactWidth = 360.dp
private val CompactHeight = 700.dp
private val ScrollableMapHeight = 360.dp

@Composable
internal fun GuestHomeLayout(
    header: @Composable (Boolean) -> Unit,
    map: @Composable (Modifier) -> Unit,
    footer: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        val compact =
            maxWidth < CompactWidth ||
                maxHeight < CompactHeight ||
                LocalDensity.current.fontScale >= LARGE_FONT_SCALE
        if (compact) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(modifier = Modifier.fillMaxWidth().testTag("guest_header")) { header(true) }
                Box(
                    modifier = Modifier.fillMaxWidth().height(ScrollableMapHeight).testTag("guest_map"),
                ) {
                    map(Modifier.matchParentSize())
                }
                Box(modifier = Modifier.fillMaxWidth().testTag("guest_bottom")) { footer(true) }
            }
        } else {
            map(Modifier.fillMaxSize().testTag("guest_map"))
            Box(modifier = Modifier.align(Alignment.TopStart).testTag("guest_header")) { header(false) }
            Box(modifier = Modifier.align(Alignment.BottomCenter).testTag("guest_bottom")) { footer(false) }
        }
    }
}
