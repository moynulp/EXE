# ClipVault
### Everything you copied. Forever. Searchable.
**Made by Zunawet · Windows 10/11 · v1.0.0 / Build 001**

A Kotlin/JVM + JetBrains Compose Desktop clipboard manager with a warm cream picker, yellow accents, offline SQLite history, and a tray-first workflow.

> **Delivery status:** buildable source implementation, not a signed or Windows-certified production release. Linux/JDK 17 compilation and JVM tests were run. Windows focus behavior, native hooks, native OCR loading, installer execution, accessibility and performance require the release checks in `docs/WINDOWS-ACCEPTANCE.md`. No Windows `.exe` or `.msi` is included or claimed to have been generated here. See `docs/STATUS.md` for scope differences from the full brief.

## Requirements

For development / packaging:
- Windows 10/11 **x64**, an interactive desktop session.
- **JDK 17 x64**, with `JAVA_HOME` pointing to its folder and `%JAVA_HOME%\bin` on PATH. A JRE alone cannot package installers.
- Internet for the **first build only**, to resolve Gradle and Maven dependencies.
- **WiX Toolset 3.11.x** for Windows `.msi` / `.exe` packaging. Add its `bin` directory to PATH; verify `candle.exe -?` and `light.exe -?` work. WiX 4 alone is not a replacement for JDK 17's WiX 3 integration.
- Microsoft Visual C++ 2015–2022 **x64 Redistributable** for the native Tesseract/Leptonica DLLs if not already installed.

End users do **not** need Kotlin, Gradle, Java, a Tesseract installation, or a network connection. jpackage bundles the Java runtime. English and Bengali language data are included.

## Local development

