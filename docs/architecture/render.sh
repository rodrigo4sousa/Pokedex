#!/usr/bin/env bash
# Renders every diagram in docs/architecture/*/ to SVG in docs/architecture/out/.
# Uses the plantuml/plantuml Docker image and mounts Arial into it, because the image only ships
# DejaVu fonts and style.puml uses Arial (text is measured with the font found in the container).
# Works in Git Bash on Windows, macOS and Linux. Run from anywhere: bash docs/architecture/render.sh
set -euo pipefail

repo="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo"

# Arial files per OS: Windows, macOS, Linux (msttcorefonts).
candidates=(
  /c/Windows/Fonts/arial.ttf /c/Windows/Fonts/arialbd.ttf /c/Windows/Fonts/ariali.ttf /c/Windows/Fonts/arialbi.ttf
  "/System/Library/Fonts/Supplemental/Arial.ttf" "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
  "/System/Library/Fonts/Supplemental/Arial Italic.ttf" "/System/Library/Fonts/Supplemental/Arial Bold Italic.ttf"
  /usr/share/fonts/truetype/msttcorefonts/Arial.ttf /usr/share/fonts/truetype/msttcorefonts/Arial_Bold.ttf
  /usr/share/fonts/truetype/msttcorefonts/Arial_Italic.ttf /usr/share/fonts/truetype/msttcorefonts/Arial_Bold_Italic.ttf
)

# Git Bash needs Windows paths for Docker volumes and must not rewrite the container paths.
to_host_path() { if command -v cygpath >/dev/null 2>&1; then cygpath -w "$1"; else printf '%s' "$1"; fi; }
export MSYS_NO_PATHCONV=1

mounts=()
for f in "${candidates[@]}"; do
  if [[ -f "$f" ]]; then
    mounts+=(-v "$(to_host_path "$f"):/usr/share/fonts/truetype/arial/$(basename "$f" | tr ' ' '_'):ro")
  fi
done
if [[ ${#mounts[@]} -eq 0 ]]; then
  echo "warning: Arial not found; text will be measured with DejaVu and may look stretched" >&2
fi

docker run --rm -v "$(to_host_path "$repo"):/data" "${mounts[@]}" -w /data \
  plantuml/plantuml -tsvg "docs/architecture/*/*.puml" -o ../out
echo "Rendered to docs/architecture/out/"
