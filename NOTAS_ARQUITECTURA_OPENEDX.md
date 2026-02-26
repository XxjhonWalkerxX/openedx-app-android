# Análisis Completo de la Arquitectura Open edX Android

> **Fecha de análisis:** Febrero 2026  
> **Proyecto:** openedx-app-android  
> **Propósito:** Documentación para personalización de identidad gráfica

---

## 1️⃣ Arquitectura General del Proyecto

### Tipo de Aplicación

El proyecto es una **aplicación multi-módulo** con arquitectura de **feature modules**. Cada funcionalidad principal está aislada en su propio módulo Gradle.

### Estructura de Módulos

```
openedx-app-android/
├── app/                    # Módulo principal (Application)
├── core/                   # Módulo compartido (utilidades, tema, API base)
├── auth/                   # Feature: autenticación y login
├── dashboard/              # Feature: pantalla principal (Learn/Home)
├── course/                 # Feature: contenido de cursos
├── discovery/              # Feature: búsqueda y catálogo de cursos
├── discussion/             # Feature: foros de discusión
├── profile/                # Feature: perfil de usuario
├── whatsnew/               # Feature: novedades de la app
└── notifications/          # Feature: notificaciones
```

### Rol de Cada Módulo

| Módulo | Tipo | Responsabilidad |
|--------|------|-----------------|
| `app` | Application | Punto de entrada, DI (Koin), navegación global, fusiona todos los módulos |
| `core` | Library | Tema Compose, componentes UI compartidos, modelos base, extensiones |
| `dashboard` | Feature Library | Pantalla Home/Learn, lista de cursos matriculados |
| `course` | Feature Library | Detalle de curso, contenido, videos, unidades |
| `auth` | Feature Library | Login, registro, recuperación de contraseña |
| `discovery` | Feature Library | Catálogo de cursos, búsqueda |
| `profile` | Feature Library | Configuración de usuario, preferencias |

### Relación Entre Módulos

```
app
 ├── dashboard ──┐
 ├── course ─────┼──► core (dependencia común)
 ├── auth ───────┤
 ├── discovery ──┤
 ├── profile ────┤
 └── notifications
```

Verificado en `dashboard/build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":core"))
    // ...
}
```

---

## 2️⃣ Cómo se Construye la UI

### Patrón Híbrido: XML + Compose

El proyecto usa un **patrón híbrido**:
- **Fragments con XML** como contenedores de navegación
- **Compose** para el contenido real de las pantallas

### Ejemplo en Dashboard

**Fragment contenedor** (`LearnFragment.kt`):
```kotlin
class LearnFragment : Fragment(R.layout.fragment_learn) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.composeCollapsingLayout.setContent {
            // Compose UI aquí
            LearnScreen(...)
        }
    }
}
```

**Layout XML** (`fragment_learn.xml`):
```xml
<FrameLayout
    android:background="@color/dashboard_background">
    
    <org.openedx.core.ui.ComposeCollapsingLayout
        android:id="@+id/composeCollapsingLayout" />
        
</FrameLayout>
```

### Dónde Vive Cada Tipo de UI

| Tipo | Ubicación | Uso |
|------|-----------|-----|
| XML Layouts | `*/src/main/res/layout/` | Contenedores de Fragment |
| Compose Screens | `*/src/main/java/.../ui/` | Contenido visual real |
| Compose Theme | `core/src/openedx/.../theme/` | Colores, tipografía, formas |
| Drawables XML | `*/src/main/res/drawable/` | Fondos, íconos vectoriales |

---

## 3️⃣ Flujo del Dashboard (Home/Learn)

### Punto de Entrada

El flujo inicia desde la navegación principal en `app`:

```
MainActivity → BottomNavigation → LearnFragment
```

### Archivos Clave del Dashboard

```
dashboard/src/main/
├── java/org/openedx/dashboard/
│   ├── presentation/
│   │   ├── DashboardRouter.kt          # Navegación
│   │   ├── LearnFragment.kt            # Fragment contenedor
│   │   ├── LearnViewModel.kt           # Estado y lógica
│   │   └── ui/
│   │       └── LearnScreen.kt          # Composable principal
│   └── data/
│       └── DashboardRepository.kt      # Acceso a datos
└── res/
    ├── layout/
    │   └── fragment_learn.xml          # Layout contenedor
    ├── values/
    │   ├── colors.xml                  # Colores del módulo
    │   └── dimens.xml                  # Dimensiones
    └── drawable/
        └── bg_dashboard_card.xml       # Fondo de cards
```

### Renderizado de Cards de Cursos

Las cards de cursos se renderizan **100% en Compose**, no hay `item_*.xml`:

