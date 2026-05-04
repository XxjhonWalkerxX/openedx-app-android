package org.openedx.learn.presentation.brand

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.domain.model.CourseEnrollments
import org.openedx.core.domain.model.EnrolledCourse
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OfflineModeDialog
import org.openedx.core.ui.brand.components.BrandSectionHeader
import org.openedx.core.ui.brand.components.BrandStatTile
import org.openedx.core.ui.brand.components.DecorativeRings
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand.BrandShapes
import org.openedx.core.ui.theme.brand.BrandSpacing
import org.openedx.core.ui.theme.brand.brand
import org.openedx.core.ui.theme.brand.rememberBrandHaptics
import org.openedx.courses.presentation.DashboardGalleryFragment.Companion.MOBILE_COURSE_LIST_ITEM_COUNT
import org.openedx.courses.presentation.DashboardGalleryScreenAction
import org.openedx.courses.presentation.DashboardGalleryUIState
import org.openedx.courses.presentation.DashboardGalleryViewModel
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.rememberWindowSize

/**
 * Dashboard rediseñado @prende.mx — paridad iOS spec §4.2.
 *
 * Layout: Hero verde con DecorativeRings + saludo + StatTiles
 *       → Surface crema offset -15dp
 *           → PrimaryCourseCardBrand (continuar aprendiendo)
 *           → BrandSectionHeader "Mis cursos" + LazyRow CourseListItemBrand
 */
@Composable
fun LearnBrandView(fragmentManager: FragmentManager) {
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

    LearnBrandContent(
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
                    fragmentManager = fragmentManager,
                    enrolledCourse = action.enrolledCourse,
                    openDates = true,
                )
                is DashboardGalleryScreenAction.OpenBlock -> viewModel.navigateToCourseOutline(
                    fragmentManager = fragmentManager,
                    enrolledCourse = action.enrolledCourse,
                    resumeBlockId = action.blockId,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LearnBrandContent(
    uiMessage: UIMessage?,
    uiState: DashboardGalleryUIState,
    updating: Boolean,
    apiHostUrl: String,
    userName: String,
    onAction: (DashboardGalleryScreenAction) -> Unit,
    hasInternetConnection: Boolean,
) {
    val brand = MaterialTheme.brand
    val scaffoldState = rememberScaffoldState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = updating,
        onRefresh = { onAction(DashboardGalleryScreenAction.SwipeRefresh) },
    )
    var isInternetConnectionShown by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        backgroundColor = brand.palette.brandGreenDark,
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
                val courses = (uiState as? DashboardGalleryUIState.Courses)?.userCourses
                LearnBrandHero(
                    userCourses = courses,
                    userName = userName,
                    onSettingsClick = { onAction(DashboardGalleryScreenAction.NavigateToSettings) },
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-15).dp),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = brand.palette.brandCream,
                    elevation = 0.dp,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 12.dp)
                                .size(width = 36.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(brand.palette.brandHandle),
                        )

                        when (uiState) {
                            is DashboardGalleryUIState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = brand.palette.brandGreen)
                                }
                            }

                            is DashboardGalleryUIState.Courses -> {
                                LearnBrandCourses(
                                    userCourses = uiState.userCourses,
                                    apiHostUrl = apiHostUrl,
                                    onAction = onAction,
                                )
                            }

                            is DashboardGalleryUIState.Empty -> {
                                LearnBrandEmpty(
                                    onFindCourse = { onAction(DashboardGalleryScreenAction.NavigateToDiscovery) },
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
                contentColor = brand.palette.brandGreen,
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

@Composable
private fun LearnBrandHero(
    userCourses: CourseEnrollments?,
    userName: String,
    onSettingsClick: () -> Unit,
) {
    val brand = MaterialTheme.brand
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
            .heightIn(min = 280.dp)
            .background(brand.gradients.heroDashboard),
    ) {
        // Stripe guinda superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(brand.palette.guinda),
        )

        // Anillos decorativos animados
        DecorativeRings(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = BrandSpacing.screenHorizontal)
                .padding(top = 8.dp, bottom = BrandSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(BrandSpacing.l),
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
                        tint = brand.palette.textOnHeader,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Bienvenido de vuelta",
                    style = brand.typography.eyebrow,
                    color = brand.palette.textOnHeader.copy(alpha = 0.65f),
                )
                Text(
                    text = if (userName.isNotEmpty()) "¡Hola, $userName!" else "¡Hola!",
                    style = brand.typography.displayHero,
                    color = brand.palette.textOnHeader,
                )
                Text(
                    text = "Continúa donde lo dejaste",
                    style = brand.typography.body,
                    color = brand.palette.textOnHeader.copy(alpha = 0.72f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(BrandSpacing.s)) {
                if (totalCount > 0) {
                    BrandStatTile(
                        value = totalCount.toString(),
                        label = "Total",
                        icon = Icons.Filled.School,
                    )
                }
                if (inProgressCount > 0) {
                    BrandStatTile(
                        value = inProgressCount.toString(),
                        label = "En curso",
                        icon = Icons.Filled.PlayCircle,
                    )
                }
                if (notStartedCount > 0) {
                    BrandStatTile(
                        value = notStartedCount.toString(),
                        label = "Por iniciar",
                        icon = Icons.Filled.TaskAlt,
                    )
                }
            }
        }
    }
}

