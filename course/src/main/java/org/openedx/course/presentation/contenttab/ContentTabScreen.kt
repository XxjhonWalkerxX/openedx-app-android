package org.openedx.course.presentation.contenttab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.ui.theme.brand_cream
import org.openedx.core.ui.theme.brand_cream_strong
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.course.presentation.assignments.CourseContentAssignmentScreen
import org.openedx.course.presentation.container.CourseContentTab
import org.openedx.course.presentation.outline.CourseContentAllScreen
import org.openedx.course.presentation.videos.CourseContentVideoScreen
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.windowSizeValue

@Composable
fun ContentTabScreen(
    viewModel: ContentTabViewModel,
    windowSize: WindowSize,
    fragmentManager: FragmentManager,
    courseId: String,
    courseName: String,
    pagerState: PagerState,
    onTabSelected: (CourseContentTab) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var progressCompleted by remember { mutableStateOf(0) }
    var progressTotal by remember { mutableStateOf(0) }

    LaunchedEffect(pagerState.currentPage) {
        val selectedTab = CourseContentTab.entries[pagerState.currentPage]
        onTabSelected(selectedTab)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = brand_cream,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Sub-tabs — estilo texto + underline (distinto a los main tabs pills)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CourseContentTab.entries.forEachIndexed { index, tab ->
                    val isSelected = pagerState.currentPage == index
                    val tabColor = if (isSelected) brand_green else Color(0xFFAAAAAA)
                    Box(
                        modifier = Modifier
                            .clickable {
                                scope.launch { pagerState.scrollToPage(index) }
                                viewModel.logTabClickEvent(CourseContentTab.entries[index])
                            }
                            .padding(horizontal = 14.dp)
                            .padding(top = 10.dp, bottom = 8.dp)
                            .drawBehind {
                                if (isSelected) {
                                    drawLine(
                                        color = brand_green,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 2.5.dp.toPx(),
                                        cap = StrokeCap.Round,
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(tab.labelResId),
                            color = tabColor,
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Progreso inline — X/Y + barra
                if (progressTotal > 0) {
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "$progressCompleted/$progressTotal",
                            color = Color(0xFFAAAAAA),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            ),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        LinearProgressIndicator(
                            progress = progressCompleted.toFloat() / progressTotal,
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(100.dp)),
                            color = brand_green,
                            backgroundColor = brand_cream_strong,
                        )
                    }
                }
            }

            // Línea divisoria bajo los sub-tabs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(brand_cream_strong)
            )

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                beyondViewportPageCount = CourseContentTab.entries.size,
            ) { page ->
                when (CourseContentTab.entries[page]) {
                    CourseContentTab.ALL -> CourseContentAllScreen(
                        windowSize = windowSize,
                        viewModel = koinViewModel(parameters = {
                            parametersOf(courseId, courseName)
                        }),
                        fragmentManager = fragmentManager,
                        onNavigateToHome = onNavigateToHome,
                        onProgressLoaded = { completed, total ->
                            progressCompleted = completed
                            progressTotal = total
                        },
                    )

                    CourseContentTab.VIDEOS -> CourseContentVideoScreen(
                        windowSize = windowSize,
                        viewModel = koinViewModel(parameters = {
                            parametersOf(courseId, courseName)
                        }),
                        fragmentManager = fragmentManager,
                        onNavigateToHome = onNavigateToHome,
                    )

                    CourseContentTab.ASSIGNMENTS -> CourseContentAssignmentScreen(
                        windowSize = windowSize,
                        viewModel = koinViewModel(parameters = { parametersOf(courseId) }),
                        fragmentManager = fragmentManager,
                        onNavigateToHome = onNavigateToHome,
                    )
                }
            }
        }
    }
}
