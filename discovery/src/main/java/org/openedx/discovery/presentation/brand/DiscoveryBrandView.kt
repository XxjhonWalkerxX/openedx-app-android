package org.openedx.discovery.presentation.brand

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.openedx.core.ui.AuthButtonsPanel
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OfflineModeDialog
import org.openedx.core.ui.brand.components.BrandChip
import org.openedx.core.ui.brand.components.BrandChipStyle
import org.openedx.core.ui.brand.components.DecorativeRings
import org.openedx.core.ui.theme.brand.BrandSpacing
import org.openedx.core.ui.theme.brand.brand
import org.openedx.core.ui.theme.brand.rememberBrandHaptics
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.presentation.DiscoveryUIState
import org.openedx.discovery.presentation.NativeDiscoveryFragment.Companion.LOAD_MORE_THRESHOLD
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize

private val heroHeight = 240.dp
private val heroOverlap = 28.dp

private enum class DiscoveryFilter(val label: String) {
    Todos("Todos"),
    Populares("Populares"),
    Recientes("Recientes"),
}

/**
 * Discovery rediseñado @prende.mx — paridad iOS spec §4.4.
 *
 * Layout: Hero verde con DecorativeRings + título "Descubre nuevo" + subtítulo
 *       → Surface crema offset -28dp
 *           → Search pill clickable
 *           → Filtros chips LazyRow (Todos / Populares / Recientes — UI only)
 *           → Grid 2 columnas con CourseDiscoveryCardBrand
 *           → Pagination loader
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun DiscoveryBrandView(
    @Suppress("UNUSED_PARAMETER") windowSize: WindowSize,
    state: DiscoveryUIState,
    uiMessage: UIMessage?,
    apiHostUrl: String,
    canLoadMore: Boolean,
    refreshing: Boolean,
    hasInternetConnection: Boolean,
    canShowBackButton: Boolean,
    isUserLoggedIn: Boolean,
    isRegistrationEnabled: Boolean,
    onSearchClick: () -> Unit,
    onSwipeRefresh: () -> Unit,
    onReloadClick: () -> Unit,
    paginationCallback: () -> Unit,
    onItemClick: (Course) -> Unit,
    onRegisterClick: () -> Unit,
    onSignInClick: () -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val brand = MaterialTheme.brand
    val haptics = rememberBrandHaptics()
    val scaffoldState = rememberScaffoldState()
    val gridState = rememberLazyGridState()
    val firstVisibleIndex = remember { mutableIntStateOf(gridState.firstVisibleItemIndex) }
    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = onSwipeRefresh)
    var isInternetConnectionShown by rememberSaveable { mutableStateOf(false) }
    var selectedFilter by rememberSaveable { mutableStateOf(DiscoveryFilter.Todos) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val firstVisible = gridState.firstVisibleItemIndex
            val total = gridState.layoutInfo.totalItemsCount
            firstVisible >= 0 && firstVisible >= total - LOAD_MORE_THRESHOLD
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        backgroundColor = brand.palette.brandCream,
        bottomBar = {
            if (!isUserLoggedIn) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = BrandSpacing.l, vertical = BrandSpacing.xxl)
                        .navigationBarsPadding(),
                ) {
                    AuthButtonsPanel(
                        onRegisterClick = onRegisterClick,
                        onSignInClick = onSignInClick,
                        showRegisterButton = isRegistrationEnabled,
                    )
                }
            }
        },
    ) { paddingValues ->
        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            DiscoveryBrandHero(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = heroHeight),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
            ) {
                when (state) {
                    is DiscoveryUIState.Loading -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(modifier = Modifier.height(heroHeight - heroOverlap))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                color = brand.palette.brandCream,
                                elevation = 0.dp,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = brand.palette.brandGreen)
                                }
                            }
                        }
                    }

                    is DiscoveryUIState.Courses -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = BrandSpacing.l,
                                end = BrandSpacing.l,
                                bottom = 80.dp,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(BrandSpacing.m),
                            verticalArrangement = Arrangement.spacedBy(BrandSpacing.m),
                        ) {
                            item(
                                span = { GridItemSpan(2) },
                                key = "spacer",
                            ) {
                                Spacer(modifier = Modifier.height(heroHeight - heroOverlap))
                            }
                            item(
                                span = { GridItemSpan(2) },
                                key = "header",
                            ) {
                                DiscoveryHeaderSection(
                                    totalCount = state.courses.size,
                                    selectedFilter = selectedFilter,
                                    onFilterSelect = { filter ->
                                        if (filter != selectedFilter) {
                                            haptics.tabChange()
                                            selectedFilter = filter
                                        }
                                    },
                                    onSearchClick = onSearchClick,
                                )
                            }
                            items(state.courses, key = { it.id }) { course ->
                                CourseDiscoveryCardBrand(
                                    course = course,
                                    apiHostUrl = apiHostUrl,
                                    onClick = {
                                        haptics.cardTap()
                                        onItemClick(course)
                                    },
                                )
                            }
                            if (canLoadMore) {
                                item(
                                    span = { GridItemSpan(2) },
                                    key = "load_more",
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = BrandSpacing.l),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(color = brand.palette.brandGreen)
                                    }
                                }
                            }
                        }
                        if (shouldLoadMore && canLoadMore) {
                            firstVisibleIndex.intValue = gridState.firstVisibleItemIndex
                            paginationCallback()
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = refreshing,
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
                            onReloadClick()
                        },
                    )
                }
            }

            DiscoveryNavOverlay(
                canShowBackButton = canShowBackButton,
                onBackClick = onBackClick,
                onSettingsClick = onSettingsClick,
            )
        }
    }
}

@Composable
private fun DiscoveryBrandHero(modifier: Modifier = Modifier) {
    val brand = MaterialTheme.brand
    Box(modifier = modifier.background(brand.gradients.heroDashboard)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(brand.palette.guinda),
        )
        DecorativeRings(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = BrandSpacing.screenHorizontal)
                .padding(top = 60.dp, bottom = BrandSpacing.xxl),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = "Descubre nuevo",
                style = brand.typography.displayHero,
                color = brand.palette.textOnHeader,
            )
            Spacer(modifier = Modifier.height(BrandSpacing.xs))
            Text(
                text = "Busquemos un curso para ti",
                style = brand.typography.body,
                color = brand.palette.textOnHeader.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun DiscoveryHeaderSection(
    totalCount: Int,
    selectedFilter: DiscoveryFilter,
    onFilterSelect: (DiscoveryFilter) -> Unit,
    onSearchClick: () -> Unit,
) {
    val brand = MaterialTheme.brand
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = brand.palette.brandCream,
        elevation = 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = BrandSpacing.m)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(brand.palette.brandHandle),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BrandSpacing.l)
                    .padding(top = BrandSpacing.l)
                    .height(46.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(brand.palette.inputBackground)
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = BrandSpacing.l),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BrandSpacing.s + 2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = brand.palette.brandGreen,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Buscar cursos…",
                        style = brand.typography.body,
                        color = brand.palette.cardSecondary,
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = BrandSpacing.m + 2.dp),
                contentPadding = PaddingValues(horizontal = BrandSpacing.l),
                horizontalArrangement = Arrangement.spacedBy(BrandSpacing.s),
            ) {
                items(DiscoveryFilter.entries) { filter ->
                    val style =
                        if (filter == selectedFilter) BrandChipStyle.Solid else BrandChipStyle.Outline
                    BrandChip(
                        label = filter.label,
                        style = style,
                        onClick = { onFilterSelect(filter) },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BrandSpacing.l)
                    .padding(top = BrandSpacing.l, bottom = BrandSpacing.m),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Todos los cursos",
                    style = brand.typography.titleSection,
                    color = brand.palette.cardPrimary,
                )
                Text(
                    text = "$totalCount disponibles",
                    style = brand.typography.caption,
                    color = brand.palette.cardSecondary,
                )
            }
        }
    }
}

@Composable
private fun DiscoveryNavOverlay(
    canShowBackButton: Boolean,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = BrandSpacing.l)
            .padding(top = BrandSpacing.s),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (canShowBackButton) {
                NavCircleButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBackClick,
                )
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }
            if (!canShowBackButton) {
                NavCircleButton(
                    icon = Icons.Default.ManageAccounts,
                    onClick = onSettingsClick,
                )
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
private fun NavCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.14f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