```kotlin
// En LearnScreen.kt o CourseCard.kt
@Composable
fun CourseCard(
    course: EnrolledCourse,
    // ...
) {
    Card(
        backgroundColor = MaterialTheme.appColors.cardViewBackground,
        // ...
    ) {
        // Contenido del curso
    }
}
```

El color `cardViewBackground` viene de `Colors.kt`:
```kotlin
val light_card_view_background = brand_cream_strong  // #DFD4C2
```

### Por Qué No Hay item_*.xml

Open edX Android **migró completamente a Compose** para las listas de cursos. Los únicos XML que quedan son:
- Contenedores de Fragment (`fragment_*.xml`)
- Drawables vectoriales y shapes
- Recursos de configuración

---

## 4️⃣ Sistema de Recursos

### Estructura de Recursos por Módulo

```
module/src/main/res/
├── values/
│   ├── colors.xml      # Colores específicos del módulo
│   ├── dimens.xml      # Dimensiones específicas
│   ├── strings.xml     # Textos localizados
│   └── themes.xml      # (raro en feature modules)
├── drawable/
│   └── *.xml           # Shapes, vectors
└── layout/
    └── *.xml           # Solo contenedores Fragment
```

### Fusión de Recursos en Gradle

Gradle/AAPT2 fusiona recursos de **abajo hacia arriba**:

```
core/res/values/colors.xml     (base)
       ↓
dashboard/res/values/colors.xml (override por módulo)
       ↓
app/res/values/colors.xml      (override final)
```

**Regla importante**: Un recurso en `app` **sobreescribe** el mismo nombre en cualquier módulo.

### Qué Recursos Van Dónde

| Recurso | Ubicación Correcta | Por Qué |
|---------|-------------------|---------|
| Colores del tema | `core/src/openedx/.../theme/Colors.kt` | Compose lee de aquí |
| Colores para XML | `app/src/main/res/values/colors.xml` | Override global |
| Colores de módulo | `dashboard/res/values/colors.xml` | Solo si ese módulo los usa en XML |
| Dimensiones globales | `core/res/values/dimens.xml` | Compartidas |
| Dimensiones de módulo | `dashboard/res/values/dimens.xml` | Específicas del feature |

### Recursos para androidTest

Los tests de Android tienen su **propio scope de recursos**:

```
dashboard/src/
├── main/res/values/dimens.xml      ← Producción
└── androidTest/res/values/dimens.xml  ← Tests instrumentados
```

**Por qué fallan los tests**: AAPT2 compila `androidTest` como un APK separado. Si un drawable referencia `@dimen/X`, ese dimen debe existir también en `androidTest/res/`.

---

## 5️⃣ Buenas Prácticas del Proyecto

### Archivos Seguros para Modificar (Merge-Safe)

| Archivo | Seguridad | Razón |
|---------|-----------|-------|
| `core/src/openedx/.../Colors.kt` | ✅ Alta | Directorio de tema configurable |
| `app/src/main/res/values/colors.xml` | ✅ Alta | Override local |
| `*/res/drawable/bg_*.xml` | ✅ Alta | Nuevos archivos no conflictúan |
| `config.yaml` | ✅ Alta | Configuración local |

### Archivos a EVITAR Modificar

| Archivo | Riesgo | Razón |
|---------|--------|-------|
| `core/src/main/.../AppColors.kt` | ⚠️ Medio | Estructura del tema |
| `*.gradle.kts` | ⚠️ Medio | Cambios frecuentes upstream |
| `*/presentation/*ViewModel.kt` | 🔴 Alto | Lógica de negocio |
| `*/data/*Repository.kt` | 🔴 Alto | Acceso a API |

### Patrón de Personalización Oficial

Open edX usa **Theme Directories** configurables en `build.gradle`:

```kotlin
// En config.yaml o build.gradle
android {
    sourceSets {
        main {
            res.srcDirs = ["src/main/res", "src/openedx/res"]
            java.srcDirs = ["src/main/java", "src/openedx"]
        }
    }
}
```

El directorio `src/openedx/` es **el lugar oficial para personalización**:
- `core/src/openedx/` → Tema Compose (Colors.kt, Typography.kt)
- Puedes crear tu propio directorio de tema y apuntarlo en Gradle

---

## 6️⃣ Testing y Variantes

### Source Sets en Android

```
module/src/
├── main/           ← Código de producción
├── test/           ← Unit tests (JVM)
├── androidTest/    ← Instrumented tests (dispositivo/emulador)
├── debug/          ← Solo variante debug
└── release/        ← Solo variante release
```

### Por Qué un Recurso Compila en main pero Falla en androidTest

