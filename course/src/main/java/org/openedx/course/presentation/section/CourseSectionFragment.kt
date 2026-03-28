package org.openedx.course.presentation.section

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.openedx.core.BlockType
import org.openedx.core.domain.model.AssignmentProgress
import org.openedx.core.domain.model.Block
import org.openedx.core.domain.model.BlockCounts
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.brand_guinda
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.course.R
import org.openedx.course.presentation.CourseRouter
import org.openedx.course.presentation.unit.container.CourseViewMode
import org.openedx.foundation.extension.serializable
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import java.util.Date
import org.openedx.core.R as CoreR

class CourseSectionFragment : Fragment() {

    private val viewModel by viewModel<CourseSectionViewModel> {
        parametersOf(requireArguments().getString(ARG_COURSE_ID, ""))
    }
    private val router by inject<CourseRouter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        val subSectionId = requireArguments().getString(ARG_SUBSECTION_ID, "")
        viewModel.mode = requireArguments().serializable(ARG_MODE)!!
        viewModel.getBlocks(subSectionId, viewModel.mode)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()

                val uiState by viewModel.uiState.observeAsState(CourseSectionUIState.Loading)
                val uiMessage by viewModel.uiMessage.observeAsState()
                CourseSectionScreen(
                    windowSize = windowSize,
                    uiState = uiState,
                    uiMessage = uiMessage,
                    onBackClick = {
                        requireActivity().supportFragmentManager.popBackStack()
                    },
                    onItemClick = { block ->
                        if (block.descendants.isNotEmpty()) {
                            viewModel.verticalClickedEvent(block.blockId)
                            router.navigateToCourseContainer(
                                fm = requireActivity().supportFragmentManager,
                                courseId = viewModel.courseId,
                                unitId = block.id,
                                mode = viewModel.mode
                            )
                        }
                    },
                )

