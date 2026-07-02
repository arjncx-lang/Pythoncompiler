package com.oneandonly.pythoncompiler

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.DefaultGrammarDefinition
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.widget.style.builtin.HandleStyleDrop
import org.eclipse.tm4e.core.registry.IGrammarSource
import org.eclipse.tm4e.core.registry.IThemeSource
import androidx.compose.runtime.DisposableEffect
import java.io.File

// ── Theme palettes ───────────────────────────────────────────────
private data class Palette(
    val bg: Color, val titlebar: Color, val surface: Color, val border: Color,
    val text: Color, val dim: Color, val accent: Color, val onAccent: Color,
    val focus: Color, val red: Color, val green: Color, val cyan: Color,
    val keyBg: Color, val stopBg: Color
)

// The original pink/dark "funky" chrome.
private val FunkyPalette = Palette(
    bg = Color(0xFF0F0E14), titlebar = Color(0xFF17151F), surface = Color(0xFF1B1923),
    border = Color(0xFF2C2A38), text = Color(0xFFF2F2F5), dim = Color(0xFF6B697C),
    accent = Color(0xFFFF2E63), onAccent = Color.White, focus = Color(0xFFFF2E63),
    red = Color(0xFFFF6B6B), green = Color(0xFF6A9955), cyan = Color(0xFF66D9EF),
    keyBg = Color(0xFF1F1D29), stopBg = Color(0xFFB71C3A)
)

// Neutral VS Code Dark chrome (matches the dark_plus editor theme).
private val VsCodeDarkPalette = Palette(
    bg = Color(0xFF1E1E1E), titlebar = Color(0xFF252526), surface = Color(0xFF252526),
    border = Color(0xFF3C3C3C), text = Color(0xFFD4D4D4), dim = Color(0xFF858585),
    accent = Color(0xFF007ACC), onAccent = Color.White, focus = Color(0xFF007ACC),
    red = Color(0xFFF14C4C), green = Color(0xFF6A9955), cyan = Color(0xFF4EC9B0),
    keyBg = Color(0xFF2D2D2D), stopBg = Color(0xFFA1260D)
)

// Neutral VS Code Light chrome (matches the light_plus editor theme).
private val VsCodeLightPalette = Palette(
    bg = Color(0xFFFFFFFF), titlebar = Color(0xFFF3F3F3), surface = Color(0xFFF3F3F3),
    border = Color(0xFFE5E5E5), text = Color(0xFF1F1F1F), dim = Color(0xFF6E7781),
    accent = Color(0xFF007ACC), onAccent = Color.White, focus = Color(0xFF007ACC),
    red = Color(0xFFE51400), green = Color(0xFF098658), cyan = Color(0xFF007ACC),
    keyBg = Color(0xFFECECEC), stopBg = Color(0xFFC42B1C)
)

/** Selectable app themes: chrome palette + matching TextMate editor theme + handle accent. */
private enum class AppTheme(
    val key: String,
    val label: String,
    val tmTheme: String,
    val palette: Palette,
    val handleAccent: Int
) {
    VSCODE_DARK("vscodeDark", "VS Code Dark", "dark_plus", VsCodeDarkPalette, 0xFF007ACC.toInt()),
    VSCODE_LIGHT("vscodeLight", "VS Code Light", "light_plus", VsCodeLightPalette, 0xFF007ACC.toInt()),
    FUNKY("funky", "Funky", "funky", FunkyPalette, 0xFFFF2E63.toInt());

    companion object {
        fun fromKey(key: String?): AppTheme = entries.firstOrNull { it.key == key } ?: FUNKY
    }
}

private val MonoFont = FontFamily(Font(R.font.jetbrains_mono))

