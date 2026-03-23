# Archivos modificados — tema @prende.mx

Registro de todos los archivos del fork que difieren del upstream.
Actualizar este archivo con cada commit `aprende:`.

## core/src/main/java/org/openedx/core/ui/theme/

| Archivo | Estado | Descripción del cambio |
|---|---|---|
| `AppColors.kt` | pendiente | Paleta institucional @prende.mx |
| `AppTheme.kt` | pendiente | AprendeTheme sin dark mode |
| `AppType.kt` | pendiente | Headline serif italic |
| `AppShapes.kt` | pendiente | Corners 12dp / 50dp |

## core/src/main/java/org/openedx/core/ui/

| Archivo | Estado | Descripción del cambio |
|---|---|---|
| `AprendeTopBar.kt` | pendiente | Nuevo — header vino institucional |
| `CourseCard.kt` | pendiente | Modificado — barra acento 4dp, ratio 3:4 |
| `AprendeSearchBar.kt` | pendiente | Nuevo — pill full-width |
| `SkeletonLoader.kt` | pendiente | Nuevo — shimmer crema |
| `FilterChip.kt` | pendiente | Nuevo — pills de categoría |

## auth/

| Archivo | Estado | Descripción del cambio |
|---|---|---|
| `signin/compose/SignInView.kt` | ✅ hecho | Header verde institucional (reemplaza PNG), separador "o continúa con", LlaveMX usa `llave_mx_primary`, sin dark previews |
| `res/values/strings.xml` | ✅ hecho | Strings en español @prende.mx (pre_auth, auth_welcome_back, errores, forgot_password) + `auth_or_continue_with` |
| `SignUpScreen.kt` | pendiente | Stepper vino |
| `ForgotPasswordScreen.kt` | pendiente | Simplificado |

## app/

| Archivo | Estado | Descripción del cambio |
|---|---|---|
| `MainScreenV2.kt` | pendiente | Pill indicator nav activo |

## dashboard/

| Archivo | Estado | Descripción del cambio |
|---|---|---|
| `DashboardGalleryView.kt` | pendiente | Greeting, stats, carrusel |
| `AllEnrolledCoursesView.kt` | pendiente | Grid 2 col + progreso |

## course/

| Archivo | Estado | Descripción del cambio |
|---|---|---|
| `CourseHomeScreen.kt` | pendiente | Header verde, badges, CTA |
| `CourseOutlineScreen.kt` | pendiente | Chips de estado |
| `VideoUnitScreen.kt` | pendiente | Tint verde controles |
| `CertificateView.kt` | pendiente | Banner vino |

## discovery/

| Archivo | Estado | Descripción del cambio |
|---|---|---|
| `NativeDiscoveryScreen.kt` | pendiente | SearchBar pill, grid 2 col |
| `CourseDetailScreen.kt` | pendiente | Hero vino, CTA inscripción |

## profile/

| Archivo | Estado | Descripción del cambio |
|---|---|---|
| `ProfileScreen.kt` | pendiente | Avatar, stats, crema |
| `EditProfileScreen.kt` | pendiente | Inputs 12dp |
| `SettingsScreen.kt` | pendiente | Toggles verde |

---
*Actualizado: inicio del proyecto*
