package com.example.syntax

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.theme.SyntaxBracket
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxFunction
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxTag
import com.example.ui.theme.SyntaxVariable
import java.util.regex.Pattern

object SyntaxHighlighter {

    private val jsKeywords = setOf(
        "function", "const", "let", "var", "return", "if", "else", "for", "while",
        "do", "switch", "case", "break", "continue", "default", "class", "extends",
        "new", "this", "super", "import", "export", "from", "as", "try", "catch",
        "finally", "throw", "async", "await", "yield", "typeof", "instanceof", "in",
        "of", "true", "false", "null", "undefined", "NaN"
    )

    private val cppKeywords = setOf(
        "int", "float", "double", "char", "bool", "void", "auto", "const", "constexpr",
        "class", "struct", "enum", "namespace", "using", "std", "return", "if", "else",
        "for", "while", "do", "switch", "case", "break", "continue", "default",
        "public", "private", "protected", "virtual", "override", "template", "typename",
        "include", "define", "ifdef", "ifndef", "endif", "true", "false", "nullptr", "sizeof"
    )

    private val phpKeywords = setOf(
        "php", "echo", "print", "function", "return", "if", "else", "elseif", "for",
        "foreach", "as", "while", "do", "switch", "case", "break", "continue", "class",
        "extends", "implements", "interface", "public", "private", "protected", "static",
        "new", "try", "catch", "finally", "throw", "include", "require", "require_once",
        "isset", "empty", "die", "exit", "array", "null", "true", "false"
    )

    private val cssKeywords = setOf(
        "color", "background", "margin", "padding", "border", "display", "flex",
        "grid", "width", "height", "font-size", "font-family", "align-items",
        "justify-content", "position", "absolute", "relative", "fixed", "none",
        "block", "inline-block", "solid", "px", "rem", "em", "vh", "vw", "!important"
    )

    fun highlight(text: String, extension: String): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")
        val ext = extension.lowercase().removePrefix(".")