private const val INITIAL_CODE = """# Welcome to PyPhone
print("Hello, Python!")

# Fibonacci sequence
def fib(n):
    a, b = 0, 1
    for _ in range(n):
        print(a, end=" ")
        a, b = b, a + b
    print()

fib(10)

# List comprehension
squares = [x**2 for x in range(1, 6)]
print("Squares:", squares)
"""

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupTextMate()
        setContent { PyPhoneApp() }
    }

    /** One-time TextMate (syntax highlighting + themes) registration. */
    private fun setupTextMate() {
        FileProviderRegistry.getInstance().addFileProvider(AssetsFileResolver(assets))
        val themes = ThemeRegistry.getInstance()
        try {
            loadTheme(themes, "dark_plus", dark = true)
            loadTheme(themes, "light_plus", dark = false)
            loadTheme(themes, "funky", dark = true)
            themes.setTheme("dark_plus")

            val grammarSource = assets.open("textmate/python/python.tmLanguage.json").use { g ->
                IGrammarSource.fromInputStream(g, "python.tmLanguage.json", null)
            }
            GrammarRegistry.getInstance().loadGrammar(
                DefaultGrammarDefinition.withGrammarSource(grammarSource, "source.python", null)
            )
        } catch (e: Exception) {
            android.util.Log.e("PyPhone", "TextMate setup failed: ${e.message}", e)
        }
    }

    private fun loadTheme(themes: ThemeRegistry, name: String, dark: Boolean) {
        assets.open("textmate/$name.json").use { input ->
            themes.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(input, "$name.json", null),
                    name
                ).apply { isDark = dark }
            )
        }
    }
}

private data class SymKey(val label: String, val ins: String, val moveLeft: Int = 0)

private val SYM_KEYS = listOf(
    SymKey("Tab", "    "),
    SymKey(":", ":"), SymKey("()", "()", 1), SymKey("[]", "[]", 1),
    SymKey("{}", "{}", 1), SymKey("\"\"", "\"\"", 1), SymKey("''", "''", 1),
    SymKey("=", "="), SymKey("==", "=="), SymKey("!=", "!="),
    SymKey("<", "<"), SymKey(">", ">"), SymKey("+", "+"), SymKey("-", "-"),
    SymKey("*", "*"), SymKey("/", "/"), SymKey("%", "%"), SymKey("**", "**"),
    SymKey("//", "//"), SymKey("#", "#"), SymKey("_", "_"),
    SymKey(",", ","), SymKey(".", ".")
)

// ── Autosave ─────────────────────────────────────────────────────
private fun autosaveFile(context: Context) = File(context.filesDir, "autosave.py")

private fun saveCode(context: Context, editor: CodeEditor?) {
    val text = editor?.text?.toString() ?: return
    try {
        autosaveFile(context).writeText(text)
    } catch (e: Exception) {
        android.util.Log.e("PyPhone", "autosave failed: ${e.message}", e)
    }
}

/** Last autosaved code, or null on first run / read failure. */
private fun loadSavedCode(context: Context): String? = try {
    autosaveFile(context).takeIf { it.exists() }?.readText()
} catch (e: Exception) {
    null
}

private fun copyAllCode(context: Context, editor: CodeEditor?) {
    val text = editor?.text?.toString() ?: return
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("PyPhone code", text))
    Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
}

private fun applyEditorTheme(editor: CodeEditor, theme: AppTheme) {
    try {
        val registry = ThemeRegistry.getInstance()
        registry.setTheme(theme.tmTheme)
        editor.colorScheme = TextMateColorScheme.create(registry).apply {
            // Tint the selection teardrop handles + insert handle to the accent.
            setColor(EditorColorScheme.SELECTION_HANDLE, theme.handleAccent)
            setColor(EditorColorScheme.SELECTION_INSERT, theme.handleAccent)
        }
    } catch (e: Exception) {
        android.util.Log.e("PyPhone", "theme switch failed: ${e.message}", e)
    }
}

