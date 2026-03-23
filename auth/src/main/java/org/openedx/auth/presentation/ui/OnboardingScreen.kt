package org.openedx.auth.presentation.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openedx.auth.R as AuthR
import org.openedx.core.R as CoreR
import org.openedx.core.ui.theme.OpenEdXTheme
import org.openedx.core.ui.theme.accent_orange
import org.openedx.core.ui.theme.accent_pink
import org.openedx.core.ui.theme.brand_cream
import org.openedx.core.ui.theme.brand_guinda
import org.openedx.core.ui.theme.brand_green

// ── Tipografía ────────────────────────────────────────────────────────────────
private val frauncesFamily = FontFamily(
    Font(CoreR.font.fraunces_black, FontWeight.Black, FontStyle.Normal),
    Font(CoreR.font.fraunces_black, FontWeight.Bold, FontStyle.Normal),
    Font(CoreR.font.fraunces_light_italic, FontWeight.Light, FontStyle.Italic),
)

// ── Cursos mock ───────────────────────────────────────────────────────────────
private data class CoursePreview(
    val title: String,
    val org: String,
    val gradient: Brush,
    val accentColor: Color,
)

private val previewCourses = listOf(
    CoursePreview(
        "Habilidades\nSocioafectivas\nen el aprendizaje", "UnADM",
        Brush.verticalGradient(listOf(Color(0xFF1A3A2A), Color(0xFF2B6959))), accent_orange,
    ),
    CoursePreview(
        "Educación\nSocio-\nemocional", "@prende.mx",
        Brush.verticalGradient(listOf(Color(0xFF1A2A3A), Color(0xFF2A4A6A))), accent_pink,
    ),
    CoursePreview(
        "Inteligencia\nArtificial\nAplicada", "Microsoft",
        Brush.verticalGradient(listOf(Color(0xFF0D1A2E), Color(0xFF1A3A5A))), brand_green,
    ),
    CoursePreview(
        "Sistemas\nSocio-\necológicos", "UNAM",
        Brush.verticalGradient(listOf(Color(0xFF2A1A0A), Color(0xFF5A3A1A))), accent_orange,
    ),
    CoursePreview(
        "Enseñanza\nMusical\nMexicana", "@prende.mx",
        Brush.verticalGradient(listOf(Color(0xFF3A1A1A), Color(0xFF6A2A2A))), accent_pink,
    ),
)

// ── Pantalla principal ────────────────────────────────────────────────────────
@Composable
fun OnboardingScreen(
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit,
    onLlaveMxSignIn: () -> Unit,
    onSignInWithCredentials: (String, String) -> Unit,
    onSearchClick: () -> Unit,
) {
    val alpha = remember { Animatable(0f) }
    val cardOffset = remember { Animatable(80f) }

    val infiniteTransition = rememberInfiniteTransition(label = "bgGradient")
    val gradientProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(7000, easing = LinearEasing),
            RepeatMode.Reverse,
        ),
        label = "gradientProgress",
    )
    val bgColor1 = lerp(Color(0xFF2B6959), Color(0xFF1D4D42), gradientProgress)
    val bgColor2 = lerp(Color(0xFF3D8A72), Color(0xFF2B6959), gradientProgress)

    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        launch { cardOffset.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = 180f)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha.value),
    ) {
        // ── Fondo verde animado que cubre la mitad superior ──────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(bgColor1, bgColor2, bgColor1),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, 840f),
                    )
                ),
        ) {
            DecorativeCircles()
            FloatingNumbers()
        }

        // ── Barra guinda institucional ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(brand_guinda),
        )

        // ── Contenido scrollable ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Sección verde: logo + headline + pill stats
            GreenHeroSection()

            // Tarjeta crema que flota sobre el verde
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-32 + cardOffset.value).dp),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = brand_cream,
                elevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                ) {
                    // Handle visual (pastilla)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFCCC8C0)),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Carrusel de cursos
                    CourseCarousel()

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botones de acción
                    ActionButtons(
                        onCreateAccount = onCreateAccount,
                        onSignIn = onSignIn,
                        onLlaveMxSignIn = onLlaveMxSignIn,
                        onSignInWithCredentials = onSignInWithCredentials,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats con iconos reales
                    StatsRow()

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buscador
                    SearchPill(onClick = onSearchClick)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Footer institucional
                    InstitutionalFooter()
                }
            }
        }
    }
}

