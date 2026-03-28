package org.openedx.courses.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openedx.Lock
import org.openedx.core.domain.model.AppConfig
import org.openedx.core.domain.model.Certificate
import org.openedx.core.domain.model.CourseAssignments
import org.openedx.core.domain.model.CourseDateBlock
import org.openedx.core.domain.model.CourseDatesCalendarSync
import org.openedx.core.domain.model.CourseEnrollments
import org.openedx.core.domain.model.CourseSharingUtmParameters
import org.openedx.core.domain.model.CourseStatus
import org.openedx.core.domain.model.CoursewareAccess
import org.openedx.core.domain.model.DashboardCourseList
import org.openedx.core.domain.model.EnrolledCourse
import org.openedx.core.domain.model.EnrolledCourseData
import org.openedx.core.domain.model.Pagination
import org.openedx.core.domain.model.Progress
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OfflineModeDialog
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.brand_cream
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.brand_green_dark
import org.openedx.core.ui.theme.brand_guinda
import org.openedx.core.ui.theme.heroGradientColors
import org.openedx.core.ui.theme.ttRoundsCompressedMedium
import org.openedx.core.ui.theme.ttRoundsCompressedThinItalic
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.core.utils.TimeUtils
import org.openedx.courses.presentation.DashboardGalleryFragment.Companion.MOBILE_COURSE_LIST_ITEM_COUNT
import org.openedx.dashboard.R
import org.openedx.foundation.extension.toImageLink
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import java.util.Date
import org.openedx.core.R as CoreR

// ── Paleta interna ────────────────────────────────────────────────────────────
private val accentColors = listOf(
    Color(0xFF611232), // guinda
    Color(0xFF2B6959), // green
    Color(0xFF3D3020), // dark warm
    Color(0xFF1D4D42), // green dark
)

// ── Punto de entrada público ──────────────────────────────────────────────────
@Composable
fun DashboardGalleryView(fragmentManager: FragmentManager) {
    val windowSize = rememberWindowSize()
    val viewModel: DashboardGalleryViewModel = koinViewModel { parametersOf(windowSize) }
    val updating by viewModel.updating.collectAsState(false)
    val uiMessage by viewModel.uiMessage.collectAsState(null)
    val uiState by viewModel.uiState.collectAsState(DashboardGalleryUIState.Loading)

    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            viewModel.updateCourses(isUpdating = false)
        }
    }

    DashboardGalleryView(
        uiMessage = uiMessage,
        uiState = uiState,
        updating = updating,
        apiHostUrl = viewModel.apiHostUrl,
        userName = viewModel.userName,
        hasInternetConnection = viewModel.hasInternetConnection,
        onAction = { action ->
            when (action) {
                DashboardGalleryScreenAction.SwipeRefresh -> viewModel.updateCourses()
                DashboardGalleryScreenAction.ViewAll -> viewModel.navigateToAllEnrolledCourses(fragmentManager)
                DashboardGalleryScreenAction.Reload -> viewModel.getCourses()
                DashboardGalleryScreenAction.NavigateToDiscovery -> viewModel.navigateToDiscovery()
                DashboardGalleryScreenAction.NavigateToSettings -> viewModel.navigateToSettings(fragmentManager)
                is DashboardGalleryScreenAction.OpenCourse -> viewModel.navigateToCourseOutline(
                    fragmentManager = fragmentManager, enrolledCourse = action.enrolledCourse
                )
                is DashboardGalleryScreenAction.NavigateToDates -> viewModel.navigateToCourseOutline(
                    fragmentManager = fragmentManager, enrolledCourse = action.enrolledCourse, openDates = true
                )
                is DashboardGalleryScreenAction.OpenBlock -> viewModel.navigateToCourseOutline(
                    fragmentManager = fragmentManager,
                    enrolledCourse = action.enrolledCourse,
                    resumeBlockId = action.blockId
                )
            }
        }
    )
}

