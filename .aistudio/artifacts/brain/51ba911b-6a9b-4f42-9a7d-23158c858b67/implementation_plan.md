# Helfrex Code — Mobile Code Editor & Development Studio

A modern, high-performance Android code editor inspired by VS Code, featuring multi-tab editing, syntax highlighting across popular languages, real-time error diagnostics, live Web & Android UI preview, project packaging (Zip/Folder), APK/EXE build compilation simulator, and 4-language localization (Turkish, Azerbaijani, Russian, English).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during the planning clarification phase:

- **Confirmed Decision 1 (Preview Environments)**: Dual preview engine offering **Live Web preview** (interactive HTML5/CSS/JavaScript via Android WebView) and an **Android UI simulator** (visual rendering for mobile components).
- **Confirmed Decision 2 (Build & Compilation)**: A dedicated **Build/Compile modal** allowing users to target `.apk` and `.exe` output with configurable output paths and file names, complete with step-by-step compilation logs, syntax validation, and package export.
- **Confirmed Decision 3 (Initial Launch State)**: Starts with a clean empty workspace as requested, ready for creating or importing files, with quick-action shortcuts to start coding immediately.
- **Confirmed Decision 4 (Brand & Aesthetics)**: Named **Helfrex Code** with `</>` branding, featuring VS Code-inspired Light (crisp white/light gray) and Dark (deep charcoal/slate) themes, plus System Theme following.

---

## 1. Overview & Core Concept

- **What It Does**: Helfrex Code brings a full-fledged IDE experience to Android phones and tablets. Users can open files from device storage, load entire folder hierarchies, create new files with custom extensions, manage multi-file tabs, write code with responsive virtual keys and indentation, highlight syntax for 15+ programming languages, detect syntax errors in real-time, preview web and mobile layouts live, package projects into ZIP archives or folder trees, and run simulated builds to APK and EXE artifacts.
- **Target Audience**: Mobile developers, students, and programmers who want to write, edit, review, and test code on the go without carrying a laptop.
- **Key Value**: Desktop-grade editing capabilities tailored for touchscreens (quick-access programming punctuation bar, pinch/zoom text sizing, tab switching, and instant preview) in a compact, native Android app.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Top Navigation Bar                              │
│  [☰ Menu]  </> Helfrex Code                [▶ Run/Preview] [💾 Save]   │
├────────────────────────────────────────────────────────────────────────┤
│  Tabs: [index.html ✕] [style.css ✕] [main.py ✕]                 [ + ]  │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  1 │ <!DOCTYPE html>                                                  │
│  2 │ <html lang="en">                                                 │
│  3 │ <head>                                                           │
│  4 │   <title>Helfrex App</title>                                     │
│  5 │ </head>                                                          │
│  6 │ <body>                                                           │
│  7 │   <h1 class="title">Hello World</h1>                             │
│  8 │ </body>                                                          │
│  9 │ </html>                                                          │
│                                                                        │
├────────────────────────────────────────────────────────────────────────┤
│ Quick Key Bar: { } ( ) [ ] < > / ; = : " ' $ # tab -> undo redo       │
├────────────────────────────────────────────────────────────────────────┤
│ Bottom Bar: [ ⟨/⟩ Code ]  |  [ 👁 Preview ]  |  [ Ln 7, Col 12 • UTF-8 ]│
└────────────────────────────────────────────────────────────────────────┘
```

#### Left Navigation Drawer (`☰` Menu)
- **Top Header**: Logo `</>` and title "Helfrex Code".
- **"Yeni" (New)** Button with `+` icon:
  - Expands 3 distinct options:
    1. *Yeni Dosya Oluştur* (Create New File): Modal with file name input, extension picker/entry (`.kt`, `.py`, `.html`, `.js`, etc.), and target directory.
    2. *Dosya Seç* (Pick File): Storage Access Framework (SAF) integration allowing user to select any local text/code file.
    3. *Klasör Seç* (Pick Folder): SAF folder picker to read and load directory trees into the editor.
- **"Dosyalar" (Files)** Button:
  - Displays list of currently opened and loaded files with their extension icons.
  - Each item features an eye icon with a diagonal slash (`VisibilityOff`), allowing user to toggle visibility/tab state.
  - Bottom actions: **İptal** (Cancel) and **Uygula** (Apply) to batch-commit visibility changes.
- **"Kaydet" (Save)** Button:
  - Offers dual options: **Zip** and **Klasör** (Folder) with checkboxes (user can select one or both).
  - Prompts for target name and output destination, bundling all project files (both open and background files).
- **"Derle" (Compile / Build)** Button:
  - Opens the compiler console where users can select `.apk` (Android Package) or `.exe` (Windows Executable).
  - Configurable app/output name and target directory.
  - Realistic compiler pipeline output (dependency resolution, syntax verification, byte compiling, linking, asset packaging, and artifact generation) with export action.
- **Bottom-Left "Ayarlar" (Settings)**:
  - **Sistem Teması** (System Theme): System Default (Recommended), Dark Mode, Light Mode.
  - **Dili Değiştir** (Change Language): Türkçe, Azərbaycanca, Русский, English.

### Visual Identity & Theme

- **Dark Theme Tokens**:
  - Background: `#1E1E1E` (editor canvas) / `#252526` (side drawer & chrome)
  - Surface & Card: `#2D2D2D` / `#333333`
  - Primary Accent: `#007ACC` / `#3794FF` (VS Code vibrant blue)
  - Text Primary: `#D4D4D4`
  - Gutter & Line Numbers: `#858585`
  - Syntax Tokens:
    - Keywords: `#569CD6` (blue) / `#C586C0` (purple)
    - Strings: `#CE9178` (coral orange)
    - Functions/Tags: `#DCDCAA` (light yellow) / `#4EC9B0` (teal)
    - Comments: `#6A9955` (forest green)
    - Numbers: `#B5CEA8` (soft green)
    - Errors: `#F44747` (bright red underline & indicator)
