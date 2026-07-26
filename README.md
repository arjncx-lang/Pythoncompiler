# PyPhone

**Run Python 3.13 on your phone** — a native Android code editor with an embedded CPython runtime.

Write Python in a real editor (syntax highlighting, themes, undo/redo), hit Run, and see output instantly. `input()` works interactively. Stop long-running scripts anytime.

| | |
|---|---|
| **App name** | PyPhone |
| **Package** | `com.oneandonly.pythoncompiler` |
| **Python** | 3.13 (via [Chaquopy](https://chaquo.com/chaquopy/)) |
| **Min Android** | API 24 (Android 7.0) |
| **ABIs** | `arm64-v8a`, `x86_64` |
| **UI** | Jetpack Compose + [Sora Editor](https://github.com/Rosemoe/sora-editor) |

---

## Features

- **Embedded CPython 3.13** — real interpreter on-device, not a remote sandbox
- **Native code editor** — Sora Editor with TextMate Python grammar and JetBrains Mono
- **Themes** — VS Code Dark, VS Code Light, and Funky (rose accent)
- **Run / Stop** — execute scripts; cooperative interrupt for tight loops
- **Interactive `input()`** — prompt dialog when the program asks for input
- **Stdout / stderr** — colored output panel with copy support
- **Autosave** — buffer saved on edit (debounced) and when the app pauses
- **Symbol bar** — quick keys for `:`, `()`, `[]`, operators, and more
- **Editor toggles** — word wrap, line numbers, show whitespace

---

## Screenshots / usage

1. Open the app — sample Fibonacci / squares code loads on first run  
2. Edit code in the main editor  
3. Tap **Play** to run  
4. Expand the **output** panel to read results or errors  
5. Use the overflow menu for New / Clear / Copy / Paste, theme, and view options  

---

## Architecture

```
┌─────────────────────────────────────────────┐
│  MainActivity (Compose UI + Sora Editor)    │
│  themes · symbol bar · autosave · output    │
└──────────────────┬──────────────────────────┘
                   │ run / stop / input
┌──────────────────▼──────────────────────────┐
│  EditorViewModel                            │
│  background executor · StateFlow output     │
└──────────────────┬──────────────────────────┘
                   │ Chaquopy Python bridge
┌──────────────────▼──────────────────────────┐
│  pyrunner.py  (run_code)                    │
│  compile/exec · stdout/stderr tee · input() │
│  sys.settrace cooperative KeyboardInterrupt │
└─────────────────────────────────────────────┘
```

| Component | Role |
|-----------|------|
| `PyApp` | Starts the Chaquopy/CPython runtime once per process |
| `MainActivity` | UI chrome, TextMate themes, Sora editor, autosave |
| `EditorViewModel` | Run/stop pipeline, output segments, `input()` queue |
| `pyrunner.py` | Executes user code as `__main__` with I/O callbacks |

---

## Build

### Requirements

- Android Studio (or JDK 17+ with Android SDK)
- Python 3.13 installed locally (Chaquopy `buildPython` needs it at configure time)
- Default path in `app/build.gradle.kts`: `C:/Python313/python.exe`  
  Override with: `-PchaquopyPython=/path/to/python`

### Commands

```bash
# Debug APK
./gradlew :app:assembleDebug

# Release APK (signed with debug keystore for easy sideload)
./gradlew :app:assembleRelease
```

Output APKs:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

> **Note:** Chaquopy’s Python 3.13 builds are only available for `arm64-v8a` and `x86_64`. 32-bit ABIs are intentionally excluded.

---

## Project layout

```
app/src/main/
├── java/com/oneandonly/pythoncompiler/
│   ├── MainActivity.kt      # UI, editor, themes, autosave
│   ├── EditorViewModel.kt   # Run/stop, output, input bridge
│   ├── PyApp.kt             # Application + Python.start()
│   └── ui/theme/            # Compose Material theme
├── python/
│   └── pyrunner.py          # CPython execution bridge
├── assets/textmate/         # TextMate Python grammar + themes
└── res/                     # strings, fonts, icons
```

---

## Releases

A prebuilt APK may be attached to [GitHub Releases](https://github.com/arjncx-lang/Pythoncompiler/releases). Install on an arm64 device (virtually all modern phones) or an x86_64 emulator.

---

## License

All rights reserved unless otherwise stated by the repository owner.