// ── Pantalla principal ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DashboardGalleryView(
    uiMessage: UIMessage?,
    uiState: DashboardGalleryUIState,
    updating: Boolean,
    apiHostUrl: String,
    userName: String,
    onAction: (DashboardGalleryScreenAction) -> Unit,
    hasInternetConnection: Boolean,
) {
    val scaffoldState = rememberScaffoldState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = updating,
        onRefresh = { onAction(DashboardGalleryScreenAction.SwipeRefresh) }
    )
    var isInternetConnectionShown by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        backgroundColor = brand_green_dark,
    ) { paddingValues ->

        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // ── Hero verde ───────────────────────────────────────────────
                val courses = (uiState as? DashboardGalleryUIState.Courses)?.userCourses
                DashboardHero(
                    userCourses = courses,
                    userName = userName,
                    onSettingsClick = { onAction(DashboardGalleryScreenAction.NavigateToSettings) },
                )

                // ── Tarjeta crema flotante ───────────────────────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-15).dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = brand_cream,
                    elevation = 0.dp,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 12.dp)
                                .size(width = 36.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFC8C3BA)),
                        )

                        when (uiState) {
                            is DashboardGalleryUIState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = brand_green)
                                }
                            }

                            is DashboardGalleryUIState.Courses -> {
                                UserCourses(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .displayCutoutForLandscape(),
                                    userCourses = uiState.userCourses,
                                    apiHostUrl = apiHostUrl,
                                    useRelativeDates = uiState.useRelativeDates,
                                    openCourse = { onAction(DashboardGalleryScreenAction.OpenCourse(it)) },
                                    navigateToDates = { onAction(DashboardGalleryScreenAction.NavigateToDates(it)) },
                                    onViewAllClick = { onAction(DashboardGalleryScreenAction.ViewAll) },
                                    resumeBlockId = { course, blockId ->
                                        onAction(DashboardGalleryScreenAction.OpenBlock(course, blockId))
                                    },
                                )
                            }

                            is DashboardGalleryUIState.Empty -> {
                                NoCoursesInfo(modifier = Modifier.fillMaxWidth())
                                FindACourseButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    findACourseClick = { onAction(DashboardGalleryScreenAction.NavigateToDiscovery) },
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = updating,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = brand_green,
            )

            if (!isInternetConnectionShown && !hasInternetConnection) {
                OfflineModeDialog(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    onDismissCLick = { isInternetConnectionShown = true },
                    onReloadClick = {
                        isInternetConnectionShown = true
                        onAction(DashboardGalleryScreenAction.SwipeRefresh)
                    },
                )
            }
        }
    }
}

