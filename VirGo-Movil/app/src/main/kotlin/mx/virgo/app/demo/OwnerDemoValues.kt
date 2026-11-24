package mx.virgo.app.demo

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource

@Composable
internal fun ownerDemoInput(
    @StringRes initial: Int,
): MutableState<String> {
    val value = stringResource(initial)
    return rememberSaveable { mutableStateOf(value) }
}

internal const val OWNER_EDITOR_STEP_COUNT = 3
internal const val OWNER_MIN_NAME_LENGTH = 3
internal const val OWNER_MIN_DESCRIPTION_LENGTH = 20
internal const val OWNER_MIN_ADDRESS_LENGTH = 5
internal const val OWNER_MIN_CAPACITY = 10
internal const val OWNER_MAX_CAPACITY = 5000
internal const val OWNER_TOUCH_TARGET = 48
internal const val OWNER_CHIP_GAP = 8
internal const val OWNER_DEMO_MONTH = 11
internal const val OWNER_DEMO_YEAR = 2026
internal const val OWNER_FIRST_BOOKING_DAY = 28
internal const val OWNER_SECOND_BOOKING_DAY = 29
internal const val OWNER_WEEK_LENGTH = 7
internal const val OWNER_PERCENT_SCALE = 100
internal const val OWNER_SHORT_PERIOD_MONTHS = 3
internal const val OWNER_FAILED_PAYMENT_TAB = 3
internal const val OWNER_MIN_REPLY_LENGTH = 3
internal val ownerDemoMonthlyVisits = listOf(42, 58, 61, 77, 90, 104)
