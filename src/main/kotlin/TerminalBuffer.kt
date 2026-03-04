package org.example

class TerminalBuffer (
    var width: Int,
    var height: Int,
    val maxScrollbackLines: Int = 1000
) {
    private var screen: Array<Array<Cell>> = Array(height) {Array(width) { Cell() } }
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
        val addToScrollback = screen[0].copyOf()
        for (row in 0 until height-1) {
            screen[row] = screen[row + 1].copyOf()
        }
        screen[height-1] = Array (width) { Cell() }
        scrollback.addFirst(addToScrollback)
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

    fun getNextEmpty(row: Int = cursorRow, col: Int = cursorCol): Pair<Int, Int>? {
        var currentRow = row
        var currentCol = col
        while (currentRow < height && currentCol < width) {
            screen[currentRow][currentCol].char ?: return Pair(currentRow, currentCol)
            currentCol++
            if (currentCol == width){
                currentCol = 0
                currentRow++
            }
        }
        return null
    }

    fun insertCharAtCursor(ch: Char) {
        val pair = getNextEmpty()
        if (pair != null) {
            val (row, col) = pair
            var currentRow = row
            var currentCol = col
            while (currentRow != cursorRow || currentCol != cursorCol) {
                var nextCol = currentCol-1
                var nextRow = currentRow
                if (nextCol == -1){
                    nextCol = width-1
                    nextRow--
                }
                screen[currentRow][currentCol] = screen[nextRow][nextCol].copy()
                currentRow = nextRow
                currentCol = nextCol
            }
            overrideCharAtCursor(ch)
        }
        else {
            //if cursor is not on the first line, we need to append a new empty line and shift all by one character,
            //like in case where next non-empty cell exists
            if (cursorRow > 0) {
                insertEmptyLine()
                cursorRow--
                var currentRow = height - 1
                var currentCol = 0
                while (currentRow != cursorRow || currentCol != cursorCol) {
                    var nextCol = currentCol - 1
                    var nextRow = currentRow
                    if (nextCol == -1) {
                        nextCol = width - 1
                        nextRow--
                    }
                    screen[currentRow][currentCol] = screen[nextRow][nextCol].copy()
                    currentRow = nextRow
                    currentCol = nextCol
                }
                overrideCharAtCursor(ch)
            }
            //However, if the whole screen is occupied and we want to insert in the first line, what we insert
            // immediately goes to scrollback.
            else {
                //insert empty line and shift all what's left on the screen
                insertEmptyLine()
                var currentRow = height - 1
                var currentCol = 0
                while (currentRow != 0 || currentCol != 0) {
                    var nextCol = currentCol - 1
                    var nextRow = currentRow
                    if (nextCol == -1) {
                        nextCol = width - 1
                        nextRow--
                    }
                    screen[currentRow][currentCol] = screen[nextRow][nextCol].copy()
                    currentRow = nextRow
                    currentCol = nextCol
                }
                //manually change the first line of the scrollback
                screen[0][0] = scrollback[0][width-1].copy()
                for (idx in width-1 downTo cursorCol+1) {
                    scrollback[0][idx] = scrollback[0][idx-1].copy()
                }
                scrollback[0][cursorCol] = Cell(ch, currentAttributes)
                //idk where to put cursor now, so just put it in the beginning of the screen
                setCursor(0,0)
            }
        }
    }

    fun insertTextAtCursor(s: String) {
        for (ch in s){
            insertCharAtCursor(ch)
        }
    }

    fun resize(newWidth: Int, newHeight: Int) {
        val cells = buildList {
            for (row in -scrollback.size until height) {
                for (col in 0 until width) {
                    add(getCell(row, col)!!)
                }
            }
        }

        val lastIdx = cells.indexOfLast { it.char != null }
        val content = if (lastIdx == -1) emptyList() else cells.subList(0, lastIdx + 1)

        width = newWidth
        height = newHeight
        screen = Array(height) { Array(width) { Cell() } }
        scrollback.clear()
        setCursor(0, 0)
        var ptr = content.size - 1
        for (row in height-1 downTo 0) {
            for (col in width-1 downTo 0) {
                if (ptr < 0){
                    screen[row][col] = Cell()
                } else {
                    screen[row][col] = content[ptr].copy()
                    ptr--
                }
            }
        }
        while (ptr > 0 && scrollback.size < maxScrollbackLines){
            scrollback.addLast(Array (width) {it -> if (ptr<0)
            {Cell()}
            else {content[ptr--]
            }}.reversedArray())
        }
    }
}