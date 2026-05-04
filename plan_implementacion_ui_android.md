# Plan de Implementación UI — Cursos @prende.mx Android

> **Documento canónico del sprint Android.** Espejo del plan iOS (`openedx-app-ios/plan_implementacion_ui.md`) adaptado a Jetpack Compose + multi-módulo Gradle + estrategia rebase-safe contra upstream openedx.
>
> **Principio rector:** la app iOS Cursos @prende.mx (post-login) define la paridad visual y el sistema de diseño. Este plan lo traduce a Compose nativo aislando todo el delta del fork en una capa propia para preservar merges futuros con `openedx/openedx-app-android`.
>
> **Branch base:** `theme-cursos`. **Branch upstream:** `openedx/main`. **Idioma:** `es_419` (es-MX). **Light mode forzado.** Sin paralelismo entre fases. Build verde por fase.
>
> **Última actualización:** 2026-04-30.

---

## 0. Glosario rápido

| Término | Significado |
|---|---|
| **Brand layer** | Carpeta `core/ui/theme/brand/` y `core/ui/brand/` — todo el delta visual del fork |
| **Brand component** | Composable nuevo prefijado `Brand*` (BrandTabBar, BrandProgressRing, etc.) |
| **Upstream file** | Archivo proveniente de `openedx/openedx-app-android` que NO debe editarse |
| **Delta file** | Archivo nuevo creado por el fork bajo `*Brand*.kt` o flavor `aprende` |
| **Flavor `aprende`** | Product flavor Gradle que aísla recursos y BuildConfig del fork |
| **Token** | Color, dp, sp o shape definido en `core/ui/theme/brand/` — única fuente |

---

## 1. Alcance y límites

### 1.1 Sí toca (rediseño completo post-login)

| Pantalla iOS de referencia | Módulo Android | Vista(s) Compose afectadas |
|---|---|---|
| `PrimaryCourseDashboardView` (Aprende) | `dashboard` | `LearnFragment` + `DashboardGalleryView`, `PrimaryCourseCard`, `CourseListItem` |
| `DiscoveryView` (Descubre) | `discovery` | `NativeDiscoveryFragment` + `NativeDiscoveryView`, `CourseCard`, `SearchView` |
| `CourseContainerView` (Detalle del curso) | `course` | `CourseContainerFragment`, `CourseHomeScreen`, `CourseOutlineScreen`, `CourseDatesScreen`, `OfflineQueueScreen`, `HandoutsScreen`, `CourseDiscussionScreen` |
| `ContentReaderView` / Lesson | `course` | `CourseUnitContainerFragment`, `VideoUnitScreen`, `HtmlUnitScreen`, `ProblemUnitScreen` |
| `ProfileView` | `profile` | `ProfileFragment`, `ProfileScreen`, `EditProfileScreen` |
| `SettingsView`, `ManageAccountView` | `profile` | `SettingsFragment`, `ManageAccountFragment`, `VideoSettingsFragment` |
| Tab bar global iOS `MainScreenView` | `app` | `MainScreenV2.kt` |

### 1.2 No toca (intocable absoluto)

Las siguientes vistas mantienen su diseño upstream — **no se rediseñan**:

- Auth: `auth/.../signin/`, `auth/.../signup/`, `auth/.../restore/`, `auth/.../logistration/`
- LlaveMX SSO: `auth/.../llavemx/` (ya integrado, no tocar UI)
- Splash: `app/.../AppActivity.kt` flujo de arranque
- WhatsNew: `whatsnew/`

> Si una refactorización en `core/ui/theme/` o `core/ui/` toca un símbolo usado por pre-login, **se preserva la API actual**. Tokens nuevos se AÑADEN, nunca se eliminan los existentes en este sprint.

### 1.3 Reglas duras

1. **Cero hex literal** fuera de `core/ui/theme/brand/`.
2. **Cero `.dp` / `.sp` mágicos** en feature modules — usar tokens `BrandSpacing` / `BrandTypography`.
3. **Light mode forzado** — `OpenEdXTheme(darkTheme = false)` siempre.
4. **`certificateData == null` jamás crashea** — fallback string "Disponible al completar el curso".
5. **`contentDescription` SIEMPRE viene del modelo**, nunca del bitmap.
6. **Pre-login intocable absoluto.**

---

## 2. Estrategia rebase-safe (delta files + flavor)

### 2.1 Patrón "delta files"

Nunca editar archivo upstream directamente. Crear archivo paralelo `*Brand*.kt` y conmutar vía un único entrypoint mínimo.

```
upstream/Foo.kt              ← intocable
fork/FooBrand.kt             ← nuevo
upstream/FooFragment.kt      ← editar 1 línea: if (BuildConfig.BRAND_APRENDE) FooBrand() else Foo()
```

Conflicto típico de rebase = una línea por feature. Resolución manual ≤ 5 minutos.

### 2.2 Product flavor `aprende`

`app/build.gradle` (y módulos feature):

```groovy
android {
    flavorDimensions += "brand"
    productFlavors {
        openedx {
            dimension "brand"
            buildConfigField "boolean", "BRAND_APRENDE", "false"
        }
        aprende {
            dimension "brand"
            buildConfigField "boolean", "BRAND_APRENDE", "true"
            applicationIdSuffix ".aprende"
            // versionNameSuffix "-aprende"  // opcional
        }
    }
}
```

Estructura de sources:

```
app/src/
├── main/                     ← código upstream + entrypoints minimal-patch
├── aprende/
│   ├── res/
│   │   ├── values/strings.xml         ← override es_419
│   │   ├── values/colors.xml          ← override @color/primary etc.
│   │   ├── drawable/                   ← logos, splash
│   │   ├── font/                       ← Noto Sans TTF
│   │   └── mipmap-*/                   ← launcher
│   └── java/                            ← lógica exclusiva fork si aplica
└── openedx/                             ← variant openedx vacía (defaults upstream)
```

Cualquier feature module espejo: `dashboard/src/aprende/...`, `course/src/aprende/...`.

### 2.3 Theme — extender, no modificar

`core/ui/theme/AppColors.kt` y `core/ui/theme/Theme.kt` se editan al **mínimo posible**:

- `Theme.kt` recibe ≤ 5 líneas de patch: forzar `darkTheme = false` y envolver con `CompositionLocalProvider(LocalBrand provides aprendeBrandTokens())`.
- `AppColors.kt` no se toca. Mapeos de campos (`primary`, `cardViewBackground`, etc.) se redirigen vía `light_*` que viven en `colors.xml` con override por flavor.

Toda lógica nueva en:

```
core/src/main/java/org/openedx/core/ui/theme/brand/
├── BrandPalette.kt
├── BrandTypography.kt
├── BrandShapes.kt
├── BrandElevations.kt
├── BrandSpacing.kt
├── BrandGradients.kt
├── BrandHaptics.kt
└── LocalBrand.kt          ← CompositionLocal + extensión MaterialTheme.brand
```

### 2.4 Configuración Git

```bash
git config rerere.enabled true
git config merge.conflictStyle zdiff3
```

`git rerere` aprende resoluciones de conflicto en archivos upstream tocados — los repite automáticamente en rebases sucesivos.

### 2.5 CI gate semanal

Workflow GitHub Actions opcional:

```yaml
- run: git fetch upstream
- run: git merge-tree $(git merge-base upstream/main HEAD) HEAD upstream/main > conflicts.txt
- run: |
    if grep -q "<<<<<<<" conflicts.txt; then
      echo "::warning::Upstream rebase tendrá conflictos"
    fi
```

Notifica si la huella sobre archivos upstream crece.

---

## 3. Sistema de diseño base

### 3.1 Paleta institucional (tokens canónicos)

Vive en `BrandPalette.kt`. Todos los hex de la spec iOS aquí, **una sola vez**.

