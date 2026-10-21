# Renders every diagram in docs/architecture/*/ to SVG in docs/architecture/out/ (Windows PowerShell).
# Same as render.sh: mounts Arial into the plantuml/plantuml image, which only ships DejaVu fonts.
# Run from anywhere: powershell -File docs\architecture\render.ps1
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

$mounts = @()
foreach ($name in 'arial.ttf', 'arialbd.ttf', 'ariali.ttf', 'arialbi.ttf') {
    $file = Join-Path $env:WINDIR "Fonts\$name"
    if (Test-Path $file) { $mounts += @('-v', "${file}:/usr/share/fonts/truetype/arial/${name}:ro") }
}
if ($mounts.Count -eq 0) { Write-Warning 'Arial not found; text will be measured with DejaVu and may look stretched' }

docker run --rm -v "${repo}:/data" @mounts -w /data plantuml/plantuml -tsvg 'docs/architecture/*/*.puml' -o ../out
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
'Rendered to docs/architecture/out/'