@Composable
fun PyPhoneApp(vm: EditorViewModel = viewModel()) {
    val output by vm.output.collectAsStateWithLifecycle()
    val isRunning by vm.isRunning.collectAsStateWithLifecycle()
    val inputPrompt by vm.inputPrompt.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pyphone", Context.MODE_PRIVATE) }
    var theme by remember { mutableStateOf(AppTheme.fromKey(prefs.getString("theme", null))) }
    val p = theme.palette

    var editor by remember { mutableStateOf<CodeEditor?>(null) }
    var outputVisible by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var aboutOpen by remember { mutableStateOf(false) }
    // Persisted editor view toggles.
    var wordWrap by remember { mutableStateOf(prefs.getBoolean("wordWrap", false)) }
    var showWhitespace by remember { mutableStateOf(prefs.getBoolean("showWhitespace", false)) }
    var lineNumbers by remember { mutableStateOf(prefs.getBoolean("lineNumbers", true)) }

    LaunchedEffect(theme, editor) {
        editor?.let { applyEditorTheme(it, theme) }
    }

    // Autosave immediately when the app goes to the background.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(editor, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) saveCode(context, editor)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(p.bg)
            .systemBarsPadding()
            .imePadding()
    ) {
        // ── Top bar (undo / redo + overflow menu) ────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .background(p.titlebar)
                .height(48.dp)
                .padding(end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Python 3.13",
                color = p.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 14.dp)
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { editor?.undo() }) {
                Icon(Icons.AutoMirrored.Outlined.Undo, "Undo", tint = p.text)
            }
            IconButton(onClick = { editor?.redo() }) {
                Icon(Icons.AutoMirrored.Outlined.Redo, "Redo", tint = p.text)
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, "Menu", tint = p.text)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.background(p.surface)
                ) {
                    MenuRow("New file", Icons.Outlined.NoteAdd, p) {
                        editor?.setText(""); vm.clearOutput(); menuOpen = false
                    }
                    MenuRow("Clear code", Icons.Outlined.Backspace, p) {
                        editor?.setText(""); menuOpen = false
                    }
                    MenuRow("Copy code", Icons.Outlined.ContentCopy, p) {
                        copyAllCode(context, editor); menuOpen = false
                    }
                    MenuRow("Paste code", Icons.Outlined.ContentPaste, p) {
                        editor?.pasteText(); menuOpen = false
                    }
                    MenuDivider(p)
                    MenuToggle("Word wrap", wordWrap, p) {
                        wordWrap = !wordWrap
                        prefs.edit().putBoolean("wordWrap", wordWrap).apply()
                        editor?.setWordwrap(wordWrap, true)
                        menuOpen = false
                    }
                    MenuToggle("Show whitespace", showWhitespace, p) {
                        showWhitespace = !showWhitespace
                        prefs.edit().putBoolean("showWhitespace", showWhitespace).apply()
                        editor?.setNonPrintablePaintingFlags(whitespaceFlags(showWhitespace))
                        menuOpen = false
                    }
                    MenuToggle("Line numbers", lineNumbers, p) {
                        lineNumbers = !lineNumbers
                        prefs.edit().putBoolean("lineNumbers", lineNumbers).apply()
                        editor?.isLineNumberEnabled = lineNumbers
                        menuOpen = false
                    }
                    MenuDivider(p)
                    MenuLabel("Theme", p)
                    AppTheme.entries.forEach { t ->
                        MenuToggle(t.label, theme == t, p) {
                            theme = t
                            prefs.edit().putString("theme", t.key).apply()
                            menuOpen = false
                        }
                    }
                    MenuDivider(p)
                    MenuRow("About", Icons.Outlined.Info, p) {
                        aboutOpen = true; menuOpen = false
                    }
                }
            }
        }

        if (aboutOpen) AboutDialog(p) { aboutOpen = false }

        // ── Editor (with floating Run button, bottom-left) ───────
        Box(Modifier.fillMaxWidth().weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    CodeEditor(ctx).apply {
                        val mono = ResourcesCompat.getFont(ctx, R.font.jetbrains_mono)
                            ?: Typeface.MONOSPACE
                        typefaceText = mono
                        typefaceLineNumber = mono
                        setTextSize(15f)
                        // Restore persisted view toggles.
                        setWordwrap(wordWrap, true)
                        setNonPrintablePaintingFlags(whitespaceFlags(showWhitespace))
                        isLineNumberEnabled = lineNumbers
                        // Keep the line-number gutter fixed while scrolling so it
                        // never drifts off-screen after edits.
                        setPinLineNumber(true)
                        // With pinned line numbers, Sora paints the gutter AFTER the
                        // selection handles (EditorRenderer.draw). HandleStyleDrop is
                        // center-aligned and 20dp wide, so the left teardrop reaches
                        // ~10dp left of column 0 and the default 2dp right-divider gap
                        // isn't enough to keep it clear — the gutter then overdraws it.
                        // Widen the right gap past the teardrop's half-width so the
                        // handle stays visible instead of hiding behind the gutter.
                        val dp = ctx.resources.displayMetrics.density
                        setDividerMargin(dividerMarginLeft, 12f * dp)
                        // Default drop handle: teardrop centered under the character,
                        // honouring the scheme's handle color.
                        try {
                            setSelectionHandleStyle(HandleStyleDrop(ctx))
                        } catch (e: Exception) {
                            android.util.Log.e("PyPhone", "handle style failed: ${e.message}", e)
                        }
                        try {
                            setEditorLanguage(TextMateLanguage.create("source.python", true))
                        } catch (e: Exception) {
                            android.util.Log.e("PyPhone", "editor lang failed: ${e.message}", e)
                        }
                        setText(loadSavedCode(ctx) ?: INITIAL_CODE)
                        // Debounced autosave: write 1.5s after the last edit.
                        val saveRunnable = Runnable { saveCode(ctx, this) }
                        subscribeEvent(ContentChangeEvent::class.java) { _, _ ->
                            removeCallbacks(saveRunnable)
                            postDelayed(saveRunnable, 1500)
                        }
                        editor = this
                        applyEditorTheme(this, theme)
                    }
                },
                onRelease = { it.release() },
                modifier = Modifier.fillMaxSize()
            )

            // Floating Run / Stop button, bottom-left.
            RunFab(
                isRunning = isRunning,
                p = p,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp)
            ) {
                if (isRunning) {
                    vm.stop()
                } else {
                    outputVisible = true
                    vm.run(editor?.text?.toString() ?: "")
                }
            }
        }

        // ── Output panel (slides up from the bottom only when shown) ─
        AnimatedVisibility(
            visible = outputVisible,
            enter = slideInVertically(animationSpec = tween(280)) { it } +
                expandVertically(animationSpec = tween(280)),
            exit = slideOutVertically(animationSpec = tween(220)) { it } +
                shrinkVertically(animationSpec = tween(220))
        ) {
            Column(Modifier.fillMaxWidth().height(320.dp)) {
                OutputHeader(
                    palette = p,
                    onClear = { vm.clearOutput() },
                    onClose = { outputVisible = false }
                )
                OutputBody(output, p, Modifier.fillMaxWidth().weight(1f))
            }
        }

        // ── Symbol keyboard bar ──────────────────────────────────
        SymbolBar(p) { key ->
            editor?.let { ed ->
                ed.commitText(key.ins)
                if (key.moveLeft != 0) {
                    val c = ed.cursor
                    val col = (c.leftColumn - key.moveLeft).coerceAtLeast(0)
                    ed.setSelection(c.leftLine, col)
                }
            }
        }
    }

    inputPrompt?.let { prompt ->
        InputDialog(prompt, p, onSubmit = { vm.submitInput(it) }, onCancel = { vm.cancelInput() })
    }
}

