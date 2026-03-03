package org.example

class TerminalBuffer (
    val width: Int,
    val height: Int,
    val maxScrollbackLines: Int = 1000
) {
    private val screen: Array<Array<Cell>> = Array(height) {Array(width) { Cell() } }
    private val scrollback: ArrayDeque<Array<Cell>> = ArrayDeque()
    private var cursorCol : Int = 0
    private var cursorRow : Int = 0
    var currentAttributes : Attributes  = Attributes()

    fun getCursor(): Pair<Int, Int> {
        return Pair(cursorRow, cursorCol)
    }

    fun inBounds(row : Int, col : Int) : Boolean {
        return (col in 0..<width && row in 0..<height)
    }

    fun setCursor(row: Int, col: Int): Boolean {
        if (inBounds(row, col)) {
            cursorCol = col
            cursorRow = row
            return true
        }
        else {
            return false
        }
    }

    fun moveCursor(direction: Char, n: Int = 0): Boolean{
        when (direction){
            'u' -> return setCursor(cursorRow-n, cursorCol)
            'd' -> return setCursor(cursorRow+n, cursorCol)
            'l' -> return setCursor(cursorRow, cursorCol-n)
            'r' -> return setCursor(cursorRow, cursorCol+n)
            'b' -> return setCursor(cursorRow, 0)     // helper functional to move to the beginning of the line
            'e' -> return setCursor(cursorRow, width-1) // helper functional to move to the end of the line
            else -> return false
        }
    }

    fun getCell (row: Int, col: Int): Cell? {
        if (inBounds(row, col)) {
            return screen[row][col]
        }
        val modifiedRow = -row-1
        if (modifiedRow in 0..<scrollback.size && col in 0..<width) {
            return scrollback[modifiedRow][col]
        }
        return null

    }

    fun setAttributes(attributes: Attributes){
        currentAttributes = attributes
    }

    fun insertEmptyLine() {
        // Naive implementation
        // TODO: implement circular array to speed up the process
        val add_to_scrollback = screen[0].copyOf()
        for (row in 0 until height-1) {
            screen[row] = screen[row + 1].copyOf()
        }
        screen[height-1] = Array (width) { Cell() }
        scrollback.addFirst(add_to_scrollback)
        if (scrollback.size > maxScrollbackLines) {
            scrollback.removeLast()
        }
    }

    fun overrideCharAtCursor(ch: Char) {
        screen[cursorRow][cursorCol] = Cell(ch, currentAttributes)
        if (moveCursor('r',1)){ //if not at the end of the line
            return
        }
        if (moveCursor('d',1)) { //if at the end of the line, but not at the bottom of the screen
            moveCursor('b')
            return
        }
        insertEmptyLine()
        moveCursor('b')
    }

    fun overrideTextAtCursor(text: String) {
        for (ch in text.toCharArray()) {
            overrideCharAtCursor(ch)
        }
    }

    fun clearScreen() {
        for (row in 0 until height) {
            screen[row].fill(Cell())
        }
        setCursor(0,0)
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }

    fun fillCurrentLine (ch: Char? = null) { //to fill with empty, call with no parameters
        screen[cursorRow].fill(Cell(ch, currentAttributes))
    }

    fun getCharAt(row: Int = cursorRow, col: Int = cursorCol): Char? {
        if (inBounds(row, col)) {
            return screen[row][col].char
        }
        val modifiedRow = -row-1
        if (col in 0..<width && modifiedRow in  0..<scrollback.size) {
            return scrollback[modifiedRow][col].char
        }
        return null
    }

    fun getAttributesAt(row: Int = cursorRow, col: Int = cursorCol): Attributes? {
        if (inBounds(row, col)) {
            return screen[row][col].attributes
        }
        val modifiedRow = -row-1
        if (col in 0..<width && modifiedRow in 0..<scrollback.size) {
            return scrollback[modifiedRow][col].attributes
        }
        return null
    }

    fun getLineAt(row: Int = cursorRow): String? {
        if (row in 0..<height) {
            return buildString {
                for (col in 0 until width) {
                    val ch = screen[row][col].char ?: ' '
                    append(ch)
                }
            }
        }
        val modifiedRow = -row-1
        if (modifiedRow in 0..<scrollback.size) {
            return buildString {
                for (col in 0 until width) {
                    val ch = scrollback[modifiedRow][col].char ?: ' '
                    append(ch)
                }
            }
        }
        return null
    }

    fun getScreen(): String {
        return (0..<height).joinToString("\n") { row -> getLineAt(row)!! }
    }

    fun getScreenAndScrollBack(): String {
        return (-scrollback.size..<height).joinToString("\n") { row -> getLineAt(row)!! }
    }
}