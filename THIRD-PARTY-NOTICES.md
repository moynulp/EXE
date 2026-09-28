# Third-party notices

Made by Zunawet. Application branding is original to this project. Segoe UI is requested as an installed Windows system font, not redistributed.

Core dependencies (review complete transitive license requirements before public distribution):
- Kotlin, kotlinx.coroutines, kotlinx.serialization — Apache-2.0
- JetBrains Compose Multiplatform — Apache-2.0; includes Skia and other native components with their own notices
- Koin — Apache-2.0
- SQLDelight — Apache-2.0
- SQLite — public domain; JDBC driver includes its own licensing/notices
- JNativeHook — LGPL-2.1 (retain notices and comply with dynamic-linking/replacement requirements)
- JNA — LGPL-2.1 or Apache-2.0
- Thumbnailator — MIT
- Tess4J, Lept4J, Tesseract — Apache-2.0; Leptonica and codec dependencies carry their own notices
- tessdata_fast English/Bengali models — Apache-2.0; full upstream license included beside the files
- Gradle wrapper — Apache-2.0

OCR data was fetched from:
https://github.com/tesseract-ocr/tessdata_fast/blob/main/eng.traineddata
https://github.com/tesseract-ocr/tessdata_fast/blob/main/ben.traineddata

The built dependency JARs retain upstream embedded license resources. Do not strip these during packaging. This list is an engineering inventory, not a completed legal redistribution audit.
