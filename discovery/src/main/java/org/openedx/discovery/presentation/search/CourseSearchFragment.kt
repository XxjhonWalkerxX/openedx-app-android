package org.openedx.discovery.presentation.search

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
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
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
import org.openedx.core.ui.SearchBar
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
import org.openedx.discovery.domain.model.Course
import org.openedx.discovery.presentation.DiscoveryRouter
import org.openedx.discovery.presentation.search.CourseSearchFragment.Companion.LOAD_MORE_THRESHOLD
import org.openedx.discovery.presentation.ui.DiscoveryCourseItem
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.rememberWindowSize

private val searchAccentColors = listOf(
    brand_green,
    Color(0xFF611232),
    Color(0xFF3D3020),
    brand_green_dark,
)
private val searchHeroHeight = 160.dp
private val heroOverlap = 28.dp

class CourseSearchFragment : Fragment() {

    private val viewModel by viewModel<CourseSearchViewModel>()
    private val router by inject<DiscoveryRouter>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()
                val uiState by viewModel.uiState.observeAsState(
                    CourseSearchUIState.Courses(emptyList(), 0)
                )
                val uiMessage by viewModel.uiMessage.observeAsState()
                val canLoadMore by viewModel.canLoadMore.observeAsState(false)
                val refreshing by viewModel.isUpdating.observeAsState(false)
                val querySearch = arguments?.getString(ARG_SEARCH_QUERY, "") ?: ""

                CourseSearchScreen(
                    windowSize = windowSize,
                    state = uiState,
                    uiMessage = uiMessage,
                    apiHostUrl = viewModel.apiHostUrl,
                    canLoadMore = canLoadMore,
                    refreshing = refreshing,
                    querySearch = querySearch,
                    isUserLoggedIn = viewModel.isUserLoggedIn,
                    isRegistrationEnabled = viewModel.isRegistrationEnabled,
                    onBackClick = {
                        requireActivity().supportFragmentManager.popBackStack()
                    },
                    onSearchTextChanged = { viewModel.search(it) },
                    onSwipeRefresh = { viewModel.updateSearchQuery() },
                    paginationCallback = { viewModel.fetchMore() },
                    onItemClick = {
                        router.navigateToCourseDetail(
                            requireActivity().supportFragmentManager, it
                        )
                    },
                    onRegisterClick = {
                        router.navigateToSignUp(parentFragmentManager, null, null)
                    },
                    onSignInClick = {
                        router.navigateToSignIn(parentFragmentManager, null, null)
                    },
                )
            }
        }
    }

    companion object {
        private const val ARG_SEARCH_QUERY = "query_search"
        const val LOAD_MORE_THRESHOLD = 4
        fun newInstance(querySearch: String): CourseSearchFragment {
            val fragment = CourseSearchFragment()
            fragment.arguments = bundleOf(ARG_SEARCH_QUERY to querySearch)
            return fragment
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun CourseSearchScreen(
    windowSize: WindowSize,
    state: CourseSearchUIState,
    uiMessage: UIMessage?,
    apiHostUrl: String,
    canLoadMore: Boolean,
    refreshing: Boolean,
    querySearch: String,
    isUserLoggedIn: Boolean,
    isRegistrationEnabled: Boolean,
    onBackClick: () -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onSwipeRefresh: () -> Unit,
    paginationCallback: () -> Unit,
    onItemClick: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onSignInClick: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val scrollState = rememberLazyListState()
    val firstVisibleIndex = remember { mutableStateOf(scrollState.firstVisibleItemIndex) }
    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = onSwipeRefresh)

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = querySearch, selection = TextRange(querySearch.length)))
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = scrollState.isScrollInProgress) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .semantics { testTagsAsResourceId = true },
        backgroundColor = brand_cream,
        bottomBar = {
            if (!isUserLoggedIn) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp)) {
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
            // 1. Verde hero — purely decorative
            SearchHeroBackground(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(searchHeroHeight),
            )

            // 2. Scrollable content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                val typingText = if (textFieldValue.text.isEmpty()) {
                    "Empieza a escribir para encontrar un curso"
                } else {
                    pluralStringResource(
                        id = org.openedx.discovery.R.plurals.discovery_found_courses,
                        (state as? CourseSearchUIState.Courses)?.numCourses ?: 0,
                        (state as? CourseSearchUIState.Courses)?.numCourses ?: 0,
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = scrollState,
                ) {
                    item(key = "spacer") {
                        Spacer(modifier = Modifier.height(searchHeroHeight - heroOverlap))
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
                                // Real search input
                                SearchBar(
                                    modifier = Modifier
                                        .testTag("tf_search_bar")
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .padding(top = 16.dp)
                                        .height(48.dp),
                                    label = "",
                                    requestFocus = true,
                                    searchValue = textFieldValue,
                                    keyboardActions = { focusManager.clearFocus() },
                                    onValueChanged = { text ->
                                        textFieldValue = text
                                        onSearchTextChanged(textFieldValue.text)
                                    },
                                    onClearValue = {
                                        textFieldValue = TextFieldValue("")
                                        onSearchTextChanged("")
                                    },
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .padding(top = 20.dp, bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        modifier = Modifier.testTag("txt_search_results_title"),
                                        text = "Resultados de búsqueda",
                                        style = TextStyle(
                                            fontFamily = ttRoundsCompressedMedium,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 22.sp,
                                            color = Color(0xFF1C1B18),
                                            letterSpacing = (-0.2).sp,
                                        ),
                                    )
                                    Text(
                                        modifier = Modifier.testTag("txt_search_results_subtitle"),
                                        text = typingText,
                                        style = TextStyle(
                                            fontFamily = ttRoundsFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 13.sp,
                                            color = Color(0xFF9A9590),
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    when (state) {
                        is CourseSearchUIState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(brand_cream)
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = brand_green)
                                }
                            }
                        }

                        is CourseSearchUIState.Courses -> {
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
                                        accentColor = searchAccentColors[index % searchAccentColors.size],
                                        onClick = { courseId -> onItemClick(courseId) },
                                    )
                                }
                            }
                            item {
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
                            if (scrollState.shouldLoadMore(firstVisibleIndex, LOAD_MORE_THRESHOLD)) {
                                paginationCallback()
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

                PullRefreshIndicator(
                    refreshing = refreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    contentColor = brand_green,
                )
            }

            // 3. Back button overlay — last child = highest z-order
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp),
            ) {
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
            }
        }
    }

    LaunchedEffect(rememberSaveable { true }) {
        onSearchTextChanged(querySearch)
    }
}