- **Light Theme Tokens**:
  - Background: `#FFFFFF` (editor canvas) / `#F3F3F3` (chrome & tabs)
  - Surface & Card: `#E5E5E5` / `#F8F9FA`
  - Primary Accent: `#005FB8`
  - Text Primary: `#1E1E1E`
  - Line Numbers: `#6E7681`
  - Syntax Tokens:
    - Keywords: `#0000FF` / `#AF00DB`
    - Strings: `#A31515`
    - Functions: `#795E26`
    - Comments: `#008000`
- **Typography**: Clean monospace code font (`JetBrains Mono` / Monospace family) for editor and line numbers; system sans-serif for UI labels and menus.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Client-Side Syntax Highlighter & Diagnostics**:
  - *Chosen Approach*: Regex-based lexer matching language profiles (`html`, `css`, `js`, `ts`, `json`, `py`, `kt`, `java`, `cpp`, `c`, `cs`, `xml`, `sql`, `sh`, `md`) returning annotated Jetpack Compose `AnnotatedString` with syntax error detection (unclosed tags, brackets, strings).
  - *Why*: Instant 60 FPS performance without lagging during typing; works completely offline with zero server dependencies.
- **Decision 2: Dual Mode Preview (Web & Mobile UI)**:
  - *Chosen Approach*: Embedded Android `WebView` executing user HTML/CSS/JS with console logs, combined with an interactive Android UI layout preview parser for mobile UI mockups.
  - *Why*: Delivers exact visual feedback for web stacks and allows mobile developers to preview UI without full external emulators.
- **Decision 3: Storage & File Management**:
  - *Chosen Approach*: Android Storage Access Framework (SAF) + local app sandbox project directory. Supports single files, folders, and in-memory multi-tab buffers with dirty tracking.
  - *Why*: Fully adheres to Android Scoped Storage and Play Store security policies while granting users total control over their local files.
- **Decision 4: Packaging & Compiler Engine**:
  - *Chosen Approach*: Real `java.util.zip` compression for ZIP archives, recursive folder tree export, and a realistic build simulator with compilation pipeline steps for `.apk` and `.exe`.
  - *Why*: Produces actual usable zip archives of code while providing a realistic developer build console for Android and Windows targets.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│                                   │                                    │
