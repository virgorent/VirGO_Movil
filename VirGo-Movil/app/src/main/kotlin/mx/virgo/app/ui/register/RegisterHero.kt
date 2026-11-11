package mx.virgo.app.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.virgo.app.R
import mx.virgo.app.ui.guest.VenueSample
import mx.virgo.app.ui.guest.kmText
import mx.virgo.app.ui.guest.ratingText
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurpleDark

private val HeroHeight = 260.dp
private val HeroPadding = 20.dp
private val HeroDecorSize = 160.dp
private val BackButtonSize = 40.dp
private const val HERO_DECOR_ALPHA = 0.10f
private const val BACK_BUTTON_ALPHA = 0.85f
private val HeroKickerColor = Color(0xFFC9CAFF)

@Composable
internal fun RegisterHero(
    venue: VenueSample?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(HeroHeight)
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(VirGoPurpleDark, Slate900),
                        ),
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = HeroPadding, end = HeroPadding)
                    .size(HeroDecorSize)
                    .background(
                        color = Color.White.copy(alpha = HERO_DECOR_ALPHA),
                        shape = CircleShape,
                    ),
        )
        IconButton(
            onClick = onBackClick,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = HeroPadding, top = HeroPadding)
                    .size(BackButtonSize)
                    .background(
                        color = Color.White.copy(alpha = BACK_BUTTON_ALPHA),
                        shape = CircleShape,
                    ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = Slate900,
            )
        }
        RegisterHeroCopy(
            venue = venue,
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = HeroPadding, end = HeroPadding, bottom = HeroPadding),
        )
    }
}

@Composable
private fun RegisterHeroCopy(
    venue: VenueSample?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (venue != null) {
            HeroKicker(text = stringResource(R.string.register_step_label))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = venue.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.register_hero_meta, venue.ratingText(), venue.kmText()),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        } else {
            HeroKicker(text = stringResource(R.string.register_kicker))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.register_hero_title),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun HeroKicker(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style =
            MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            ),
        color = HeroKickerColor,
    )
}