@Composable
private fun SearchHeroBackground(modifier: Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = heroGradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, 320f),
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
                .size(180.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .border(28.dp, Color.White.copy(alpha = 0.06f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.BottomStart)
                .clip(CircleShape)
                .border(16.dp, Color.White.copy(alpha = 0.05f), CircleShape),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 72.dp, end = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = "Buscar",
                style = TextStyle(
                    fontFamily = ttRoundsCompressedMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 30.sp,
                    color = Color.White,
                    letterSpacing = (-0.3).sp,
                ),
            )
            Text(
                text = "Explora nuestra oferta educativa",
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CourseSearchScreenPreview() {
    OpenEdXTheme {
        CourseSearchScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            state = CourseSearchUIState.Courses(listOf(mockCourse, mockCourse), 2),
            uiMessage = null,
            apiHostUrl = "",
            canLoadMore = false,
            refreshing = false,
            querySearch = "",
            isUserLoggedIn = true,
            isRegistrationEnabled = true,
            onBackClick = {},
            onSearchTextChanged = {},
            onSwipeRefresh = {},
            paginationCallback = {},
            onItemClick = {},
            onSignInClick = {},
            onRegisterClick = {},
        )
    }
}

@Preview(device = Devices.NEXUS_9, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun CourseSearchScreenTabletPreview() {
    OpenEdXTheme {
        CourseSearchScreen(
            windowSize = WindowSize(WindowType.Medium, WindowType.Medium),
            state = CourseSearchUIState.Courses(listOf(mockCourse, mockCourse), 2),
            uiMessage = null,
            apiHostUrl = "",
            canLoadMore = false,
            refreshing = false,
            querySearch = "",
            isUserLoggedIn = false,
            isRegistrationEnabled = true,
            onBackClick = {},
            onSearchTextChanged = {},
            onSwipeRefresh = {},
            paginationCallback = {},
            onItemClick = {},
            onSignInClick = {},
            onRegisterClick = {},
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
    isEnrolled = false,
)
