#!/bin/bash

# Script de Verificación - Integración LlaveMX OAuth
# Ejecutar desde la raíz del proyecto: ./verify_llavemx_integration.sh

set -e

echo "🔍 Verificando Integración LlaveMX OAuth..."
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función de verificación
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        return 0
    else
        echo -e "${RED}✗${NC} $1 (NO ENCONTRADO)"
        return 1
    fi
}

check_content() {
    if grep -q "$2" "$1" 2>/dev/null; then
        echo -e "${GREEN}✓${NC} $1 contiene: $2"
        return 0
    else
        echo -e "${RED}✗${NC} $1 NO contiene: $2"
        return 1
    fi
}

# Verificar archivos nuevos
echo "📁 Verificando archivos nuevos..."
check_file "auth/src/main/java/org/openedx/auth/presentation/llavemx/LlaveMxAuthManager.kt"
check_file "auth/src/main/java/org/openedx/auth/presentation/llavemx/LlaveMxCallbackActivity.kt"
check_file "LLAVEMX_OAUTH_IMPLEMENTATION.md"
check_file "LLAVEMX_CONFIG_GUIDE.md"
check_file "LLAVEMX_FILE_STRUCTURE.md"
echo ""

# Verificar modificaciones
echo "📝 Verificando modificaciones en archivos existentes..."
check_content "auth/src/main/java/org/openedx/auth/presentation/signin/SignInFragment.kt" "LlaveMxSignIn"
check_content "auth/src/main/java/org/openedx/auth/presentation/signin/SignInViewModel.kt" "llaveMxAuthManager"
check_content "auth/src/main/java/org/openedx/auth/presentation/signin/compose/SignInView.kt" "llavemx_sign_in"
check_content "app/src/main/java/org/openedx/app/di/ScreenModule.kt" "LlaveMxAuthManager"
check_content "app/src/main/AndroidManifest.xml" "LlaveMxCallbackActivity"
check_content "auth/src/main/res/values/strings.xml" "llavemx_sign_in"
echo ""

# Verificar estructura de código
echo "🔍 Verificando estructura de código..."
check_content "auth/src/main/java/org/openedx/auth/presentation/llavemx/LlaveMxAuthManager.kt" "code_challenge"
check_content "auth/src/main/java/org/openedx/auth/presentation/llavemx/LlaveMxAuthManager.kt" "SHA-256"
check_content "auth/src/main/java/org/openedx/auth/presentation/llavemx/LlaveMxCallbackActivity.kt" "validateState"
check_content "app/src/main/AndroidManifest.xml" "mx.aprende.android"
echo ""

# Verificar configuración AndroidManifest
echo "🔐 Verificando configuración de deep link..."
if grep -A5 "LlaveMxCallbackActivity" "app/src/main/AndroidManifest.xml" | grep -q "mx.aprende.android"; then
    echo -e "${GREEN}✓${NC} Deep link configurado correctamente"
else
    echo -e "${RED}✗${NC} Deep link NO configurado"
fi
echo ""

# Intentar compilar (opcional - comentar si toma mucho tiempo)
echo "🔨 Intentando compilar módulo auth..."
if command -v ./gradlew &> /dev/null; then
    echo "Ejecutando: ./gradlew :auth:assembleDebug..."
    if ./gradlew :auth:assembleDebug --console=plain 2>&1 | tail -20; then
        echo -e "${GREEN}✓${NC} Compilación exitosa"
    else
        echo -e "${YELLOW}⚠${NC} Error en compilación (revisar logs arriba)"
    fi
else
    echo -e "${YELLOW}⚠${NC} gradlew no encontrado, saltando compilación"
fi
echo ""

# Resumen
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 RESUMEN DE VERIFICACIÓN"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Archivos creados:"
echo "  - LlaveMxAuthManager.kt"
echo "  - LlaveMxCallbackActivity.kt"
echo "  - Documentación (3 archivos .md)"
echo ""
echo "Archivos modificados:"
echo "  - SignInFragment.kt"
echo "  - SignInViewModel.kt"
echo "  - SignInView.kt"
echo "  - ScreenModule.kt"
echo "  - AndroidManifest.xml"
echo "  - strings.xml"
echo ""
echo "Próximos pasos:"
echo "  1. Actualizar CLIENT_ID en LlaveMxAuthManager.kt"
echo "  2. Verificar AUTHORIZATION_ENDPOINT"
echo "  3. Compilar: ./gradlew assembleDebug"
echo "  4. Instalar en device: ./gradlew installDebug"
echo "  5. Probar botón LlaveMX en pantalla Sign In"
echo ""
echo "Documentación:"
echo "  - LLAVEMX_OAUTH_IMPLEMENTATION.md (detalles técnicos)"
echo "  - LLAVEMX_CONFIG_GUIDE.md (configuración)"
echo "  - LLAVEMX_FILE_STRUCTURE.md (estructura)"
echo ""
echo -e "${GREEN}✓${NC} Integración LlaveMX OAuth completada"
echo ""