```kotlin
object BrandPalette {
    // Institucionales
    val Guinda            = Color(0xFF611232)
    val GuindaDeep        = Color(0xFF3E0A20)
    val BrandGreen        = Color(0xFF2B6959)
    val BrandGreenDark    = Color(0xFF1D4D42)
    val BrandGreenLight   = Color(0xFF3D8A72)
    val BrandGreenLighter = Color(0xFF5BB89A)
    val BrandCream        = Color(0xFFECE9E4)
    val BrandCreamStrong  = Color(0xFFDFD4C2)
    val BrandHandle       = Color(0xFFC8C3BA)
    val SurfaceWhite      = Color(0xFFFFFFFF)

    // Texto
    val CardPrimary       = Color(0xFF1C1B18)
    val CardMedium        = Color(0xFF5A5650)
    val CardSecondary     = Color(0xFF9A9590)
    val TextInactive      = Color(0xFFAAAAAA)
    val TextOnHeader      = Color(0xFFFFFFFF)

    // UI
    val Divider           = Color(0xFFF0ECE8)
    val ProgressTrack     = Color(0xFFE5E0D8)
    val InputBackground   = Color(0xFFF7F6F2)
    val InputStroke       = Color(0xFFDBD6CE)
    val GreenTint         = BrandGreen.copy(alpha = 0.12f)
    val GreenSoft         = BrandGreen.copy(alpha = 0.06f)
    val CardStrokeSubtle  = Color.Black.copy(alpha = 0.04f)
    val CardShadowSubtle  = Color.Black.copy(alpha = 0.04f)
    val SemanticDestructive = Color(0xFF8C1430)

    // Pills/badges
    val PillGreen         = Color(0xFF9ADBC8)
    val PillPink          = Color(0xFFF0A0B0)
    val PillYellow        = Color(0xFFF5DC80)

    // Dates timeline (paridad iOS)
    val DateToday         = Guinda
    val DateThisWeek      = BrandGreen
    val DateNextWeek      = BrandCreamStrong
    val DatePastDue       = SemanticDestructive
    val DateUpcoming      = CardSecondary
}
```

**Mapeo a `AppColors` upstream** (en `LocalBrand.kt` o init de Theme):

| Campo `AppColors` | Token brand |
|---|---|
| `primary` (material) | `Guinda` |
| `secondary` (material) | `BrandGreen` |
| `background` | `BrandCream` |
| `surface` | `SurfaceWhite` |
| `error` | `SemanticDestructive` |
| `cardViewBackground` | `SurfaceWhite` |
| `cardViewBorder` | `CardStrokeSubtle` |
| `divider` | `Divider` |
| `textPrimary` | `CardPrimary` |
| `textSecondary` | `CardMedium` |
| `textPrimaryLight` | `CardSecondary` |
| `progressBarColor` | `Guinda` |
| `progressBarBackgroundColor` | `ProgressTrack` |
| `datesSectionBarToday` | `DateToday` |
| `datesSectionBarThisWeek` | `DateThisWeek` |
| `datesSectionBarNextWeek` | `DateNextWeek` |
| `datesSectionBarPastDue` | `DatePastDue` |
| `datesSectionBarUpcoming` | `DateUpcoming` |
| `tabSelectedBtnContent` | `Guinda` |
| `tabUnselectedBtnContent` | `TextInactive` |
| `successGreen` | `BrandGreen` |

Los demás campos `light_*` se sobrescriben en `core/src/aprende/res/values/colors.xml` apuntando al hex brand.

### 3.2 Gradientes

```kotlin
object BrandGradients {
    val HeroDashboard = Brush.verticalGradient(
        listOf(BrandPalette.BrandGreenDark, BrandPalette.BrandGreen)
    )
    val HeroProfile = Brush.verticalGradient(
        listOf(BrandPalette.BrandGreen, BrandPalette.BrandGreenDark)
    )
    val HeroGuinda = Brush.verticalGradient(
        listOf(BrandPalette.GuindaDeep, BrandPalette.Guinda)
    )
    val ScrimBottom = Brush.verticalGradient(
        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
    )
}
```

### 3.3 Tipografía — Noto Sans

