param([string]$TimestampUrl = 'http://timestamp.digicert.com')
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not $env:CLIPVAULT_CERT_THUMBPRINT) { throw 'Set CLIPVAULT_CERT_THUMBPRINT to your certificate-store thumbprint.' }
$app = Join-Path $PWD 'desktop\build\compose\binaries\main\app\ClipVault'
if (-not (Test-Path "$app\ClipVault.exe")) { throw 'Run .\gradlew.bat :desktop:createDistributable first.' }
function Sign-File([string]$Path) {
 & signtool sign /sha1 $env:CLIPVAULT_CERT_THUMBPRINT /fd SHA256 /tr $TimestampUrl /td SHA256 $Path
 if ($LASTEXITCODE -ne 0) { throw "Signing failed: $Path" }
}
Sign-File "$app\ClipVault.exe"
New-Item -ItemType Directory -Force release | Out-Null
foreach ($format in @('exe','msi')) {
 & "$env:JAVA_HOME\bin\jpackage.exe" --type $format --name ClipVault --app-version 1.0.0 --vendor Zunawet --app-image $app --dest release --win-per-user-install --win-dir-chooser --win-menu --win-menu-group ClipVault --win-shortcut --win-upgrade-uuid 7f3dab41-f647-4eab-95dd-6ba8c311ed79
 if ($LASTEXITCODE -ne 0) { throw "jpackage $format failed" }
 Get-ChildItem "release\*.$format" | ForEach-Object { Sign-File $_.FullName }
}
