package org.openedx.core.ui.theme.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.openedx.core.R
import org.openedx.core.ui.theme.OpenEdXTheme

@Composable
fun SignInLogoView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.aprende_logo_marquesina),
            contentDescription = "@prende.mx",
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 12.dp)
                .widthIn(max = 200.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(widthDp = 375, heightDp = 200)
@Composable
fun SignInLogoViewPreview() {
    OpenEdXTheme {
        SignInLogoView()
    }
}
