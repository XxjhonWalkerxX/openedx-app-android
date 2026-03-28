package org.openedx.profile.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.openedx.core.AppUpdateState
import org.openedx.core.R
import org.openedx.core.domain.model.AgreementUrls
import org.openedx.core.presentation.global.AppData
import org.openedx.core.system.notifier.app.AppUpgradeEvent
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.statusBarsInset
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.ui.theme.brand_cream
import org.openedx.core.ui.theme.brand_cream_strong
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.brand_guinda
import org.openedx.core.ui.theme.heroGradientColors
import org.openedx.core.ui.theme.ttRoundsFamily
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.profile.domain.model.Configuration
import org.openedx.profile.R as profileR


@Composable
internal fun SettingsScreen(
    windowSize: WindowSize,
    uiState: SettingsUIState,
    onBackClick: () -> Unit,
    onAction: (SettingsScreenAction) -> Unit,
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    val contentWidth by remember(key1 = windowSize) {
        mutableStateOf(
            windowSize.windowSizeValue(
                expanded = Modifier.widthIn(Dp.Unspecified, 420.dp),
                compact = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brand_cream)
    ) {
        // Hero verde — banda guinda en top absoluto, identico a ProfileHero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = heroGradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, 440f),
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(brand_guinda)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsInset()
                    .displayCutoutForLandscape()
            ) {
                // Header con titulo + subtitulo — identico al diseno HTML
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            ),
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.core_settings),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.White,
                            )
                        )
                        Text(
                            text = stringResource(id = profileR.string.profile_settings_subtitle),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        )
                    }
                }
            }
        }

        if (showLogoutDialog) {
            LogoutDialog(
                onDismissRequest = { showLogoutDialog = false },
                onLogoutClick = {
                    showLogoutDialog = false
                    onAction(SettingsScreenAction.LogoutClick)
                }
            )
        }

        // Contenido — Surface redondeado que emerge del hero
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = brand_cream
        ) {
            Box(contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .displayCutoutForLandscape(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (uiState) {
                        is SettingsUIState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = brand_green)
                            }
                        }

                        is SettingsUIState.Data -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .then(contentWidth)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Spacer(Modifier.height(8.dp))

                                // Seccion: Cuenta
                                SettingsSection(
                                    title = stringResource(id = R.string.core_manage_account)
                                ) {
                                    SettingsCardItem(
                                        text = stringResource(id = R.string.core_manage_account),
                                        testTag = "btn_manage_account",
                                        onClick = { onAction(SettingsScreenAction.ManageAccountClick) }
                                    )
                                }

                                // Seccion: Settings
                                SettingsSection(
                                    title = stringResource(id = R.string.core_settings)
                                ) {
                                    SettingsCardItem(
                                        text = stringResource(id = profileR.string.profile_video),
                                        testTag = "btn_video",
                                        onClick = { onAction(SettingsScreenAction.VideoSettingsClick) }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    SettingsCardItem(
                                        text = stringResource(id = profileR.string.profile_dates_and_calendar),
                                        testTag = "btn_dates_calendar",
                                        onClick = { onAction(SettingsScreenAction.CalendarSettingsClick) }
                                    )
                                }

                                // Seccion: Soporte
                                SettingsSection(
                                    title = stringResource(id = profileR.string.profile_support_info)
                                ) {
                                    if (uiState.configuration.supportEmail.isNotBlank()) {
                                        SettingsCardItem(
                                            text = stringResource(id = profileR.string.profile_contact_support),
                                            testTag = "btn_contact_support",
                                            onClick = { onAction(SettingsScreenAction.SupportClick) }
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    if (uiState.configuration.agreementUrls.tosUrl.isNotBlank()) {
                                        SettingsCardItem(
                                            text = stringResource(id = R.string.core_terms_of_use),
                                            testTag = "btn_terms",
                                            onClick = { onAction(SettingsScreenAction.TermsClick) }
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    if (uiState.configuration.agreementUrls.privacyPolicyUrl.isNotBlank()) {
                                        SettingsCardItem(
                                            text = stringResource(id = R.string.core_privacy_policy),
                                            testTag = "btn_privacy",
                                            onClick = { onAction(SettingsScreenAction.PrivacyPolicyClick) }
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    if (uiState.configuration.agreementUrls.cookiePolicyUrl.isNotBlank()) {
                                        SettingsCardItem(
                                            text = stringResource(id = R.string.core_cookie_policy),
                                            testTag = "btn_cookie",
                                            onClick = { onAction(SettingsScreenAction.CookiePolicyClick) }
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    if (uiState.configuration.agreementUrls.dataSellConsentUrl.isNotBlank()) {
                                        SettingsCardItem(
                                            text = stringResource(id = R.string.core_data_sell),
                                            testTag = "btn_data_sell",
                                            onClick = { onAction(SettingsScreenAction.DataSellClick) }
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    if (uiState.configuration.faqUrl.isNotBlank()) {
                                        val uriHandler = LocalUriHandler.current
                                        SettingsCardItem(
                                            text = stringResource(id = R.string.core_faq),
                                            testTag = "btn_faq",
                                            external = true,
                                            onClick = {
                                                uriHandler.openUri(uiState.configuration.faqUrl)
                                                onAction(SettingsScreenAction.FaqClick)
                                            }
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    // Version badge — crema, centrado
                                    AppVersionItem(
                                        versionName = uiState.configuration.versionName,
                                        appUpgradeEvent = AppUpdateState.lastAppUpgradeEvent,
                                        onClick = { onAction(SettingsScreenAction.AppVersionClick) }
                                    )
                                }

                                // Log Out — boton outline guinda
                                LogoutButton(onClick = { showLogoutDialog = true })

                                Spacer(Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Section wrapper con label uppercase ──────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            style = TextStyle(
                fontFamily = ttRoundsFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                color = brand_green,
            )
        )
        content()
    }
}

// ── Card individual por item — fondo blanco con sombra ───────────────────────

@Composable
private fun SettingsCardItem(
    text: String,
    testTag: String,
    external: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .testTag("txt_$testTag")
                    .weight(1f),
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = ttRoundsFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFF1a1a1a),
                )
            )
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = if (external)
                    Icons.AutoMirrored.Filled.OpenInNew
                else
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = brand_green,
            )
        }
    }
}

// ── Version badge — crema, identico al HTML ──────────────────────────────────

@Composable
private fun AppVersionItem(
    versionName: String,
    appUpgradeEvent: AppUpgradeEvent?,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(brand_cream_strong)
            .then(
                if (appUpgradeEvent != null)
                    Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(16.dp)
    ) {
        when (appUpgradeEvent) {
            is AppUpgradeEvent.UpgradeRecommendedEvent -> {
                Row(
                    modifier = Modifier
                        .testTag("btn_upgrade_recommended")
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            modifier = Modifier.testTag("txt_app_version_code"),
                            text = stringResource(id = R.string.core_version, versionName),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF1a1a1a),
                            )
                        )
                        Text(
                            modifier = Modifier.testTag("txt_upgrade_recommended"),
                            text = stringResource(
                                id = R.string.core_tap_to_update_to_version,
                                appUpgradeEvent.newVersionName
                            ),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = brand_green,
                            )
                        )
                    }
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.core_ic_icon_upgrade),
                        tint = brand_green,
                        contentDescription = null
                    )
                }
            }

            is AppUpgradeEvent.UpgradeRequiredEvent -> {
                Row(
                    modifier = Modifier
                        .testTag("btn_upgrade_required")
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(id = R.drawable.core_ic_warning),
                                contentDescription = null
                            )
                            Text(
                                modifier = Modifier.testTag("txt_app_version_code"),
                                text = stringResource(id = R.string.core_version, versionName),
                                style = TextStyle(
                                    fontFamily = ttRoundsFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1a1a1a),
                                )
                            )
                        }
                        Text(
                            modifier = Modifier.testTag("txt_upgrade_required"),
                            text = stringResource(id = R.string.core_tap_to_install_required_app_update),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = brand_green,
                            )
                        )
                    }
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.core_ic_icon_upgrade),
                        tint = brand_green,
                        contentDescription = null
                    )
                }
            }

            else -> {
                // Up-to-date — badge crema centrado
                Column(
                    modifier = Modifier
                        .testTag("txt_app_version_code")
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.core_version, versionName),
                        style = TextStyle(
                            fontFamily = ttRoundsFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1a1a1a),
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            painter = painterResource(id = R.drawable.core_ic_check),
                            contentDescription = null,
                            tint = brand_green,
                        )
                        Text(
                            modifier = Modifier.testTag("txt_up_to_date"),
                            text = stringResource(id = R.string.core_up_to_date),
                            style = TextStyle(
                                fontFamily = ttRoundsFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color(0xFF666666),
                            )
                        )
                    }
                }
            }
        }
    }
}

