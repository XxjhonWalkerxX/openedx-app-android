package org.openedx.course.presentation.dates

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentManager
import org.openedx.core.NoContentScreenType
import org.openedx.core.data.model.DateType
import org.openedx.core.domain.model.CourseDateBlock
import org.openedx.core.domain.model.CourseDatesBannerInfo
import org.openedx.core.domain.model.CourseDatesResult
import org.openedx.core.domain.model.DatesSection
import org.openedx.core.presentation.CoreAnalyticsScreen
import org.openedx.core.presentation.dialog.alert.ActionDialogFragment
import org.openedx.core.presentation.settings.calendarsync.CalendarSyncState
import org.openedx.core.ui.CircularProgress
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.NoContentScreen
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.brand_cream_strong
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.brand_guinda
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.core.utils.TimeUtils
import org.openedx.core.utils.TimeUtils.formatToString
import org.openedx.core.utils.clearTime
import org.openedx.course.presentation.ui.CourseDatesBanner
import org.openedx.course.presentation.ui.CourseDatesBannerTablet
import org.openedx.course.presentation.unit.container.CourseViewMode
import org.openedx.foundation.extension.isNotEmptyThenLet
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.openedx.core.R as CoreR

// ─── Color helpers ───────────────────────────────────────────────
private fun sectionChipColor(section: DatesSection): Color = when (section) {
    DatesSection.TODAY      -> brand_green
    DatesSection.THIS_WEEK  -> Color(0xFF4A8E72)
    DatesSection.NEXT_WEEK  -> Color(0xFF7B9E87)
    DatesSection.UPCOMING   -> brand_guinda
    DatesSection.PAST_DUE   -> Color(0xFFC0392B)
    DatesSection.COMPLETED  -> Color(0xFF999999)
    else                    -> brand_green
}

@Composable
fun CourseDatesScreen(
    windowSize: WindowSize,
    viewModel: CourseDatesViewModel,
    fragmentManager: FragmentManager,
    isFragmentResumed: Boolean,
    updateCourseStructure: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState(CourseDatesUIState.Loading)
    val uiMessage by viewModel.uiMessage.collectAsState(null)
    val context = LocalContext.current

    CourseDatesUI(
        windowSize = windowSize,
        uiState = uiState,
        uiMessage = uiMessage,
        isSelfPaced = viewModel.isSelfPaced,
        useRelativeDates = viewModel.useRelativeDates,
        onItemClick = { block ->
            if (block.blockId.isNotEmpty()) {
                viewModel.getVerticalBlock(block.blockId)
                    ?.let { verticalBlock ->
                        viewModel.logCourseComponentTapped(true, block)
                        if (viewModel.isCourseExpandableSectionsEnabled) {
                            viewModel.courseRouter.navigateToCourseContainer(
                                fm = fragmentManager,
                                courseId = viewModel.courseId,
                                unitId = verticalBlock.id,
                                componentId = "",
                                mode = CourseViewMode.FULL
                            )
                        } else {
                            viewModel.getSequentialBlock(verticalBlock.id)
                                ?.let { sequentialBlock ->
                                    viewModel.courseRouter.navigateToCourseSubsections(
                                        fm = fragmentManager,
                                        subSectionId = sequentialBlock.id,
                                        courseId = viewModel.courseId,
                                        unitId = verticalBlock.id,
                                        mode = CourseViewMode.FULL
                                    )
                                }
                        }
                    } ?: {
                    viewModel.logCourseComponentTapped(false, block)
                    ActionDialogFragment.newInstance(
                        title = context.getString(CoreR.string.core_leaving_the_app),
                        message = context.getString(
                            CoreR.string.core_leaving_the_app_message,
                            context.getString(CoreR.string.platform_name)
                        ),
                        url = block.link,
                        source = CoreAnalyticsScreen.COURSE_DATES.screenName
                    ).show(
                        fragmentManager,
                        ActionDialogFragment::class.simpleName
                    )
                }
            }
        },
        onPLSBannerViewed = {
            if (isFragmentResumed) {
                viewModel.logPlsBannerViewed()
            }
        },
        onSyncDates = {
            viewModel.logPlsShiftButtonClicked()
            viewModel.resetCourseDatesBanner {
                viewModel.logPlsShiftDates(it)
                if (it) {
                    updateCourseStructure()
                }
            }
        },
        onCalendarSyncStateClick = {
            viewModel.calendarRouter.navigateToCalendarSettings(fragmentManager)
        }
    )
}