@Composable
private fun RunFab(isRunning: Boolean, p: Palette, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(50.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(if (isRunning) p.stopBg else p.accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (isRunning) Icons.Filled.Stop else Icons.Filled.PlayArrow,
            contentDescription = if (isRunning) "Stop" else "Run",
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun MenuRow(label: String, icon: ImageVector, p: Palette, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, color = p.text, fontSize = 14.sp) },
        leadingIcon = { Icon(icon, null, tint = p.dim, modifier = Modifier.size(20.dp)) },
        onClick = onClick
    )
}

@Composable
private fun MenuToggle(label: String, checked: Boolean, p: Palette, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, color = p.text, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                if (checked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                null,
                tint = if (checked) p.text else p.dim,
                modifier = Modifier.size(20.dp)
            )
        },
        onClick = onClick
    )
}

@Composable
private fun AboutDialog(p: Palette, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = p.surface,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = p.accent) }
        },
        icon = {
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "PyPhone logo",
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text("PyPhone", color = p.text, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                AboutParagraph(
                    "PyPhone is a mobile Python coding app. You write Python in a native " +
                        "code editor and run it right on your phone with no internet needed.",
                    p
                )
                AboutHeading("Goal", p)
                AboutParagraph(
                    "Give you a fast, offline Python playground on Android with real CPython, " +
                        "syntax highlighting, and instant output.",
                    p
                )
                AboutHeading("Built with", p)
                AboutParagraph(
                    "Editor: Sora Editor, a native Android code editor by Rosemoe.\n" +
                        "Python: CPython 3.13 embedded through Chaquopy 17, so your code runs on a " +
                        "real Python interpreter bundled inside the app.\n" +
                        "UI: Jetpack Compose.",
                    p
                )
                AboutHeading("Python version", p)
                AboutParagraph("CPython 3.13.1", p)
                AboutHeading("Limitations", p)
                AboutParagraph(
                    "Packages needing heavy native builds may not install.\n" +
                        "No system shell access. File and network features depend on Android permissions.\n" +
                        "The interpreter runs inside the app sandbox, so it is not a full desktop Python setup.\n" +
                        "Very long computations can be slow on low end devices.",
                    p
                )
                AboutHeading("Versions", p)
                AboutParagraph(
                    "App 1.0\nSora Editor 0.23.6\nChaquopy 17.0.0",
                    p
                )
                AboutHeading("Sora editor repo", p)
                AboutParagraph("https://github.com/Rosemoe/sora-editor", p)
            }
        }
    )
}

