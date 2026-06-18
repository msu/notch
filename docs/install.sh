#!/usr/bin/env bash
set -euo pipefail

REPO="msu/notch"
INSTALL_DIR="${HOME}/.notch"
BIN_DIR="${INSTALL_DIR}/bin"
PATH_LINE='export PATH="$HOME/.notch/bin:$PATH"'

err() { printf '\033[31merror:\033[0m %s\n' "$*" >&2; }

case "$(uname -s)" in
  Darwin) os="macos" ;;
  Linux)  os="linux" ;;
  *) err "Unsupported OS: $(uname -s). Use install.ps1 on Windows."; exit 1 ;;
esac

case "$(uname -m)" in
  arm64|aarch64) arch="arm64" ;;
  x86_64|amd64)  arch="x86_64" ;;
  *) err "Unsupported architecture: $(uname -m)."; exit 1 ;;
esac

asset="notch-${os}-${arch}.tar.gz"
url="https://github.com/${REPO}/releases/latest/download/${asset}"

tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT INT TERM

echo ":: Downloading ${asset}"
curl --retry 3 --retry-delay 2 -fsSL "$url" -o "${tmp}/notch.tar.gz" \
  || { err "Download failed: ${url}"; exit 1; }

if [[ -e "$INSTALL_DIR" ]]; then
  if [[ ! -O "$INSTALL_DIR" ]]; then
    err "${INSTALL_DIR} is not owned by you. Run: sudo rm -rf ${INSTALL_DIR} && retry."
    exit 1
  fi
  echo ":: Replacing existing install at ${INSTALL_DIR}"
  rm -rf "$INSTALL_DIR" \
    || { err "Couldn't remove ${INSTALL_DIR} (root-owned contents?). Run: sudo rm -rf ${INSTALL_DIR} && retry."; exit 1; }
fi

mkdir -p "$INSTALL_DIR"
tar -xzf "${tmp}/notch.tar.gz" -C "$INSTALL_DIR"

if [[ "$os" == "macos" ]]; then
  launcher="${INSTALL_DIR}/notch.app/Contents/MacOS/notch"
  rc="${HOME}/.zshrc"
else
  launcher="${INSTALL_DIR}/notch/bin/notch"
  rc="${HOME}/.bashrc"
fi

if [[ ! -x "$launcher" ]]; then
  err "Tarball is missing the launcher at ${launcher}. Report at https://github.com/${REPO}/issues"
  exit 1
fi

mkdir -p "$BIN_DIR"
ln -sf "$launcher" "${BIN_DIR}/notch"

path_wired=true
if ! grep -Fq "$PATH_LINE" "$rc" 2>/dev/null; then
  if ! printf '\n# notch\n%s\n' "$PATH_LINE" >> "$rc" 2>/dev/null; then
    err "Couldn't write to ${rc}."
    path_wired=false
  fi
fi

echo "[ok] Notch installed at ${INSTALL_DIR}"
if $path_wired; then
  echo "Open a new terminal and run: notch"
else
  echo "Add this to your shell rc, then open a new terminal:"
  printf '    %s\n' "$PATH_LINE"
fi
echo "(Other shells: add ${BIN_DIR} to your PATH manually.)"