// ── Hero header ───────────────────────────────────────────────────────────────
@Composable
private fun DashboardHero(
    userCourses: CourseEnrollments?,
    userName: String,
    onSettingsClick: () -> Unit,
) {
    val (totalCount, inProgressCount, notStartedCount) = remember(userCourses) {
        val all = buildList {
            userCourses?.primary?.let { add(it) }
            userCourses?.enrollments?.courses?.let { addAll(it) }
        }
        Triple(
            all.size,
            all.count { it.progress.value in 0.01f..0.99f },
            all.count { it.progress.value == 0f },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = heroGradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, 440f),
                    )
                ),
        )

        // Guinda brand stripe at the very top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(brand_guinda),
        )

        Box(
            modifier = Modifier
                .size(210.dp)
                .offset(x = 220.dp, y = (-60).dp)
                .clip(CircleShape)
                .border(34.dp, Color.White.copy(alpha = 0.06f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(110.dp)
                .offset(x = (-22).dp, y = 130.dp)
                .clip(CircleShape)
                .border(20.dp, Color.White.copy(alpha = 0.05f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .offset(x = 60.dp, y = 40.dp)
                .clip(CircleShape)
                .border(13.dp, Color.White.copy(alpha = 0.07f), CircleShape),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                        .clickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = "Bienvenido de vuelta",
                        style = TextStyle(
                            fontFamily = ttRoundsCompressedThinItalic,
                            fontWeight = FontWeight.Thin,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            letterSpacing = 0.3.sp,
                        ),
                    )
                    Text(
                        text = if (userName.isNotEmpty()) "¡Hola, $userName!" else "¡Hola!",
                        style = TextStyle(
                            fontFamily = ttRoundsCompressedMedium,
                            fontWeight = FontWeight.Medium,
                            fontSize = 30.sp,
                            color = Color.White,
                            letterSpacing = (-0.3).sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Continúa donde lo dejaste",
                        style = TextStyle(
                            fontFamily = ttRoundsFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.58f),
                        ),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (totalCount > 0) {
                    StatPill(
                        label = "$totalCount cursos",
                        dotColor = Color(0xFF9ADBC8),
                    )
                }
                if (inProgressCount > 0) {
                    StatPill(
                        label = "$inProgressCount en progreso",
                        dotColor = Color(0xFFF0A0B0),
                    )
                }
                if (notStartedCount > 0) {
                    StatPill(
                        label = "$notStartedCount por iniciar",
                        dotColor = Color(0xFFF5DC80),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, dotColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White.copy(alpha = 0.11f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Text(
            text = label,
            style = TextStyle(
                fontFamily = ttRoundsFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.88f),
                letterSpacing = 0.2.sp,
            ),
        )
    }
}

// ── Encabezado de sección ─────────────────────────────────────────────────────
@Composable
private fun SectionHeader(
    title: String,
    linkText: String? = null,
    onLinkClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 22.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = ttRoundsCompressedMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = Color(0xFF1C1B18),
                letterSpacing = (-0.2).sp,
            ),
        )
        if (linkText != null && onLinkClick != null) {
            Row(
                modifier = Modifier.clickable(onClick = onLinkClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = linkText,
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = brand_green,
                    ),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = brand_green,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ── Contenido de cursos (primario + secundarios) ──────────────────────────────
@Composable
private fun UserCourses(
    modifier: Modifier = Modifier,
    userCourses: CourseEnrollments,
    apiHostUrl: String,
    useRelativeDates: Boolean,
    openCourse: (EnrolledCourse) -> Unit,
    navigateToDates: (EnrolledCourse) -> Unit,
    onViewAllClick: () -> Unit,
    resumeBlockId: (enrolledCourse: EnrolledCourse, blockId: String) -> Unit,
) {
    Column(modifier = modifier) {
        userCourses.primary?.let { primary ->
            SectionHeader(title = "Continuar aprendiendo")
            PrimaryCourseCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                course = primary,
                apiHostUrl = apiHostUrl,
                useRelativeDates = useRelativeDates,
                navigateToDates = navigateToDates,
                resumeBlockId = resumeBlockId,
                openCourse = openCourse,
            )
        }

        val secondary = userCourses.enrollments.courses.take(MOBILE_COURSE_LIST_ITEM_COUNT)
        if (secondary.isNotEmpty()) {
            val totalCount = secondary.size + (if (userCourses.primary != null) 1 else 0)
            SectionHeader(
                title = "Mis cursos",
                linkText = "Ver todos ($totalCount)",
                onLinkClick = onViewAllClick,
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(secondary) { index, course ->
                    CourseCarouselCard(
                        course = course,
                        apiHostUrl = apiHostUrl,
                        accentColor = accentColors[index % accentColors.size],
                        onClick = { openCourse(course) },
                    )
                }
            }
        }
    }
}

// ── Tarjeta curso primario ────────────────────────────────────────────────────
@Composable
private fun PrimaryCourseCard(
    modifier: Modifier = Modifier,
    course: EnrolledCourse,
    apiHostUrl: String,
    useRelativeDates: Boolean,
    navigateToDates: (EnrolledCourse) -> Unit,
    resumeBlockId: (enrolledCourse: EnrolledCourse, blockId: String) -> Unit,
    openCourse: (EnrolledCourse) -> Unit,
) {
    val context = LocalContext.current
    val progressValue = course.progress.value
    val hasPastAssignment = !course.courseAssignments?.pastAssignments.isNullOrEmpty()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        elevation = 4.dp,
    ) {
        Column {
            // ── ACCENT BLOCK: thumbnail + info ───────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(course.course.courseImage.toImageLink(apiHostUrl))
                        .error(CoreR.drawable.core_no_image_course)
                        .placeholder(CoreR.drawable.core_no_image_course)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 0.dp)),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = course.course.org,
                        style = TextStyle(
                            fontFamily = ttRoundsFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp,
                            color = Color(0xFF9A9590),
                            letterSpacing = 0.3.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = course.course.name,
                        style = TextStyle(
                            fontFamily = ttRoundsCompressedMedium,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF1C1B18),
                            letterSpacing = (-0.2).sp,
                            lineHeight = 18.sp,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = buildString {
                            append(if (course.course.isSelfPaced) "A tu ritmo" else "Con fechas")
                            append("  ·  ")
                            append(
                                TimeUtils.getCourseFormattedDate(
                                    context, Date(),
                                    course.auditAccessExpires,
                                    course.course.start, course.course.end,
                                    course.course.startType, course.course.startDisplay,
                                )
                            )
                        },
                        style = TextStyle(
                            fontFamily = ttRoundsFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            color = Color(0xFF9A9590),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // ── Franja inferior: progreso + botón ────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF0EDE8)),
            )

            if (hasPastAssignment) {
                val pastAssignments = course.courseAssignments!!.pastAssignments!!
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(brand_guinda.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable {
                            if (pastAssignments.size == 1) {
                                resumeBlockId(course, pastAssignments.first().blockId)
                            } else {
                                navigateToDates(course)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(brand_guinda),
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.dashboard_past_due_assignment,
                            pastAssignments.size,
                            pastAssignments.size,
                        ),
                        style = TextStyle(
                            fontFamily = ttRoundsFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = brand_guinda,
                        ),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 10.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFE5E0D8)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressValue)
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(brand_green, Color(0xFF5BB89A)),
                                )
                            ),
                    )
                }
                Text(
                    text = "${(progressValue * 100).toInt()}%",
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF5A5650),
                    ),
                )

                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(brand_green)
                        .clickable {
                            if (course.courseStatus == null) {
                                openCourse(course)
                            } else {
                                resumeBlockId(
                                    course,
                                    course.courseStatus?.lastVisitedBlockId ?: "",
                                )
                            }
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = if (course.courseStatus == null) {
                                stringResource(R.string.dashboard_start_course)
                            } else {
                                stringResource(R.string.dashboard_resume_course)
                            },
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = Color.White,
                            ),
                        )
                    }
                }
            }
        }
    }
}

