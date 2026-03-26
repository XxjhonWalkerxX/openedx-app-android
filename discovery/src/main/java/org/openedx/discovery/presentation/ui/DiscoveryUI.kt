package org.openedx.discovery.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.openedx.core.domain.model.Media
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.ttRoundsCompressedMedium
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.discovery.R
import org.openedx.discovery.domain.model.Course
import org.openedx.foundation.extension.toImageLink
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.rememberWindowSize
import org.openedx.core.R as сoreR

@Composable
fun ImageHeader(
    modifier: Modifier,
    apiHostUrl: String,
    courseImage: String?,
    courseName: String,
) {
    val configuration = LocalConfiguration.current
    val windowSize = rememberWindowSize()
    val contentScale =
        if (!windowSize.isTablet && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ContentScale.Fit
        } else {
            ContentScale.Crop
        }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(courseImage?.toImageLink(apiHostUrl))
                .error(сoreR.drawable.core_no_image_course)
                .placeholder(сoreR.drawable.core_no_image_course)
                .build(),
            contentDescription = stringResource(
                id = сoreR.string.core_accessibility_header_image_for,
                courseName
            ),
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.appShapes.cardShape)
        )
    }
}

@Composable
fun DiscoveryCourseItem(
    apiHostUrl: String,
    course: Course,
    accentColor: Color = brand_green,
    onClick: (String) -> Unit,
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .testTag("btn_course_card")
            .fillMaxWidth()
            .clickable { onClick(course.courseId) },
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(course.media.courseImage?.uri?.toImageLink(apiHostUrl) ?: "")
                        .error(сoreR.drawable.core_no_image_course)
                        .placeholder(сoreR.drawable.core_no_image_course)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(96.dp)
                        .fillMaxHeight(),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        modifier = Modifier.testTag("txt_course_org"),
                        text = course.org,
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
                        modifier = Modifier
                            .testTag("txt_course_title")
                            .fillMaxWidth(),
                        text = course.name,
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
                    if (course.isEnrolled) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(brand_green),
                            )
                            Text(
                                text = "Inscrito",
                                style = TextStyle(
                                    fontFamily = ttRoundsFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.sp,
                                    color = brand_green,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WarningLabel(
    painter: Painter,
    text: String
) {
    val borderColor = if (!isSystemInDarkTheme()) {
        MaterialTheme.appColors.cardViewBorder
    } else {
        MaterialTheme.appColors.surface
    }
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(
                0.dp,
                MaterialTheme.appShapes.material.medium
            )
            .background(
                MaterialTheme.appColors.surface,
                MaterialTheme.appShapes.material.medium
            )
            .border(
                1.dp,
                borderColor,
                MaterialTheme.appShapes.material.medium
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = MaterialTheme.appColors.warning
            )
            Spacer(Modifier.width(12.dp))
            Text(
                modifier = Modifier.testTag("txt_enroll_internet_error"),
                text = text,
                color = MaterialTheme.appColors.textPrimaryVariant,
                style = MaterialTheme.appTypography.titleSmall
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WarningLabelPreview() {
    OpenEdXTheme {
        WarningLabel(
            painter = painterResource(id = сoreR.drawable.core_ic_offline),
            text = stringResource(id = R.string.discovery_no_internet_label)
        )
    }
}