@Composable
private fun CourseDatesUI(
    windowSize: WindowSize,
    uiState: CourseDatesUIState,
    uiMessage: UIMessage?,
    isSelfPaced: Boolean,
    useRelativeDates: Boolean,
    onItemClick: (CourseDateBlock) -> Unit,
    onPLSBannerViewed: () -> Unit,
    onSyncDates: () -> Unit,
    onCalendarSyncStateClick: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.appColors.background
    ) {
        val modifierScreenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        val listBottomPadding by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = PaddingValues(bottom = 24.dp),
                    compact = PaddingValues(bottom = 24.dp)
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        val isPLSBannerAvailable = (uiState as? CourseDatesUIState.CourseDates)
            ?.courseDatesResult
            ?.courseBanner
            ?.isBannerAvailableForUserType(isSelfPaced)

        LaunchedEffect(key1 = isPLSBannerAvailable) {
            if (isPLSBannerAvailable == true) {
                onPLSBannerViewed()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .displayCutoutForLandscape(),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = modifierScreenWidth,
                color = MaterialTheme.appColors.background,
            ) {
                Box(Modifier.fillMaxWidth()) {
                    when (uiState) {
                        is CourseDatesUIState.CourseDates -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = listBottomPadding,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                val courseBanner = uiState.courseDatesResult.courseBanner
                                val datesSection = uiState.courseDatesResult.datesSection

                                if (courseBanner.isBannerAvailableForUserType(isSelfPaced)) {
                                    item {
                                        if (windowSize.isTablet) {
                                            CourseDatesBannerTablet(
                                                modifier = Modifier.padding(top = 16.dp),
                                                banner = courseBanner,
                                                resetDates = onSyncDates,
                                            )
                                        } else {
                                            CourseDatesBanner(
                                                modifier = Modifier.padding(top = 16.dp),
                                                banner = courseBanner,
                                                resetDates = onSyncDates
                                            )
                                        }
                                    }
                                }

                                // Calendar sync card
                                item {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    CalendarSyncCard(
                                        calendarSyncState = uiState.calendarSyncState,
                                        onClick = onCalendarSyncStateClick,
                                    )
                                }

                                // Completed section (expandable)
                                datesSection[DatesSection.COMPLETED]?.isNotEmptyThenLet { section ->
                                    item {
                                        ExpandableView(
                                            sectionKey = DatesSection.COMPLETED,
                                            sectionDates = section,
                                            onItemClick = onItemClick,
                                            useRelativeDates = useRelativeDates
                                        )
                                    }
                                }

                                // Other sections
                                val sectionsKey =
                                    datesSection.keys.minus(DatesSection.COMPLETED).toList()
                                sectionsKey.forEach { sectionKey ->
                                    datesSection[sectionKey]?.isNotEmptyThenLet { section ->
                                        item {
                                            CourseDateBlockSection(
                                                sectionKey = sectionKey,
                                                sectionDates = section,
                                                onItemClick = onItemClick,
                                                useRelativeDates = useRelativeDates
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        CourseDatesUIState.Error -> {
                            NoContentScreen(noContentScreenType = NoContentScreenType.COURSE_DATES)
                        }

                        CourseDatesUIState.Loading -> {
                            CircularProgress()
                        }
                    }
                }
            }
        }
    }
}

// ─── Calendar sync card ──────────────────────────────────────────
@Composable
private fun CalendarSyncCard(
    calendarSyncState: CalendarSyncState,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = 2.dp,
        backgroundColor = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon wrap
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(brand_cream_strong),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = calendarSyncState.icon,
                    tint = calendarSyncState.tint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(calendarSyncState.longTitle),
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                    ),
                    color = Color(0xFF19212F),
                )
                Text(
                    text = "Toca para sincronizar fechas",
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                    ),
                    color = Color(0xFF888888),
                )
            }
            // Status pill
            val (pillBg, pillText) = when (calendarSyncState) {
                CalendarSyncState.SYNCED ->
                    brand_green.copy(alpha = 0.12f) to brand_green
                else ->
                    Color(0xFF888888).copy(alpha = 0.12f) to Color(0xFF888888)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(pillBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = if (calendarSyncState == CalendarSyncState.SYNCED) "Sync" else "Offline",
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                    ),
                    color = pillText,
                )
            }
        }
    }
}

// ─── Section divider header ───────────────────────────────────────
@Composable
private fun DatesSectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(brand_guinda.copy(alpha = 0.22f))
        )
        Text(
            text = title.uppercase(),
            color = brand_guinda,
            style = TextStyle(
                fontFamily = ttRoundsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                letterSpacing = 1.4.sp,
            ),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(brand_guinda.copy(alpha = 0.22f))
        )
    }
}

