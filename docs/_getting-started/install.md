---
title: "Install"
subtitle: "For macOS, Linux, and Windows"
description: "Install the Notch scripting language on macOS, Linux, or Windows with a single command, or download a release to install manually."
order: 1
---

No prerequisites

You can also install manually: download the latest release and extract it from there.

## macOS

```bash
curl -fsSL https://notch.cs.montana.edu/install.sh | bash
```

## Linux

```bash
curl -fsSL https://notch.cs.montana.edu/install.sh | bash
```

For `x86_64` and `arm64`.

## Shells other than bash on Linux

The install script writes to `~/.bashrc`. If your interactive shell is
something else, add the PATH entry manually after install.

## Windows

In PowerShell:

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

If you added the PATH entry manually for a shell other than bash, remove it
manually too.

### Windows

```powershell
Remove-Item -Recurse -Force $env:LOCALAPPDATA\notch
$appDir = "$env:LOCALAPPDATA\notch\notch"
$userPath = [Environment]::GetEnvironmentVariable("PATH", "User")
$cleaned = (($userPath -split ";") | Where-Object { $_ -ne $appDir -and $_ -ne "" }) -join ";"
[Environment]::SetEnvironmentVariable("PATH", $cleaned, "User")
```

## See also

- [Quickstart](../quickstart/) to run your first script.
- [Add to your project (Maven)](../maven/) to embed Notch as a library.
