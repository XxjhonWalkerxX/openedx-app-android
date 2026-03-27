package org.openedx.profile.presentation.video

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.openedx.core.domain.model.VideoSettings
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.profile.presentation.ui.SettingsHeroLayout
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.profile.R
import org.openedx.core.R as CoreR

class VideoSettingsFragment : Fragment() {

    private val viewModel by viewModel<VideoSettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OpenEdXTheme {
                val windowSize = rememberWindowSize()

                val videoSettings by viewModel.videoSettings.observeAsState(viewModel.currentSettings)

                VideoSettingsScreen(
                    videoSettings = videoSettings,
                    windowSize = windowSize,
                    onBackClick = {
                        requireActivity().supportFragmentManager.popBackStack()
                    },
                    wifiDownloadChanged = {
                        viewModel.setWifiDownloadOnly(it)
                    },
                    videoStreamingQualityClick = {
                        viewModel.navigateToVideoStreamingQuality(requireActivity().supportFragmentManager)
                    },
                    videoDownloadQualityClick = {
                        viewModel.navigateToVideoDownloadQuality(requireActivity().supportFragmentManager)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun VideoSettingsScreen(
    windowSize: WindowSize,
    videoSettings: VideoSettings,
    wifiDownloadChanged: (Boolean) -> Unit,
    videoStreamingQualityClick: () -> Unit,
    videoDownloadQualityClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    var wifiDownloadOnly by rememberSaveable {
        mutableStateOf(videoSettings.wifiDownloadOnly)
    }

    val contentWidth by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = Modifier.widthIn(Dp.Unspecified, 420.dp),
                compact = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        )
    }

    SettingsHeroLayout(
        title = stringResource(id = R.string.profile_video),
        onBackClick = onBackClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // WiFi only toggle card
            VideoSettingCard {
                Row(
                    Modifier
                        .testTag("btn_wifi_only")
                        .fillMaxWidth()
                        .noRippleClickable {
                            wifiDownloadOnly = !wifiDownloadOnly
                            wifiDownloadChanged(wifiDownloadOnly)
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            modifier = Modifier.testTag("txt_wifi_only_label"),
                            text = stringResource(id = R.string.profile_wifi_only_download),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = MaterialTheme.appColors.textPrimary,
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            modifier = Modifier.testTag("txt_wifi_only_description"),
                            text = stringResource(id = R.string.profile_only_download_when_wifi_turned_on),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = MaterialTheme.appColors.textSecondary,
                            )
                        )
                    }
                    Switch(
                        modifier = Modifier.testTag("sw_wifi_only"),
                        checked = wifiDownloadOnly,
                        onCheckedChange = {
                            wifiDownloadOnly = !wifiDownloadOnly
                            wifiDownloadChanged(wifiDownloadOnly)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = brand_green,
                            checkedTrackColor = brand_green,
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Streaming quality card
            VideoSettingCard(onClick = videoStreamingQualityClick) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = CoreR.string.core_video_streaming_quality),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = MaterialTheme.appColors.textPrimary,
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = videoSettings.videoStreamingQuality.titleResId),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = MaterialTheme.appColors.textSecondary,
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        tint = brand_green,
                        contentDescription = null,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Download quality card
            VideoSettingCard(onClick = videoDownloadQualityClick) {
                Row(
                    Modifier
                        .testTag("btn_video_quality")
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = CoreR.string.core_video_download_quality),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = MaterialTheme.appColors.textPrimary,
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = videoSettings.videoDownloadQuality.titleResId),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = MaterialTheme.appColors.textSecondary,
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        tint = brand_green,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoSettingCard(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    androidx.compose.material.Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = androidx.compose.ui.graphics.Color.White,
        content = { content() }
    )
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun VideoSettingsScreenPreview() {
    OpenEdXTheme {
        VideoSettingsScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            wifiDownloadChanged = {},
            videoStreamingQualityClick = {},
            videoDownloadQualityClick = {},
            onBackClick = {},
            videoSettings = VideoSettings.default
        )
    }
}
