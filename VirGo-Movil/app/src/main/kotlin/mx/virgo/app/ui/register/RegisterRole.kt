package mx.virgo.app.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mx.virgo.app.R

private val RoleContainerShape = RoundedCornerShape(13.dp)
private const val LARGE_FONT_SCALE = 1.5f
private val RoleChipShape = RoundedCornerShape(10.dp)
private val RoleChipMinHeight = 48.dp
private val NoteShape = RoundedCornerShape(10.dp)
private val NoteAmber = Color(0xFFFFF8E9)
private val NoteAmberText = Color(0xFF7A520F)
private val NoteAmberBorder = Color(0xFFF3DEAE)

enum class RegisterRole(
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
    val groupModifier =
        modifier
            .fillMaxWidth()
            .semantics { contentDescription = groupLabel }
            .selectableGroup()
            .clip(RoleContainerShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    if (LocalDensity.current.fontScale >= LARGE_FONT_SCALE) {
        Column(
            modifier = groupModifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RegisterRole.entries.forEach { item ->
                RoleChip(
                    label = stringResource(item.labelRes),
                    selected = item == selected,
                    onClick = { onSelected(item) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } else {
        Row(
            modifier = groupModifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RegisterRole.entries.forEach { item ->
                RoleChip(
                    label = stringResource(item.labelRes),
                    selected = item == selected,
                    onClick = { onSelected(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RoleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .heightIn(min = RoleChipMinHeight)
                .clip(RoleChipShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                ).selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onClick,
                ).padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color =
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            textAlign = TextAlign.Center,
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
