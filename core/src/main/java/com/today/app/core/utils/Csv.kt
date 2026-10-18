package com.today.app.core.utils

object Csv {

    private const val DELIMITER = ','

    fun parse(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var field = StringBuilder()
        var row = mutableListOf<String>()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes -> when {
                    c == '"' -> {
                        if (i + 1 < text.length && text[i + 1] == '"') {
                            field.append('"')
                            i++
                        } else {
                            inQuotes = false
                        }
                    }

                    else -> field.append(c)
                }

                c == '"' -> inQuotes = true

                c == DELIMITER -> {
                    row.add(field.toString())
                    field = StringBuilder()
                }

                c == '\r' || c == '\n' -> {
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    row.add(field.toString())
                    field = StringBuilder()
                    rows.add(row)
                    row = mutableListOf()
                }

                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            rows.add(row)
        }
        return rows.filter { it.any { cell -> cell.isNotBlank() } }
    }

    fun escape(value: String): String {
        val needsQuote = value.any { it == DELIMITER || it == '"' || it == '\n' || it == '\r' }
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuote) "\"$escaped\"" else escaped
    }

    fun row(values: List<String>): String = values.joinToString(",") { escape(it) }
}