PowerShell from the extracted project directory:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
.\gradlew.bat check
.\gradlew.bat run
```

With a Unix-style shell: `./gradlew check` and `./gradlew run`. Core tests compile on Linux; the delivered app is Windows-focused and requires a tray-enabled graphical session. Headless execution is not supported.

**Starting successfully shows only the tray icon.** It is not an error that no main window appears. Use `Ctrl+Shift+V`, or right-click the yellow ClipVault tray icon → **Settings…**. Windows may put the icon in the overflow (`^`) tray. Keep the terminal open for `gradlew run`; installed builds do not need a terminal.

## Build a Windows EXE / MSI

Run these **on Windows**; jpackage does not cross-compile Windows installers from Linux:

```powershell
.\gradlew.bat check
.\gradlew.bat :desktop:createDistributable   # runnable app image
.\gradlew.bat packageExe                   # installer .exe
.\gradlew.bat packageMsi                   # installer .msi
```

Expected paths for the configured version:

| Artifact | Path from project root |
|---|---|
| Runnable app launcher | `desktop\build\compose\binaries\main\app\ClipVault\ClipVault.exe` |
| EXE installer | `desktop\build\compose\binaries\main\exe\ClipVault-1.0.0.exe` |
| MSI installer | `desktop\build\compose\binaries\main\msi\ClipVault-1.0.0.msi` |

The app-image launcher needs the **entire adjacent app/runtime directory**, not just the `.exe`. Distribute the installer for normal users. Confirm the exact generated filename with:

```powershell
Get-ChildItem .\desktop\build\compose\binaries -Recurse -Include *.exe,*.msi
```

A Windows GitHub Actions build is included at `.github/workflows/windows.yml`. It produces unsigned installers for testing, not a trusted public release.

## Sign a public release

1. Obtain an Authenticode code-signing certificate from a trusted provider (or configure your hardware/cloud signing provider). Never commit a PFX, private key, password, or token.
2. Install the Windows SDK to obtain `signtool.exe`. Put it on PATH.
3. Import the signing certificate securely into the release account's certificate store or configure its provider. Obtain its SHA-1 thumbprint (this selects the certificate; file signing uses SHA-256).
4. Build the app image. Sign the launcher, then package from that same signed image. The supplied script invokes jpackage with `--app-image` so the signed launcher is not regenerated.

```powershell
.\gradlew.bat :desktop:createDistributable
$env:CLIPVAULT_CERT_THUMBPRINT = 'YOUR_CERTIFICATE_THUMBPRINT'
.\scripts\sign-and-package.ps1
```

5. Verify the signatures and certificate chain on a clean machine:

```powershell
signtool verify /pa /all .\release\ClipVault-1.0.0.exe
signtool verify /pa /all .\release\ClipVault-1.0.0.msi
```

Use your CA's approved RFC3161 timestamp endpoint if different from the script default. SmartScreen reputation is separate from signature validity; a new signed app may still show reputation warnings. Do not instruct users to bypass security warnings from an untrusted download.

## End-user installation

1. Obtain the signed `ClipVault-1.0.0.exe` (or `.msi`) from the publisher you trust.
2. Check **Properties → Digital Signatures** and verify the signer. Unsigned CI artifacts are only for controlled testing.
3. Close any older ClipVault process via tray → **Quit**.
4. Run the installer, choose a destination if prompted, and finish installation. It is configured for a per-user install.
5. Launch **ClipVault** from Start or the desktop shortcut. Find the yellow tray icon, including in the `^` overflow.
6. Copy some text. In Notepad, press **Ctrl+Shift+V**. The panel should appear while Notepad retains focus. Click a row to paste it.
7. Right-click the tray icon → **Settings…** to change shortcuts, theme, exclusions and capture feedback.
8. To start automatically at sign-in, press `Win+R`, enter `shell:startup`, and copy a shortcut to the installed `ClipVault.exe` into that folder. The app does **not** modify startup registration automatically.
9. Uninstall through **Settings → Apps → Installed apps → ClipVault**. User history is retained. To erase it, quit ClipVault and delete `%LOCALAPPDATA%\ClipVault`.

## Default shortcuts

| Action | Shortcut |
|---|---|
| Main picker | Ctrl+Shift+V |
| Compact picker | Alt+V |
| Pause/resume capture | Ctrl+Shift+P |
| Paste latest clip as plain text | Ctrl+Shift+T |
| Paste first / second / third most recent | Ctrl+Shift+1 / 2 / 3 |

Some defaults overlap with application shortcuts. JNativeHook observes global hotkeys; it does **not suppress their delivery to the foreground app**. Windows RegisterHotKey is a fallback backend. Navigation is handled separately by a Win32 low-level keyboard hook so arrows, Enter and Esc can be consumed while the picker remains non-activating. Ordinary typing passes through.

Clicking the search box explicitly activates the picker. `/` is a visual search hint only in this release: it is **not intercepted while the background app is focused**, because doing so would violate the stronger requirement that ordinary typing continues into that app.

Keybind recording suspends all ClipVault actions, captures modifier + letter/number/F1–F12, and supports Esc and a 10-second timeout. Known Windows/browser/editor combinations are conservatively rejected, and conflicts with another ClipVault binding are rejected. This is not an exhaustive discovery of other apps' hotkeys. Windows secure-attention combinations cannot be captured or overridden.

## No-focus-steal design

- `PanelWindow` embeds `ComposePanel` in an undecorated transparent **JWindow**.
- Before showing: `focusableWindowState=false`, `WS_EX_NOACTIVATE`, `WS_EX_TOOLWINDOW`, and topmost `SetWindowPos` with **SWP_NOACTIVATE**.
- The original foreground HWND is saved before opening.
- Only an explicit pointer press in search clears NOACTIVATE and enables focus.
- JWindow focus requires a showing Frame owner. A transparent 1×1 off-screen non-activating tool Frame provides that owner without a taskbar entry. The default hidden shared owner would prevent the search field from activating.
- Outside clicks dismiss without restoring the old target; the user's newly chosen app keeps focus.
- Paste restores the saved target, **verifies** it is foreground, waits for invocation modifiers to be released, writes typed clipboard data, and sends Robot Ctrl+V. If foreground restoration is denied, paste fails closed instead of typing into an unintended window.
- Elevated targets, secure desktops, RDP, full-screen apps, IMEs and mixed-DPI monitors need explicit testing. Running ClipVault as administrator is not recommended as a blanket workaround.

## Data and privacy

Everything remains local in `%LOCALAPPDATA%\ClipVault`:

```text
clipvault.db       SQLite history, preferences, keybinds, exclusions
clipvault.db-wal   SQLite write-ahead log while in use
images/           PNG captures and thumbnails
tessdata/         Offline language data extracted from resources
clipvault.log      Diagnostic errors (not clipboard contents)
instance.lock     Single-instance file lock
show.request      File-based request to show an existing instance
```

- Clipboard changes use a FlavorListener **plus polling**, since FlavorListener does not report same-format text changes. On Windows, `GetClipboardSequenceNumber` prevents repeated image decoding when unchanged.
- Text, image and file-list transfers are captured. Type detection includes text, code, links, emails, colors, phone numbers and addresses. The clipboard's HTML/RTF formats are not preserved; text pastes are plain text.
- Suspected passwords, API secrets and Luhn-valid card numbers are **skipped**, not put in plaintext behind a cosmetic PIN. Classification is best-effort. Exclude password managers and sensitive apps, e.g. `1Password.exe`, `KeePass.exe`, `Bitwarden.exe`.
- History is **not encrypted at rest**. Use Windows account protections and BitLocker. A real PIN-protected encrypted vault is not implemented.
- Notifications and capture sounds are off by default. Notifications can expose clip previews when enabled.
- Pinned clips survive “Clear unpinned history”. Deleting an image removes its PNG and thumbnail. Clearing does not securely erase old SQLite/WAL pages; use full-disk encryption.
- No network listener, analytics, cloud requests, or update checker runs. Dependencies and OCR data downloads occur at build time only.
- There is no automatic retention limit; the initial cache loads all history. Very large histories need a future paged DAO/index before performance targets can be guaranteed.

## OCR bundling

`app/src/main/resources/tessdata/` contains real English and Bengali **tessdata_fast** language files and the upstream Apache-2.0 license. Tess4J 5.8.0 packages `libtesseract532.dll`; its Lept4J dependency packages `libleptonica1831.dll`, including x64 variants. JNA extracts native resources from dependency JARs. Keep these dependencies in the packaged application; do not replace them with incompatible DLLs on PATH.

On first image OCR, trained data is extracted to the app data folder. OCR uses `Dispatchers.IO` and a mutex to avoid simultaneous native OCR work. Results are cached in SQLite and become searchable. Native failures are logged and do not block clipboard capture. On Linux, running OCR requires system Tesseract/Leptonica libraries; that is outside the Windows installer path.

## Architecture

```text
app/      Domain models/search, data repositories, SQLDelight schema,
          clipboard capture, content detection, OCR, optional file watcher
 desktop/ Compose UI, Koin composition root, native Windows adapters,
          application lifecycle, tray, focus/paste/hotkey orchestration