// ── Log Out — boton outline guinda, ancho completo ───────────────────────────

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .testTag("btn_logout")
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, brand_guinda, RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.testTag("txt_logout"),
                text = stringResource(id = profileR.string.profile_logout),
                style = TextStyle(
                    fontFamily = ttRoundsFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = brand_guinda,
                )
            )
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = profileR.drawable.profile_ic_logout),
                contentDescription = null,
                tint = brand_guinda,
            )
        }
    }
}

// ── Logout Dialog ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LogoutDialog(
    onDismissRequest: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        content = {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.appColors.background,
                        MaterialTheme.appShapes.cardShape
                    )
                    .clip(MaterialTheme.appShapes.cardShape)
                    .border(
                        1.dp,
                        MaterialTheme.appColors.cardViewBorder,
                        MaterialTheme.appShapes.cardShape
                    )
                    .padding(horizontal = 40.dp, vertical = 36.dp)
                    .semantics { testTagsAsResourceId = true },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(
                        modifier = Modifier
                            .testTag("ib_close")
                            .size(24.dp),
                        onClick = onDismissRequest
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(id = R.string.core_cancel),
                            tint = brand_green,
                        )
                    }
                }
                Icon(
                    modifier = Modifier
                        .width(88.dp)
                        .height(85.dp),
                    painter = painterResource(profileR.drawable.profile_ic_exit),
                    contentDescription = null,
                    tint = MaterialTheme.appColors.onBackground
                )
                Spacer(Modifier.size(36.dp))
                Text(
                    modifier = Modifier.testTag("txt_logout_dialog_title"),
                    text = stringResource(id = profileR.string.profile_logout_dialog_body),
                    style = TextStyle(
                        fontFamily = ttRoundsFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    ),
                    color = MaterialTheme.appColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(36.dp))
                OpenEdXButton(
                    text = stringResource(id = profileR.string.profile_logout),
                    backgroundColor = brand_guinda,
                    onClick = onLogoutClick,
                    content = {
                        Box(
                            Modifier
                                .testTag("btn_logout")
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                modifier = Modifier
                                    .testTag("txt_logout")
                                    .fillMaxWidth(),
                                text = stringResource(id = profileR.string.profile_logout),
                                style = TextStyle(
                                    fontFamily = ttRoundsFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                ),
                                textAlign = TextAlign.Center
                            )
                            Icon(
                                modifier = Modifier.testTag("ic_logout"),
                                painter = painterResource(id = profileR.drawable.profile_ic_logout),
                                contentDescription = null,
                                tint = Color.White,
                            )
                        }
                    }
                )
            }
        }
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val mockAppData = AppData(
    appName = "openedx",
    versionName = "1.0.0",
    applicationId = "org.example.com"
)

private val mockConfiguration = Configuration(
    agreementUrls = AgreementUrls(),
    faqUrl = "https://example.com/faq",
    supportEmail = "test@example.com",
    versionName = mockAppData.versionName,
)

private val mockUiState = SettingsUIState.Data(
    configuration = mockConfiguration
)

@Preview
@Composable
private fun SettingsScreenPreview() {
    OpenEdXTheme {
        SettingsScreen(
            onBackClick = {},
            windowSize = WindowSize(WindowType.Medium, WindowType.Medium),
            uiState = mockUiState,
            onAction = {},
        )
    }
}

@Preview
@Composable
fun AppVersionItemUpgradeRequiredPreview() {
    OpenEdXTheme {
        AppVersionItem(
            versionName = mockAppData.versionName,
            appUpgradeEvent = AppUpgradeEvent.UpgradeRequiredEvent,
            onClick = {}
        )
    }
}
