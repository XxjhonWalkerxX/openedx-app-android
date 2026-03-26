package org.openedx.profile.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.ttRoundsCompressedMedium
import org.openedx.core.ui.theme.ttRoundsFamily
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.openedx.core.R
import org.openedx.core.domain.model.ProfileImage
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.profile.domain.model.Account
import org.openedx.profile.R as ProfileR

@Composable
fun ProfileTopic(image: String, title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .error(R.drawable.core_ic_default_profile_picture)
                .placeholder(R.drawable.core_ic_default_profile_picture)
                .build(),
            contentDescription = stringResource(
                id = R.string.core_accessibility_user_profile_image,
                title
            ),
            modifier = Modifier
                .testTag("img_profile")
                .size(80.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (title.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .testTag("txt_profile_name")
                        .fillMaxWidth(),
                    text = title,
                    color = MaterialTheme.appColors.textPrimary,
                    style = MaterialTheme.appTypography.titleLarge
                )
            }
            Text(
                modifier = Modifier
                    .testTag("txt_profile_username")
                    .fillMaxWidth(),
                text = subtitle,
                color = MaterialTheme.appColors.textPrimary,
                style = MaterialTheme.appTypography.bodyMedium
            )
        }
    }
}

@Composable
fun ProfileInfoSection(account: Account) {
    if (account.bio.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = 2.dp,
            backgroundColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = ProfileR.string.profile_about_me),
                    style = TextStyle(
                        fontFamily = ttRoundsCompressedMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = brand_green,
                        letterSpacing = (-0.1).sp,
                    ),
                )
                Text(
                    modifier = Modifier
                        .testTag("txt_profile_bio")
                        .fillMaxWidth(),
                    text = account.bio,
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color(0xFF5A5650),
                    ),
                )
            }
        }
    }
}

val mockAccount = Account(
    username = "thom84",
    bio = "He as compliment unreserved projecting. Between had observe pretend delight for believe. Do newspaper " +
            "questions consulted sweetness do. Our sportsman his unwilling fulfilled departure law.",
    requiresParentalConsent = true,
    name = "Thomas",
    country = "Ukraine",
    isActive = true,
    profileImage = ProfileImage("", "", "", "", false),
    yearOfBirth = 2000,
    levelOfEducation = "Bachelor",
    goals = "130",
    languageProficiencies = emptyList(),
    gender = "male",
    mailingAddress = "",
    "example@email.com",
    null,
    accountPrivacy = Account.Privacy.ALL_USERS
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileTopicPreview() {
    OpenEdXTheme {
        ProfileTopic(
            image = mockAccount.profileImage.imageUrlFull,
            title = mockAccount.name,
            subtitle = mockAccount.username,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileInfoSectionPreview() {
    OpenEdXTheme {
        ProfileInfoSection(
            account = mockAccount
        )
    }
}