// ── Círculos decorativos (como el web) ───────────────────────────────────────
@Composable
private fun DecorativeCircles() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Círculo grande top-right
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 240.dp, y = (-60).dp)
                .clip(CircleShape)
                .border(36.dp, Color.White.copy(alpha = 0.06f), CircleShape),
        )
        // Círculo mediano bottom-left
        Box(
            modifier = Modifier
                .size(130.dp)
                .offset(x = (-30).dp, y = 280.dp)
                .clip(CircleShape)
                .border(22.dp, Color.White.copy(alpha = 0.05f), CircleShape),
        )
        // Círculo pequeño mid-right
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 300.dp, y = 200.dp)
                .clip(CircleShape)
                .border(14.dp, Color.White.copy(alpha = 0.07f), CircleShape),
        )
    }
}

// ── Números flotantes decorativos ─────────────────────────────────────────────
@Composable
private fun FloatingNumbers() {
    data class FloatNum(val text: String, val xFrac: Float, val yFrac: Float, val phaseMs: Int, val size: Float)
    val nums = listOf(
        FloatNum("1,359", 0.06f, 0.14f, 0,    24f),
        FloatNum("2.7M",  0.70f, 0.06f, 900,  20f),
        FloatNum("95",    0.82f, 0.48f, 450,  28f),
        FloatNum("3.2M",  0.12f, 0.64f, 1300, 18f),
        FloatNum("100%",  0.55f, 0.76f, 650,  22f),
    )
    val infiniteTransition = rememberInfiniteTransition(label = "floatNums")
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        nums.forEach { num ->
            val floatY by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    tween(2800, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse,
                    StartOffset(num.phaseMs),
                ),
                label = "floatY_${num.text}",
            )
            Text(
                text = num.text,
                style = TextStyle(
                    fontSize = num.size.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = frauncesFamily,
                    color = Color.White.copy(alpha = 0.09f),
                    letterSpacing = (-0.5).sp,
                ),
                modifier = Modifier.absoluteOffset(
                    x = w * num.xFrac,
                    y = h * num.yFrac + floatY.dp,
                ),
            )
        }
    }
}

// ── Sección hero verde ────────────────────────────────────────────────────────
@Composable
private fun GreenHeroSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Logo en blanco
        Image(
            painter = painterResource(id = AuthR.drawable.aprende_logo_marquesina),
            contentDescription = "Cursos @prende.mx",
            modifier = Modifier
                .width(180.dp)
                .padding(horizontal = 16.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Headline editorial
        val headline = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontFamily = frauncesFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 38.sp,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                )
            ) { append("Aprende\n") }
            withStyle(
                SpanStyle(
                    fontFamily = frauncesFamily,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    fontSize = 34.sp,
                    color = Color.White.copy(alpha = 0.80f),
                    letterSpacing = (-0.5).sp,
                )
            ) { append("sin límites\n") }
            withStyle(
                SpanStyle(
                    fontFamily = frauncesFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 38.sp,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                )
            ) { append("con la SEP.") }
        }

        Text(
            text = headline,
            textAlign = TextAlign.Center,
            lineHeight = 42.sp,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Pill stats compacto
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB8E0D4)),
            )
            Text(
                text = "95 instituciones  ·  1,359 cursos  ·  100% gratuito",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 0.2.sp,
                ),
            )
        }
    }
}

// ── Carrusel de cursos ────────────────────────────────────────────────────────
@Composable
private fun CourseCarousel() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(previewCourses) { course ->
            CoursePortraitCard(course)
        }
    }
}

@Composable
private fun CoursePortraitCard(course: CoursePreview) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0DDD7), RoundedCornerShape(14.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(course.accentColor),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(158.dp)
                .background(course.gradient),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.40f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
            ) {
                Text(
                    text = course.org,
                    style = TextStyle(fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White),
                )
            }
            Text(
                text = course.title,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 16.sp,
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
            )
        }
    }
}