        return buildAnnotatedString {
            append(text)

            when (ext) {
                "js", "javascript", "ts", "typescript", "json" -> highlightGenericCode(this, text, jsKeywords)
                "cpp", "c", "h", "hpp", "java", "kt" -> highlightGenericCode(this, text, cppKeywords)
                "php" -> highlightPhpCode(this, text)
                "css", "scss" -> highlightCssCode(this, text)
                "html", "xml" -> highlightHtmlCode(this, text)
                else -> {
                    // Plain text or other format: still highlight quotes, numbers, brackets
                    highlightBasic(this, text)
                }
            }
        }
    }

    private fun highlightGenericCode(builder: AnnotatedString.Builder, text: String, keywords: Set<String>) {
        // Strings ("..." and '...' and `...`)
        highlightPattern(builder, text, "\"[^\"]*\"|'[^']*'|`[^`]*`", SpanStyle(color = SyntaxString))

        // Single line comments //...
        highlightPattern(builder, text, "//.*", SpanStyle(color = SyntaxComment, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))

        // Multi line comments /* ... */
        highlightPattern(builder, text, "/\\*[\\s\\S]*?\\*/", SpanStyle(color = SyntaxComment, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))

        // Numbers
        highlightPattern(builder, text, "\\b\\d+(\\.\\d+)?\\b", SpanStyle(color = SyntaxNumber))

        // Function calls: word followed by (
        highlightPattern(builder, text, "\\b[a-zA-Z_][a-zA-Z0-9_]*(?=\\s*\\()", SpanStyle(color = SyntaxFunction, fontWeight = FontWeight.SemiBold))

        // Keywords
        val keywordRegex = "\\b(" + keywords.joinToString("|") + ")\\b"
        highlightPattern(builder, text, keywordRegex, SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold))

        // Brackets
        highlightPattern(builder, text, "[{}()\\[\\]]", SpanStyle(color = SyntaxBracket))
    }

    private fun highlightPhpCode(builder: AnnotatedString.Builder, text: String) {
        // Strings
        highlightPattern(builder, text, "\"[^\"]*\"|'[^']*'", SpanStyle(color = SyntaxString))

        // Comments
        highlightPattern(builder, text, "//.*|#.*", SpanStyle(color = SyntaxComment))
        highlightPattern(builder, text, "/\\*[\\s\\S]*?\\*/", SpanStyle(color = SyntaxComment))

        // PHP tags <?php and ?>
        highlightPattern(builder, text, "<\\?php|\\?>", SpanStyle(color = SyntaxNeonPink, fontWeight = FontWeight.Bold))

        // Variables $foo
        highlightPattern(builder, text, "\\$[a-zA-Z_\\x7f-\\xff][a-zA-Z0-9_\\x7f-\\xff]*", SpanStyle(color = SyntaxVariable, fontWeight = FontWeight.Medium))

        // Numbers
        highlightPattern(builder, text, "\\b\\d+(\\.\\d+)?\\b", SpanStyle(color = SyntaxNumber))

        // Keywords
        val keywordRegex = "\\b(" + phpKeywords.joinToString("|") + ")\\b"
        highlightPattern(builder, text, keywordRegex, SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold))

        // Brackets
        highlightPattern(builder, text, "[{}()\\[\\]]", SpanStyle(color = SyntaxBracket))
    }

    private fun highlightCssCode(builder: AnnotatedString.Builder, text: String) {
        // Comments
        highlightPattern(builder, text, "/\\*[\\s\\S]*?\\*/", SpanStyle(color = SyntaxComment))

        // Selectors before {
        highlightPattern(builder, text, "[.#]?[a-zA-Z0-9_\\-]+(?=\\s*[{,])", SpanStyle(color = SyntaxTag, fontWeight = FontWeight.Bold))

        // Properties before :
        highlightPattern(builder, text, "[a-zA-Z0-9\\-]+(?=\\s*:)", SpanStyle(color = SyntaxKeyword))

        // Hex colors #FFF, #00E5FF
        highlightPattern(builder, text, "#[0-9a-fA-F]{3,8}\\b", SpanStyle(color = SyntaxVariable))

        // Numbers with units
        highlightPattern(builder, text, "\\b\\d+(px|rem|em|%|vh|vw|s|ms)?\\b", SpanStyle(color = SyntaxNumber))

        // Brackets
        highlightPattern(builder, text, "[{}]", SpanStyle(color = SyntaxBracket))
    }

    private fun highlightHtmlCode(builder: AnnotatedString.Builder, text: String) {
        // Strings inside attributes
        highlightPattern(builder, text, "\"[^\"]*\"|'[^']*'", SpanStyle(color = SyntaxString))

        // Comments <!-- ... -->
        highlightPattern(builder, text, "<!--[\\s\\S]*?-->", SpanStyle(color = SyntaxComment))

        // Tags <tag> or </tag>
        highlightPattern(builder, text, "</?[a-zA-Z0-9\\-]+", SpanStyle(color = SyntaxTag, fontWeight = FontWeight.Bold))

        // Attributes inside tags: name=
        highlightPattern(builder, text, "\\b[a-zA-Z0-9\\-]+(?=\\s*=)", SpanStyle(color = SyntaxVariable))

        // Angle brackets
        highlightPattern(builder, text, "[<>/]", SpanStyle(color = SyntaxBracket))
    }

    private fun highlightBasic(builder: AnnotatedString.Builder, text: String) {
        highlightPattern(builder, text, "\"[^\"]*\"|'[^']*'", SpanStyle(color = SyntaxString))
        highlightPattern(builder, text, "\\b\\d+\\b", SpanStyle(color = SyntaxNumber))
        highlightPattern(builder, text, "[{}()\\[\\]<>]", SpanStyle(color = SyntaxBracket))
    }

    private fun highlightPattern(
        builder: AnnotatedString.Builder,
        text: String,
        regex: String,
        style: SpanStyle
    ) {
        try {
            val matcher = Pattern.compile(regex).matcher(text)
            while (matcher.find()) {
                builder.addStyle(style, matcher.start(), matcher.end())
            }
        } catch (_: Exception) {}
    }

    private val SyntaxNeonPink = androidx.compose.ui.graphics.Color(0xFFFF007F)

    fun createVisualTransformation(extension: String): VisualTransformation {
        return VisualTransformation { text ->
            TransformedText(
                text = highlight(text.text, extension),
                offsetMapping = OffsetMapping.Identity
            )
        }
    }
}
