# Manual-assisted smoke check. Run after starting ClipVault.
$ErrorActionPreference = 'Stop'
Add-Type @'
using System;
using System.Runtime.InteropServices;
public static class CvFocusSmoke {
 [DllImport("user32.dll")] public static extern IntPtr GetForegroundWindow();
}
'@
Add-Type -AssemblyName System.Windows.Forms
Write-Host 'Open Notepad, click its text field, then return here to start the countdown.'
Read-Host 'Press Enter, then switch to Notepad within 5 seconds'
Start-Sleep -Seconds 5
$before = [CvFocusSmoke]::GetForegroundWindow()
[System.Windows.Forms.SendKeys]::SendWait('^+v')
Start-Sleep -Milliseconds 500
$after = [CvFocusSmoke]::GetForegroundWindow()
if ($before -ne $after) { throw "FAIL: foreground changed from $before to $after" }
Write-Host 'Foreground unchanged. Visually confirm the panel is actually open; this check alone cannot establish that.'
Write-Host 'Now type into Notepad, navigate the picker, and click search; follow docs/WINDOWS-ACCEPTANCE.md.'
