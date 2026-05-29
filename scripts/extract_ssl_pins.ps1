# =============================================================================
# extract_ssl_pins.ps1
# =============================================================================
# Extrae los pines SPKI SHA-256 para implementar SSL Pinning en la app Android.
#
# Detecta:
#   1. Si el dominio esta detras de Cloudflare (via headers cf-ray, server)
#   2. Cadena completa de certificados servida hoy
#   3. SPKI SHA-256 del cert hoja (pin primario)
#   4. SPKI SHA-256 de la CA intermediaria (pin backup)
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File scripts\extract_ssl_pins.ps1
# =============================================================================

$openssl = "C:\Program Files\Git\usr\bin\openssl.exe"
$domains = @("dev.mexicox.gob.mx", "cursos.aprende.gob.mx")
$outDir  = "$PSScriptRoot\..\ssl_pins_output"

if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

foreach ($domain in $domains) {
    Write-Host ""
    Write-Host "===================================================================="
    Write-Host " Dominio: $domain"
    Write-Host "===================================================================="

    # --- 1. Detectar Cloudflare via headers ---
    Write-Host ""
    Write-Host "[1/4] Detectando Cloudflare..."
    try {
        $resp = Invoke-WebRequest -Uri "https://$domain/" -Method Head -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
        $server     = $resp.Headers["Server"]
        $cfRay      = $resp.Headers["CF-Ray"]
        $cfCache    = $resp.Headers["CF-Cache-Status"]

        if ($server -match "cloudflare" -or $cfRay) {
            Write-Host "  CLOUDFLARE DETECTADO" -ForegroundColor Green
            Write-Host "    Server:          $server"
            Write-Host "    CF-Ray:          $cfRay"
            Write-Host "    CF-Cache-Status: $cfCache"
        } else {
            Write-Host "  NO se detecto Cloudflare" -ForegroundColor Yellow
            Write-Host "    Server:          $server"
        }
    } catch {
        Write-Host "  ERROR al hacer HEAD request: $($_.Exception.Message)" -ForegroundColor Red
    }

    # --- 2. Descargar la cadena completa de certs ---
    Write-Host ""
    Write-Host "[2/4] Descargando cadena de certificados..."
    $chainFile = Join-Path $outDir "$domain.chain.pem"
    "" | & $openssl s_client -connect "${domain}:443" -servername $domain -showcerts -verify 5 2>$null | Out-File -Encoding ASCII -FilePath $chainFile

    if (-not (Test-Path $chainFile) -or (Get-Item $chainFile).Length -eq 0) {
        Write-Host "  ERROR: no se pudo descargar la cadena" -ForegroundColor Red
        continue
    }

    # Separar cada cert de la cadena en archivos individuales
    $chainContent = Get-Content $chainFile -Raw
    $certBlocks = [regex]::Matches($chainContent, "(?s)-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----")
    Write-Host "  Cadena recibida: $($certBlocks.Count) certificados"

    for ($i = 0; $i -lt $certBlocks.Count; $i++) {
        $certFile = Join-Path $outDir "$domain.cert$i.pem"
        $certBlocks[$i].Value | Out-File -Encoding ASCII -FilePath $certFile
    }

    # --- 3. Analizar cada cert: subject, issuer, SPKI SHA-256 ---
    Write-Host ""
    Write-Host "[3/4] Analizando certificados..."
    for ($i = 0; $i -lt $certBlocks.Count; $i++) {
        $certFile = Join-Path $outDir "$domain.cert$i.pem"

        $subject = & $openssl x509 -in $certFile -noout -subject 2>$null
        $issuer  = & $openssl x509 -in $certFile -noout -issuer 2>$null
        $dates   = & $openssl x509 -in $certFile -noout -dates 2>$null

        # Calcular SPKI SHA-256 en base64 (formato que usa OkHttp y NSC)
        # Usamos cmd /c para evitar que PowerShell contamine los pipes binarios con BOM.
        $opensslCmd = $openssl.Replace('\', '\\')
        $certFileCmd = $certFile.Replace('\', '\\')
        $cmdLine = "`"$opensslCmd`" x509 -in `"$certFileCmd`" -pubkey -noout | `"$opensslCmd`" pkey -pubin -outform der | `"$opensslCmd`" dgst -sha256 -binary | `"$opensslCmd`" enc -base64"
        $spki = (& cmd.exe /c $cmdLine 2>$null).Trim()

        # Determinar si es hoja o CA
        $tipo = if ($i -eq 0) { "HOJA (leaf)" } else { "CA #$i" }

        Write-Host ""
        Write-Host "  --- Cert [$i] $tipo ---" -ForegroundColor Cyan
        Write-Host "    $subject"
        Write-Host "    $issuer"
        Write-Host "    $dates"
        Write-Host ""
        Write-Host "    SPKI SHA-256 (base64):" -ForegroundColor Yellow
        Write-Host "    $spki" -ForegroundColor Yellow
    }

    # --- 4. Guardar resumen ---
    Write-Host ""
    Write-Host "[4/4] Archivos guardados en: $outDir\$domain.*"
}

Write-Host ""
Write-Host "===================================================================="
Write-Host " RESUMEN PARA IMPLEMENTAR PINNING"
Write-Host "===================================================================="
Write-Host ""
Write-Host "Para network_security_config.xml usa los SPKI SHA-256 base64 de:"
Write-Host "  - cert[0]  (hoja)        -> pin primario"
Write-Host "  - cert[1]  (CA interm.)  -> pin backup"
Write-Host ""
Write-Host "Para OkHttp CertificatePinner el formato es:  sha256/<base64>"
Write-Host ""
