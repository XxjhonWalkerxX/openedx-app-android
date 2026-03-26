package org.openedx.auth.presentation.signin.compose

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.auth.R
import org.openedx.auth.presentation.signin.AuthEvent
import org.openedx.auth.presentation.signin.SignInUIState
import org.openedx.auth.presentation.ui.PasswordVisibilityIcon
import org.openedx.core.extension.TextConverter
import org.openedx.core.ui.HandleUIMessage
import org.openedx.core.ui.HyperlinkText
import org.openedx.core.ui.noRippleClickable
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.brand_cream
import org.openedx.core.ui.theme.brand_green
import org.openedx.core.ui.theme.brand_guinda
import org.openedx.foundation.presentation.UIMessage
import org.openedx.foundation.presentation.WindowSize
import org.openedx.foundation.presentation.WindowType
import org.openedx.foundation.presentation.windowSizeValue
import androidx.compose.material.rememberScaffoldState
import org.openedx.core.R as coreR
import org.openedx.auth.R as AuthR

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun LoginScreen(
    windowSize: WindowSize,
    state: SignInUIState,
    uiMessage: UIMessage?,
    onEvent: (AuthEvent) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()

    HandleUIMessage(uiMessage = uiMessage, scaffoldState = scaffoldState)

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Fondo verde hero (45% superior) ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .background(brand_green),
        ) {
            // Círculos decorativos
            Box(modifier = Modifier.size(180.dp).offset(x = 220.dp, y = (-40).dp)
                .clip(CircleShape).border(28.dp, Color.White.copy(alpha = 0.06f), CircleShape))
            Box(modifier = Modifier.size(100.dp).offset(x = (-20).dp, y = 200.dp)
                .clip(CircleShape).border(18.dp, Color.White.copy(alpha = 0.05f), CircleShape))
        }

        // ── Barra guinda ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(brand_guinda),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Hero verde: back + logo + slogan ─────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 32.dp),
            ) {
                // Botón back
                if (state.isLogistrationEnabled) {
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onEvent(AuthEvent.BackClick) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = if (state.isLogistrationEnabled) 32.dp else 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Logo @prende.mx en blanco
                    Image(
                        painter = painterResource(id = AuthR.drawable.aprende_logo_marquesina),
                        contentDescription = "Cursos @prende.mx",
                        modifier = Modifier
                            .width(140.dp)
                            .padding(horizontal = 16.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color.White),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Slogan en blanco
                    Image(
                        painter = painterResource(id = AuthR.drawable.aprende_slogan),
                        contentDescription = "La plataforma de cursos en línea",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color.White),
                    )
                }
            }

            // ── Tarjeta crema flotando sobre el verde ─────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = brand_cream,
                elevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Handle
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFCCC8C0)),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val contentPaddings by remember {
                        mutableStateOf(
                            windowSize.windowSizeValue(
                                expanded = Modifier.widthIn(Dp.Unspecified, 420.dp),
                                compact = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            )
                        )
                    }
                    val buttonWidth by remember(windowSize) {
                        mutableStateOf(
                            windowSize.windowSizeValue(
                                expanded = Modifier.widthIn(232.dp, Dp.Unspecified),
                                compact = Modifier.fillMaxWidth(),
                            )
                        )
                    }

                    Column(modifier = contentPaddings) {
                        AuthForm(buttonWidth, state, onEvent)
                        state.agreement?.let {
                            Spacer(modifier = Modifier.height(20.dp))
                            val linkedText = TextConverter.htmlTextToLinkedText(state.agreement.label)
                            HyperlinkText(
                                modifier = Modifier.testTag("txt_${state.agreement.name}"),
                                fullText = linkedText.text,
                                hyperLinks = linkedText.links,
                                linkTextColor = brand_green,
                                linkTextDecoration = TextDecoration.Underline,
                                action = { link -> onEvent(AuthEvent.OpenLink(linkedText.links, link)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
    var showTraditionalLogin by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        // ── Card LlaveMX ─────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            elevation = 2.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.llavemx_card_title),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3D3A36),
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Logo LlaveMX correcto (ícono dorado + texto guinda)
                Image(
                    painter = painterResource(id = R.drawable.ic_llavemx_logo),
                    contentDescription = "Llave MX",
                    modifier = Modifier
                        .width(180.dp)
                        .height(56.dp),
                    contentScale = ContentScale.Fit,
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (state.showProgress) {
                    CircularProgressIndicator(color = brand_guinda)
                } else {
                    Box(
                        modifier = buttonWidth
                            .testTag("btn_llavemx")
                            .height(52.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(brand_guinda)
                            .clickable {
                                keyboardController?.hide()
                                onEvent(AuthEvent.LlaveMxSignIn)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(id = R.string.llavemx_sign_in_button),
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            ),
                        )
                    }
                }
            }
        }

        // ── Acceso con correo (colapsable) ────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            elevation = 1.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleClickable { showTraditionalLogin = !showTraditionalLogin }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.llavemx_traditional_login_label),
                        style = TextStyle(fontSize = 13.sp, color = Color(0xFF7A7060)),
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = if (showTraditionalLogin) Icons.Filled.KeyboardArrowUp
                                      else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF9A9590),
                        modifier = Modifier.size(20.dp),
                    )
                }

                if (showTraditionalLogin) {
                    Divider(color = Color(0xFFF0EDE8), thickness = 1.dp)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (!state.isBrowserLoginEnabled) {
                            // Campo correo/usuario
                            BrandTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tf_login"),
                                label = stringResource(id = R.string.auth_email_username),
                                placeholder = stringResource(id = R.string.auth_enter_email_username),
                                isError = isEmailError,
                                errorText = stringResource(id = R.string.auth_error_empty_username_email),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                onValueChanged = { login = it; isEmailError = false },
                            )

                            // Campo contraseña
                            BrandPasswordField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tf_password"),
                                isError = isPasswordError,
                                onValueChanged = { password = it; isPasswordError = false },
                                onPressDone = {
                                    keyboardController?.hide()
                                    if (login.isNotEmpty() && password.isNotEmpty()) {
                                        onEvent(AuthEvent.SignIn(login, password))
                                    } else {
                                        isEmailError = login.isEmpty()
                                        isPasswordError = password.isEmpty()
                                    }
                                },
                            )
                        }

                        // ¿Olvidaste tu contraseña?
                        Text(
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("txt_forgot_password")
                                .noRippleClickable { onEvent(AuthEvent.ForgotPasswordClick) },
                            text = stringResource(id = R.string.auth_forgot_password),
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = brand_green,
                                fontWeight = FontWeight.Medium,
                            ),
                        )

                        // Botón Inicia sesión
                        Box(
                            modifier = buttonWidth
                                .testTag("btn_sign_in")
                                .height(52.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(brand_green)
                                .clickable {
                                    if (state.isBrowserLoginEnabled) {
                                        onEvent(AuthEvent.SignInBrowser)
                                    } else {
                                        keyboardController?.hide()
                                        if (login.isNotEmpty() && password.isNotEmpty()) {
                                            onEvent(AuthEvent.SignIn(login, password))
                                        } else {
                                            isEmailError = login.isEmpty()
                                            isPasswordError = password.isEmpty()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(id = coreR.string.core_sign_in),
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Campos de texto con estilo de marca ───────────────────────────────────────
@Composable
private fun BrandTextField(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    isError: Boolean,
    errorText: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChanged: (String) -> Unit,
) {
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = brand_green,
                letterSpacing = 0.3.sp,
            ),
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = textFieldValue,
            onValueChange = { textFieldValue = it; onValueChanged(it.text.trim()) },
            placeholder = {
                Text(text = placeholder, style = TextStyle(fontSize = 14.sp, color = Color(0xFFB0AB9F)))
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color(0xFF1C1C1C),
                backgroundColor = Color(0xFFF8F6F2),
                unfocusedBorderColor = Color(0xFFDDD9D2),
                focusedBorderColor = brand_green,
                cursorColor = brand_green,
                errorBorderColor = Color(0xFFD94F4F),
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = keyboardOptions,
            isError = isError,
            singleLine = true,
        )
        if (isError) {
            Text(
                text = errorText,
                style = TextStyle(fontSize = 11.sp, color = Color(0xFFD94F4F)),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun BrandPasswordField(
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

    Column(modifier = modifier) {
        Text(
            text = stringResource(id = coreR.string.core_password),
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = brand_green,
                letterSpacing = 0.3.sp,
            ),
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("tf_password"),
            value = passwordTextFieldValue,
            onValueChange = { passwordTextFieldValue = it; onValueChanged(it.text.trim()) },
            placeholder = {
                Text(
                    text = stringResource(id = R.string.auth_enter_password),
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFFB0AB9F)),
                )
            },
            trailingIcon = {
                PasswordVisibilityIcon(
                    isPasswordVisible = isPasswordVisible,
                    onClick = { isPasswordVisible = !isPasswordVisible },
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color(0xFF1C1C1C),
                backgroundColor = Color(0xFFF8F6F2),
                unfocusedBorderColor = Color(0xFFDDD9D2),
                focusedBorderColor = brand_green,
                cursorColor = brand_green,
                errorBorderColor = Color(0xFFD94F4F),
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardActions = KeyboardActions { focusManager.clearFocus(); onPressDone() },
            isError = isError,
            singleLine = true,
        )
        if (isError) {
            Text(
                text = stringResource(id = R.string.auth_error_empty_password),
                style = TextStyle(fontSize = 11.sp, color = Color(0xFFD94F4F)),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────
@Preview(uiMode = UI_MODE_NIGHT_NO, showSystemUi = true)
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

@Preview(uiMode = UI_MODE_NIGHT_NO, showSystemUi = true)
@Composable
private fun SignInExpandedPreview() {
    OpenEdXTheme {
        LoginScreen(
            windowSize = WindowSize(WindowType.Compact, WindowType.Compact),
            state = SignInUIState().copy(isBrowserLoginEnabled = true),
            uiMessage = null,
            onEvent = {},
        )
    }
}

@Preview(name = "NEXUS_9", device = Devices.NEXUS_9, uiMode = UI_MODE_NIGHT_NO)
@Composable
private fun SignInTabletPreview() {
    OpenEdXTheme {
        LoginScreen(
            windowSize = WindowSize(WindowType.Expanded, WindowType.Expanded),
            state = SignInUIState(),
            uiMessage = null,
            onEvent = {},
        )
    }
}
