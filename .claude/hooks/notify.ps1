param(
    [Parameter(Position=0)]
    [ValidateSet('sound', 'toast', 'both', 'permission')]
    [string]$Type = 'sound',
    [Parameter(Position=1)]
    [string]$Message = '',
    [Parameter(Position=2)]
    [string]$Title = 'Claude Code'
)

$MutedFile = Join-Path $PSScriptRoot '.muted'
function Is-Muted { Test-Path $MutedFile }

function Play-Sound {
    param([string]$Tone = 'notify')
    if (Is-Muted) { return }
    switch ($Tone) {
        'success'  { [System.Console]::Beep(800, 150); Start-Sleep -Milliseconds 100; [System.Console]::Beep(1000, 200) }
        'error'    { [System.Console]::Beep(300, 300); Start-Sleep -Milliseconds 100; [System.Console]::Beep(200, 400) }
        'notify'   { [System.Console]::Beep(880, 150) }
        'complete' { [System.Console]::Beep(523, 100); Start-Sleep -Milliseconds 80; [System.Console]::Beep(659, 100); Start-Sleep -Milliseconds 80; [System.Console]::Beep(784, 200) }
        'warning'  { [System.Console]::Beep(440, 200); Start-Sleep -Milliseconds 100; [System.Console]::Beep(440, 200) }
    }
}

function Show-Toast {
    param([string]$Msg, [string]$Ttl)
    if (!$Msg) { return }
    try {
        Add-Type -AssemblyName System.Windows.Forms
        $notify = New-Object System.Windows.Forms.NotifyIcon
        $notify.Icon = [System.Drawing.Icon]::ExtractAssociatedIcon("$env:ComSpec")
        $notify.BalloonTipTitle = $Ttl
        $notify.BalloonTipText = $Msg
        $notify.Visible = $true
        $notify.ShowBalloonTip(5000)
        Start-Sleep -Seconds 5
        $notify.Dispose()
    } catch {
        try { msg * "$Ttl`: $Msg" 2>$null } catch {}
    }
}

switch ($Type) {
    'sound'      { Play-Sound -Tone $Message; break }
    'toast'      { Show-Toast -Msg $Message -Ttl $Title; break }
    'permission' { Play-Sound -Tone 'warning'; Show-Toast -Msg "需要权限: $Message" -Ttl '⚠️ Claude Code'; break }
    'both'       { Play-Sound -Tone 'complete'; Show-Toast -Msg $Message -Ttl $Title; break }
}
