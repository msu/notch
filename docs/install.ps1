$ErrorActionPreference = 'Stop'

$Repo = 'msu/notch'
$InstallRoot = Join-Path $env:LOCALAPPDATA 'notch'
$AppDir = Join-Path $InstallRoot 'notch'

function Write-Info($msg) { Write-Host ":: $msg" -ForegroundColor Blue }
function Write-Ok($msg)   { Write-Host "[ok] $msg" -ForegroundColor Green }
function Write-Err($msg)  { Write-Host "error: $msg" -ForegroundColor Red }

$arch = $env:PROCESSOR_ARCHITECTURE
if ($arch -ne 'AMD64' -and $arch -ne 'ARM64') {
    Write-Err "Unsupported architecture: $arch. Notch supports x86_64 (AMD64), and ARM64 via Prism emulation."
    exit 1
}

$asset = 'notch-windows-x86_64.zip'
$url = "https://github.com/$Repo/releases/latest/download/$asset"
$tmpZip = Join-Path $env:TEMP 'notch.zip'

Write-Info "Downloading $asset"
try {
    Invoke-WebRequest -Uri $url -OutFile $tmpZip -UseBasicParsing
} catch {
    Write-Err "Download failed from $url. Check your network or visit https://github.com/$Repo/releases"
    exit 1
}

if (Test-Path $InstallRoot) {
    Write-Info "Replacing existing install at $InstallRoot"
    try {
        Remove-Item -Recurse -Force $InstallRoot
    } catch {
        Write-Err "Could not remove $InstallRoot. If notch.exe is currently running, close it and re-run."
        exit 1
    }
}

New-Item -ItemType Directory -Force -Path $InstallRoot | Out-Null
try {
    Expand-Archive -Path $tmpZip -DestinationPath $InstallRoot -Force
} catch {
    Write-Err "Extraction failed - the download may have been incomplete. Re-run this installer."
    exit 1
} finally {
    Remove-Item -Force -ErrorAction SilentlyContinue $tmpZip
}

if (-not (Test-Path "$AppDir\notch.exe")) {
    Write-Err "Archive is missing the launcher at $AppDir\notch.exe. Report at https://github.com/$Repo/issues"
    exit 1
}

$userPath = [Environment]::GetEnvironmentVariable('PATH', 'User')
if (-not $userPath) { $userPath = '' }
$parts = $userPath -split ';' | Where-Object { $_ -ne '' -and $_ -ne $AppDir }
$newPath = (@($AppDir) + $parts) -join ';'
if ($newPath -ne $userPath) {
    [Environment]::SetEnvironmentVariable('PATH', $newPath, 'User')
}
$env:PATH = "$AppDir;$env:PATH"

Write-Ok "Notch installed at $InstallRoot"
Write-Host ""
Write-Host "Run: notch  (works now in this session; new PowerShell sessions pick up PATH automatically)"
Write-Host "Docs and troubleshooting: https://notch.cs.montana.edu/"
Write-Host "Uninstall: Remove-Item -Recurse '$InstallRoot'  and remove '$AppDir' from your User PATH."