// ─── Expandable "Completed" card ─────────────────────────────────
@Composable
fun ExpandableView(
    sectionKey: DatesSection = DatesSection.NONE,
    useRelativeDates: Boolean,
    sectionDates: List<CourseDateBlock>,
    onItemClick: (CourseDateBlock) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = expanded, label = "expandable")
    val iconRotationDeg by transition.animateFloat(label = "icon rotation") { if (it) 180f else 0f }
    val enterTransition = remember {
        expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(initialAlpha = 0.3f, animationSpec = tween(durationMillis = 300))
    }
    val exitTransition = remember {
        shrinkVertically(
            shrinkTowards = Alignment.Top,
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = 0.dp,
        backgroundColor = brand_cream_strong,
        border = BorderStroke(1.5.dp, brand_green.copy(alpha = 0.20f)),
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(brand_green.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = brand_green,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = sectionKey.stringResId),
                        style = TextStyle(
                            fontFamily = ttRoundsFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = Color(0xFF19212F),
                    )
                    AnimatedVisibility(visible = !expanded) {
                        Text(
                            text = pluralStringResource(
                                id = CoreR.plurals.core_date_items_hidden,
                                count = sectionDates.size,
                                formatArgs = arrayOf(sectionDates.size)
                            ),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                            ),
                            color = Color(0xFF888888),
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    tint = brand_green,
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(iconRotationDeg),
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = enterTransition,
                exit = exitTransition,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                ) {
                    Divider(color = brand_green.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(8.dp))
                    CourseDateBlockSection(
                        sectionKey = sectionKey,
                        sectionDates = sectionDates,
                        onItemClick = onItemClick,
                        useRelativeDates = useRelativeDates,
                    )
                }
            }
        }
    }
}

// ─── Section wrapper ──────────────────────────────────────────────
@Composable
private fun CourseDateBlockSection(
    sectionKey: DatesSection = DatesSection.NONE,
    useRelativeDates: Boolean,
    sectionDates: List<CourseDateBlock>,
    onItemClick: (CourseDateBlock) -> Unit,
) {
    val chipColor = sectionChipColor(sectionKey)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (sectionKey != DatesSection.COMPLETED) {
            DatesSectionHeader(title = stringResource(id = sectionKey.stringResId))
        }

        var lastDate = sectionDates.first().date.clearTime()
        sectionDates.forEachIndexed { index, dateBlock ->
            val canShowDate = index == 0 || lastDate != dateBlock.date
            DateItemCard(
                dateBlock = dateBlock,
                chipColor = chipColor,
                canShowDate = canShowDate,
                useRelativeDates = useRelativeDates,
                onItemClick = onItemClick,
            )
            lastDate = dateBlock.date
        }
    }
}