// ── Botones ───────────────────────────────────────────────────────────────────
@Composable
private fun ActionButtons(
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit,
    onLlaveMxSignIn: () -> Unit,
    onSignInWithCredentials: (String, String) -> Unit,
) {

    // Shimmer del botón LlaveMX
    val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by shimmerTransition.animateFloat(
        initialValue = -350f,
        targetValue = 800f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = LinearEasing),
            RepeatMode.Restart,
            StartOffset(2200),
        ),
        label = "shimmerX",
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.18f), Color.Transparent),
        start = Offset(shimmerX, 0f),
        end = Offset(shimmerX + 220f, 104f),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        // ── Card LlaveMX ─────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            elevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // Título
                Text(
                    text = "Inicia sesión con tu cuenta",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3D3A36),
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Logo LlaveMX (ícono + texto en colores originales)
                Image(
                    painter = painterResource(id = AuthR.drawable.ic_llavemx_logo),
                    contentDescription = "Llave MX",
                    modifier = Modifier
                        .width(160.dp)
                        .height(108.dp),
                    contentScale = ContentScale.Fit,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Iniciar sesión — guinda con shimmer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(brand_guinda)
                        .clickable(onClick = onLlaveMxSignIn),
                    contentAlignment = Alignment.Center,
                ) {
                    // Capa shimmer
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(shimmerBrush),
                    )
                    Text(
                        "Iniciar sesión",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE8E3DC)),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // "¿Aún no tienes cuenta?"
                Text(
                    text = "¿Aún no tienes una cuenta LlaveMX?",
                    style = TextStyle(fontSize = 13.sp, color = Color(0xFF9A9590)),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Crear cuenta — link guinda
                Text(
                    text = "Crear cuenta",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = brand_guinda,
                    ),
                    modifier = Modifier.clickable(onClick = onCreateAccount),
                )
            }
        }

        // ── Acceso tradicional — botón directo a SignIn ──────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFD0CBC3), RoundedCornerShape(12.dp))
                .clickable(onClick = onSignIn)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Iniciar sesión con correo y contraseña",
                style = TextStyle(fontSize = 13.sp, color = Color(0xFF9A9590)),
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF9A9590),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Stats ─────────────────────────────────────────────────────────────────────
@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatItem(CoreR.drawable.aprende_ic_registradas,   "2.7M",   "Personas\nregistradas", Modifier.weight(1f))
        StatItem(CoreR.drawable.aprende_ic_constancias,   "3.2M",   "Constancias\nentregadas", Modifier.weight(1f))
        StatItem(CoreR.drawable.aprende_ic_colaboradores, "95",     "Instituciones\naliadas", Modifier.weight(1f))
        StatItem(CoreR.drawable.aprende_ic_impartidos,    "1,359",  "Cursos\nimpartidos", Modifier.weight(1f))
    }
}

@Composable
private fun StatItem(iconRes: Int, number: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = brand_green,
            modifier = Modifier.size(26.dp),
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = number,
            style = TextStyle(
                fontFamily = frauncesFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = brand_green,
                letterSpacing = (-0.5).sp,
            ),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = TextStyle(fontSize = 8.5.sp, color = Color(0xFF9A9590), lineHeight = 11.sp, textAlign = TextAlign.Center),
            textAlign = TextAlign.Center,
        )
    }
}

// ── Buscador ──────────────────────────────────────────────────────────────────
@Composable
private fun SearchPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFD6D2CC), RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, null, tint = Color(0xFF9A9590), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text("Busca tu curso", style = TextStyle(fontSize = 14.sp, color = Color(0xFFB0AB9F)), modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(brand_green)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text("Buscar", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
        }
    }
}

// ── Footer ────────────────────────────────────────────────────────────────────
@Composable
private fun InstitutionalFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFDFD4C2))
            .padding(vertical = 18.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = AuthR.drawable.aprende_logo_educacion),
            contentDescription = "Educación SEP · @prende.mx",
            modifier = Modifier.fillMaxWidth().height(38.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    OpenEdXTheme {
        OnboardingScreen(
            onCreateAccount = {},
            onSignIn = {},
            onLlaveMxSignIn = {},
            onSignInWithCredentials = { _, _ -> },
            onSearchClick = {},
        )
    }
}