```
❌ Error: resource dimen/dashboard_card_radius not found
   (org.openedx.dashboard.test)
```

**Causa**: El APK de androidTest se compila **separadamente**. AAPT2 resuelve referencias de recursos solo dentro de su scope.

**Solución**: Duplicar recursos necesarios en `androidTest/res/`:

```
dashboard/src/androidTest/res/values/dimens.xml
dashboard/src/androidTest/res/values/colors.xml
```

### Build Variants

El proyecto tiene múltiples variantes:

```
buildTypes:
  - debug
  - release

productFlavors:
  - develop
  - stage  
  - prod
```

Cada combinación genera un APK distinto. Los recursos se fusionan así:

```
main → {flavor} → {buildType} → final
```

---

## 7️⃣ Resumen Visual de la Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                          APP MODULE                          │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              MainActivity + Navigation                   ││
│  └─────────────────────────────────────────────────────────┘│
│         │          │           │          │                  │
│         ▼          ▼           ▼          ▼                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐        │
│  │dashboard │ │  course  │ │   auth   │ │ profile  │        │
│  │ Fragment │ │ Fragment │ │ Fragment │ │ Fragment │        │
│  │   (XML)  │ │   (XML)  │ │   (XML)  │ │   (XML)  │        │
│  │    ↓     │ │    ↓     │ │    ↓     │ │    ↓     │        │
│  │ Compose  │ │ Compose  │ │ Compose  │ │ Compose  │        │
│  │  Screen  │ │  Screen  │ │  Screen  │ │  Screen  │        │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘        │
│       │            │            │            │               │
│       └────────────┴────────────┴────────────┘               │
│                          │                                   │
│                          ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                    CORE MODULE                          ││
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      ││
│  │  │  Colors.kt  │  │ AppColors   │  │ Components  │      ││
│  │  │ (Tu tema)   │  │ (Interface) │  │ (Shared UI) │      ││
│  │  └─────────────┘  └─────────────┘  └─────────────┘      ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

---

## 8️⃣ Colores Institucionales EMI Aplicados

### Paleta de Colores

| Nombre | Hex | Uso iOS | Uso Android |
|--------|-----|---------|-------------|
| Verde institucional | `#2B6959` | AccentColor, AccentXColor | `light_primary`, `light_text_primary` |
| Crema claro | `#ECE9E4` | backgroundColor | `light_background`, `light_surface` |
| Crema fuerte | `#DFD4C2` | cardViewBackground, primaryHeaderColor | `light_card_view_background`, `light_course_home_header_shade` |
| Guinda institucional | `#611232` | Botones Llave MX | `llave_mx_primary`, `llave_mx_secondary_border` |

### Mapeo iOS → Android

| Variable iOS | Variable Android | Valor |
|--------------|------------------|-------|
| `AccentColor` | `light_primary` | `brand_green` |
| `AccentXColor` | `light_text_accent` | `brand_green` |
| `AccentButtonColor` | `light_primary_button_background` | `brand_green` |
| `InfoColor` | `light_info` | `brand_green` |
| `SecondaryButtonBorderColor` | `light_secondary_button_border` | `brand_green` |
| `SecondaryButtonTextColor` | `light_secondary_button_bordered_text` | `brand_green` |
| `slidingStrokeColor` | `light_component_horizontal_progress_selected` | `brand_green` |
| `slidingTextColor` | `light_tab_unselected_btn_content` | `brand_green` |
| `progressPercentage` | `light_progress_bar_color` | `brand_green` |
| `backgroundColor` | `light_background` | `brand_cream` |
| `cardViewBackground` | `light_card_view_background` | `brand_cream_strong` |
| `primaryHeaderColor` | `light_course_home_header_shade` | `brand_cream_strong` |
| `secondaryHeaderColor` | `light_course_home_header_shade` | `brand_cream_strong` |
| `textPrimaryColor` | `light_text_primary` | `brand_green` |

### Mapeo de Dimensiones iOS → Android

| Elemento | iOS (pts) | Android (dp) | Archivo |
|----------|-----------|--------------|---------|
| Course Header altura | 300→550 | 200→280 | `CourseContainerFragment.kt` |
| Dashboard Card imagen | 140→350 | 140→180 | `DashboardGalleryView.kt` |
| Logo maxWidth | 189→220 | 220 | `SignInLogoView.kt` |
| Logo maxHeight | 89→131 | 131 | `SignInLogoView.kt` |
| Card cornerRadius | 8 | 12 | `dimens.xml` |
| Button cornerRadius | 8 | 8 | `dimens.xml` |

### Archivos Modificados para Branding

