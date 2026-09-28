# Validation record

Environment: Linux x64 sandbox, Eclipse Temurin JDK 17, Gradle wrapper 8.7.
Date: 2026-09-28.

## Executed

Command: `./gradlew check --no-daemon --console=plain`

Result: **BUILD SUCCESSFUL**.

- SQLDelight schema generated successfully.
- All app and desktop Kotlin sources compiled.
- CoreTest: **3 tests passed**, zero failures/errors.
  - Content-type classification, including sensitive-content suppression classification.
  - 200ms duplicate filter.
  - Exact/fuzzy query and category filtering.
- RepositoryTest: **1 test passed**, zero failures/errors.
  - Temporary real SQLite DB creation and restart.
  - Clip insertion, pin protection from clear, deletion.
  - OCR text, settings, custom keybind and exclusion persistence.
- SQLDelight schema verification task passed (initial v1 schema; no upgrade migrations yet).
- Inspected resolved Tess4J/Lept4J jars: Windows x64 Tesseract and Leptonica DLL resources present.
- Included actual English/Bengali traineddata and upstream license.

## Not executed / not established

- No Windows desktop or Windows focus/hook integration tests were run.
- No installer was built, signed, installed, or upgraded.
- Native OCR DLL loading and image recognition were not tested on Windows.
- No automated visual/pixel comparison to reference images.
- No performance benchmarks, frame-time measurements, memory certification or accessibility audit.
- GitHub Actions workflow and signing script are supplied but not executed.

Compilation plus four unit/integration tests is useful evidence, **not a production acceptance certificate**. Follow `WINDOWS-ACCEPTANCE.md` before release.
