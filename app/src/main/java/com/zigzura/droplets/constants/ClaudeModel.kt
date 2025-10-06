package com.zigzura.droplets.constants

object ClaudeModel {
    val models = listOf(
        "claude-sonnet-4-5-20250929",
        "claude-sonnet-4-20250514",
        "claude-3-7-sonnet-latest",
        "claude-3-5-haiku-latest",
        "claude-3-haiku-20240307"
    )

    val modelCosts = mapOf(
        "claude-sonnet-4-5-20250929" to "~\$3 / 1M input + \$15 / 1M output",
        "claude-sonnet-4-20250514" to "~\$3 / 1M input + \$15 / 1M output",
        "claude-3-7-sonnet-latest" to "~\$3 / 1M input + \$15 / 1M output",
        "claude-3-5-haiku-latest" to "~\$0.80 / 1M input + ~\$4.00 / 1M output",
        "claude-3-haiku-20240307" to "~\$0.25 / 1M input + \$1.25 / 1M output"
    )

    fun getDefaultModel(): String = "claude-3-haiku-20240307"
}