```
core/src/openedx/org/openedx/core/ui/theme/Colors.kt
├── brand_green = Color(0xFF2B6959)
├── brand_cream = Color(0xFFECE9E4)
├── brand_cream_strong = Color(0xFFDFD4C2)
├── brand_guinda = Color(0xFF611232)
├── light_primary = brand_green
├── light_background = brand_cream
├── light_surface = brand_cream
├── light_card_view_background = brand_cream_strong
├── light_primary_button_background = brand_green
├── light_text_primary = brand_green
├── light_text_accent = brand_green
├── light_info = brand_green
├── light_course_home_header_shade = brand_cream_strong
├── light_progress_bar_color = brand_green
├── llave_mx_primary = brand_guinda (para Llave MX)
└── llave_mx_secondary_* (colores Llave MX)

core/src/openedx/org/openedx/core/ui/theme/AppDimens.kt (NUEVO)
├── signInLogoMaxWidth = 220.dp
├── signInLogoMaxHeight = 131.dp
├── courseHeaderHeightExpanded = 280.dp
├── dashboardPrimaryCourseImageHeight = 180.dp
├── cardCornerRadius = 12.dp
└── buttonCornerRadius = 8.dp

core/src/openedx/org/openedx/core/ui/theme/compose/SignInLogoView.kt
├── fillMaxHeight(0.22f) (aumentado)
├── widthIn(max = 220.dp)
└── heightIn(max = 131.dp)

course/src/main/.../CourseContainerFragment.kt
└── imageHeight = 280 (antes 200)

dashboard/src/main/.../DashboardGalleryView.kt
└── imageHeight = 180.dp (antes 140.dp)

dashboard/src/main/res/values/colors.xml
├── brand_green, brand_cream, brand_cream_strong, brand_guinda
├── dashboard_background → @color/brand_cream
└── brand_card → @color/brand_cream_strong

dashboard/src/main/res/values/dimens.xml
├── card_radius = 12dp
├── dashboard_card_radius = 12dp
├── dashboard_primary_card_image_height = 180dp
├── dashboard_hero_height = 280dp
└── button_corner_radius = 8dp

dashboard/src/androidTest/res/values/colors.xml (sincronizado)
dashboard/src/androidTest/res/values/dimens.xml (sincronizado)
```

---

## 9️⃣ Funcionalidad Pendiente: Llave MX

### Descripción (desde iOS)
Sección especial en Login con:
- Imagen cargada desde URL externa
- Botón principal (guinda #611232, texto blanco)
- Botón secundario (blanco, borde guinda)
- cornerRadius: 8dp

### Colores ya definidos en Colors.kt
```kotlin
val llave_mx_primary = brand_guinda           // #611232
val llave_mx_primary_text = Color.White
val llave_mx_secondary_background = Color.White
val llave_mx_secondary_border = brand_guinda
val llave_mx_secondary_text = brand_guinda
```

### Implementación requerida
1. Crear componente `LlaveMxSection.kt` en `auth/src/main/.../ui/`
2. Agregar al `SignInView.kt` después del formulario de login
3. No modificar ViewModel - usar configuración estática o remota

---

## 🔒 Forzar Modo Claro (Light Mode)

### En iOS
Se configura en `Info.plist` con `UIUserInterfaceStyle = Light`

### En Android
Modificar `Theme.kt` para ignorar el tema del sistema:

```kotlin
// En OpenEdXTheme()
@Composable
fun OpenEdXTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    // Forzar siempre tema claro ignorando isSystemInDarkTheme()
    val colors = LightColorPalette
    // ...
}
```

**Estado**: Pendiente de implementar si se requiere forzar modo claro.

---

## 🔟 Conclusión para Personalización

Para personalizar la identidad gráfica de forma **escalable y merge-safe**:

1. **Colores Compose** → Modifica `core/src/openedx/.../theme/Colors.kt`
2. **Colores XML** → Crea/modifica `app/src/main/res/values/colors.xml`
3. **Nuevos drawables** → Agrégalos en el módulo correspondiente
4. **Tests** → Duplica recursos necesarios en `androidTest/res/`
5. **No toques** → ViewModels, Repositories, archivos en `src/main/` del core

---

## 📚 Referencias Útiles

- **Documentación oficial**: `Documentation/ConfigurationManagement.md`
- **Tema por defecto**: `core/src/openedx/org/openedx/core/ui/theme/`
- **Configuración de flavors**: `core/build.gradle` (líneas 50-70)
- **Ejemplo de theming**: Ver cómo `THEME_DIRECTORY` se usa en build.gradle

---

*Documento generado como referencia para el equipo de desarrollo EMI.*

