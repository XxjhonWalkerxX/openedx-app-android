package org.openedx.course.presentation.handouts

import android.content.res.Configuration
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
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.brand_cream
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.brand_guinda
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.course.R as courseR

@Composable
fun HandoutsScreen(
    windowSize: WindowSize,
    onHandoutsClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.appColors.background
    ) {
        val screenWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(Dp.Unspecified, 560.dp),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .displayCutoutForLandscape(),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = screenWidth,
                color = MaterialTheme.appColors.background
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        ResourceSectionHeader(title = "Material del curso")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    item {
                        ResourceCard(
                            index = 0,
                            title = stringResource(id = courseR.string.course_handouts),
                            description = stringResource(id = courseR.string.course_find_important_info),
                            painter = painterResource(id = courseR.drawable.course_ic_handouts),
                            accentColor = brand_green,
                            onClick = onHandoutsClick,
                        )
                    }
                    item {
                        ResourceCard(
                            index = 1,
                            title = stringResource(id = courseR.string.course_announcements),
                            description = stringResource(id = courseR.string.course_latest_news),
                            painter = painterResource(id = courseR.drawable.course_ic_announcements),
                            accentColor = brand_guinda,
                            onClick = onAnnouncementsClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceSectionHeader(title: String) {
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
private fun ResourceCard(
    index: Int,
    title: String,
    description: String,
    painter: Painter,
    accentColor: Color,
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(accentColor, RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Number badge
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

            // Icon wrap
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    color = Color(0xFF19212F),
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = Color(0xFF888888),
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                    ),
                )
            }

            // Arrow
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
private fun HandoutsScreenPreview() {
    OpenEdXTheme {
        HandoutsScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            onHandoutsClick = {},
            onAnnouncementsClick = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_9)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.NEXUS_9)
@Composable
private fun HandoutsScreenTabletPreview() {
    OpenEdXTheme {
        HandoutsScreen(
            windowSize = WindowSize(WindowType.Medium, WindowType.Medium),
            onHandoutsClick = {},
            onAnnouncementsClick = {}
        )
    }
}
