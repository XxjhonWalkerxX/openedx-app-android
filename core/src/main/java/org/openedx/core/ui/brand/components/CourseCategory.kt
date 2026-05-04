package org.openedx.core.ui.brand.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.openedx.core.ui.theme.brand.BrandPalette

/**
 * Categorías canónicas para portadas generativas y badges de curso.
 * Mapea cada categoría a un Material Icon (paridad SF Symbols iOS) + tinte de paleta.
 *
 * | Categoría     | iOS SF Symbol     | Android Material Icon |
 * |---------------|-------------------|------------------------|
 * | Seguridad     | shield.checkered  | Security               |
 * | Salud         | cross.case        | HealthAndSafety        |
 * | Administración| building.columns  | AccountBalance         |
 * | Educación     | graduationcap     | School                 |
 * | Tecnología    | cpu               | Memory                 |
 * | Ciudadanía    | person.3          | Groups                 |
 * | Ambiente      | leaf              | Eco                    |
 */
enum class CourseCategory(
    val icon: ImageVector,
    val tint: Color,
    val ink: Color,
) {
    Seguridad      (Icons.Filled.Security,        BrandPalette.Guinda,         BrandPalette.TextOnHeader),
    Salud          (Icons.Filled.HealthAndSafety, BrandPalette.BrandGreen,     BrandPalette.TextOnHeader),
    Administracion (Icons.Filled.AccountBalance,  BrandPalette.GuindaDeep,     BrandPalette.TextOnHeader),
    Educacion      (Icons.Filled.School,          BrandPalette.Guinda,         BrandPalette.TextOnHeader),
    Tecnologia     (Icons.Filled.Memory,          BrandPalette.BrandGreenDark, BrandPalette.TextOnHeader),
    Ciudadania     (Icons.Filled.Groups,          BrandPalette.Guinda,         BrandPalette.TextOnHeader),
    Ambiente       (Icons.Filled.Eco,             BrandPalette.BrandGreen,     BrandPalette.TextOnHeader),
    ;

    companion object {
        /**
         * Heurística temporal para inferir categoría desde tags / nombre del curso.
         * Cuando backend exponga campo explícito, reemplazar.
         *
         * Default seguro: [Educacion].
         */
        fun from(tags: List<String>?): CourseCategory {
            if (tags.isNullOrEmpty()) return Educacion
            val lower = tags.joinToString(" ") { it.lowercase() }
            return when {
                "seguridad" in lower || "protec" in lower -> Seguridad
                "salud" in lower || "médico" in lower || "medico" in lower -> Salud
                "administra" in lower || "gestión" in lower || "gestion" in lower || "gobierno" in lower -> Administracion
                "tecnolog" in lower || "digital" in lower || "informát" in lower || "informat" in lower -> Tecnologia
                "ciudadan" in lower || "social" in lower || "comunidad" in lower -> Ciudadania
                "ambient" in lower || "ecolog" in lower || "sustentab" in lower -> Ambiente
                else -> Educacion
            }
        }
    }
}
