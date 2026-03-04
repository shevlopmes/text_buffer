package org.example

class TerminalBufferCartesianTree (
    val width: Int,
    val height: Int,
    val maxScrollbackLines: Int = 1000
) {
    private val screen = CartesianTree()
    private val scrollback: ArrayDeque<Array<Cell>> = ArrayDeque()
    private var cursorCol : Int = 0
    private var cursorRow : Int = 0
    var currentAttributes : Attributes  = Attributes()

    private fun pos(row: Int, col: Int): Int = row * width + col

    init {
        repeat (width * height) {
            screen.insert(0,Cell() )
        }
    }

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
            return screen.get(pos(row, col))
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
        val addToScrollback = Array(width) {screen.get(pos(0,it))!!}
        scrollback.addFirst(addToScrollback)
        if (scrollback.size > maxScrollbackLines) {
            scrollback.removeLast()
        }
        val (toDelete, rest) = screen.split(screen.root,width)
        screen.root = rest
        repeat(width) {screen.insert((height-1)*width, Cell())}
    }

    fun overrideCharAtCursor(ch: Char) {
        screen.set(pos(cursorRow, cursorCol), Cell(ch, currentAttributes))
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
        repeat(width * height) { idx ->
            screen.set(idx, Cell())
        }
        setCursor(0,0)
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }

    fun fillCurrentLine (ch: Char? = null) { //to fill with empty, call with no parameters
        repeat(width) { idx ->
            screen.set(pos(cursorRow, idx), Cell(ch, currentAttributes))
        }
    }

    fun getCharAt(row: Int = cursorRow, col: Int = cursorCol): Char? {
        if (inBounds(row, col)) {
            return screen.get(pos(row,col))!!.char
        }
        val modifiedRow = -row-1
        if (col in 0..<width && modifiedRow in  0..<scrollback.size) {
            return scrollback[modifiedRow][col].char
        }
        return null
    }

    fun getAttributesAt(row: Int = cursorRow, col: Int = cursorCol): Attributes? {
        if (inBounds(row, col)) {
            return screen.get(pos(row,col))!!.attributes
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
                    val ch = screen.get(pos(row,col))!!.char ?: ' '
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
        val allChars = screen.getScreen()
        return allChars.chunked(width).take(height).joinToString("\n")
    }

    fun getScreenAndScrollBack(): String {
        val scrollbackString =  (-scrollback.size..<0).joinToString("\n") { row -> getLineAt(row)!! }
        val screenString = getScreen()
        return if (scrollbackString.isEmpty()) {
            screenString
        } else {
            scrollbackString + "\n" + screenString
        }
    }

    fun getNextEmpty(row: Int = cursorRow, col: Int = cursorCol): Pair<Int, Int>? {
        var currentRow = row
        var currentCol = col
        while (currentRow < height && currentCol < width) {
            screen.get(pos(currentRow, currentCol))!!.char ?: return Pair(currentRow, currentCol)
            currentCol++
            if (currentCol == width){
                currentCol = 0
                currentRow++
            }
        }
        return null
    }

    fun moveCursorOneStep(){
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

    fun insertCharAtCursor(ch: Char) {
        val pair = getNextEmpty()
        if (pair != null) {
            val (row, col) = pair
            screen.delete(pos(row,col))
            screen.insert(pos(cursorRow,cursorCol), Cell(ch, currentAttributes))
            moveCursorOneStep()
        }
        else {
            //if cursor is not on the first line, we need to append a new empty line and shift all by one character,
            //like in case where next non-empty cell exists
            if (cursorRow > 0) {
                insertEmptyLine()
                cursorRow--
                val row = height - 1
                val col = width - 1
                screen.delete(pos(row,col))
                screen.insert(pos(cursorRow,cursorCol), Cell(ch, currentAttributes))
                moveCursorOneStep()
            }
            //However, if the whole screen is occupied and we want to insert in the first line, what we insert
            // immediately goes to scrollback.
            else {
                //insert empty line and shift all what's left on the screen
                insertEmptyLine()
                val row= height - 1
                val col = width - 1
                screen.delete(pos(row,col))
                //manually change the first line of the scrollback
                screen.insert(0,scrollback[0][width-1].copy())
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
}