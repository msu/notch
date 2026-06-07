---
layout: default
title: Install
permalink: /install/
---

# Install

Prerequisites: Should be None

You can always manually install also. For those who wish to, just download from the latest release and extract from there.

### macOS

```bash
curl -fsSL https://notch.cs.montana.edu/install.sh | bash
```

### Linux

```bash
curl -fsSL https://notch.cs.montana.edu/install.sh | bash
```

For `x86_64` and `arm64`.

### Shells other than bash for linux

The install script writes only to `~/.bashrc`. If the interactive shell is something else, add the PATH entry manually after install:

### Windows

In PowerShell

```powershell
iwr https://notch.cs.montana.edu/install.ps1 | iex
```

---

## Uninstall

### macOS

```bash
rm -rf ~/.notch
sed -i '' '/^# notch$/,+1d' ~/.zshrc
```

### Linux

```bash
rm -rf ~/.notch
sed -i '/^# notch$/,+1d' ~/.bashrc
```

Reminder too remove manually path addition if you manually added for a shell other than bash.

### Windows

```powershell
Remove-Item -Recurse -Force $env:LOCALAPPDATA\notch
$appDir = "$env:LOCALAPPDATA\notch\notch"
$userPath = [Environment]::GetEnvironmentVariable("PATH", "User")
$cleaned = (($userPath -split ";") | Where-Object { $_ -ne $appDir -and $_ -ne "" }) -join ";"
[Environment]::SetEnvironmentVariable("PATH", $cleaned, "User")
```
