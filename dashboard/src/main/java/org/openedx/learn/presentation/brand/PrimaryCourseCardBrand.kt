package org.openedx.learn.presentation.brand

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.openedx.core.domain.model.EnrolledCourse
import org.openedx.core.ui.brand.components.BrandProgressRing
import org.openedx.core.ui.brand.components.CourseCategory
import org.openedx.core.ui.brand.components.CourseCoverImage
import org.openedx.core.ui.theme.brand.BrandShapes
import org.openedx.core.ui.theme.brand.BrandSpacing
import org.openedx.core.ui.theme.brand.brand
import org.openedx.foundation.extension.toImageLink

/**
 * Card primaria del Dashboard — paridad iOS spec §4.2.1.
 *
 * Layout horizontal: cover izquierda 140×140 + columna derecha (badge + título +
 * org + meta) + franja inferior con BrandProgressRing + CTA pill.
 *
 * Usa `CourseCoverImage` (Auto: heurística width/height) y `BrandProgressRing` del F0.
 */
@Composable
fun PrimaryCourseCardBrand(
    course: EnrolledCourse,
    apiHostUrl: String,
    onResume: () -> Unit,
    onStart: () -> Unit,
    onPastAssignmentClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brand = MaterialTheme.brand
    val progress = course.progress.value
    val notStarted = course.courseStatus == null
    val ctaLabel = if (notStarted) "Iniciar curso" else "Reanudar curso"
    val onCta = if (notStarted) onStart else onResume
    val hasPastAssignment = !course.courseAssignments?.pastAssignments.isNullOrEmpty()
    val pastCount = course.courseAssignments?.pastAssignments?.size ?: 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BrandShapes.Hero,
        color = brand.palette.surfaceWhite,
        elevation = 4.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            ) {
                CourseCoverImage(
                    courseName = course.course.name,
                    imageUrl = course.course.courseImage.toImageLink(apiHostUrl),
                    category = CourseCategory.from(listOf(course.course.org, course.course.name)),
                    institutionShort = course.course.org.take(4).uppercase(),
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight(),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = BrandSpacing.l, vertical = BrandSpacing.l),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(BrandShapes.Pill)
                            .background(brand.palette.greenTint)
                            .padding(horizontal = BrandSpacing.s, vertical = 3.dp),
                    ) {
                        Text(
                            text = if (notStarted) "Por iniciar" else "En curso",
                            style = brand.typography.eyebrow,
                            color = brand.palette.brandGreen,
                        )
                    }

                    Text(
                        text = course.course.name,
                        style = brand.typography.titleCard,
                        color = brand.palette.cardPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = course.course.org,
                        style = brand.typography.caption,
                        color = brand.palette.cardSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(brand.palette.divider),
            )

            if (hasPastAssignment) {
                Row(
                    modifier = Modifier
                        .padding(start = BrandSpacing.m, end = BrandSpacing.m, top = BrandSpacing.s)
                        .clip(BrandShapes.Pill)
                        .background(brand.palette.guinda.copy(alpha = 0.08f))
                        .clickable(onClick = onPastAssignmentClick)
                        .padding(horizontal = BrandSpacing.s + 2.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(brand.palette.guinda),
                    )
                    Text(
                        text = if (pastCount == 1) "1 tarea vencida" else "$pastCount tareas vencidas",
                        style = brand.typography.caption,
                        color = brand.palette.guinda,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BrandSpacing.m)
                    .padding(top = BrandSpacing.s + 2.dp, bottom = BrandSpacing.m),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BrandSpacing.m),
            ) {
                BrandProgressRing(
                    progress = progress,
                    size = 44.dp,
                    strokeWidth = 5.dp,
                    showLabel = true,
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(BrandShapes.Control)
                        .background(brand.palette.brandGreen)
                        .clickable(onClick = onCta)
                        .padding(horizontal = BrandSpacing.l),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = brand.palette.textOnHeader,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = ctaLabel,
                            style = brand.typography.ctaLabel,
                            color = brand.palette.textOnHeader,
                        )
                    }
                }
            }
        }
    }
}
