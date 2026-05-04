package org.openedx.learn.presentation.brand

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
 * Card de carrusel "Mis cursos" — paridad iOS spec §4.2.2.
 *
 * Layout vertical: cover top 200×120 + meta bottom (título 2 líneas + estado +
 * mini ProgressRing).
 */
@Composable
fun CourseListItemBrand(
    course: EnrolledCourse,
    apiHostUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brand = MaterialTheme.brand
    val progress = course.progress.value
    val statusText = when {
        progress >= 1f -> "Completado"
        progress > 0f -> "En progreso"
        else -> "Por iniciar"
    }
    val statusColor = when {
        progress >= 1f -> brand.palette.brandGreen
        progress > 0f -> brand.palette.cardMedium
        else -> brand.palette.cardSecondary
    }

    Surface(
        modifier = modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = BrandShapes.Card,
        color = brand.palette.surfaceWhite,
        elevation = 2.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            ) {
                CourseCoverImage(
                    courseName = course.course.name,
                    imageUrl = course.course.courseImage.toImageLink(apiHostUrl),
                    category = CourseCategory.from(listOf(course.course.org, course.course.name)),
                    institutionShort = course.course.org.take(4).uppercase(),
                    modifier = Modifier.fillMaxSize(),
                )
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 8.dp, bottom = 8.dp)
                            .size(36.dp)
                            .clip(BrandShapes.Pill)
                            .background(brand.palette.surfaceWhite),
                    ) {
                        BrandProgressRing(
                            progress = progress,
                            size = 36.dp,
                            strokeWidth = 3.dp,
                            showLabel = false,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BrandSpacing.m)
                    .padding(top = BrandSpacing.s, bottom = BrandSpacing.m),
            ) {
                Text(
                    text = course.course.name,
                    style = brand.typography.titleCard,
                    color = brand.palette.cardPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(BrandSpacing.xs))
                Text(
                    text = course.course.org,
                    style = brand.typography.caption,
                    color = brand.palette.cardSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(BrandSpacing.xs))
                Text(
                    text = statusText,
                    style = brand.typography.caption,
                    color = statusColor,
                )
            }
        }
    }
}