│                    HelfrexTheme (Light / Dark / Auto)                  │
│                                   │                                    │
│                      HelfrexEditorScreen (Scaffold)                    │
│      ┌────────────────────────────┴─────────────────────────────┐      │
│      ▼                                                          ▼      │
│ ┌──────────────────────┐                           ┌─────────────────┐ │
│ │  NavigationDrawer    │                           │ EditorWorkspace │ │
│ │  ├─ New (File/Folder)│                           │ ├─ Tab Bar      │ │
│ │  ├─ Files Manager    │                           │ ├─ Code Canvas  │ │
│ │  ├─ Save (Zip/Folder)│                           │ ├─ Gutter/Lines │ │
│ │  ├─ Build (.apk/.exe)│                           │ ├─ Quick Toolbar│ │
│ │  └─ Settings (Lang)  │                           │ └─ Status Info  │ │
│ └──────────────────────┘                           └────────┬────────┘ │
│                                                             ▼          │
│                                                    ┌─────────────────┐ │
│                                                    │ PreviewEngine   │ │
│                                                    │ ├─ Web (WebView)│ │
│                                                    │ └─ Android UI   │ │
│                                                    └─────────────────┘ │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
                ┌───────────────────────────────────────┐
                │          EditorViewModel              │
                │  ├─ openFiles: StateFlow<List<File>>  │
                │  ├─ activeFileId: StateFlow<String>   │
                │  ├─ appSettings: StateFlow<Settings>  │
                │  ├─ currentLanguage: StringsProvider  │
                │  ├─ buildLogs: StateFlow<List<Log>>   │
                │  └─ isPreviewMode: StateFlow<Boolean> │
                └───────────────────┬───────────────────┘
                                    │
                                    ▼
                ┌───────────────────────────────────────┐
                │             Domain Layer              │
                │  ├─ SyntaxHighlighter (15+ languages) │
                │  ├─ ErrorChecker (Linter engine)      │
                │  ├─ ArchiveManager (Zip/Folder export)│
                │  ├─ CompilerEngine (.apk/.exe build)  │
                │  └─ LocalizationManager (4 languages) │
                └───────────────────────────────────────┘
```

### Multilingual Support (Turkish, Azerbaijani, Russian, English)

All user-facing strings are dynamically localized across:
- **Türkçe**: "Yeni", "Yeni Dosya Oluştur", "Dosya Seç", "Klasör Seç", "Dosyalar", "Kaydet", "Derle", "Ayarlar", "Sistem Teması", "Dark Mode", "Light Mode", "Dili Değiştir", "İptal", "Uygula", "Kod", "Ön İzleme".
- **Azərbaycanca**: "Yeni", "Yeni Fayl Yarat", "Fayl Seç", "Qovluq Seç", "Fayllar", "Yadda Saxla", "Tərtib Et", "Tənzimləmələr", "Sistem Mövzusu", "Qaranlıq Rejim", "İşıqlı Rejim", "Dili Dəyiş", "Ləğv Et", "Tətbiq Et", "Kod", "Ön Baxış".
- **Русский**: "Новый", "Создать файл", "Выбрать файл", "Выбрать папку", "Файлы", "Сохранить", "Скомпилировать", "Настройки", "Системная тема", "Темная тема", "Светлая тема", "Изменить язык", "Отмена", "Применить", "Код", "Предпросмотр".
- **English**: "New", "Create New File", "Select File", "Select Folder", "Files", "Save", "Build", "Settings", "System Theme", "Dark Mode", "Light Mode", "Change Language", "Cancel", "Apply", "Code", "Preview".

---

## 5. Verification Plan

1. **Compilation Check**: Run `compile_applet` to ensure zero compilation or syntax errors.
2. **File & Folder Operations**: Verify creation of files with custom extensions, picking files, and loading folder contents.
3. **Multi-Tab Workspace**: Test switching between multiple open files, closing tabs, and visibility toggles in the "Dosyalar" menu.
4. **Syntax Highlighting & Error Detection**: Validate colorizing keywords, strings, tags, numbers across `.html`, `.css`, `.js`, `.py`, `.kt`, and red error markers.
5. **Preview Functionality**: Test live Web preview in WebView and Android UI simulator.
6. **Save as Zip / Folder**: Verify packaging project into a real zip archive and directory tree.
7. **Compiler Simulation**: Verify build flow for `.apk` and `.exe` with realistic logs and file output.
8. **Settings & Themes**: Verify toggling between System, Dark, and Light themes, as well as switching between all 4 languages.
