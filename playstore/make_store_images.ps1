# Builds the Google Play listing images from the captures in playstore\raw.
#   - phone screenshots: 1080x1920 (9:16, inside Play's 2:1 side-ratio limit), max 8
#   - feature graphic:   1024x500
# Usage: powershell -ExecutionPolicy Bypass -File playstore\make_store_images.ps1
Add-Type -AssemblyName System.Drawing

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$raw = Join-Path $root 'raw'
$out = Join-Path $root 'screenshots'
New-Item -ItemType Directory -Force $out | Out-Null

# Eskooltech logo palette: charcoal lettering + lime green
$charcoal = [System.Drawing.Color]::FromArgb(30, 34, 32)
$greenDeep = [System.Drawing.Color]::FromArgb(62, 107, 22)
$lime = [System.Drawing.Color]::FromArgb(140, 198, 63)
$white = [System.Drawing.Color]::White
$muted = [System.Drawing.Color]::FromArgb(214, 232, 196)
$bezelColor = [System.Drawing.Color]::FromArgb(17, 19, 18)

function New-RoundedPath([float]$x, [float]$y, [float]$w, [float]$h, [float]$r) {
    $p = New-Object System.Drawing.Drawing2D.GraphicsPath
    $d = 2 * $r
    $p.AddArc($x, $y, $d, $d, 180, 90)
    $p.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
    $p.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
    $p.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
    $p.CloseFigure()
    return $p
}

# Pixelates a rectangle (student photo / name).
function Hide-Region([System.Drawing.Bitmap]$bmp, [int]$x, [int]$y, [int]$w, [int]$h) {
    $block = 22
    $small = New-Object System.Drawing.Bitmap([int][math]::Ceiling($w / $block), [int][math]::Ceiling($h / $block))
    $gs = [System.Drawing.Graphics]::FromImage($small)
    $gs.InterpolationMode = 'HighQualityBilinear'
    $gs.DrawImage($bmp, (New-Object System.Drawing.Rectangle(0, 0, $small.Width, $small.Height)), (New-Object System.Drawing.Rectangle($x, $y, $w, $h)), 'Pixel')
    $gs.Dispose()
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = 'NearestNeighbor'
    $g.PixelOffsetMode = 'Half'
    $g.DrawImage($small, (New-Object System.Drawing.Rectangle($x, $y, $w, $h)))
    $g.Dispose(); $small.Dispose()
}

# Crops off the status bar (personal notification icons) and the gesture bar.
function Get-Cropped([string]$file, [int]$top, [int]$bottom, $hide) {
    $src = [System.Drawing.Bitmap]::FromFile($file)
    $full = New-Object System.Drawing.Bitmap($src.Width, $src.Height)
    $g0 = [System.Drawing.Graphics]::FromImage($full); $g0.DrawImage($src, 0, 0, $src.Width, $src.Height); $g0.Dispose(); $src.Dispose()
    foreach ($r in $hide) { Hide-Region $full $r[0] $r[1] $r[2] $r[3] }
    $h = $full.Height - $top - $bottom
    $crop = $full.Clone((New-Object System.Drawing.Rectangle(0, $top, $full.Width, $h)), $full.PixelFormat)
    $full.Dispose()
    return $crop
}

function Fill-Gradient([System.Drawing.Graphics]$g, [int]$w, [int]$h) {
    $rect = New-Object System.Drawing.Rectangle(0, 0, $w, $h)
    $brush = New-Object System.Drawing.Drawing2D.LinearGradientBrush($rect, $charcoal, $greenDeep, 60)
    $g.FillRectangle($brush, $rect)
    $brush.Dispose()
    # soft lime glow, top-right
    $glow = New-Object System.Drawing.Drawing2D.GraphicsPath
    $glow.AddEllipse([float]($w * 0.55), [float](-$w * 0.35), [float]($w * 0.9), [float]($w * 0.9))
    $pb = New-Object System.Drawing.Drawing2D.PathGradientBrush($glow)
    $pb.CenterColor = [System.Drawing.Color]::FromArgb(70, $lime)
    $pb.SurroundColors = @([System.Drawing.Color]::FromArgb(0, $lime))
    $g.FillPath($pb, $glow)
    $pb.Dispose(); $glow.Dispose()
}

function Draw-Centered([System.Drawing.Graphics]$g, [string]$text, [System.Drawing.Font]$font, [System.Drawing.Color]$color, [float]$y, [int]$w) {
    $fmt = New-Object System.Drawing.StringFormat
    $fmt.Alignment = 'Center'
    $brush = New-Object System.Drawing.SolidBrush($color)
    $g.DrawString($text, $font, $brush, [System.Drawing.RectangleF]::new(60, $y, ($w - 120), 400), $fmt)
    $brush.Dispose()
}

