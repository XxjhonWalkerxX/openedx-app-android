package org.openedx.discovery.presentation

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.core.domain.model.Media
import org.openedx.core.ui.AuthButtonsPanel
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.OfflineModeDialog
import org.openedx.core.ui.shouldLoadMore
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand_cream
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.brand_green_dark
import org.openedx.core.ui.theme.brand_guinda
import org.openedx.core.ui.theme.heroGradientColors
import org.openedx.core.ui.theme.ttRoundsCompressedMedium
import org.openedx.core.ui.theme.ttRoundsCompressedThinItalic
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.discovery.R
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.presentation.NativeDiscoveryFragment.Companion.LOAD_MORE_THRESHOLD
import org.openedx.discovery.presentation.ui.DiscoveryCourseItem
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.rememberWindowSize

// ── Paleta y constantes de diseño ────────────────────────────────────────────
private val accentColors = listOf(
    brand_green,
    Color(0xFF611232), // brand_guinda
    Color(0xFF3D3020), // dark warm
    brand_green_dark,
)
private val heroHeight = 200.dp
private val heroOverlap = 28.dp

class NativeDiscoveryFragment : Fragment() {

    private val viewModel by viewModel<NativeDiscoveryViewModel>()
    private val router: DiscoveryRouter by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()

                val uiState by viewModel.uiState.observeAsState()
                val uiMessage by viewModel.uiMessage.observeAsState()
                val canLoadMore by viewModel.canLoadMore.observeAsState(false)
                val refreshing by viewModel.isUpdating.observeAsState(false)
                val querySearch = arguments?.getString(ARG_SEARCH_QUERY, "") ?: ""

                DiscoveryScreen(
                    windowSize = windowSize,
                    state = uiState!!,
                    uiMessage = uiMessage,
                    apiHostUrl = viewModel.apiHostUrl,
                    canLoadMore = canLoadMore,
                    refreshing = refreshing,
                    hasInternetConnection = viewModel.hasInternetConnection,
                    canShowBackButton = viewModel.canShowBackButton,
                    isUserLoggedIn = viewModel.isUserLoggedIn,
                    isRegistrationEnabled = viewModel.isRegistrationEnabled,
                    onSearchClick = {
                        viewModel.discoverySearchBarClickedEvent()
                        router.navigateToCourseSearch(
                            requireActivity().supportFragmentManager,
                            ""
                        )
                    },
                    paginationCallback = {
                        viewModel.fetchMore()
                    },
                    onSwipeRefresh = {
                        viewModel.updateData()
                    },
                    onReloadClick = {
                        viewModel.getCoursesList()
                    },
                    onItemClick = { course ->
                        viewModel.discoveryCourseClicked(course.id, course.name)
                        viewModel.courseDetailClickedEvent(course.id, course.name)
                        router.navigateToCourseDetail(
                            requireActivity().supportFragmentManager,
                            course.id
                        )
                    },
                    onRegisterClick = {
                        router.navigateToSignUp(parentFragmentManager, null, null)
                    },
                    onSignInClick = {
                        router.navigateToSignIn(parentFragmentManager, null, null)
                    },
                    onBackClick = {
                        requireActivity().supportFragmentManager.popBackStackImmediate()
                    },
                    onSettingsClick = {
                        router.navigateToSettings(requireActivity().supportFragmentManager)
                    }
                )
                LaunchedEffect(uiState) {
                    if (querySearch.isNotEmpty()) {
                        router.navigateToCourseSearch(
                            requireActivity().supportFragmentManager,
                            querySearch
                        )
                        arguments?.putString(ARG_SEARCH_QUERY, "")
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_SEARCH_QUERY = "query_search"
        const val LOAD_MORE_THRESHOLD = 4
        fun newInstance(querySearch: String = ""): NativeDiscoveryFragment {
            val fragment = NativeDiscoveryFragment()
            fragment.arguments = bundleOf(
                ARG_SEARCH_QUERY to querySearch
            )
            return fragment
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun DiscoveryScreen(
    windowSize: WindowSize,
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
    val scaffoldState = rememberScaffoldState()
    val scrollState = rememberLazyListState()
    val firstVisibleIndex = remember { mutableIntStateOf(scrollState.firstVisibleItemIndex) }
    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = { onSwipeRefresh() })
    var isInternetConnectionShown by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
        backgroundColor = brand_cream,
        bottomBar = {
            if (!isUserLoggedIn) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                        .navigationBarsPadding()
                ) {
                    AuthButtonsPanel(
                        onRegisterClick = onRegisterClick,
                        onSignInClick = onSignInClick,
                        showRegisterButton = isRegistrationEnabled,
                    )
                }
            }
        }
    ) { paddingValues ->
        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Verde hero — purely decorative, no interactive elements
            DiscoveryHeroBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight),
            )

            // 2. Scrollable content with pull-to-refresh
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when (state) {
                    is DiscoveryUIState.Loading -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Spacer(modifier = Modifier.height(heroHeight - heroOverlap))
                            }
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                    color = brand_cream,
                                    elevation = 0.dp,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(color = brand_green)
                                    }
                                }
                            }
                        }
                    }

                    is DiscoveryUIState.Courses -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = scrollState,
                        ) {
                            item(key = "spacer") {
                                Spacer(modifier = Modifier.height(heroHeight - heroOverlap))
                            }
                            item(key = "header") {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                    color = brand_cream,
                                    elevation = 0.dp,
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .padding(top = 12.dp)
                                                .size(width = 36.dp, height = 4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color(0xFFC8C3BA)),
                                        )
                                        // Search bar — interactive, inside LazyColumn so touches work
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp)
                                                .padding(top = 16.dp)
                                                .height(46.dp)
                                                .clip(RoundedCornerShape(50.dp))
                                                .background(Color.White)
                                                .clickable(onClick = onSearchClick)
                                                .padding(horizontal = 16.dp),
                                            contentAlignment = Alignment.CenterStart,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = brand_green,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                                Text(
                                                    text = "Buscar cursos...",
                                                    style = TextStyle(
                                                        fontFamily = ttRoundsFamily,
                                                        fontWeight = FontWeight.Normal,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF9A9590),
                                                    ),
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp)
                                                .padding(top = 20.dp, bottom = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                modifier = Modifier.testTag("txt_discovery_new"),
                                                text = "Todos los cursos",
                                                style = TextStyle(
                                                    fontFamily = ttRoundsCompressedMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 20.sp,
                                                    color = Color(0xFF1C1B18),
                                                    letterSpacing = (-0.2).sp,
                                                ),
                                            )
                                            Text(
                                                text = "${state.courses.size} disponibles",
                                                style = TextStyle(
                                                    fontFamily = ttRoundsFamily,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF9A9590),
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                            itemsIndexed(state.courses) { index, course ->
                                Box(
                                    modifier = Modifier
                                        .background(brand_cream)
                                        .padding(horizontal = 20.dp)
                                        .padding(bottom = 10.dp),
                                ) {
                                    DiscoveryCourseItem(
                                        apiHostUrl = apiHostUrl,
                                        course = course,
                                        accentColor = accentColors[index % accentColors.size],
                                        onClick = { onItemClick(course) },
                                    )
                                }
                            }
                            item(key = "load_more") {
                                if (canLoadMore) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(brand_cream)
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(color = brand_green)
                                    }
                                }
                            }
                            item(key = "bottom_space") {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .background(brand_cream),
                                )
                            }
                        }
                        if (scrollState.shouldLoadMore(firstVisibleIndex, LOAD_MORE_THRESHOLD)) {
                            paginationCallback()
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = refreshing,
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
                            onReloadClick()
                        }
                    )
                }
            }

            // 3. Nav buttons overlay — drawn last = highest z-order = always clickable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (canShowBackButton) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                                .clickable(onClick = onBackClick),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }

                    if (!canShowBackButton) {
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
                    } else {
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}