@Composable
private fun AboutHeading(text: String, p: Palette) {
    Text(
        text,
        color = p.accent,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
    )
}

@Composable
private fun AboutParagraph(text: String, p: Palette) {
    Text(text, color = p.text, fontSize = 13.sp, lineHeight = 18.sp)
}

@Composable
private fun MenuLabel(label: String, p: Palette) {
    Text(
        label,
        color = p.dim,
        fontSize = 11.sp,
        modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun MenuDivider(p: Palette) {
    Box(Modifier.fillMaxWidth().height(1.dp).background(p.border))
}

/** Whitespace-rendering flags: leading (Python indent) + inner + trailing when on, else none. */
private fun whitespaceFlags(on: Boolean): Int =
    if (on) CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or
        CodeEditor.FLAG_DRAW_WHITESPACE_INNER or
        CodeEditor.FLAG_DRAW_WHITESPACE_TRAILING
    else 0

@Composable
private fun OutputHeader(
    palette: Palette,
    onClear: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(palette.titlebar)
            .height(44.dp)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "OUTPUT",
            color = palette.text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onClear) {
            Icon(Icons.Outlined.DeleteSweep, "Clear output", tint = palette.dim, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.KeyboardArrowDown, "Close output", tint = palette.dim)
        }
    }
}

@Composable
private fun OutputBody(output: List<OutSeg>, p: Palette, modifier: Modifier) {
    val scroll = rememberScrollState()
    val text = remember(output, p) {
        buildAnnotatedString {
            if (output.isEmpty()) {
                withStyle(SpanStyle(color = p.dim)) { append("Run code to see output here.") }
            } else {
                for (seg in output) {
                    withStyle(SpanStyle(color = if (seg.isErr) p.red else p.text)) {
                        append(seg.text)
                    }
                }
            }
        }
    }
    LaunchedEffect(output) { scroll.scrollTo(scroll.maxValue) }
    Box(modifier.background(p.bg)) {
        SelectionContainer(Modifier.fillMaxSize().verticalScroll(scroll).padding(14.dp)) {
            Text(text, fontSize = 13.sp, fontFamily = MonoFont, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun SymbolBar(p: Palette, onKey: (SymKey) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(p.titlebar)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (key in SYM_KEYS) {
            Box(
                Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(p.keyBg)
                    .clickable { onKey(key) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    key.label,
                    color = if (key.label == "Tab") p.cyan else p.text,
                    fontSize = 14.sp,
                    fontFamily = MonoFont,
                    fontWeight = if (key.label == "Tab") FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun InputDialog(prompt: String, p: Palette, onSubmit: (String) -> Unit, onCancel: () -> Unit) {
    var value by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = p.surface,
        title = { Text(prompt.ifBlank { "input()" }, color = p.cyan, fontSize = 14.sp) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit(value) }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = p.bg,
                    unfocusedContainerColor = p.bg,
                    focusedTextColor = p.text,
                    unfocusedTextColor = p.text,
                    focusedIndicatorColor = p.focus,
                    unfocusedIndicatorColor = p.border,
                    cursorColor = p.focus
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
        },
        confirmButton = { TextButton(onClick = { onSubmit(value) }) { Text("Submit", color = p.cyan) } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel", color = p.dim) } }
    )
}