# top/bottom = status bar / gesture bar height of the device the capture came from
# (emulator 1080x2400: 132/66, phone 1080x2414: 121/62).
$shots = @(
    @{ src = '1_splash.png';        top = 132; bottom = 66; title = 'Eskooltech';                     sub = 'Stay updated about your school' },
    @{ src = '2_roles.png';         top = 132; bottom = 66; title = 'One app for the whole school';   sub = 'Separate sign-in for parents, teachers and admins' },
    @{ src = '3_login.png';         top = 132; bottom = 66; title = 'Simple, secure sign in';         sub = 'Use the login details given by your school' },
    @{ src = '10_home.png';         top = 121; bottom = 62; title = 'Everything in one place';        sub = 'Attendance, homework, fees, results and more' },
    @{ src = '11_menu.png';         top = 121; bottom = 62; title = 'Every feature, one tap away';    sub = 'Quick menu, and switch between your children'
       hide = @(,@(30, 125, 560, 285)) },                                                              # student photo + name
    @{ src = '21_attendance.png';   top = 121; bottom = 62; title = 'Track daily attendance';         sub = 'Month-wise view of present, absent and leave' },
    @{ src = '24_fee.png';          top = 121; bottom = 62; title = 'Fees at a glance';               sub = 'Total, paid and balance, with receipts' },
    @{ src = '22_notification.png'; top = 121; bottom = 62; title = 'Never miss a notice';            sub = 'School announcements straight to your phone' }
)

$W = 1080; $H = 1920
$titleFont = New-Object System.Drawing.Font('Segoe UI Semibold', 64, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
$subFont = New-Object System.Drawing.Font('Segoe UI', 34, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)

$n = 0
foreach ($s in $shots) {
    $file = Join-Path $raw $s.src
    if (-not (Test-Path $file)) { Write-Host "skip $($s.src) (missing)"; continue }
    $crop = Get-Cropped $file $s.top $s.bottom $s.hide
    $n++

    $bmp = New-Object System.Drawing.Bitmap($W, $H)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = 'AntiAlias'; $g.InterpolationMode = 'HighQualityBicubic'; $g.TextRenderingHint = 'AntiAliasGridFit'
    Fill-Gradient $g $W $H

    $accent = New-Object System.Drawing.SolidBrush($lime)
    $g.FillRectangle($accent, ($W / 2 - 40), 96, 80, 8)
    $accent.Dispose()
    Draw-Centered $g $s.title $titleFont $white 130 $W
    Draw-Centered $g $s.sub $subFont $muted 236 $W

    # Phone frame: screen 720 wide, bottom runs off the image
    $pw = 720; $ph = [int]($crop.Height * $pw / $crop.Width)
    $px = [int](($W - $pw) / 2); $py = 370
    $bezel = 18
    $frame = New-RoundedPath ($px - $bezel) ($py - $bezel) ($pw + 2 * $bezel) ($ph + 2 * $bezel) 64
    $frameBrush = New-Object System.Drawing.SolidBrush($bezelColor)
    $g.FillPath($frameBrush, $frame)
    $frameBrush.Dispose()
    $g.SetClip((New-RoundedPath $px $py $pw $ph 48))
    $g.DrawImage($crop, $px, $py, $pw, $ph)
    $g.ResetClip()

    $g.Dispose()
    $name = ($s.src -replace '^\d+_', '')
    $dest = Join-Path $out ("{0}_{1}" -f $n, $name)
    $bmp.Save($dest, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose(); $crop.Dispose()
    Write-Host "saved $dest"
}

# Feature graphic 1024x500: logo on a white card (its charcoal letters need a light background)
$fw = 1024; $fh = 500
$fg = New-Object System.Drawing.Bitmap($fw, $fh)
$g = [System.Drawing.Graphics]::FromImage($fg)
$g.SmoothingMode = 'AntiAlias'; $g.InterpolationMode = 'HighQualityBicubic'; $g.TextRenderingHint = 'AntiAliasGridFit'
Fill-Gradient $g $fw $fh
$card = New-RoundedPath 48 90 400 320 36
$cb = New-Object System.Drawing.SolidBrush($white); $g.FillPath($cb, $card); $cb.Dispose()
$logo = [System.Drawing.Bitmap]::FromFile((Join-Path (Split-Path -Parent $root) 'app\src\main\res\mipmap-hdpi\school_logo.png'))
$lw = 330; $lh = [int]($logo.Height * $lw / $logo.Width)
$g.DrawImage($logo, 83, [int](250 - $lh / 2), $lw, $lh)
$logo.Dispose()
$big = New-Object System.Drawing.Font('Segoe UI Semibold', 50, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
$small = New-Object System.Drawing.Font('Segoe UI', 26, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
$wb = New-Object System.Drawing.SolidBrush($white); $mb = New-Object System.Drawing.SolidBrush($muted); $lb = New-Object System.Drawing.SolidBrush($lime)
$g.FillRectangle($lb, 500, 150, 64, 6)
$g.DrawString('Your school,', $big, $wb, 494, 170)
$g.DrawString('in your pocket', $big, $wb, 494, 232)
$g.DrawString('Attendance, fees, homework and notices', $small, $mb, 498, 322)
$g.DrawString('for parents, teachers and admins.', $small, $mb, 498, 358)
$g.Dispose()
$fgPath = Join-Path $out 'feature_graphic_1024x500.png'
$fg.Save($fgPath, [System.Drawing.Imaging.ImageFormat]::Png)
$fg.Dispose()
Write-Host "saved $fgPath"
