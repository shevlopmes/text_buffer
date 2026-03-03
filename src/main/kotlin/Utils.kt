package org.example

enum class Colour{
    DEFAULT,
    BLACK, RED, GREEN, YELLOW, BLUE, PURPLE, CYAN, WHITE,
    BRIGHT_BLACK, BRIGHT_RED, BRIGHT_GREEN, BRIGHT_YELLOW, BRIGHT_BLUE, BRIGHT_PURPLE, BRIGHT_CYAN, BRIGHT_WHITE
}

data class Cell(
    val char: Char? = null,
    val attributes: Attributes = Attributes()
)

data class Attributes(
    val foreground: Colour = Colour.DEFAULT,
    val background: Colour = Colour.DEFAULT,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false
)