// ─── Date item card ───────────────────────────────────────────────
@Composable
private fun DateItemCard(
    dateBlock: CourseDateBlock,
    chipColor: Color,
    canShowDate: Boolean,
    useRelativeDates: Boolean,
    onItemClick: (CourseDateBlock) -> Unit,
) {
    val context = LocalContext.current

    // Parse day + month abbreviation
    val calendar = remember(dateBlock.date) {
        Calendar.getInstance().apply { time = dateBlock.date }
    }
    val dayNum = calendar.get(Calendar.DAY_OF_MONTH).toString()
    val monthAbbr = remember(dateBlock.date) {
        SimpleDateFormat("MMM", Locale.forLanguageTag("es"))
            .format(dateBlock.date)
            .uppercase()
    }

    val isClickable = dateBlock.blockId.isNotEmpty() && dateBlock.learnerHasAccess

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable) { onItemClick(dateBlock) },
        shape = RoundedCornerShape(14.dp),
        elevation = 2.dp,
        backgroundColor = Color.White,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left date chip
            Column(
                modifier = Modifier
                    .width(52.dp)
                    .background(
                        chipColor,
                        RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = dayNum,
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                    ),
                    color = Color.White,
                )
                Text(
                    text = monthAbbr,
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp,
                    ),
                    color = Color.White.copy(alpha = 0.75f),
                )
            }

            // Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                // Icon + title row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    val lockOrIcon = if (!dateBlock.learnerHasAccess) {
                        CoreR.drawable.core_ic_lock
                    } else {
                        dateBlock.dateType.drawableResId
                    }
                    lockOrIcon?.let { iconRes ->
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = chipColor,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                    Text(
                        text = if (!dateBlock.assignmentType.isNullOrEmpty()) {
                            "${dateBlock.assignmentType}: ${dateBlock.title}"
                        } else {
                            dateBlock.title
                        },
                        style = TextStyle(
                            fontFamily = ttRoundsFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                        ),
                        color = Color(0xFF19212F),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (dateBlock.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = dateBlock.description,
                        style = TextStyle(
                            fontFamily = ttRoundsFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                        ),
                        color = Color(0xFF888888),
                    )
                }
            }

            // Arrow if clickable
            if (isClickable) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFCCCCCC),
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(20.dp),
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyCourseDatesScreenPreview() {
    OpenEdXTheme {
        CourseDatesUI(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            uiState = CourseDatesUIState.Error,
            uiMessage = null,
            isSelfPaced = true,
            useRelativeDates = true,
            onItemClick = {},
            onPLSBannerViewed = {},
            onSyncDates = {},
            onCalendarSyncStateClick = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CourseDatesScreenPreview() {
    OpenEdXTheme {
        CourseDatesUI(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            uiState = CourseDatesUIState.CourseDates(
                CourseDatesResult(mockedResponse, mockedCourseBannerInfo),
                CalendarSyncState.SYNCED
            ),
            uiMessage = null,
            isSelfPaced = true,
            useRelativeDates = true,
            onItemClick = {},
            onPLSBannerViewed = {},
            onSyncDates = {},
            onCalendarSyncStateClick = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_9)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.NEXUS_9)
@Composable
private fun CourseDatesScreenTabletPreview() {
    OpenEdXTheme {
        CourseDatesUI(
            windowSize = WindowSize(WindowType.Medium, WindowType.Medium),
            uiState = CourseDatesUIState.CourseDates(
                CourseDatesResult(mockedResponse, mockedCourseBannerInfo),
                CalendarSyncState.SYNCED
            ),
            uiMessage = null,
            isSelfPaced = true,
            useRelativeDates = true,
            onItemClick = {},
            onPLSBannerViewed = {},
            onSyncDates = {},
            onCalendarSyncStateClick = {},
        )
    }
}

val mockedCourseBannerInfo = CourseDatesBannerInfo(
    missedDeadlines = true,
    missedGatedContent = false,
    verifiedUpgradeLink = "",
    contentTypeGatingEnabled = false,
    hasEnded = false,
)

private val mockedResponse: LinkedHashMap<DatesSection, List<CourseDateBlock>> =
    linkedMapOf(
        Pair(
            DatesSection.COMPLETED,
            listOf(
                CourseDateBlock(
                    title = "Homework 1: ABCD",
                    description = "After this date, course content will be archived",
                    date = TimeUtils.iso8601ToDate("2023-10-20T15:08:07Z")!!,
                )
            )
        ),

        Pair(
            DatesSection.PAST_DUE,
            listOf(
                CourseDateBlock(
                    title = "Homework 1: ABCD",
                    description = "After this date, course content will be archived",
                    date = Date(),
                    dateType = DateType.ASSIGNMENT_DUE_DATE,
                )
            )
        ),

        Pair(
            DatesSection.TODAY,
            listOf(
                CourseDateBlock(
                    title = "Homework 2: ABCD",
                    description = "After this date, course content will be archived",
                    date = TimeUtils.iso8601ToDate("2023-10-21T15:08:07Z")!!,
                )
            )
        ),

        Pair(
            DatesSection.THIS_WEEK,
            listOf(
                CourseDateBlock(
                    title = "Assignment Due: ABCD",
                    description = "After this date, course content will be archived",
                    date = TimeUtils.iso8601ToDate("2023-10-22T15:08:07Z")!!,
                    dateType = DateType.ASSIGNMENT_DUE_DATE,
                ),
                CourseDateBlock(
                    title = "Assignment Due",
                    description = "After this date, course content will be archived",
                    date = TimeUtils.iso8601ToDate("2023-10-23T15:08:07Z")!!,
                    dateType = DateType.ASSIGNMENT_DUE_DATE,
                ),
                CourseDateBlock(
                    title = "Surprise Assignment",
                    description = "After this date, course content will be archived",
                    date = TimeUtils.iso8601ToDate("2023-10-24T15:08:07Z")!!,
                )
            )
        ),

        Pair(
            DatesSection.NEXT_WEEK,
            listOf(
                CourseDateBlock(
                    title = "Homework 5: ABCD",
                    description = "After this date, course content will be archived",
                    date = TimeUtils.iso8601ToDate("2023-10-25T15:08:07Z")!!,
                )
            )
        ),

        Pair(
            DatesSection.UPCOMING,
            listOf(
                CourseDateBlock(
                    title = "Last Assignment",
                    description = "After this date, course content will be archived",
                    date = TimeUtils.iso8601ToDate("2023-10-26T15:08:07Z")!!,
                    assignmentType = "Module 1",
                    dateType = DateType.VERIFICATION_DEADLINE_DATE,
                )
            )
        )
    )