// ── Hero verde de exploración — decorativo, sin elementos interactivos ────────
@Composable
private fun DiscoveryHeroBackground(modifier: Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = heroGradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, 440f),
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(brand_guinda),
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .border(32.dp, Color.White.copy(alpha = 0.06f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .border(20.dp, Color.White.copy(alpha = 0.05f), CircleShape),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 60.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Explorar",
                    style = TextStyle(
                        fontFamily = ttRoundsCompressedMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 30.sp,
                        color = Color.White,
                        letterSpacing = (-0.3).sp,
                    ),
                )
                Text(
                    text = "Encuentra tu próximo aprendizaje",
                    style = TextStyle(
                        fontFamily = ttRoundsCompressedThinItalic,
                        fontWeight = FontWeight.Thin,
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        letterSpacing = 0.2.sp,
                    ),
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun CourseItemPreview() {
    OpenEdXTheme {
        DiscoveryCourseItem(
            apiHostUrl = "",
            course = mockCourse,
            accentColor = brand_green,
            onClick = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiscoveryScreenPreview() {
    OpenEdXTheme {
        DiscoveryScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            state = DiscoveryUIState.Courses(
                listOf(
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                )
            ),
            uiMessage = null,
            apiHostUrl = "",
            onSearchClick = {},
            paginationCallback = {},
            onSwipeRefresh = {},
            onItemClick = {},
            onReloadClick = {},
            canLoadMore = false,
            refreshing = false,
            hasInternetConnection = true,
            isUserLoggedIn = false,
            isRegistrationEnabled = true,
            onSignInClick = {},
            onRegisterClick = {},
            onBackClick = {},
            onSettingsClick = {},
            canShowBackButton = false
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_9)
@Composable
private fun DiscoveryScreenTabletPreview() {
    OpenEdXTheme {
        DiscoveryScreen(
            windowSize = WindowSize(WindowType.Medium, WindowType.Medium),
            state = DiscoveryUIState.Courses(
                listOf(
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                    mockCourse,
                )
            ),
            uiMessage = null,
            apiHostUrl = "",
            onSearchClick = {},
            paginationCallback = {},
            onSwipeRefresh = {},
            onItemClick = {},
            onReloadClick = {},
            canLoadMore = false,
            refreshing = false,
            hasInternetConnection = true,
            isUserLoggedIn = true,
            isRegistrationEnabled = true,
            onSignInClick = {},
            onRegisterClick = {},
            onBackClick = {},
            onSettingsClick = {},
            canShowBackButton = false
        )
    }
}

private val mockCourse = Course(
    id = "id",
    blocksUrl = "blocksUrl",
    courseId = "courseId",
    effort = "effort",
    enrollmentStart = null,
    enrollmentEnd = null,
    hidden = false,
    invitationOnly = false,
    media = Media(),
    mobileAvailable = true,
    name = "Test course",
    number = "number",
    org = "EdX",
    pacing = "pacing",
    shortDescription = "shortDescription",
    start = "start",
    end = "end",
    startDisplay = "startDisplay",
    startType = "startType",
    overview = "",
    isEnrolled = false
)
