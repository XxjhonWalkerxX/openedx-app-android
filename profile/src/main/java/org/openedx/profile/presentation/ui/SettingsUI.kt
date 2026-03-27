package org.openedx.profile.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.foundation.extension.tagId

@Composable
fun SettingsItem(
    text: String,
    external: Boolean = false,
    onClick: () -> Unit
) {
    val icon = if (external) {
        Icons.AutoMirrored.Filled.OpenInNew
    } else {
        Icons.AutoMirrored.Filled.KeyboardArrowRight
    }
    Row(
        Modifier
            .testTag("btn_${text.tagId()}")
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                vertical = 24.dp,
                horizontal = 20.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .testTag("txt_${text.tagId()}")
                .weight(1f),
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontFamily = ttRoundsFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
            ),
            color = MaterialTheme.appColors.textPrimary
        )
        Icon(
            modifier = Modifier.size(22.dp),
            imageVector = icon,
            contentDescription = null
        )
    }
}

@Composable
fun SettingsDivider() {
    Divider(
        modifier = Modifier
            .padding(
                horizontal = 20.dp
            ),
        color = MaterialTheme.appColors.divider
    )
}
