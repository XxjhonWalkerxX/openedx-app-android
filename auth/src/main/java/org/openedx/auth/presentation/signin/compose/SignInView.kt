package org.openedx.auth.presentation.signin.compose

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openedx.auth.R
import org.openedx.auth.presentation.signin.AuthEvent
import org.openedx.auth.presentation.signin.SignInUIState
import org.openedx.auth.presentation.ui.LoginTextField
import org.openedx.auth.presentation.ui.PasswordVisibilityIcon
import org.openedx.core.extension.TextConverter
import org.openedx.core.ui.BackBtn
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.HyperlinkText
import org.openedx.core.ui.OpenEdXButton
import org.openedx.core.ui.displayCutoutForLandscape
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.appColors
import org.openedx.core.ui.theme.appShapes
import org.openedx.core.ui.theme.appTypography
import org.openedx.core.ui.theme.compose.SignInLogoView
import org.openedx.core.ui.theme.llave_mx_primary
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue
import org.openedx.core.R as coreR

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LoginScreen(
    windowSize: WindowSize,
    state: SignInUIState,
    uiMessage: UIMessage?,
    onEvent: (AuthEvent) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val scrollState = rememberScrollState()

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .semantics {
                testTagsAsResourceId = true
            }
            .fillMaxSize()
            .navigationBarsPadding(),
        backgroundColor = MaterialTheme.appColors.background
    ) {
        val contentPaddings by remember {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier
                        .widthIn(Dp.Unspecified, 420.dp)
                        .padding(
                            top = 32.dp,
                            bottom = 40.dp
                        ),
                    compact = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                )
            )
        }
        val buttonWidth by remember(key1 = windowSize) {
            mutableStateOf(
                windowSize.windowSizeValue(
                    expanded = Modifier.widthIn(232.dp, Dp.Unspecified),
                    compact = Modifier.fillMaxWidth()
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.3f)
                .background(MaterialTheme.appColors.primary)
        )
        HandleUIMessage(
            uiMessage = uiMessage,
            scaffoldState = scaffoldState
        )
        if (state.isLogistrationEnabled) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                BackBtn(
                    modifier = Modifier.padding(end = 16.dp),
                    tint = Color.White
                ) {
                    onEvent(AuthEvent.BackClick)
                }
            }
        }
        Column(
            Modifier.padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignInLogoView()
            Surface(
                color = MaterialTheme.appColors.background,
                shape = MaterialTheme.appShapes.screenBackgroundShape,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.TopCenter) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.appColors.background)
                            .verticalScroll(scrollState)
                            .displayCutoutForLandscape()
                            .then(contentPaddings),
                    ) {
                        AuthForm(
                            buttonWidth,
                            state,
                            onEvent,
                        )
                        state.agreement?.let {
                            Spacer(modifier = Modifier.height(24.dp))
                            val linkedText =
                                TextConverter.htmlTextToLinkedText(state.agreement.label)
                            HyperlinkText(
                                modifier = Modifier.testTag("txt_${state.agreement.name}"),
                                fullText = linkedText.text,
                                hyperLinks = linkedText.links,
                                linkTextColor = MaterialTheme.appColors.textHyperLink,
                                linkTextDecoration = TextDecoration.Underline,
                                action = { link ->
                                    onEvent(AuthEvent.OpenLink(linkedText.links, link))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthForm(
    buttonWidth: Modifier,
    state: SignInUIState,
    onEvent: (AuthEvent) -> Unit,
) {
    var login by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isEmailError by rememberSaveable { mutableStateOf(false) }
    var isPasswordError by rememberSaveable { mutableStateOf(false) }
    var showTraditionalLogin by rememberSaveable { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // ── Tarjeta LlaveMX (opción principal) ──────────────────────────
        Card(
            shape = MaterialTheme.appShapes.cardShape,
            elevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.llavemx_card_title),
                    color = MaterialTheme.appColors.textPrimary,
                    style = MaterialTheme.appTypography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_llavemx_logo),
                    contentDescription = "Llave MX",
                    modifier = Modifier
                        .width(160.dp)
                        .height(108.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.height(28.dp))
                if (state.showProgress) {
                    CircularProgressIndicator(color = llave_mx_primary)
                } else {
                    OpenEdXButton(
                        modifier = buttonWidth.testTag("btn_llavemx"),
                        text = stringResource(id = R.string.llavemx_sign_in_button),
                        textColor = Color.White,
                        backgroundColor = llave_mx_primary,
                        onClick = {
                            keyboardController?.hide()
                            onEvent(AuthEvent.LlaveMxSignIn)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Banner de acceso tradicional (opción secundaria) ────────────
        androidx.compose.material.Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.appShapes.cardShape,
            border = BorderStroke(1.dp, MaterialTheme.appColors.divider),
            color = MaterialTheme.appColors.background,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                        .noRippleClickable { showTraditionalLogin = !showTraditionalLogin },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.llavemx_traditional_login_label),
                        style = MaterialTheme.appTypography.bodySmall,
                        color = MaterialTheme.appColors.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = if (showTraditionalLogin) Icons.Filled.KeyboardArrowUp
                                      else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.textSecondary,
                    )
                }

                if (showTraditionalLogin) {
                    if (!state.isBrowserLoginEnabled) {
                        LoginTextField(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(id = R.string.auth_email_username),
                            description = stringResource(id = R.string.auth_enter_email_username),
                            onValueChanged = {
                                login = it
                                isEmailError = false
                            },
                            isError = isEmailError,
                            errorMessages = stringResource(id = R.string.auth_error_empty_username_email),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PasswordTextField(
                            modifier = Modifier.fillMaxWidth(),
                            onValueChanged = {
                                password = it
                                isPasswordError = false
                            },
                            onPressDone = {
                                keyboardController?.hide()
                                if (login.isNotEmpty() && password.isNotEmpty()) {
                                    onEvent(AuthEvent.SignIn(login = login, password = password))
                                } else {
                                    isEmailError = login.isEmpty()
                                    isPasswordError = password.isEmpty()
                                }
                            },
                            isError = isPasswordError,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            modifier = Modifier
                                .testTag("txt_forgot_password")
                                .noRippleClickable { onEvent(AuthEvent.ForgotPasswordClick) },
                            text = stringResource(id = R.string.auth_forgot_password),
                            color = MaterialTheme.appColors.infoVariant,
                            style = MaterialTheme.appTypography.labelLarge,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OpenEdXButton(
                        modifier = buttonWidth.testTag("btn_sign_in"),
                        text = stringResource(id = coreR.string.core_sign_in),
                        textColor = MaterialTheme.appColors.primaryButtonText,
                        backgroundColor = MaterialTheme.appColors.secondaryButtonBackground,
                        onClick = {
                            if (state.isBrowserLoginEnabled) {
                                onEvent(AuthEvent.SignInBrowser)
                            } else {
                                keyboardController?.hide()
                                if (login.isNotEmpty() && password.isNotEmpty()) {
                                    onEvent(AuthEvent.SignIn(login = login, password = password))
                                } else {
                                    isEmailError = login.isEmpty()
                                    isPasswordError = password.isEmpty()
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun PasswordTextField(
    modifier: Modifier = Modifier,
    isError: Boolean,
    onValueChanged: (String) -> Unit,
    onPressDone: () -> Unit,
) {
    var passwordTextFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Text(
        modifier = Modifier
            .testTag("txt_password_label")
            .fillMaxWidth(),
        text = stringResource(id = coreR.string.core_password),
        color = MaterialTheme.appColors.textPrimary,
        style = MaterialTheme.appTypography.labelLarge
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        modifier = modifier.testTag("tf_password"),
        value = passwordTextFieldValue,
        onValueChange = {
            passwordTextFieldValue = it
            onValueChanged(it.text.trim())
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = MaterialTheme.appColors.textFieldText,
            backgroundColor = MaterialTheme.appColors.textFieldBackground,
            unfocusedBorderColor = MaterialTheme.appColors.textFieldBorder,
            cursorColor = MaterialTheme.appColors.textFieldText,
        ),
        shape = MaterialTheme.appShapes.textFieldShape,
        placeholder = {
            Text(
                modifier = Modifier.testTag("txt_password_placeholder"),
                text = stringResource(id = R.string.auth_enter_password),
                color = MaterialTheme.appColors.textFieldHint,
                style = MaterialTheme.appTypography.bodyMedium
            )
        },
        trailingIcon = {
            PasswordVisibilityIcon(
                isPasswordVisible = isPasswordVisible,
                onClick = { isPasswordVisible = !isPasswordVisible }
            )
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardActions = KeyboardActions {
            focusManager.clearFocus()
            onPressDone()
        },
        isError = isError,
        textStyle = MaterialTheme.appTypography.bodyMedium,
        singleLine = true,
    )
    if (isError) {
        Text(
            modifier = Modifier
                .testTag("txt_password_error")
                .fillMaxWidth()
                .padding(top = 4.dp),
            text = stringResource(id = R.string.auth_error_empty_password),
            style = MaterialTheme.appTypography.bodySmall,
            color = MaterialTheme.appColors.error,
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "NEXUS_5_Light", device = Devices.NEXUS_5, uiMode = UI_MODE_NIGHT_NO)
@Composable
private fun SignInScreenPreview() {
    OpenEdXTheme {
        LoginScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            state = SignInUIState(),
            uiMessage = null,
            onEvent = {},
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(name = "NEXUS_5_Light", device = Devices.NEXUS_5, uiMode = UI_MODE_NIGHT_NO)
@Composable
private fun SignInUsingBrowserScreenPreview() {
    OpenEdXTheme {
        LoginScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            state = SignInUIState().copy(
                isBrowserLoginEnabled = true,
            ),
            uiMessage = null,
            onEvent = {},
        )
    }
}

@Preview(name = "NEXUS_9_Light", device = Devices.NEXUS_9, uiMode = UI_MODE_NIGHT_NO)
@Composable
private fun SignInScreenTabletPreview() {
    OpenEdXTheme {
        LoginScreen(
            windowSize = WindowSize(WindowType.Expanded, WindowType.Expanded),
            state = SignInUIState().copy(
                isSocialAuthEnabled = true,
                isFacebookAuthEnabled = true,
                isGoogleAuthEnabled = true,
                isMicrosoftAuthEnabled = true,
            ),
            uiMessage = null,
            onEvent = {},
        )
    }
}
