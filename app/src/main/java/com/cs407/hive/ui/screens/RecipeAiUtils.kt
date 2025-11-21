package com.cs407.hive.ui.screens

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Builds the structured prompt expected by the Perplexity API so that we can reliably parse
 * the response into [RecipeNote] entries separated by ";;;".
 */
fun buildRecipePrompt(ingredients: List<String>): String {
    val pantryStaples = "oil, butter, salt, pepper, sugar, vinegar, water"
    val formattedIngredients = ingredients.joinToString(separator = ", ")
    return buildString {
        appendLine("You are a culinary assistant who crafts approachable recipes.")
        appendLine("Use the provided ingredients heavily. You may add common staples ($pantryStaples) if required.")
        appendLine("Return EXACTLY three recipes.")
        appendLine("Each recipe must follow this RecipeNote schema and be separated by ';;;':")
        appendLine("RecipeNote")
        appendLine("id=<short identifier>")
        appendLine("dishName=<Title Case name>")
        appendLine("difficulty=<Easy|Medium|Hard>")
        appendLine("cookingTime=<e.g., 25 mins or 1 hour>")
        appendLine("ingredients=• item one\\n• item two (use bullet list with newline separators)")
        appendLine("instructions=1. Step one\\n2. Step two (number each instruction)")
        appendLine("timestamp=<YYYY-MM-DD>")
        appendLine("Do not add explanations outside of the recipe blocks. Separate each block strictly with ';;;'.")
        append("Ingredients provided: ")
        append(formattedIngredients)
    }
}

/** Utility that parses the `;;;` separated response from Perplexity into [RecipeNote] objects. */
object RecipeNoteParser {
    fun parse(rawResponse: String): List<RecipeNote> {
        if (rawResponse.isBlank()) return emptyList()
        return rawResponse.split(";;;")
            .mapNotNull { block ->
                val trimmedBlock = block.trim()
                if (trimmedBlock.isEmpty()) return@mapNotNull null
                toRecipeNote(trimmedBlock)
            }
    }

    private fun toRecipeNote(block: String): RecipeNote? {
        val fields = mutableMapOf<String, String>()
        var currentKey: String? = null
        block.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.equals("RecipeNote", ignoreCase = true)) return@forEach
            if (trimmed.isEmpty()) {
                if (currentKey != null && fields[currentKey]?.isNotEmpty() == true) {
                    fields[currentKey!!] = fields[currentKey!!] + "\n"
                }
                return@forEach
            }

            val delimiterIndex = sequenceOf('=', ':')
                .map { trimmed.indexOf(it) }
                .firstOrNull { it >= 0 }

            val isNewField = delimiterIndex != null && delimiterIndex > 0 &&
                    trimmed.substring(0, delimiterIndex).none { it.isWhitespace() }

            if (isNewField && delimiterIndex != null) {
                val key = trimmed.substring(0, delimiterIndex).trim().lowercase()
                val value = trimmed.substring(delimiterIndex + 1).trim()
                if (key.isNotEmpty()) {
                    fields[key] = value
                    currentKey = key
                } else {
                    currentKey = null
                }
            } else if (currentKey != null) {
                val previous = fields[currentKey].orEmpty()
                val continuation = line.trimEnd()
                fields[currentKey!!] =
                    if (previous.isEmpty()) continuation else "$previous\n$continuation"
            }
        }

        val dishName = fields["dishname"]?.takeIf { it.isNotBlank() } ?: return null
        return RecipeNote(
            id = fields["id"].orEmpty().ifBlank { UUID.randomUUID().toString() },
            dishName = dishName,
            difficulty = fields["difficulty"].orEmpty().ifBlank { "Medium" },
            cookingTime = fields["cookingtime"].orEmpty().ifBlank { "30 mins" },
            ingredients = sanitizeMultilineField(fields["ingredients"].orEmpty())
                .ifBlank { "No ingredients listed" },
            instructions = sanitizeMultilineField(fields["instructions"].orEmpty())
                .ifBlank { "No instructions provided" },
            timestamp = fields["timestamp"].orEmpty().ifBlank { currentDateIso() }
        )
    }

    private fun sanitizeMultilineField(value: String): String {
        if (value.isEmpty()) return value
        val builder = StringBuilder(value.length)
        var index = 0
        while (index < value.length) {
            val current = value[index]
            if (current == '\\' && index + 1 < value.length) {
                when (val next = value[index + 1]) {
                    'n' -> {
                        builder.append('\n')
                        index += 2
                        continue
                    }

                    'r' -> {
                        builder.append('\n')
                        index += 2
                        continue
                    }

                    't' -> {
                        builder.append('\t')
                        index += 2
                        continue
                    }

                    else -> builder.append(current)
                }
            } else {
                builder.append(current)
            }
            index++
        }
        return builder.toString()
    }

    private fun currentDateIso(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }
}
