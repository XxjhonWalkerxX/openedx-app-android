package org.openedx.discovery.presentation.brand

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.openedx.core.ui.brand.components.CourseCategory
import org.openedx.core.ui.brand.components.CourseCoverImage
import org.openedx.core.ui.theme.brand.BrandShapes
import org.openedx.core.ui.theme.brand.BrandSpacing
import org.openedx.core.ui.theme.brand.brand
import org.openedx.discovery.domain.model.Course

/**
 * Celda del catálogo Discovery — paridad iOS spec §4.4.5.
 *
 * Layout vertical: portada con `CourseCoverImage(Auto)` arriba + título 2 líneas +
 * organización + badge "Inscrito" si aplica.
 *
 * Sin progreso: cards de descubrimiento no requieren ProgressRing.
 */
@Composable
fun CourseDiscoveryCardBrand(
    course: Course,
    apiHostUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brand = MaterialTheme.brand
    val imageUrl = course.media.courseImage?.uri?.let { uri ->
        if (uri.startsWith("http")) uri else apiHostUrl.trimEnd('/') + uri
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = BrandShapes.Card,
        color = brand.palette.surfaceWhite,
        elevation = 2.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f / 1.05f),
            ) {
                CourseCoverImage(
                    courseName = course.name,
                    imageUrl = imageUrl,
                    category = CourseCategory.from(listOf(course.org, course.name)),
                    institutionShort = course.org.take(4).uppercase(),
                    modifier = Modifier.fillMaxSize(),
                )
                if (course.isEnrolled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(BrandSpacing.s)
                            .background(
                                brand.palette.brandGreen,
                                BrandShapes.Pill,
                            )
                            .padding(horizontal = BrandSpacing.s, vertical = 3.dp),
                    ) {
                        Text(
                            text = "Inscrito",
                            style = brand.typography.eyebrow,
                            color = brand.palette.textOnHeader,
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
                    text = course.name,
                    style = brand.typography.titleCard,
                    color = brand.palette.cardPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(BrandSpacing.xs))
                Text(
                    text = course.org,
                    style = brand.typography.caption,
                    color = brand.palette.cardSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
