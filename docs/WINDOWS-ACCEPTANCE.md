# Windows release acceptance

Run on a **clean Windows 10 x64 and Windows 11 x64 VM**, as a standard user. Repeat critical items on physical hardware. Record OS build, JDK build, display scaling, monitor layout, app commit, measured values and pass/fail. None of the native items below are pre-certified by this source delivery.

## Focus: blocking release gate
- Launch installed app: only tray icon visible, no taskbar window, no foreground change.
- Focus Notepad and start typing; Ctrl+Shift+V opens picker. Continue typing without clicking: Notepad receives every character and caret continues blinking.
- Repeat for Alt+V; opening never activates the popup.
- Inspect extended styles: NOACTIVATE (0x08000000), TOOLWINDOW (0x80), TOPMOST (0x8); no taskbar/Alt-Tab picker entry.
- While unfocused, Up/Down changes picker selection without moving the Notepad caret. Enter pastes exactly once and Esc dismisses without inserting a newline.
- Click search: only then does keyboard focus move to the popup. Enter pastes into the saved target, not the picker.
- Click outside into another app: hide without pulling focus back to the previous target.
- Close the original target while searching: paste refuses injection into another window.
- Hold Ctrl/Shift after invoking: no accidental Ctrl+Shift+V/Alt+Ctrl+V is injected. Release or timeout safely.
- Repeat on Windows Terminal, VS Code, Chrome contenteditable, Word, Notepad, and an elevated editor. Elevated-target failure should be safe and visible.
- Repeat across 100/125/150/200% DPI, mixed monitors, taskbar edges and RDP. Panel remains within usable monitor bounds.
- Repeat during IME composition (Bengali), a full-screen app, and after lock/unlock and sleep/resume.

## Hotkeys and recorder
- Each default works with settings closed and app tray-only.
- Force JNativeHook registration failure in a controlled build; verify RegisterHotKey fallback reports conflicts and works.
- Record Ctrl+Shift+V: panel must NOT open while recording. New shortcut should persist across restart.
- Single key: error/shake, recording remains active. Esc, timeout and settings close cancel; normal hotkeys resume.
- Duplicate ClipVault binding and reserved browser/Windows combos are rejected.
- Double Ctrl enabled/disabled; Ctrl+C then Ctrl does not falsely trigger it.
- No stuck modifier states after recording, cancelling or pasting.

## Capture/storage/security
- Same-flavor text changes captured; identical clipboard polling produces no repeat entries.
- Identical text recopied after 200ms behaves as expected on Windows sequence changes.
- Image capture writes PNG + thumbnail, OCR progress completes, OCR text is searchable after restart.
- English and Bengali OCR work offline, with no system Tesseract installed.
- File lists paste as file lists in Explorer. Missing image/file targets fail safely.
- Pause/exclusions skip capture; resuming does not import skipped content.
- Password/API-key keywords and known Luhn-valid card numbers are not stored. Test false positives/negatives; document best-effort classification.
- Pin/delete/clear; pinned entries survive clear. Deleted image files and thumbnails removed.
- Tray clear requires confirmation. Data survives app restart and installer upgrade.
- Second launch signals the existing process, not a duplicate capture loop.
- Locked clipboard and OCR DLL failures do not terminate the capture loop.

## UX/accessibility/performance
- All settings persist. Dark/light, default reset, sound and notifications behave honestly.
- Test Narrator, keyboard-only navigation, contrast, 200% scaling and long clip content.
- Reduced-motion Windows preference removes popup scale/slide.
- Measure cold start using ETW/WPA; record memory RSS/private bytes at 1000 representative clips, including images.
- Measure hotkey-to-first-frame latency separately from the 320ms entrance animation.
- Measure search CPU and end-to-end latency separately: deliberate 50ms debounce is not included in the requested <16ms filtering budget.
- Measure frame times at 60Hz and 120Hz under capture/OCR load; no “zero jank” claim without traces.
- Exercise 10,000+ clips before removing the large-history release limitation.

## Packaging
- Build signed EXE and MSI on Windows using JDK 17 + WiX 3.
- Clean VM without Java: install, start, capture, OCR, restart and uninstall.
- Validate bundled JDK modules/native DLL loading, Authenticode signatures, per-user paths and Start/desktop shortcuts.
- Test upgrade using the stable configured upgrade UUID; do not change it across releases.
- Confirm no credentials, raw clipboard logs or test history in the release bundle.
- Native DLL licenses / transitive dependency redistribution review before public distribution.