                LaunchedEffect(rememberSaveable { true }) {
                    val unitId = requireArguments().getString(ARG_UNIT_ID, "")
                    if (unitId.isNotEmpty()) {
                        router.navigateToCourseContainer(
                            fm = requireActivity().supportFragmentManager,
                            courseId = viewModel.courseId,
                            unitId = unitId,
                            componentId = requireArguments().getString(ARG_COMPONENT_ID, ""),
                            mode = viewModel.mode
                        )
                        requireArguments().putString(ARG_UNIT_ID, "")
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_COURSE_ID = "courseId"
        private const val ARG_SUBSECTION_ID = "subSectionId"
        private const val ARG_UNIT_ID = "unitId"
        private const val ARG_COMPONENT_ID = "componentId"
        private const val ARG_MODE = "mode"
        fun newInstance(
            courseId: String,
            subSectionId: String,
            unitId: String?,
            componentId: String?,
            mode: CourseViewMode,
        ): CourseSectionFragment {
            val fragment = CourseSectionFragment()
            fragment.arguments = bundleOf(
                ARG_COURSE_ID to courseId,
                ARG_SUBSECTION_ID to subSectionId,
                ARG_UNIT_ID to unitId,
                ARG_COMPONENT_ID to componentId,
                ARG_MODE to mode
            )
            return fragment
        }
    }
}

@Composable
private fun CourseSectionScreen(
    windowSize: WindowSize,
    uiState: CourseSectionUIState,
    uiMessage: UIMessage?,
    onBackClick: () -> Unit,
    onItemClick: (Block) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val title = when (uiState) {
        is CourseSectionUIState.Blocks -> uiState.sectionName
        else -> ""
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.appColors.background
    ) { paddingValues ->

        val contentWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        val listPadding by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = PaddingValues(vertical = 24.dp),
                    compact = PaddingValues(24.dp)
                )
            )
        }

        HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsInset(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(contentWidth) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .displayCutoutForLandscape()
                        .zIndex(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BackBtn {
                        onBackClick()
                    }
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 56.dp),
                        text = title,
                        color = MaterialTheme.appColors.textPrimary,
                        style = MaterialTheme.appTypography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.appColors.background,
                    shape = MaterialTheme.appShapes.screenBackgroundShape
                ) {
                    when (uiState) {
                        is CourseSectionUIState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                            }
                        }

                        is CourseSectionUIState.Blocks -> {
                            Column(Modifier.fillMaxSize()) {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    item {
                                        SectionResourceHeader(title = "Contenido de la sección")
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    itemsIndexed(uiState.blocks) { index, block ->
                                        val accentColor = if (index % 3 == 2) brand_guinda else brand_green
                                        CourseSubsectionItem(
                                            index = index,
                                            block = block,
                                            accentColor = accentColor,
                                            onClick = { onItemClick(it) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionResourceHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(brand_guinda.copy(alpha = 0.25f))
        )
        Text(
            text = title.uppercase(),
            color = brand_guinda,
            style = TextStyle(
                fontFamily = ttRoundsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
            ),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(brand_guinda.copy(alpha = 0.25f))
        )
    }
}

@Composable
private fun CourseSubsectionItem(
    index: Int,
    block: Block,
    accentColor: Color,
    onClick: (Block) -> Unit,
) {
    val iconPainter = if (block.isCompleted()) {
        painterResource(R.drawable.course_ic_task_alt)
    } else {
        painterResource(CoreR.drawable.core_ic_chapter_icon)
    }
    val iconDescription = if (block.isCompleted()) {
        stringResource(id = R.string.course_accessibility_section_completed)
    } else {
        stringResource(id = R.string.course_accessibility_section_uncompleted)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(block) },
        shape = RoundedCornerShape(14.dp),
        elevation = 2.dp,
        backgroundColor = Color.White,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Barra de acento izquierda
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(
                        accentColor,
                        RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Número de orden
            Text(
                text = String.format("%02d", index + 1),
                color = accentColor.copy(alpha = 0.45f),
                style = TextStyle(
                    fontFamily = ttRoundsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                ),
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Ícono envuelto
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = iconPainter,
                    contentDescription = iconDescription,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Título
            Text(
                modifier = Modifier.weight(1f),
                text = block.displayName,
                style = TextStyle(
                    fontFamily = ttRoundsFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                ),
                color = Color(0xFF19212F),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )

            // Flecha
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFCCCCCC),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(20.dp),
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CourseSectionScreenPreview() {
    OpenEdXTheme {
        CourseSectionScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            uiState = CourseSectionUIState.Blocks(
                listOf(
                    mockBlock,
                    mockBlock,
                    mockBlock,
                    mockBlock
                ),
                "",
                "Course default"
            ),
            uiMessage = null,
            onBackClick = {},
            onItemClick = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_9)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.NEXUS_9)
@Composable
private fun CourseSectionScreenTabletPreview() {
    OpenEdXTheme {
        CourseSectionScreen(
            windowSize = WindowSize(WindowType.Medium, WindowType.Medium),
            uiState = CourseSectionUIState.Blocks(
                listOf(
                    mockBlock,
                    mockBlock,
                    mockBlock,
                    mockBlock
                ),
                "",
                "Course default",
            ),
            uiMessage = null,
            onBackClick = {},
            onItemClick = {},
        )
    }
}

private val mockBlock = Block(
    id = "id",
    blockId = "blockId",
    lmsWebUrl = "lmsWebUrl",
    legacyWebUrl = "legacyWebUrl",
    studentViewUrl = "studentViewUrl",
    type = BlockType.HTML,
    displayName = "Block",
    graded = false,
    studentViewData = null,
    studentViewMultiDevice = false,
    blockCounts = BlockCounts(0),
    descendants = emptyList(),
    descendantsType = BlockType.HTML,
    completion = 0.0,
    containsGatedContent = false,
    assignmentProgress = AssignmentProgress("", 1f, 2f, "HM1"),
    due = Date(),
    offlineDownload = null
)
