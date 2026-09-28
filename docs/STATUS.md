# Scope and release status

This is a working source implementation, not a claim that every production requirement is complete.

## Implemented
- Kotlin 1.9.23, Compose Desktop 1.6.11, JDK 17, Koin, coroutines/Flow, SQLDelight SQLite.
- Silent tray startup, file-lock single instance and file-based activation request.
- Cream/yellow main picker and compact overlay; dark theme; settings navigation and functional preferences.
- Non-activating JWindow/ComposePanel, pre-show Win32 styles, explicit-click search activation, foreground-verified paste.
- JNativeHook globals, RegisterHotKey fallback, Win32 navigation interception, outside-click dismissal.
- Binding recording lockdown, modifier validation, known conflict checks, reset, timeout and settings-close cancellation.
- Text/image/file-list history, exclusions, pause, type detection, deduplication, pin/delete, clear confirmation.
- Debounced 50ms search, edit-distance-one fuzzy matching, substring highlights, category filters and 50-row batches.
- Real English/Bengali trained data, Tess4J/Lept4J native resource dependencies, background OCR and thumbnail generation.
- Appear/disappear transforms, hover color, recorder pulse/error shake, animated reorder, native reduced-motion popup preference.
- Tray assets in PNG/ICO; EXE/MSI Gradle configuration; signing script and Windows CI workflow.

## Deliberate differences / incomplete items
1. **Production validation:** native Windows execution and packaging have not been performed in the delivery sandbox. No certified performance numbers or 60/120 FPS guarantee.
2. **Native notifications:** AWT SystemTray shell notification support is wired. There is no dedicated JNA/WinRT toast COM registration, AppUserModelID, activation handler or Action Center persistence implementation.
3. **Sensitive content:** skipped, not masked/persisted behind a PIN. Encrypted PIN vault, recovery and unlock flows are not implemented.
4. **Cloud sync and Squirrel auto-updating:** absent; Cloud Sync is explicitly labeled planned v1.1. No inactive toggle pretending to enable them.
5. **Screenshot folder watching:** reusable WatchService adapter exists; automatic folder selection/import wiring is not exposed in the UI.
6. **Accessibility:** basic semantic descriptions and minimum 44dp action targets are present. Full Narrator audit, Windows high-contrast integration and 200% text reflow certification are outstanding.
7. **Animation fidelity:** not all micro-animations from the brief are present (e.g. sliding tab underline, green click flash, pin rotation, precise toggle spring and backdrop blur). Theme/reference styling is implemented, not asserted pixel-exact.
8. **Search hint:** slash is not captured while the popup lacks focus; normal typing must continue to the underlying app. Search is explicitly activated by clicking.
9. **Memory and search scaling:** history is held in an in-memory list, and insertions refresh it. Search is off-thread but is not an FTS-indexed large-history implementation; the <16ms, <150MB and <2s targets are unverified. LazyColumn batches visible entries, but previously loaded rows remain available.
10. **Clipboard richness:** HTML/RTF and arbitrary custom clipboard formats are not preserved. Plain-text hotkey pastes the newest saved clip (or OCR text for an image), not arbitrary current HTML clipboard content.
11. **Fonts/platforms:** Segoe UI is requested from the OS, with system fallback. Inter/Cascadia font binaries are not bundled. Windows ARM64 is not a supported packaging target.
12. **Error UX:** OCR failures are logged and the progress indicator stops, but a dedicated per-row retry/error menu is not implemented.
13. **Persistence upgrades:** schema v1 is included; future versions need explicit SQLDelight migrations and upgrade tests before changing the schema.

Do not publish the application as production-ready until the Windows acceptance matrix is signed off and required gaps are closed.
