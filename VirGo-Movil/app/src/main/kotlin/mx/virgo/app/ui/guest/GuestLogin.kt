package mx.virgo.app.ui.guest

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoOutlinedButton

@Composable
internal fun GuestLogin(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VirGoOutlinedButton(onClick = onClick, modifier = modifier) {
        Text(
            text = stringResource(R.string.guest_login),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
