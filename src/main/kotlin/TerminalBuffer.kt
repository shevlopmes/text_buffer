package org.example

class TerminalBuffer (
    val width: Int,
    val height: Int,
    val maxScrollbackLines: Int = 1000
) {
    private val screen: Array<Array<Cell>> = Array(height) {Array(width) { Cell() } }
    private val scrollback: MutableList<Array<Cell>> = mutableListOf()
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

    fun moveCursor(direction: Char, n: Int): Boolean{
        when (direction){
            'u' -> return setCursor(cursorRow-n, cursorCol)
            'd' -> return setCursor(cursorRow+n, cursorCol)
            'l' -> return setCursor(cursorRow, cursorCol-n)
            'r' -> return setCursor(cursorRow, cursorCol+n)
            else -> return false
        }
    }

    fun getCell (row: Int, col: Int): Cell? {
        if (inBounds(row, col)) {
            return screen[row][col]
        }
        else{
            return null
        }
    }
}