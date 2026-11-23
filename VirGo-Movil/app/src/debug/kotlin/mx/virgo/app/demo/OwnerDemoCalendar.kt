package mx.virgo.app.demo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import java.time.YearMonth
import mx.virgo.app.R.string as S

@Composable
internal fun OwnerCalendar(notice: (String) -> Unit) {
    var month by rememberSaveable { mutableStateOf(OWNER_DEMO_MONTH) }
    var year by rememberSaveable { mutableStateOf(OWNER_DEMO_YEAR) }
    var day by rememberSaveable { mutableStateOf(OWNER_FIRST_BOOKING_DAY) }
    var blocked by rememberSaveable { mutableStateOf("") }
    val current = YearMonth.of(year, month)
    val key = "$year-$month-$day"
    val reserved =
        month == OWNER_DEMO_MONTH &&
            year == OWNER_DEMO_YEAR &&
            day in listOf(OWNER_FIRST_BOOKING_DAY, OWNER_SECOND_BOOKING_DAY)
    val isBlocked = "|$key|" in blocked
    val months = stringArrayResource(R.array.demo_owner_months)
    val changed = stringResource(S.demo_owner_calendar_changed)
    DemoPage(stringResource(S.demo_owner_calendar), stringResource(S.demo_owner_venue)) {
        DemoNotice(stringResource(S.demo_owner_calendar_hint))
        Column {
            DemoAction(stringResource(S.demo_owner_previous)) {
                val previous = current.minusMonths(1)
                month = previous.monthValue
                year = previous.year
                day = 1
            }
            DemoAction(stringResource(S.demo_owner_next)) {
                val next = current.plusMonths(1)
                month = next.monthValue
                year = next.year
                day = 1
            }
        }
        DemoSection(stringResource(S.demo_owner_month_title, months[month - 1], year))
        OwnerMonthGrid(current, day) { day = it }
        DemoSection(stringResource(S.demo_owner_selected, day, months[month - 1]))
        if (reserved) {
            val client = if (day == OWNER_FIRST_BOOKING_DAY) S.demo_owner_client else S.demo_owner_client_two
            DemoCard(stringResource(client), stringResource(S.demo_owner_confirmed_demo))
        } else {
            DemoNotice(stringResource(if (isBlocked) S.demo_owner_blocked else S.demo_owner_free))
            DemoAction(stringResource(if (isBlocked) S.demo_owner_unblock else S.demo_owner_block)) {
                blocked = if (isBlocked) blocked.replace("|$key|", "") else "$blocked|$key|"
                notice(changed)
            }
        }
    }
}

@Composable
private fun OwnerMonthGrid(
    current: YearMonth,
    day: Int,
    select: (Int) -> Unit,
) {
    val weekdays = stringArrayResource(R.array.demo_owner_weekdays)
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row { weekdays.forEach { Text(it, Modifier.width(OWNER_TOUCH_TARGET.dp)) } }
        val offset = current.atDay(1).dayOfWeek.value % OWNER_WEEK_LENGTH
        val weeks = (offset + current.lengthOfMonth() + OWNER_WEEK_LENGTH - 1) / OWNER_WEEK_LENGTH
        for (week in 0 until weeks) {
            Row {
                for (column in 0 until OWNER_WEEK_LENGTH) {
                    val number = week * OWNER_WEEK_LENGTH + column - offset + 1
                    if (number in 1..current.lengthOfMonth()) {
                        FilterChip(
                            selected = day == number,
                            onClick = { select(number) },
                            label = { Text(stringResource(S.demo_owner_number, number)) },
                            modifier =
                                Modifier.sizeIn(
                                    minWidth = OWNER_TOUCH_TARGET.dp,
                                    minHeight = OWNER_TOUCH_TARGET.dp,
                                ),
                        )
                    } else {
                        Surface(
                            Modifier.sizeIn(minWidth = OWNER_TOUCH_TARGET.dp, minHeight = OWNER_TOUCH_TARGET.dp),
                        ) {}
                    }
                }
            }
        }
    }
}