@Composable
private fun LearnBrandCourses(
    userCourses: CourseEnrollments,
    apiHostUrl: String,
    onAction: (DashboardGalleryScreenAction) -> Unit,
) {
    val haptics = rememberBrandHaptics()
    val openCourse: (EnrolledCourse) -> Unit = { course ->
        haptics.cardTap()
        onAction(DashboardGalleryScreenAction.OpenCourse(course))
    }
    val resumeBlock: (EnrolledCourse, String) -> Unit = { course, blockId ->
        haptics.ctaPrimary()
        onAction(DashboardGalleryScreenAction.OpenBlock(course, blockId))
    }
    val navigateToDates: (EnrolledCourse) -> Unit = { course ->
        onAction(DashboardGalleryScreenAction.NavigateToDates(course))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        userCourses.primary?.let { primary ->
            BrandSectionHeader(
                title = "Continuar aprendiendo",
                eyebrow = "ESTA SEMANA",
            )
            Spacer(modifier = Modifier.height(BrandSpacing.m))
            PrimaryCourseCardBrand(
                modifier = Modifier.padding(horizontal = BrandSpacing.screenHorizontal),
                course = primary,
                apiHostUrl = apiHostUrl,
                onResume = { resumeBlock(primary, primary.courseStatus?.lastVisitedBlockId.orEmpty()) },
                onStart = { openCourse(primary) },
                onPastAssignmentClick = { navigateToDates(primary) },
            )
        }

        val secondary = userCourses.enrollments.courses.take(MOBILE_COURSE_LIST_ITEM_COUNT)
        if (secondary.isNotEmpty()) {
            val totalCount = secondary.size + (if (userCourses.primary != null) 1 else 0)
            Spacer(modifier = Modifier.height(BrandSpacing.xxl))
            BrandSectionHeader(
                title = "Mis cursos",
                actionLabel = "Ver todo ($totalCount)",
                onActionClick = { onAction(DashboardGalleryScreenAction.ViewAll) },
            )
            Spacer(modifier = Modifier.height(BrandSpacing.m))
            LazyRow(
                contentPadding = PaddingValues(horizontal = BrandSpacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(BrandSpacing.m),
            ) {
                itemsIndexed(secondary) { _, course ->
                    CourseListItemBrand(
                        course = course,
                        apiHostUrl = apiHostUrl,
                        onClick = { openCourse(course) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LearnBrandEmpty(onFindCourse: () -> Unit) {
    val brand = MaterialTheme.brand
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = BrandSpacing.screenHorizontal)
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(brand.palette.greenTint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                tint = brand.palette.brandGreen,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.height(BrandSpacing.l))
        Text(
            text = "Aún no tienes cursos",
            style = brand.typography.titleSection,
            color = brand.palette.cardPrimary,
        )
        Spacer(modifier = Modifier.height(BrandSpacing.xs))
        Text(
            text = "Explora el catálogo y comienza tu primer curso.",
            style = brand.typography.body,
            color = brand.palette.cardMedium,
        )
        Spacer(modifier = Modifier.height(BrandSpacing.l))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(BrandShapes.Pill)
                .background(brand.palette.brandGreen)
                .clickable(onClick = onFindCourse),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Buscar cursos",
                style = brand.typography.ctaLabel,
                color = brand.palette.textOnHeader,
            )
        }
    }
}

@Preview
@Composable
private fun LearnBrandEmptyPreview() {
    OpenEdXTheme {
        LearnBrandEmpty(onFindCourse = {})
    }
}
