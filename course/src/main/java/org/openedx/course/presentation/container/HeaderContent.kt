package org.openedx.course.presentation.container

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand_cream
import org.openedx.core.ui.theme.brand_cream_strong
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.brand_guinda
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.foundation.presentation.rememberWindowSize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun ExpandedHeaderContent(
    modifier: Modifier = Modifier,
    org: String,
    courseTitle: String,
    end: Date? = null,
    isSelfPaced: Boolean = false,
) {
    val windowSize = rememberWindowSize()
    val horizontalPadding = if (!windowSize.isTablet) 20.dp else 98.dp

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        elevation = 6.dp,
        backgroundColor = brand_cream,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Stripe guinda — firma institucional
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(brand_guinda)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(top = 14.dp, bottom = 14.dp)
            ) {

                // Org badge pill
                if (org.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(brand_green.copy(alpha = 0.1f))
                            .border(0.5.dp, brand_green.copy(alpha = 0.3f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(brand_green)
                        )
                        Text(
                            text = org,
                            color = brand_green,
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.3.sp,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Título del curso
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = courseTitle,
                    color = Color(0xFF19212F),
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.3).sp,
                    ),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3,
                )

                // Metadata chips
                val chips = buildList {
                    add(if (isSelfPaced) "A tu ritmo" else "Con instructor")
                    end?.let {
                        val sdf = SimpleDateFormat("MMM yyyy", Locale.forLanguageTag("es"))
                        add("Hasta ${sdf.format(it)}")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chips.forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(brand_cream_strong)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                color = Color(0xFF555555),
                                style = TextStyle(
                                    fontFamily = ttRoundsFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
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
internal fun CollapsedHeaderContent(
    modifier: Modifier = Modifier,
    courseTitle: String
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 3.dp),
        text = courseTitle,
        color = brand_green,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontFamily = ttRoundsFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
        ),
        maxLines = 1
    )
}

@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
private fun ExpandedHeaderContentPreview() {
    OpenEdXTheme {
        ExpandedHeaderContent(
            modifier = Modifier.fillMaxWidth(),
            org = "Organización de Estados Iberoamericanos",
            courseTitle = "El aprendizaje basado en proyectos: metodología para educar en ciudadanía global",
            isSelfPaced = true,
        )
    }
}

@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
private fun CollapsedHeaderContentPreview() {
    OpenEdXTheme {
        CollapsedHeaderContent(
            modifier = Modifier.fillMaxWidth(),
            courseTitle = "Course title"
        )
    }
}
