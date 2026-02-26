package org.openedx.core.ui.theme.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.openedx.core.R
import org.openedx.core.ui.theme.OpenEdXTheme

/**
 * Logo institucional en pantalla de Login
 * Dimensiones equivalentes a iOS:
 * - maxWidth: 220 → 220.dp
 * - maxHeight: 131 → 131.dp
 */
@Composable
fun SignInLogoView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.22f),  // Ajustado para más espacio
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.core_ic_logo),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 16.dp)
                .widthIn(max = 220.dp)   // iOS: maxWidth 220
                .heightIn(max = 131.dp)  // iOS: maxHeight 131
        )
    }
}

@Preview(widthDp = 375, heightDp = 400)
@Composable
fun SignInLogoViewPreview() {
    OpenEdXTheme {
        SignInLogoView()
    }
}