// ── Tarjeta pequeña del carrusel ──────────────────────────────────────────────
@Composable
private fun CourseCarouselCard(
    course: EnrolledCourse,
    apiHostUrl: String,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val progressValue = course.progress.value
    val statusText = when {
        progressValue >= 1f -> "Completado"
        progressValue > 0f -> "En progreso"
        else -> "Por iniciar"
    }
    val statusColor = when {
        progressValue >= 1f -> brand_green
        progressValue > 0f -> Color(0xFF5A5650)
        else -> Color(0xFF9A9590)
    }

    Surface(
        modifier = Modifier
            .width(144.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        elevation = 2.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(accentColor),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(900f / 992f),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(course.course.courseImage.toImageLink(apiHostUrl))
                        .error(CoreR.drawable.core_no_image_course)
                        .placeholder(CoreR.drawable.core_no_image_course)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                ProgressRing(
                    progress = progressValue,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .padding(end = 8.dp, bottom = 8.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(top = 10.dp, bottom = 12.dp),
            ) {
                Text(
                    text = course.course.name,
                    style = TextStyle(
                        fontFamily = ttRoundsCompressedMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color(0xFF1C1B18),
                        lineHeight = 17.sp,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = statusText,
                    style = TextStyle(
                        fontFamily = ttRoundsCompressedThinItalic,
                        fontWeight = if (progressValue >= 1f) FontWeight.Medium else FontWeight.Thin,
                        fontStyle = if (progressValue >= 1f) FontStyle.Normal else FontStyle.Italic,
                        fontSize = 11.sp,
                        color = statusColor,
                    ),
                )
            }
        }
    }
}

// ── Ring de progreso (Canvas) ─────────────────────────────────────────────────
@Composable
private fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(32.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.5.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)
            val circumference = Math.PI.toFloat() * diameter
            val sweepAngle = 360f * progress

            // Arco de fondo
            drawArc(
                color = Color.White.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Arco de progreso
            if (progress > 0f) {
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        Text(
            text = "${(progress * 100).toInt()}%",
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                fontFamily = ttRoundsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 7.sp,
                color = Color.White,
            ),
        )
    }
}

// ── Estado vacío ──────────────────────────────────────────────────────────────
@Composable
private fun NoCoursesInfo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(brand_green.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = CoreR.drawable.core_ic_book),
                tint = brand_green,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier
                .testTag("txt_empty_state_title")
                .fillMaxWidth(),
            text = stringResource(id = R.string.dashboard_all_courses_empty_title),
            style = TextStyle(
                fontFamily = ttRoundsCompressedMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = Color(0xFF1C1B18),
                letterSpacing = (-0.2).sp,
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier
                .testTag("txt_empty_state_description")
                .fillMaxWidth(),
            text = stringResource(id = R.string.dashboard_all_courses_empty_description),
            style = TextStyle(
                fontFamily = ttRoundsFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color(0xFF5A5650),
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FindACourseButton(
    modifier: Modifier = Modifier,
    findACourseClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(brand_green)
            .clickable(onClick = findACourseClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.dashboard_find_a_course),
            style = TextStyle(
                fontFamily = ttRoundsFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.White,
            ),
        )
    }
}

// ── Preview data ──────────────────────────────────────────────────────────────
private val mockCourse = EnrolledCourse(
    auditAccessExpires = Date(),
    created = "created",
    certificate = Certificate(""),
    mode = "mode",
    isActive = true,
    progress = Progress(35, 100),
    courseStatus = CourseStatus("", emptyList(), "", "Unidad: Aprendizaje colaborativo"),
    courseAssignments = CourseAssignments(
        pastAssignments = listOf(
            CourseDateBlock(
                title = "Tarea 1",
                description = "",
                date = TimeUtils.iso8601ToDate("2024-11-30T15:00:00Z")!!,
                assignmentType = "Homework",
            )
        ),
        futureAssignments = emptyList(),
    ),
    course = EnrolledCourseData(
        id = "id",
        name = "El aprendizaje basado en proyectos: metodología para educar en ciudadanía global",
        number = "",
        org = "OEI · Iberoamérica",
        start = Date(),
        startDisplay = "",
        startType = "",
        end = Date(),
        dynamicUpgradeDeadline = "",
        subscriptionId = "",
        coursewareAccess = CoursewareAccess(true, "", "", "", "", ""),
        media = null,
        courseImage = "",
        courseAbout = "",
        courseSharingUtmParameters = CourseSharingUtmParameters("", ""),
        courseUpdates = "",
        courseHandouts = "",
        discussionUrl = "",
        videoOutline = "",
        isSelfPaced = true,
    )
)

private val mockCourse2 = mockCourse.copy(
    progress = Progress(25, 100),
    course = mockCourse.course.copy(name = "Búsqueda en Internet para Universidad", org = "UNAM"),
)
private val mockCourse3 = mockCourse.copy(
    progress = Progress(80, 100),
    course = mockCourse.course.copy(name = "Alimentación saludable y sostenible", org = "INSP"),
)
private val mockCourse4 = mockCourse.copy(
    progress = Progress(0, 100),
    courseStatus = null,
    course = mockCourse.course.copy(name = "Ciberseguridad para todos", org = "CERT-MX"),
)

private val mockUserCourses = CourseEnrollments(
    enrollments = DashboardCourseList(
        pagination = Pagination(10, "", 4, "1"),
        courses = listOf(mockCourse2, mockCourse3, mockCourse4),
    ),
    configs = AppConfig(CourseDatesCalendarSync(true, true, true, true)),
    primary = mockCourse,
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Composable
private fun DashboardGalleryViewPreview() {
    OpenEdXTheme {
        DashboardGalleryView(
            uiState = DashboardGalleryUIState.Courses(mockUserCourses, true),
            apiHostUrl = "",
            userName = "Diego",
            uiMessage = null,
            updating = false,
            hasInternetConnection = true,
            onAction = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
@Composable
private fun DashboardEmptyPreview() {
    OpenEdXTheme {
        DashboardGalleryView(
            uiState = DashboardGalleryUIState.Empty,
            apiHostUrl = "",
            userName = "Diego",
            uiMessage = null,
            updating = false,
            hasInternetConnection = true,
            onAction = {},
        )
    }
}
