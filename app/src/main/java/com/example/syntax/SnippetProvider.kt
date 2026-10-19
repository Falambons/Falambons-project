package com.example.syntax

data class CodeSnippet(
    val label: String,
    val code: String,
    val cursorOffset: Int = code.length
)

object SnippetProvider {

    val quickSymbols = listOf(
        "{", "}", "(", ")", "[", "]", "<", ">",
        ";", ":", "\"", "'", "`", "=", "==", "===",
        "+", "-", "*", "/", "%", "!", "?", "&", "&&",
        "|", "||", "$", "->", "=>", ".", ",", "#"
    )

    fun getSnippetsForExtension(extension: String): List<CodeSnippet> {
        val ext = extension.lowercase().removePrefix(".")
        return when (ext) {
            "js", "javascript", "ts", "typescript" -> listOf(
                CodeSnippet("log", "console.log();", 12),
                CodeSnippet("const =>", "const func = () => {\n    \n};", 25),
                CodeSnippet("func", "function name() {\n    \n}", 14),
                CodeSnippet("if/else", "if () {\n    \n} else {\n    \n}", 4),
                CodeSnippet("try/catch", "try {\n    \n} catch (error) {\n    console.error(error);\n}", 10),
                CodeSnippet("async", "async function fetch() {\n    const res = await ;\n}", 47),
                CodeSnippet("for", "for (let i = 0; i < length; i++) {\n    \n}", 20)
            )

            "css", "scss" -> listOf(
                CodeSnippet("flex-center", "display: flex;\njustify-content: center;\nalign-items: center;"),
                CodeSnippet("grid", "display: grid;\ngrid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\ngap: 16px;"),
                CodeSnippet("bg-color", "background-color: #0A0A10;"),
                CodeSnippet("border-rad", "border-radius: 12px;"),
                CodeSnippet("neon-glow", "box-shadow: 0 0 15px rgba(0, 229, 255, 0.5);\nborder: 1px solid #00E5FF;"),
                CodeSnippet("transition", "transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);"),
                CodeSnippet("margin", "margin: 0 auto;")
            )

            "cpp", "c", "h", "hpp" -> listOf(
                CodeSnippet("#include", "#include <iostream>\n"),
                CodeSnippet("main", "int main() {\n    \n    return 0;\n}", 17),
                CodeSnippet("cout", "std::cout << \"\" << std::endl;", 14),
                CodeSnippet("cin", "std::cin >> ;", 12),
                CodeSnippet("for-i", "for (int i = 0; i < n; ++i) {\n    \n}", 20),
                CodeSnippet("vector", "std::vector<int> list;"),
                CodeSnippet("struct", "struct Node {\n    int val;\n    Node* next;\n};")
            )

            "php" -> listOf(
                CodeSnippet("<?php", "<?php\n\n?>", 7),
                CodeSnippet("echo", "echo \"\";", 6),
                CodeSnippet("foreach", "foreach (\$items as \$item) {\n    \n}", 9),
                CodeSnippet("fn", "function name() {\n    \n}", 14),
                CodeSnippet("json", "echo json_encode(\$data, JSON_PRETTY_PRINT);"),
                CodeSnippet("header-json", "header('Content-Type: application/json; charset=utf-8');"),
                CodeSnippet("class", "class Service {\n    public function __construct() {\n    }\n}")
            )

            "html", "xml" -> listOf(
                CodeSnippet("html5", "<!DOCTYPE html>\n<html>\n<head>\n    <title></title>\n</head>\n<body>\n    \n</body>\n</html>", 43),
                CodeSnippet("div", "<div class=\"\">\n    \n</div>", 12),
                CodeSnippet("btn", "<button id=\"\">Klik</button>", 12),
                CodeSnippet("input", "<input type=\"text\" placeholder=\"\" />", 32),
                CodeSnippet("link-css", "<link rel=\"stylesheet\" href=\"style.css\">"),
                CodeSnippet("script-js", "<script src=\"app.js\"></script>")
            )

            else -> listOf(
                CodeSnippet("Credit", "Credit by : falambons developer\n"),
                CodeSnippet("Header", "=== FALAMBONS STUDIO ===\n"),
                CodeSnippet("TODO", "[ ] TODO: "),
                CodeSnippet("Done", "[x] SELESAI: "),
                CodeSnippet("Date", "Tanggal: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n")
            )
        }
    }
}