```

Sources use `com.zunawet.clipvault`. Classes are grouped by responsibility rather than generating empty one-class facades for every filename in the brief. `ClipVaultDatabase` is generated by SQLDelight; do not hand-edit generated build files. `VaultRepository` owns clips, preferences, keybinds and exclusions; `PanelViewModel` exposes debounced search and selection as flows. DB/file/OCR work is dispatched off the UI thread.

The maintained JNativeHook coordinate is **`com.github.kwhat:jnativehook:2.2.2`**, with `com.github.kwhat.jnativehook` imports, rather than the obsolete requested 2.1.0 coordinate. Koin uses its runtime libraries, not a nonexistent required `io.insert-koin.koin` Gradle plugin.

## Tests and troubleshooting

```powershell
.\gradlew.bat check
.\scripts\focus-smoke.ps1   # on Windows, after launching ClipVault
```

See `docs/VALIDATION.md` for the executed checks, and `docs/WINDOWS-ACCEPTANCE.md` for the full release matrix.

- **No visible window:** expected; inspect the tray overflow.
- **No tray icon:** inspect `%LOCALAPPDATA%\ClipVault\clipvault.log`; ensure an interactive desktop, not a service or headless session.
- **Hotkey ignored:** check Settings → Keybinds backend, another app's shortcut, security software and whether recording is active.
- **Paste failed:** the original target may have closed, become elevated, or denied foreground activation. The app deliberately refuses unsafe injection.
- **OCR failure:** check x64 Visual C++ runtime, antivirus DLL extraction restrictions, writable app-data/temp directories, and the log.
- **Installer task missing/fails on Linux:** use Windows x64. `.exe` and `.msi` packaging is host-specific.

**Made by Zunawet.**
