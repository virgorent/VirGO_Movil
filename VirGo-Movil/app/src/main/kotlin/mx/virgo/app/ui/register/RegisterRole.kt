package mx.virgo.app.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.theme.Slate50
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.VirGoPurple

private const val MAX_ROLE_LINES = 2

private val RoleContainerShape = RoundedCornerShape(13.dp)
private val RoleChipShape = RoundedCornerShape(10.dp)
private val RoleChipHeight = 34.dp
private val NoteShape = RoundedCornerShape(10.dp)
private val NoteAmber = Color(0xFFFFF8E9)
private val NoteAmberText = Color(0xFF9C6B1C)
private val NoteAmberBorder = Color(0xFFF3DEAE)

internal enum class RegisterRole(
    val labelRes: Int,
    val noteRes: Int?,
) {
    CLIENT(R.string.register_role_client, null),
    OWNER(R.string.register_role_owner, R.string.register_role_note_owner),
    PROVIDER(R.string.register_role_provider, R.string.register_role_note_provider),
}

@Composable
internal fun RolePicker(
    selected: RegisterRole,
    onSelected: (RegisterRole) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupLabel = stringResource(R.string.register_role_group)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics { contentDescription = groupLabel }
                .clip(RoleContainerShape)
                .background(Slate50)
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RegisterRole.entries.forEach { item ->
            RoleChip(
                label = stringResource(item.labelRes),
                selected = item == selected,
                onClick = { onSelected(item) },
            )
        }
    }
}

@Composable
private fun RowScope.RoleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .weight(1f)
                .height(RoleChipHeight)
                .clip(RoleChipShape)
                .background(if (selected) Color.White else Color.Transparent)
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onClick,
                ).padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) VirGoPurple else Slate500,
            textAlign = TextAlign.Center,
            maxLines = MAX_ROLE_LINES,
        )
    }
}

@Composable
internal fun RoleNote(
    role: RegisterRole,
    modifier: Modifier = Modifier,
) {
    val noteRes = role.noteRes ?: return
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = stringResource(noteRes),
        modifier =
            modifier
                .fillMaxWidth()
                .clip(NoteShape)
                .background(NoteAmber)
                .border(1.dp, NoteAmberBorder, NoteShape)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        color = NoteAmberText,
    )
}