**Bundle:**
1. Descargar TTFs de [Google Fonts Noto Sans](https://fonts.google.com/noto/specimen/Noto+Sans) — pesos Regular, Medium, SemiBold, Bold + variantes Italic.
2. Copiar a `core/src/aprende/res/font/`:
   - `noto_sans_regular.ttf`
   - `noto_sans_medium.ttf`
   - `noto_sans_semi_bold.ttf`
   - `noto_sans_bold.ttf`
   - `noto_sans_italic.ttf`
   - `noto_sans_medium_italic.ttf`
   - `noto_sans_semi_bold_italic.ttf`
   - `noto_sans_bold_italic.ttf`
3. **No borrar** `tt_rounds_neue_variable.ttf` ni `fraunces_*.ttf` aún — `grep -rn "tt_rounds\|fraunces" core/ dashboard/ course/ profile/ discovery/ auth/` debe estar vacío antes de eliminar.

**API en `BrandTypography.kt`:**

```kotlin
val NotoSans = FontFamily(
    Font(R.font.noto_sans_regular,        FontWeight.Normal),
    Font(R.font.noto_sans_medium,         FontWeight.Medium),
    Font(R.font.noto_sans_semi_bold,      FontWeight.SemiBold),
    Font(R.font.noto_sans_bold,           FontWeight.Bold),
    Font(R.font.noto_sans_italic,         FontWeight.Normal, FontStyle.Italic),
    Font(R.font.noto_sans_medium_italic,  FontWeight.Medium, FontStyle.Italic),
    Font(R.font.noto_sans_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.noto_sans_bold_italic,    FontWeight.Bold,   FontStyle.Italic),
)

@Immutable
data class BrandTypography(
    val displayHero: TextStyle,         // 28sp Bold — saludo Dashboard, hero Discovery
    val displayLarge: TextStyle,        // 24sp Bold — Profile name
    val titleSection: TextStyle,        // 17sp SemiBold — "Mis cursos"
    val titleCard: TextStyle,           // 14sp SemiBold — nombre curso en card
    val body: TextStyle,                // 13sp Regular — descripciones
    val bodyEmphasis: TextStyle,        // 13sp Medium
    val caption: TextStyle,             // 11sp Medium — metadata
    val eyebrow: TextStyle,             // 10sp Medium uppercase — section header eyebrow
    val tabLabel: TextStyle,            // 12sp Medium — tabs Course
    val ctaLabel: TextStyle,            // 14sp SemiBold — botones primarios
)

internal val DefaultBrandTypography = BrandTypography(
    displayHero  = TextStyle(NotoSans, 28.sp, FontWeight.Bold,     lineHeight = 34.sp),
    displayLarge = TextStyle(NotoSans, 24.sp, FontWeight.Bold,     lineHeight = 30.sp),
    titleSection = TextStyle(NotoSans, 17.sp, FontWeight.SemiBold, lineHeight = 22.sp),
    titleCard    = TextStyle(NotoSans, 14.sp, FontWeight.SemiBold, lineHeight = 18.sp),
    body         = TextStyle(NotoSans, 13.sp, FontWeight.Normal,   lineHeight = 18.sp),
    bodyEmphasis = TextStyle(NotoSans, 13.sp, FontWeight.Medium,   lineHeight = 18.sp),
    caption      = TextStyle(NotoSans, 11.sp, FontWeight.Medium,   lineHeight = 14.sp, letterSpacing = 0.2.sp),
    eyebrow      = TextStyle(NotoSans, 10.sp, FontWeight.Medium,   lineHeight = 12.sp, letterSpacing = 1.0.sp),
    tabLabel     = TextStyle(NotoSans, 12.sp, FontWeight.Medium,   lineHeight = 14.sp),
    ctaLabel     = TextStyle(NotoSans, 14.sp, FontWeight.SemiBold, lineHeight = 18.sp),
)
```

**Equivalencia iOS → Android (paridad):**

| Rol iOS | iOS pt | Android sp | Peso |
|---|---|---|---|
| Saludo hero | 30 | 28 | Bold |
| Título sección | 18 | 17 | SemiBold |
| Nombre curso card | 15 | 14 | SemiBold |
| Body | 14 | 13 | Regular |
| Caption | 12 | 11 | Medium |
| Tab labels Course | 13 | 12 | Medium |
| Eyebrow uppercase | 11 | 10 | Medium |

### 3.4 Shapes

```kotlin
object BrandShapes {
    val Control     = RoundedCornerShape(8.dp)
    val Card        = RoundedCornerShape(16.dp)
    val Hero        = RoundedCornerShape(20.dp)
    val Sheet       = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val Pill        = RoundedCornerShape(999.dp)
    val ChipFilter  = RoundedCornerShape(50)
    val TabPill     = RoundedCornerShape(28.dp)
}
```

### 3.5 Spacing (4dp grid)

```kotlin
object BrandSpacing {
    val xs = 4.dp
    val s  = 8.dp
    val m  = 12.dp
    val l  = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val screenHorizontal = 20.dp
    val sectionVertical  = 24.dp
}
```

### 3.6 Elevations / sombras

```kotlin
object BrandElevations {
    val cardSubtle = 2.dp        // sombra y=2 blur=8 negro 4%
    val cardRaised = 6.dp
    val tabBarFloating = 12.dp
}
```

Sombra precisa via `Modifier.shadow(elevation, shape, ambientColor = Color.Black, spotColor = Color.Black)` con `clip = false`.

### 3.7 Hápticos

```kotlin
object BrandHaptics {
    @Composable
    fun rememberBrand(): BrandHapticsApi {
        val view = LocalView.current
        return remember(view) { BrandHapticsImpl(view) }
    }
}

interface BrandHapticsApi {
    fun cardTap()       // soft → HapticFeedbackConstants.CONTEXT_CLICK
    fun ctaPrimary()    // medium → HapticFeedbackConstants.CONFIRM (API 30+) / LONG_PRESS fallback
    fun tabChange()     // selection → HapticFeedbackConstants.CLOCK_TICK
    fun success()       // CONFIRM
    fun error()         // REJECT (API 30+) / LONG_PRESS fallback
}
```

Mapping iOS → Android:

| Evento iOS | Android constante |
|---|---|
| `.impact(.soft)` | `HapticFeedbackConstants.CONTEXT_CLICK` |
| `.impact(.medium)` | `HapticFeedbackConstants.CONFIRM` (fallback `LONG_PRESS`) |
| `.selection()` | `HapticFeedbackConstants.CLOCK_TICK` |
| `.notification(.success)` | `HapticFeedbackConstants.CONFIRM` |
| `.notification(.error)` | `HapticFeedbackConstants.REJECT` (fallback `LONG_PRESS`) |

### 3.8 LocalBrand CompositionLocal

```kotlin
@Immutable
data class BrandTokens(
    val palette: BrandPaletteHolder,
    val typography: BrandTypography,
    val shapes: BrandShapesHolder,
    val spacing: BrandSpacing,
    val gradients: BrandGradientsHolder,
    val elevations: BrandElevations,
)

val LocalBrand = staticCompositionLocalOf<BrandTokens> {
    error("BrandTokens no provistos. Envuelve con OpenEdXTheme.")
}

val MaterialTheme.brand: BrandTokens
    @Composable
    @ReadOnlyComposable
    get() = LocalBrand.current
```

Uso en feature:

```kotlin
val brand = MaterialTheme.brand
Text("Hola", style = brand.typography.displayHero, color = brand.palette.cardPrimary)
```

### 3.9 Patch a `OpenEdXTheme`

Único cambio en `core/ui/theme/Theme.kt`:

```kotlin
@Composable
fun OpenEdXTheme(
    darkTheme: Boolean = false,                              // forzado false
    content: @Composable () -> Unit
) {
    val colors = LightColorPalette                            // siempre light
    val brand = remember { aprendeBrandTokens() }             // ← nuevo
    MaterialTheme(
        colors = colors.material,
        shapes = LocalShapes.current.material,
    ) {
        CompositionLocalProvider(
            LocalOverscrollFactory provides null,
            LocalBrand provides brand,                        // ← nuevo
            content = content
        )
    }
}
```

3 líneas nuevas. Conflict rebase trivial.

---

## 4. Brand Components — catálogo

Todos viven en `core/src/main/java/org/openedx/core/ui/brand/`. Cada uno: API Compose pura, parámetros tipados, `@Preview` light, KDoc breve. Sin lógica de feature.

| Componente | Archivo | Reemplaza/extiende | Fase |
|---|---|---|---|
| `BrandTabBar` | `brand/components/BrandTabBar.kt` | TabBar custom de `MainScreenV2` | F2 |
| `DecorativeRings` | `brand/components/DecorativeRings.kt` | Nuevo | F1 |
| `BrandProgressRing` | `brand/components/BrandProgressRing.kt` | Nuevo | F0 |
| `BrandSectionHeader` | `brand/components/BrandSectionHeader.kt` | Nuevo | F0 |
| `BrandChip` | `brand/components/BrandChip.kt` | Nuevo | F0 |
| `BrandStatTile` | `brand/components/BrandStatTile.kt` | Nuevo | F0 |
| `BrandFactTile` | `brand/components/BrandFactTile.kt` | Nuevo | F0 |
| `BrandCertificateCard` | `brand/components/BrandCertificateCard.kt` | Nuevo | F6 |
| `BrandFloatingCTA` | `brand/components/BrandFloatingCTA.kt` | Nuevo | F4 |
| `BrandToggleRow` | `brand/components/BrandToggleRow.kt` | Nuevo | F7 |
| `BrandUnitAccordion` | `brand/components/BrandUnitAccordion.kt` | Nuevo | F4 |
| `BrandTimelineItem` | `brand/components/BrandTimelineItem.kt` | Nuevo | F4 |
| `CourseStatsStrip` | `brand/components/CourseStatsStrip.kt` | Nuevo | F4 |
| `PatternCover` | `brand/components/PatternCover.kt` | Nuevo (Canvas + Material Icon) | F0 |
| `CourseCoverImage` | `brand/components/CourseCoverImage.kt` | Reemplaza `CourseImageHeader` upstream en uso fork | F0 |
| `BrandReaderTopBar` | `brand/components/reader/BrandReaderTopBar.kt` | Nuevo | F5 |
| `BrandReaderBottomBar` | `brand/components/reader/BrandReaderBottomBar.kt` | Nuevo | F5 |
| `BrandReaderProgressRail` | `brand/components/reader/BrandReaderProgressRail.kt` | Nuevo | F5 |

### 4.1 Reglas generales de cards

```
Card blanca (SurfaceWhite):
  shape       = BrandShapes.Card (16dp)
  elevation   = BrandElevations.cardSubtle (sombra negro 4% y=2 blur=8)
  border      = 1dp BrandPalette.CardStrokeSubtle (negro 4%)

Card cream (BrandCream como fondo):
  background  = BrandPalette.BrandCream
  cards internas = SurfaceWhite con sombra
  secciones   = BrandCreamStrong

Separador entre secciones:
  HorizontalDivider thickness = 1.dp color = BrandPalette.Divider
```

### 4.2 PatternCover — categorías

```kotlin
enum class CourseCategory(val icon: ImageVector, val tint: Color) {
    Seguridad   (Icons.Filled.Security,         BrandPalette.Guinda),
    Salud       (Icons.Filled.HealthAndSafety,  BrandPalette.BrandGreen),
    Administracion(Icons.Filled.AccountBalance, BrandPalette.GuindaDeep),
    Educacion   (Icons.Filled.School,           BrandPalette.Guinda),
    Tecnologia  (Icons.Filled.Memory,           BrandPalette.BrandGreenDark),
    Ciudadania  (Icons.Filled.Groups,           BrandPalette.Guinda),
    Ambiente    (Icons.Filled.Eco,              BrandPalette.BrandGreen),
    ;
    companion object {
        fun from(courseTags: List<String>?): CourseCategory { /* heurística por keyword */ }
    }
}
```

PatternCover render: fondo color tint + ícono `alpha = 0.15f` esquina inferior derecha tamaño 120dp, opcional eyebrow institución arriba izquierda.

### 4.3 CourseCoverImage — estrategia híbrida

```kotlin
sealed interface CourseCoverVariant {
    object Auto : CourseCoverVariant
    data class Photo(val scrim: Boolean = true) : CourseCoverVariant
    data class Letterbox(val background: Color = BrandPalette.BrandCream) : CourseCoverVariant
    object Pattern : CourseCoverVariant
    data class Typographic(val title: String) : CourseCoverVariant
}

@Composable
fun CourseCoverImage(
    course: EnrolledCourse,                  // o modelo apropiado
    variant: CourseCoverVariant = CourseCoverVariant.Auto,
    modifier: Modifier = Modifier,
)
```

Heurística `Auto`:
1. `course.imageUrl == null || isBlank` → `Pattern`
2. `imageWidth / imageHeight < 1.05` (póster vertical) → `Letterbox`
3. Resto → `Photo(scrim = true)`

> Cuando la API exponga flag explícito `hasEmbeddedText`, sustituir heurística por flag. Documentar deuda técnica.

`contentDescription` = `course.name` siempre. Nunca depende del bitmap.

---

## 5. Plan de fases — orden estricto

Cada fase: **build verde** + **smoke test** en emulador o dispositivo + **commit** prefijo `aprende:` + verificación rebase upstream limpio. Sin saltos. Sin paralelismo.

---

### Fase 0 — Fundación (sin UI visible)

**Objetivo:** dejar montado el sistema de diseño, flavor `aprende`, fuentes Noto Sans, brand components átomos. Cero cambios visibles en pantallas existentes.

#### 0.1 Setup flavor `aprende`

- Editar `app/build.gradle`: agregar `flavorDimensions` + `productFlavors openedx/aprende` + `buildConfigField BRAND_APRENDE`.
- Replicar declaración mínima en cada feature module (`dashboard/build.gradle`, `course/build.gradle`, `discovery/build.gradle`, `profile/build.gradle`, `auth/build.gradle`, `core/build.gradle`).
- Crear estructura de directorios `<modulo>/src/aprende/` con `res/values/strings.xml` vacío inicialmente.
- Validar `./gradlew :app:assembleAprendeDebug` compila.
- Ajustar configuraciones de IDE (Build Variants → seleccionar `aprendeDebug`).

#### 0.2 Bundle Noto Sans

- Descargar TTFs y colocar en `core/src/aprende/res/font/` (8 archivos listados §3.3).
- Crear `core/ui/theme/brand/BrandTypography.kt` con `FontFamily NotoSans` y `data class BrandTypography` + `DefaultBrandTypography`.
- Validar carga en debug:
  ```kotlin
  if (BuildConfig.DEBUG) {
      val ok = ResourcesCompat.getFont(context, R.font.noto_sans_regular) != null
      check(ok) { "Noto Sans no carga" }
  }
  ```
- **No borrar** `tt_rounds_neue_variable.ttf` / `fraunces_*.ttf` aún.

#### 0.3 Brand tokens y theme patch

- Crear `core/ui/theme/brand/BrandPalette.kt` con todos los tokens §3.1.
- Crear `BrandShapes.kt`, `BrandSpacing.kt`, `BrandElevations.kt`, `BrandGradients.kt` según §3.4-3.6.
- Crear `LocalBrand.kt` con `BrandTokens` data class + `staticCompositionLocalOf` + extensión `MaterialTheme.brand`.
- Crear `aprendeBrandTokens()` factory.
- Editar `core/ui/theme/Theme.kt`: forzar `darkTheme = false`, envolver con `CompositionLocalProvider(LocalBrand provides aprendeBrandTokens())`.
- Crear `core/src/aprende/res/values/colors.xml` con override de `light_primary`, `light_primary_variant`, `light_background`, `light_card_view_background`, `light_divider`, `light_progress_bar_color`, etc., usando hex de §3.1.

**Validación:** abrir cualquier pantalla existente — compila, render idéntico salvo colores institucionales si el override de colors.xml ya cubre los puntos clave.

#### 0.4 Componentes átomos

Implementar y publicar en `core/ui/brand/components/`:

- `BrandHaptics.kt` (interfaz + impl Android + `rememberBrand`)
- `BrandChip.kt` (estados: solid/outline/tonal; activo/inactivo)
- `BrandProgressRing.kt` (Canvas drawArc, parámetros `progress`, `size`, `strokeWidth`, `trackColor`, `progressColor`)
- `BrandSectionHeader.kt` (eyebrow + acción opcional "Ver todo")
- `BrandStatTile.kt` (icono + número grande + label uppercase, variante light/dark)
- `BrandFactTile.kt` (label uppercase + valor)

Cada uno con `@Preview(showBackground = true, backgroundColor = 0xFFECE9E4)` light + KDoc breve.

#### 0.5 PatternCover + CourseCategory

- `CourseCategory` enum §4.2.
- `PatternCover` Composable Canvas + Material Icon overlay.
- Heurística `from(tags)` que infiere categoría desde tags/labels backend o fallback `Educacion`.
- Preview: 7 categorías × 2 tamaños = 14 snapshots.

#### 0.6 CourseCoverImage v2

- Crear `CourseCoverImage.kt` con `CourseCoverVariant` sealed interface.
- Implementar resolución `Auto` por heurística width/height.
- Usar Coil `AsyncImage` para `Photo` y `Letterbox`. Para `Letterbox` aplicar `ContentScale.Fit` sobre fondo `Letterbox.background`.
- `contentDescription = course.name` obligatorio.
- `Modifier.semantics { role = Role.Image }`.

#### 0.7 DecorativeRings

- Composable basado en `rememberInfiniteTransition` + `animateFloat` (`0f` → `360f`, `tween 60_000ms`, repeat).
- Canvas drawCircle 3-4 anillos con `style = Stroke(1.dp.toPx())`, `color = Color.White.copy(alpha = 0.08f)`.
- Pause-aware: `LaunchedEffect(LocalLifecycleOwner) { lifecycle.repeatOnLifecycle(STARTED) { ... } }`.

**Criterio Fase 0:** `./gradlew :app:assembleAprendeDebug` y `:app:assembleOpenedxDebug` compilan. Theme expone API pública nueva. Todas las pantallas existentes renderizan idénticas. Componentes átomos en preview verde.

**Commit sugerido:** `aprende: feat(core) brand foundation - flavor, tokens, Noto Sans, atoms`

---

### Fase 1 — Dashboard (Aprende)

Orden interno: hero → primary card → mis cursos carrusel → recomendados → weekly.

#### 1.1 Crear `LearnBrandView.kt`

- Ruta: `dashboard/src/main/java/org/openedx/learn/presentation/brand/LearnBrandView.kt`.
- Composable que reproduce layout iOS §4.2 spec:
  - Hero verde (`BrandGradients.HeroDashboard`) altura ~280dp con `DecorativeRings` detrás.
  - Saludo `"¡Hola, ${user.name}!"` con `displayHero`.
  - Subtítulo `"Continúa donde lo dejaste"` con `body`.
  - Row de `BrandStatTile` (Total / En curso / No iniciados).
- Card crema con offset `-15dp` shape `RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)`.

#### 1.2 PrimaryCourseCard rediseño

- Crear `PrimaryCourseCardBrand.kt`.
- Layout horizontal: `CourseCoverImage` 200×140dp `BrandShapes.Card` izquierda, columna texto derecha (badge "En curso" + título + organización + ProgressRing pequeño + CTA "Reanudar curso" / "Iniciar curso").
- `BrandHaptics.ctaPrimary()` en click CTA.

#### 1.3 Mis cursos — LazyRow

- `LazyRow` con `CourseListItemBrand` 200dp ancho.
- Card SurfaceWhite + sombra subtle + `CourseCoverImage 200×120dp` arriba + meta abajo.
- `BrandHaptics.cardTap()` al tap.

#### 1.4 Recomendados — full-width rows

- `Column` con cards full-width + `CourseCoverImage(Photo(scrim=true))` 100×140dp izquierda + texto.

#### 1.5 Switch en `LearnFragment`

- Editar `dashboard/.../LearnFragment.kt` el `onViewCreated`:
  ```kotlin
  binding.composeCollapsingLayout.setContent {
      OpenEdXTheme {
          if (BuildConfig.BRAND_APRENDE) LearnBrandView(...)
          else DashboardGalleryView(...)
      }
  }
  ```
- ≤3 líneas modificadas en archivo upstream.

#### 1.6 Strings es_419

- `dashboard/src/aprende/res/values/strings.xml`: override
  - `dashboard_my_courses` → "Mis cursos"
  - `dashboard_view_all` → "Ver todo"
  - `dashboard_recommended` → "Recomendados"
  - `dashboard_resume_course` → "Reanudar curso"
  - `dashboard_start_course` → "Iniciar curso"
  - Hardcoded del hero: `dashboard_greeting` → `"¡Hola, %1$s!"`, `dashboard_continue_where` → `"Continúa donde lo dejaste"`.

**Criterio Fase 1:** Dashboard renderiza paridad con iOS spec §4.2. Smoke test: login → ve hero verde + cards + scroll horizontal funcional + ProgressRing animado + CTA con háptico. Sin tocar otras pantallas.

**Commit:** `aprende: feat(dashboard) brand redesign - hero green + carousels + primary card`

---

### Fase 2 — Tab bar global (BrandTabBar)

#### 2.1 Implementar `BrandTabBar.kt`

- Composable `BrandTabBar(items: List<TabItem>, selectedIndex: Int, onSelect: (Int) -> Unit)`.
- Layout: `Row` centrado horizontalmente, ancho fit-content, padding `BrandSpacing.s` vertical y `BrandSpacing.l` horizontal.
- Shape `BrandShapes.TabPill` (28dp).
- Background: `Surface(tonalElevation = 8.dp, color = SurfaceWhite.copy(alpha = 0.92f))` + `Modifier.blur(20.dp)` si SDK ≥ 31, fallback `tonalElevation` solo.
- Border `1.dp BlendMode + Color.Black.copy(alpha = 0.08f)`.
- Sombra `BrandElevations.tabBarFloating`.
- Tab activa: ícono + label color `Guinda`, fondo pill `BrandShapes.Pill` con `Guinda.copy(alpha = 0.08f)`.
- Tab inactiva: ícono + label color `TextInactive`.
- `BrandHaptics.tabChange()` al tap.
- Posición flotante: `Modifier.padding(bottom = 16.dp)` + `Modifier.navigationBarsPadding()`.

#### 2.2 Tabs

3 tabs según iOS spec:
1. **Aprende** — ícono `Icons.Outlined.School`
2. **Descubre** — ícono `Icons.Outlined.Explore`
3. **Perfil** — ícono `Icons.Outlined.Person`

Si `MainScreenV2` actual tiene más tabs (Programs/Notifications), conservarlos detrás de flag pero ocultarlos en flavor `aprende`.

#### 2.3 Integrar en `MainScreenV2`

- Crear `MainScreenV2BrandView.kt` paralelo.
- Reemplazar `BottomNavigation` upstream por `BrandTabBar` flotante.
- Manejar `WindowInsets.navigationBars` para que no se monte sobre la barra del sistema.
- Ocultar BrandTabBar cuando navega a Course Detail / Lesson (push). Detectar vía `NavController.currentBackStackEntryAsState()`.
- Switch en `MainScreenV2.kt` upstream: `if (BuildConfig.BRAND_APRENDE) MainScreenV2BrandView() else MainScreenV2()`.

**Criterio Fase 2:** TabBar pill flotante visible en Dashboard/Discovery/Profile. Oculta en push navigation a Course Detail. Háptico funcional. Tab activa pill guinda.

**Commit:** `aprende: feat(app) BrandTabBar pill flotante 3 tabs`

---

### Fase 3 — Discovery (Descubre)

#### 3.1 Crear `DiscoveryBrandView.kt`

- Hero verde gradiente con DecorativeRings.
- Display "Descubre nuevo" 28sp Bold.
- Subtítulo "Busquemos un curso para ti" 13sp Regular.

#### 3.2 SearchView pill

- TextField con shape `BrandShapes.Pill`.
- Background `BrandPalette.InputBackground`.
- Border `1.dp InputStroke`.
- Placeholder "Buscar cursos…".
- Leading icon: `Icons.Outlined.Search`.

#### 3.3 Filtros chips

- LazyRow horizontal con `BrandChip`.
- Categorías: "Todos", "Populares", "Recientes" (más categorías cuando backend provea).
- Chip seleccionado: `solid` variant (fondo `Guinda`, texto blanco).
- Inactivo: `outline` variant (fondo blanco, borde `CardStrokeSubtle`, texto `CardPrimary`).
- `BrandHaptics.tabChange()` al tap.

#### 3.4 Featured editorial

- Card grande 300×180dp con `BrandGradients.HeroGuinda`.
- DecorativeRings overlay alpha bajo.
- Eyebrow "Destacado esta semana" + título Bold blanco + CTA "Ver curso".

#### 3.5 Catálogo grid 2 columnas

- `LazyVerticalGrid(GridCells.Fixed(2))` o `LazyVerticalStaggeredGrid` si necesario.
- `CourseDiscoveryCardBrand` cada celda: `CourseCoverImage(Auto)` arriba 1:1.05 ratio, título + organización + meta abajo.

#### 3.6 Switch en `NativeDiscoveryFragment`

- Igual patrón Fase 1: condicional BuildConfig.

#### 3.7 Strings

`discovery/src/aprende/res/values/strings.xml`:
- `discovery_title` → "Descubre nuevo"
- `discovery_subtitle` → "Busquemos un curso para ti"
- `discovery_search_hint` → "Buscar cursos…"
- `discovery_view_course` → "Ver curso"
- `discovery_enroll_now` → "Enlístate ahora"

**Criterio Fase 3:** Discovery navegable desde tab. Tap en card → push a Course Detail (Fase 4). Filtros chips funcionan con haptics. Featured card destacada visible.

**Commit:** `aprende: feat(discovery) brand redesign - hero + chips + grid 2col`

---

### Fase 4 — Course Detail (Hub + tabs)

Pantalla más compleja. Subdividir en 4.1–4.7. Cada paso commit independiente si build verde.

#### 4.1 Hero del curso

- `CourseHeaderBrandView.kt` en `course/.../brand/`.
- 300dp altura.
- `CourseCoverImage(Photo(scrim=true))` o `Letterbox(BrandCream)` según heurística.
- Top icons: back pill + bookmark + download (Surface circular 40dp + ícono + alpha 0.7 white).
- Overlay bottom: Eyebrow institución + título display Bold blanco + chips meta.

#### 4.2 Tab bar Course (sticky)

- 7 tabs según spec iOS: Inicio, Contenido, Progreso, Fechas, Desconectado, Discusiones, Más.
- Implementar como `LazyRow` scroll horizontal con `BrandChip` variante tab.
- Tab activa: solid (fondo `Guinda`, texto blanco), shape `BrandShapes.Pill`.
- Tab inactiva: outline (fondo `SurfaceWhite`, borde `CardStrokeSubtle`, texto `BrandGreen`).
- Sticky via `Modifier.layoutId("tabBar")` dentro de `CollapsingToolbarLayout` o equivalente Compose.

#### 4.3 Tab Inicio (Home)

- Reescribir `CourseHomeScreen` como `CourseHomeBrandScreen`:
  - Hero card blanca con `BrandProgressRing` 100dp + "X% completado" + CTA `BrandFloatingCTA` "Continuar".
  - SectionHeader "Esta semana" + LazyRow cards 220×140dp (lección/tarea/examen + fecha).
  - SectionHeader "Destacados" + lista vertical de stats: mejor calificación, racha, tiempo total.

#### 4.4 Tab Contenido (Content) — Syllabus dinámico

- `BrandUnitAccordion` para cada `chapter`.
- Estado por `completion_stat`:
  - `1.0` → check fill `BrandGreen`
  - `0 < x < 1` → dots o ring porcentaje `Guinda`
  - `0` → outline `TextInactive`
- Filtros chip: Todo / Videos / Tareas (BrandChip).
- Sequential row dentro accordion: progress dots + título + tipo (video/lectura/evaluación) + duración.
- Tap row → navega al reader (Fase 5) en ese vertical.

#### 4.5 Tab Progreso

- Hero card blanca: `BrandProgressRing` 88dp + "Calificación general 78%" + barra guinda con marker pass/fail.
- SectionHeader "Desempeño por tipo" + barras horizontales por categoría (Tareas/Quizzes/Examen).
- Card constancia: si `certificateData == null` → texto "Disponible al completar el curso", sin spinner. Si presente → CTA "Ver certificado".

#### 4.6 Tab Fechas — Timeline vertical

- `BrandTimelineItem` con dot color tone + fecha + título + chip estado.
- Colores tone: hoy `DateToday`, esta semana `DateThisWeek`, próxima `DateNextWeek`, vencido `DatePastDue`, futuro `DateUpcoming`.
- Stats bar arriba: "N pendientes | Próxima: dd mmm".

#### 4.7 Sticky CTA bottom

- `BrandFloatingCTA` con gradient fade desde `Color.Transparent` a `BrandCream`.
- Botón verde `BrandGreen` "Continuar curso" + botón cuadrado download icon.
- Visible solo cuando hero scrolleó fuera.
- `BrandHaptics.ctaPrimary()` en tap.

#### 4.8 Switch en `CourseContainerFragment`

Patrón habitual condicional.

#### 4.9 Strings

`course/src/aprende/res/values/strings.xml` ajustar a labels iOS spec:
- `course_tab_home` → "Inicio"
- `course_tab_content` → "Contenido"
- `course_tab_progress` → "Progreso"
- `course_tab_dates` → "Fechas"
- `course_tab_offline` → "Desconectado"
- `course_tab_discussion` → "Discusiones"
- `course_tab_handouts` → "Más"
- `course_progress_overall_grade` → "Calificación general"
- `course_progress_passing` → "Aprobado"
- `course_progress_view_certificate` → "Ver certificado"
- `course_certificate_pending` → "Disponible al completar el curso"
- `course_continue` → "Continuar curso"

**Criterio Fase 4:** Course detail navegable de tab a tab. 7 tabs visibles. Hero scroll → CTA flotante aparece. `certificateData null` no crashea (regla absoluta). Tap en lección → reader Fase 5.

**Commit:** `aprende: feat(course) hero + 7 tabs + accordion + timeline + sticky CTA`

---

### Fase 5 — Lesson Reader

#### 5.1 BrandReaderTopBar

- `Surface(shape = BrandShapes.Pill, tonalElevation = 4.dp, color = BrandCream)`.
- Padding horizontal 16dp.
- Layout: back pill (icon close) + título unidad centrado + counter "X/N lecciones".
- BrandReaderProgressRail debajo: barra horizontal segmentada (un tick por vertical en sequential actual). Color: `BrandGreen` completado, `Guinda` actual, `BrandPalette.ProgressTrack` pendiente. Tap tick = jump.

#### 5.2 BrandReaderBottomBar

- Row con 3 botones: ← Anterior (outline) | ✓ Marcar (variant secundaria) | Siguiente → (solid `BrandGreen`).
- Si último vertical: botón Siguiente cambia a "Finalizar".

#### 5.3 VideoUnit

- `ExoPlayer` o player upstream existente.
- Cover con `PatternCover` cuando no hay imagen.
- Controles tint `BrandGreen` en play/pause/seek.
- BrandHaptics.cardTap() al play.

#### 5.4 HtmlUnit

- `WebView` con CSS inject:
  ```css
  body { font-family: 'Noto Sans'; font-size: 17px; line-height: 1.6;
         color: #1C1B18; background: #ECE9E4; padding: 24px;
         max-width: 680px; margin: 0 auto; overflow-x: hidden; word-wrap: break-word; }
  img, video, iframe { max-width: 100%; height: auto; border-radius: 12px; }
  table { display: block; overflow-x: auto; }
  pre { white-space: pre-wrap; word-break: break-word; }
  ```
- Inject vía `evaluateJavascript` post-load.
- WebViewClient maneja `shouldOverrideUrlLoading` (paridad upstream).

#### 5.5 ProblemUnit (quiz)

- Form Compose nativo (NO WebView para problem types simples).
- Pregunta: `displayLarge`.
- Opciones A/B/C/D: `RadioButton` tint `Guinda` + label.
- BrandHaptics.tabChange() al elegir.
- Submit: BrandHaptics.success() o error() según resultado.
- Para tipos avanzados (drag-drop): fallback WebView con scrim "Abre en pantalla completa".

#### 5.6 Navegación entre verticals

- `HorizontalPager` + `rememberPagerState` con verticals del sequential.
- Cross-sequential: detectar último vertical, prefetch siguiente sequential, animación slide + breadcrumb "Cap I → Cap II" 600ms.
- Final del chapter → `ModalBottomSheet` "Unidad completada" + CTA "Siguiente unidad".

#### 5.7 Switch en `CourseUnitContainerFragment`

Patrón condicional.

#### 5.8 Strings

- `lesson_previous` → "Anterior"
- `lesson_next` → "Siguiente"
- `lesson_finish` → "Finalizar"
- `lesson_mark_complete` → "Marcar completo"
- `lesson_good_work` → "¡Buen trabajo!"
- `lesson_unit_complete` → "Unidad completada"

**Criterio Fase 5:** Reader funcional para los 3 tipos de lección. Swipe horizontal entre verticals. Top/Bottom bar autohide al scroll. CSS Noto Sans aplicado en HTML. Quiz nativo con haptics.

**Commit:** `aprende: feat(course) reader brand - top/bottom bars + pager + html injection`

---

### Fase 6 — Profile

#### 6.1 Hero verde

- `ProfileBrandScreen.kt`.
- Hero `BrandGradients.HeroProfile` altura 280dp + DecorativeRings.
- Avatar circular 80dp (Coil) + nombre `displayLarge` + handle `body`.
- Top right: settings icon (Surface circular translúcida).

#### 6.2 Stats card flotante

- Card SurfaceWhite con `Modifier.offset(y = (-40).dp)` + sombra subtle + shape `BrandShapes.Card`.
- Grid 3 columnas: MIEMBRO DESDE | PAÍS | AÑO NAC.
- Datos reales de `UserProfile`:
  - `dateJoined` formateado a "MMM yyyy" en es_MX.
  - `country` (ocultar si vacío).
  - `yearOfBirth` (ocultar si 0).
- Si quedan ≤1 columnas → layout centrado con sólo "Miembro desde Mar 2024".

#### 6.3 Carousel certificados

- LazyRow con `BrandCertificateCard` (gradient `BrandCreamStrong` → `BrandCream`).
- Card final dashed "Continúa cursos para obtener más" si lista vacía.
- Si endpoint constancias no existe aún → placeholder dashed total.

#### 6.4 Sección Ajustes

- SectionHeader "Ajustes" eyebrow.
- Cards blancas con `ProfileRowBrand` (icon en círculo `GreenTint` + label + chevron):
  - Ajustes de video → navega `VideoSettings`
  - Fechas y calendario → navega `DatesAndCalendar`

#### 6.5 Sección Soporte

- Soporte de contacto, Términos, Privacidad, Cookies, FAQ.

#### 6.6 Sección Cuenta

- Administrar cuenta → `ManageAccountFragment`.
- Botón Cerrar sesión: outline `Guinda` full-width.

#### 6.7 Switch en `ProfileFragment`

Condicional.

#### 6.8 Strings

- `profile_title` → "Perfil"
- `profile_member_since` → "MIEMBRO DESDE"
- `profile_country` → "PAÍS"
- `profile_year_of_birth` → "AÑO NAC."
- `profile_my_certificates` → "Mis constancias"
- `profile_settings` → "Ajustes"
- `profile_support` → "Soporte"
- `profile_logout` → "Cerrar sesión"
- `profile_logout_confirm` → "¿Está seguro de que desea cerrar sesión?"

**Criterio Fase 6:** Profile completo navegable. Stats card respeta nulls. Avatar carga vía Coil. Logout pide confirmación.

**Commit:** `aprende: feat(profile) brand redesign - hero + stats + certificates + menu`

---

### Fase 7 — Settings + ManageAccount

#### 7.1 SettingsBrandScreen

- Fondo `BrandCream`.
- Grupos card blanca:
  - **Video**: `BrandToggleRow` "Descarga solo wifi" toggle `Guinda`. Row "Calidad de streaming" + chevron.
  - **App**: row "Versión: 1.0.x".
- Card `BrandCreamStrong` zona peligrosa:
  - "Eliminar cuenta" texto `SemanticDestructive`.

#### 7.2 ManageAccountBrandScreen

- Avatar grande + nombre + email arriba.
- Card editable: Nombre / Correo / Usuario / Contraseña (read-only en este sprint, navegan a `EditProfile`).
- Botón guardar `BrandGreen` (cuando aplique edición).
- Zona peligrosa: card icono trash + "Eliminar cuenta" → confirma + endpoint.

#### 7.3 BrandToggleRow

- Row 56dp altura.
- Label izquierda + Switch derecha.
- Switch tint: `checkedThumbColor = Guinda`, `checkedTrackColor = GreenTint`.

#### 7.4 VideoSettings + DatesAndCalendar

- Reusar BrandToggleRow.
- Mantener lógica VM upstream.
- UI rediseñada con cards blancas.

#### 7.5 Strings

- `settings_wifi_only` → "Descarga solo wifi"
- `settings_video_quality` → "Calidad de transmisión de vídeo"
- `settings_quality_auto` → "Auto"
- `settings_quality_360p` → "360p"
- `settings_quality_720p` → "720p"
- `settings_version` → "Versión:"
- `delete_account_title` → "Borrar cuenta"
- `delete_account_confirm` → "Sí, eliminar cuenta"
- `delete_account_back` → "Volver al perfil"

**Criterio Fase 7:** Toda la app rediseñada. Smoke test E2E desde login → Dashboard → Course → Lesson → Profile → Settings → ManageAccount → Eliminar cuenta (sin ejecutar borrado).

**Commit:** `aprende: feat(profile) settings + manage account redesign`

---

### Fase 8 — Cleanup y QA final

#### 8.1 Lint custom

- Detekt regla custom: prohibir hex literal en módulos feature.
- Detekt regla: prohibir `.dp` literal fuera de archivos `Brand*.kt` (whitelist por path).
- Si Detekt no soporta, script bash en CI: `grep -rE "Color\(0x[A-F0-9]{8}\)" dashboard/src/main course/src/main profile/src/main discovery/src/main` debe vaciar.

#### 8.2 Limpieza de fonts viejos

- `grep -rn "tt_rounds\|fraunces" core/ dashboard/ course/ profile/ discovery/ auth/` confirmar vacío.
- Eliminar `core/src/main/res/font/tt_rounds_neue_variable.ttf` y `fraunces_*.ttf`.
- Eliminar referencias en `AppTypography.kt` si las hay.

#### 8.3 Dead code

- `DarkColorPalette` queda muerto pero no se elimina (compat upstream).
- Eliminar archivos paralelos de variantes no usadas.

#### 8.4 A11y sweep

- Verificar todos los `CourseCoverImage` tienen `contentDescription` desde modelo.
- TalkBack: navegar Dashboard → Course → Lesson → Profile leyendo todos los elementos.
- Validar contraste WCAG con app Accessibility Scanner (Material lens).
- Dynamic Type: subir fuente sistema a XL — verificar no hay overflow.

#### 8.5 Performance

- Verificar `LazyColumn`/`LazyRow` en todos los listados (NO `Column` con scroll).
- DecorativeRings: validar pause en `onStop` lifecycle (no drena batería en background).
- Coil: cache funcional, no re-descarga al scrollear.
- ProgressRing: animación `tween` corta (<300ms), sin recompositions infinitas.

#### 8.6 Rebase upstream simulado

```bash
git fetch upstream
git checkout -b rebase-test
git rebase upstream/main
```

Conflictos esperados ≤ 10 archivos:
- `core/ui/theme/Theme.kt` (3 líneas)
- Fragments con switch BuildConfig (1 línea cada uno)
- `app/build.gradle` (flavor block)

Resolver, build verde, descartar branch test.

**Criterio Fase 8:** Build aprende+openedx ambos compilan. Rebase test limpio. Lint sin warnings nuevos. A11y aprobado. Performance verde.

**Commit:** `aprende: chore(core) cleanup fonts + lint rules + a11y sweep`

---

## 6. Checklist técnico global

### Imágenes y portadas
- [ ] `CourseCoverImage` v2 es el único punto de entrada para portadas en feature modules
- [ ] Heurística width/height < 1.05 documentada
- [ ] PatternCover renderiza con Material Icon + Canvas
- [ ] `contentDescription` viene del modelo en TODA portada
- [ ] Cero `ContentScale.Crop` + `clip` improvisados — usar `CourseCoverVariant`

### Accesibilidad
- [ ] TalkBack lee header → contenido → CTA en orden correcto en cada pantalla
- [ ] Cards usan `Modifier.semantics(mergeDescendants = true)` con label compuesto
- [ ] Botones CTA con label explícito (`"Continuar curso de $name"`)
- [ ] Validado Dynamic Type XL sin overflow
- [ ] Contrastes WCAG validados (Accessibility Scanner)

### Tipografía
- [ ] Noto Sans cargada (debug check al boot)
- [ ] Toda llamada a `TextStyle` usa `BrandTypography` roles
- [ ] Cero referencias a `tt_rounds_neue_variable` / `fraunces` en código
- [ ] Cero `sp` literal en feature modules

### Paleta
- [ ] Cero hex hardcoded fuera de `core/ui/theme/brand/`
- [ ] Cero introducción de tokens fuera de paleta institucional
- [ ] Tokens de `light_*` overrideados solo vía `core/src/aprende/res/values/colors.xml`

### Hápticos
- [ ] Cada CTA primario dispara `ctaPrimary()`
- [ ] Tab change dispara `tabChange()`
- [ ] Card tap dispara `cardTap()`
- [ ] Quiz answer → `tabChange()`; submit → `success()` / `error()`
- [ ] Nunca háptico en scroll (cap)

### Animaciones
- [ ] DecorativeRings pausa con lifecycle (no drena batería en background)
- [ ] Tap feedback con `animateFloatAsState` corto (<200ms)
- [ ] ProgressRing animado tween corto

### Pre-login intocable
- [ ] Cero modificaciones en `auth/.../signin|signup|restore|logistration|llavemx`
- [ ] Cero cambios en `whatsnew/`
- [ ] APIs públicas de `core` mantienen retrocompatibilidad

### Performance
- [ ] LazyColumn/LazyRow en todos los listados
- [ ] Coil cache funcional
- [ ] Sin GeometryReader-equivalentes que reaccionen al primer layout (regla iOS ERR-004 portada — ver §7.4)
- [ ] Recompositions estables (verificar Compose Layout Inspector)

### Rebase-safe
- [ ] Solo se editan archivos upstream para insertar switch BuildConfig (≤3 líneas)
- [ ] `git rerere` activo
- [ ] CI gate semanal opcional configurado
- [ ] Test rebase en Fase 8 limpio

### Build
- [ ] `:app:assembleAprendeDebug` verde
- [ ] `:app:assembleOpenedxDebug` verde (no rompe upstream)
- [ ] Tests existentes verdes (no romper Mockito mocks)

---

## 7. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Upstream rompe `OpenEdXTheme` API en rebase | Patch mínimo (3 líneas). `git rerere` aprende resolución. CI gate semanal detecta divergencia |
| Noto Sans no carga en runtime | Debug assertion al boot vía `ResourcesCompat.getFont` |
| Heurística `hasEmbeddedText` falla | Backend debe exponer flag explícito en sprint M+1. Documentar deuda técnica |
| BrandTabBar pill choca con `WindowInsets` | `Modifier.navigationBarsPadding()` + test físico Android 14/15 |
| `BlurEffect` no disponible < SDK 31 | Fallback `tonalElevation` solo |
| Hápticos excesivos cansan UX | Cap: máximo 1 háptico por interacción, nunca en scroll |
| Migración rompe pre-login al renombrar tokens | Tokens nuevos se AÑADEN. Los `light_*` upstream nunca se eliminan |
| `WebView` swipe horizontal pelea con HorizontalPager | CSS inject §5.4 + `requireCanScrollHorizontally` check + fallback botones siempre presentes (paridad iOS §6) |
| `certificateData null` crashea | Modelos Domain con campos opcionales. UI con fallback explícito (regla absoluta) |
| `Task` o flow lifecycle leak en re-creación de Composable | `LaunchedEffect(key)` con keys estables, NUNCA llamadas async en `init` (paridad iOS Regla 2) |
| DecorativeRings drena batería en background | Pause vía `LifecycleOwner.repeatOnLifecycle(STARTED)` |
| Lint custom no soporta regla hex | Script bash en CI: `grep -rE "Color\(0x" módulos/src/main` debe estar vacío |

---

## 8. Reglas de código adoptadas (paridad iOS)

Heredadas de `feedback_swiftui_patterns.md` adaptadas a Compose:

### Regla A — `remember` vs `rememberSaveable` vs ViewModel ownership

- ViewModel se inyecta vía Hilt/Koin al Composable. Nunca crear VM dentro de Composable salvo para state local trivial.
- `remember { }` solo para state efímero local (animaciones, toggles UI).
- `rememberSaveable` para state que sobrevive recomposición y configuration change.

### Regla B — Async/coroutines via `LaunchedEffect(key)`, NUNCA en init

- Equivalente directo a iOS `.task(id:)`.
- `LaunchedEffect(courseId) { vm.load(courseId) }` cancela auto al salir + re-dispara solo si cambia key.
- NUNCA `viewModelScope.launch` en init de Composable o en `@Composable fun` body sin LaunchedEffect.

### Regla C — Layout feedback (paridad ERR-004)

- Cuando un Composable reporta posiciones via `onGloballyPositioned` o `Modifier.onSizeChanged` a un sistema downstream que reacciona, descartar la primera lectura.
- Patrón: `var hasSettled by remember { mutableStateOf(false) }`. Primera lectura solo flippea `hasSettled = true`. Subsecuentes propagan.

### Regla D — Decoding API: opcionales propagados

- Si la API puede devolver `null`, modelo Data declara `?` opcional.
- Propagar opcionales hasta UI con `?.` chaining + fallback explícito.
- NO crear adapter custom que mapee `null → struct vacío` — oculta errores lógicos.
- UI muestra copy explícito (ej. "Disponible al completar el curso").

### Regla E — Barrer todo el módulo tras detectar anti-pattern

- Tras detectar UN caso de anti-pattern, NO solo arreglar ese archivo.
- `grep -rn "PATRON" modulo/src/` para encontrar TODOS.
- Hacer barrido completo en un commit (o commits ordenados por capa).

---

## 9. Estructura final de archivos delta

```
openedx-app-android/
├── app/
│   ├── build.gradle                                  ← +flavor block
│   └── src/
│       ├── main/java/.../MainScreenV2.kt             ← +switch BuildConfig (1 línea)
│       └── aprende/
│           └── res/{values,drawable,mipmap,font}/
├── core/
│   ├── build.gradle                                  ← +flavor block
│   └── src/
│       ├── main/java/org/openedx/core/ui/
│       │   ├── theme/
│       │   │   ├── Theme.kt                          ← +3 líneas (CompositionLocalProvider)
│       │   │   └── brand/
│       │   │       ├── BrandPalette.kt
│       │   │       ├── BrandTypography.kt
│       │   │       ├── BrandShapes.kt
│       │   │       ├── BrandSpacing.kt
│       │   │       ├── BrandElevations.kt
│       │   │       ├── BrandGradients.kt
│       │   │       ├── BrandHaptics.kt
│       │   │       └── LocalBrand.kt
│       │   └── brand/
│       │       └── components/
│       │           ├── BrandTabBar.kt
│       │           ├── BrandChip.kt
│       │           ├── BrandProgressRing.kt
│       │           ├── BrandSectionHeader.kt
│       │           ├── BrandStatTile.kt
│       │           ├── BrandFactTile.kt
│       │           ├── BrandFloatingCTA.kt
│       │           ├── BrandToggleRow.kt
│       │           ├── BrandUnitAccordion.kt
│       │           ├── BrandTimelineItem.kt
│       │           ├── BrandCertificateCard.kt
│       │           ├── DecorativeRings.kt
│       │           ├── PatternCover.kt
│       │           ├── CourseCategory.kt
│       │           ├── CourseCoverImage.kt
│       │           ├── CourseStatsStrip.kt
│       │           └── reader/
│       │               ├── BrandReaderTopBar.kt
│       │               ├── BrandReaderBottomBar.kt
│       │               └── BrandReaderProgressRail.kt
│       └── aprende/
│           └── res/
│               ├── values/colors.xml                  ← override light_*
│               └── font/                              ← Noto Sans TTFs
├── dashboard/
│   ├── build.gradle                                  ← +flavor block
│   └── src/
│       ├── main/java/.../LearnFragment.kt            ← +switch BuildConfig
│       ├── main/java/.../brand/
│       │   ├── LearnBrandView.kt
│       │   ├── PrimaryCourseCardBrand.kt
│       │   └── CourseListItemBrand.kt
│       └── aprende/res/values/strings.xml
├── discovery/
│   ├── build.gradle
│   └── src/
│       ├── main/java/.../NativeDiscoveryFragment.kt  ← +switch
│       ├── main/java/.../brand/
│       │   ├── DiscoveryBrandView.kt
│       │   └── CourseDiscoveryCardBrand.kt
│       └── aprende/res/values/strings.xml
├── course/
│   ├── build.gradle
│   └── src/
│       ├── main/java/.../CourseContainerFragment.kt  ← +switch
│       ├── main/java/.../CourseUnitContainerFragment.kt ← +switch
│       ├── main/java/.../brand/
│       │   ├── CourseHeaderBrandView.kt
│       │   ├── CourseHomeBrandScreen.kt
│       │   ├── CourseOutlineBrandScreen.kt
│       │   ├── CourseProgressBrandScreen.kt
│       │   ├── CourseDatesBrandScreen.kt
│       │   ├── HtmlUnitBrandScreen.kt
│       │   ├── VideoUnitBrandScreen.kt
│       │   ├── ProblemUnitBrandScreen.kt
│       │   └── ContentReaderBrandView.kt
│       └── aprende/res/values/strings.xml
├── profile/
│   ├── build.gradle
│   └── src/
│       ├── main/java/.../ProfileFragment.kt          ← +switch
│       ├── main/java/.../SettingsFragment.kt         ← +switch
│       ├── main/java/.../ManageAccountFragment.kt    ← +switch
│       ├── main/java/.../brand/
│       │   ├── ProfileBrandScreen.kt
│       │   ├── SettingsBrandScreen.kt
│       │   ├── ManageAccountBrandScreen.kt
│       │   └── ProfileRowBrand.kt
│       └── aprende/res/values/strings.xml
└── plan_implementacion_ui_android.md                  ← este documento
```

**Total touch points upstream:** ~10 archivos con patches ≤ 5 líneas cada uno.

---

## 10. Estado de ejecución

| Fase | Scope | Estado | Commit |
|---|---|---|---|
| F0 | Fundación: flavor + tokens + Noto Sans + atoms | ⏳ pendiente | — |
| F1 | Dashboard (Aprende) | ⏳ pendiente | — |
| F2 | BrandTabBar global | ⏳ pendiente | — |
| F3 | Discovery (Descubre) | ⏳ pendiente | — |
| F4 | Course Detail (hub + 7 tabs + sticky CTA) | ⏳ pendiente | — |
| F5 | Lesson reader (top/bottom + pager + html injection + quiz nativo) | ⏳ pendiente | — |
| F6 | Profile (hero + stats + certificates + menu) | ⏳ pendiente | — |
| F7 | Settings + ManageAccount | ⏳ pendiente | — |
| F8 | Cleanup + QA final + rebase test | ⏳ pendiente | — |

Cada fase: build verde → smoke test → commit `aprende:` → siguiente fase. Sin paralelismo.

---

## 11. Orden absoluto de ejecución

```
F0 (Fundación) → F1 (Dashboard) → F2 (TabBar) → F3 (Discovery) → F4 (Course) → F5 (Lesson) → F6 (Profile) → F7 (Settings) → F8 (Cleanup/QA)
```

**Próxima acción:** confirmar inicio Fase 0 — setup flavor `aprende` + bundle Noto Sans + tokens brand + atoms.

---

## 12. Referencias

- iOS spec canónico: `/Users/diegonicolas/proyectos/app/openedx-app-ios/ANDROID_DESIGN_SPEC.md`
- iOS plan implementación: `/Users/diegonicolas/proyectos/app/openedx-app-ios/plan_implementacion_ui.md`
- iOS plan curso: `/Users/diegonicolas/proyectos/app/openedx-app-ios/plan_master_curso.md`
- iOS registro errores: `/Users/diegonicolas/proyectos/app/openedx-app-ios/registro_errores_ui.md`
- Android arquitectura módulos: `/Users/diegonicolas/proyectos/app/openedx-app-android/NOTAS_ARQUITECTURA_OPENEDX.md`
- Theme changes log: `/Users/diegonicolas/proyectos/app/openedx-app-android/THEME_CHANGES.md` (deprecar plan TT Rounds/Fraunces ahí mismo)
- Memoria iOS sesión: `/Users/diegonicolas/.claude/projects/-Users-diegonicolas-proyectos-app-openedx-app-ios/memory/`

---

*Documento canónico del sprint Android. Cada commit `aprende:` debe citarse en sección 10 al cerrar fase. Errores documentados van en `registro_errores_ui_android.md` (crear al primer incidente).*
