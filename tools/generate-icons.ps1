[CmdletBinding()]
param(
    [string]$Source = (Join-Path $PSScriptRoot "..\design\ic_launcher_source.jpg"),
    [string]$ResDir = (Join-Path $PSScriptRoot "..\app\src\main\res"),
    [string]$Background = "#F7F1EA",
    [string]$PreviewDir = ""
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

function Convert-HexColor {
    param([string]$Hex)
    $hexValue = $Hex.TrimStart('#')
    return [System.Drawing.Color]::FromArgb(
        [Convert]::ToInt32($hexValue.Substring(0, 2), 16),
        [Convert]::ToInt32($hexValue.Substring(2, 2), 16),
        [Convert]::ToInt32($hexValue.Substring(4, 2), 16)
    )
}

function Save-SquareIcon {
    param(
        [System.Drawing.Image]$Image,
        [int]$Size,
        [string]$Path,
        [bool]$Round,
        [bool]$Rounded,
        [System.Drawing.Color]$Fill
    )

    $bitmap = New-Object System.Drawing.Bitmap(
        $Size,
        $Size,
        [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
    )
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)

    try {
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
        $graphics.Clear($Fill)

        if ($Round) {
            $clip = New-Object System.Drawing.Drawing2D.GraphicsPath
            $clip.AddEllipse(0, 0, $Size, $Size)
            $graphics.SetClip($clip)
            $clip.Dispose()
        }
        elseif ($Rounded) {
            $radius = [float]($Size * 0.22)
            $diameter = $radius * 2
            $clip = New-Object System.Drawing.Drawing2D.GraphicsPath
            $clip.AddArc(0, 0, $diameter, $diameter, 180, 90)
            $clip.AddArc($Size - $diameter, 0, $diameter, $diameter, 270, 90)
            $clip.AddArc($Size - $diameter, $Size - $diameter, $diameter, $diameter, 0, 90)
            $clip.AddArc(0, $Size - $diameter, $diameter, $diameter, 90, 90)
            $clip.CloseFigure()
            $graphics.SetClip($clip)
            $clip.Dispose()
        }

        $side = [Math]::Min($Image.Width, $Image.Height)
        $sourceX = [int](($Image.Width - $side) / 2)
        $sourceY = [int](($Image.Height - $side) / 2)
        $sourceRect = New-Object System.Drawing.Rectangle($sourceX, $sourceY, $side, $side)
        $destinationRect = New-Object System.Drawing.Rectangle(0, 0, $Size, $Size)
        $graphics.DrawImage(
            $Image,
            $destinationRect,
            $sourceRect,
            [System.Drawing.GraphicsUnit]::Pixel
        )
    }
    finally {
        $graphics.Dispose()
    }

    $directory = Split-Path -Parent $Path
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

$resolvedSource = (Resolve-Path -LiteralPath $Source).Path
$image = [System.Drawing.Image]::FromFile($resolvedSource)
$backgroundColor = Convert-HexColor $Background

try {
    $densities = @(
        @{ Folder = "mipmap-mdpi"; Size = 48 },
        @{ Folder = "mipmap-hdpi"; Size = 72 },
        @{ Folder = "mipmap-xhdpi"; Size = 96 },
        @{ Folder = "mipmap-xxhdpi"; Size = 144 },
        @{ Folder = "mipmap-xxxhdpi"; Size = 192 }
    )

    foreach ($density in $densities) {
        $folder = Join-Path $ResDir $density.Folder
        Save-SquareIcon -Image $image -Size $density.Size `
            -Path (Join-Path $folder "ic_launcher.png") -Round $false -Rounded $true `
            -Fill ([System.Drawing.Color]::Transparent)
        Save-SquareIcon -Image $image -Size $density.Size `
            -Path (Join-Path $folder "ic_launcher_round.png") -Round $true -Rounded $false `
            -Fill ([System.Drawing.Color]::Transparent)
    }

    Save-SquareIcon -Image $image -Size 432 `
        -Path (Join-Path $ResDir "drawable-nodpi\ic_launcher_foreground.png") `
        -Round $false -Rounded $false -Fill $backgroundColor

    if ($PreviewDir -ne "") {
        New-Item -ItemType Directory -Force -Path $PreviewDir | Out-Null
        $preview = New-Object System.Drawing.Bitmap(
            432,
            432,
            [System.Drawing.Imaging.PixelFormat]::Format32bppArgb
        )
        $previewGraphics = [System.Drawing.Graphics]::FromImage($preview)
        try {
            $previewGraphics.Clear([System.Drawing.Color]::FromArgb(255, 221, 221, 221))
            $previewGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $fullRect = New-Object System.Drawing.Rectangle(0, 0, 432, 432)
            $center = New-Object System.Drawing.Rectangle(72, 72, 288, 288)
            $clip = New-Object System.Drawing.Drawing2D.GraphicsPath
            $clip.AddEllipse($center)
            $previewGraphics.SetClip($clip)
            $previewGraphics.DrawImage($image, $fullRect, 0, 0, $image.Width, $image.Height, [System.Drawing.GraphicsUnit]::Pixel)
        }
        finally {
            $previewGraphics.Dispose()
        }
        $preview.Save((Join-Path $PreviewDir "icon-preview-circle.png"), [System.Drawing.Imaging.ImageFormat]::Png)
        $preview.Dispose()
    }
}
finally {
    $image.Dispose()
}

Write-Output "Icons generated in $ResDir"
