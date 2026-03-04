package org.example
import kotlin.test.*


class TerminalBufferCartesianTreeTest {
    @Test
    fun InBoundsCheck(){
        val buffer = TerminalBufferCartesianTree(10,40)
        assertFalse(buffer.inBounds(-1,0))
        assertFalse(buffer.inBounds(0,-1))
        assertFalse(buffer.inBounds(39,-1))
        assertFalse(buffer.inBounds(39,10))
        assertFalse(buffer.inBounds(0,10))
        assertFalse(buffer.inBounds(40,0))
        assertTrue(buffer.inBounds(0,0))
        assertTrue(buffer.inBounds(0,9))
        assertTrue(buffer.inBounds(39,9))
        assertTrue(buffer.inBounds(39,0))
    }
    @Test
    fun InitialCursor() {
        val buffer = TerminalBufferCartesianTree(80, 24)
        val (row, col) = buffer.getCursor()
        assertEquals(0, row)
        assertEquals(0, col)
    }

    @Test
    fun SetCursorValid() {
        val buffer = TerminalBufferCartesianTree(80, 24)
        assertTrue(buffer.setCursor(21,32))
        val (row, col) = buffer.getCursor()
        assertEquals(32, col)
        assertEquals(21, row)
    }

    @Test
    fun SetCursorInvalid() {
        val buffer = TerminalBufferCartesianTree(80, 24)
        assertFalse(buffer.setCursor(32,21))
    }

    @Test
    fun SetCursorValidEdgeCases() {
        val buffer = TerminalBufferCartesianTree(10,40)
        assertTrue(buffer.setCursor(0,0))
        assertTrue(buffer.setCursor(0,9))
        assertTrue(buffer.setCursor(39,9))
        assertTrue(buffer.setCursor(39,0))
    }

    @Test
    fun SetCursorInvalidEdgeCases() {
        val buffer = TerminalBufferCartesianTree(10,40)
        assertFalse(buffer.setCursor(-1,0))
        assertFalse(buffer.setCursor(0,-1))
        assertFalse(buffer.setCursor(39,-1))
        assertFalse(buffer.setCursor(39,10))
        assertFalse(buffer.setCursor(0,10))
        assertFalse(buffer.setCursor(40,0))
    }

    @Test
    fun MoveCursorValid() {
        val buffer = TerminalBufferCartesianTree(80, 24)
        assertTrue(buffer.setCursor(11,10))
        assertTrue(buffer.moveCursor('u',11))
        val (row, col) = buffer.getCursor()
        assertEquals(0, row)
        assertEquals(10, col)
        assertTrue(buffer.moveCursor('r',56))
        val (row1, col1) = buffer.getCursor()
        assertEquals(0, row1)
        assertEquals(66, col1)
        assertTrue(buffer.moveCursor('d',21))
        val (row2, col2) = buffer.getCursor()
        assertEquals(21, row2)
        assertEquals(66, col2)
        assertTrue(buffer.moveCursor('l',66))
        val (row3, col3) = buffer.getCursor()
        assertEquals(21, row3)
        assertEquals(0, col3)
        assertTrue(buffer.moveCursor('e'))
        val (row4, col4) = buffer.getCursor()
        assertEquals(21, row4)
        assertEquals(79, col4)
        assertTrue(buffer.moveCursor('b'))
        val (row5, col5) = buffer.getCursor()
        assertEquals(21, row5)
        assertEquals(0, col5)
    }

    @Test
    fun MoveCursorInvalid() {
        val buffer = TerminalBufferCartesianTree(80, 24)
        assertTrue(buffer.setCursor(11,10))
        assertFalse(buffer.moveCursor('l',11))
        assertFalse(buffer.moveCursor('w',1))
    }

    @Test
    fun OverrideCharMovesCursor() {
        val buffer = TerminalBufferCartesianTree(3,3)
        buffer.setCursor(1,1)
        buffer.overrideCharAtCursor('>')
        val (row, col) = buffer.getCursor()
        assertEquals(1, row)
        assertEquals(2, col)
        buffer.overrideCharAtCursor('v')
        val (row1, col1) = buffer.getCursor()
        assertEquals(2, row1)
        assertEquals(0, col1)
        buffer.setCursor(2,2)
        buffer.overrideCharAtCursor('!')
        val (row2, col2) = buffer.getCursor()
        assertEquals(2, row2)
        assertEquals(0, col2)
    }

    @Test
    fun GetCharAtCursorTest() {
        val buffer = TerminalBufferCartesianTree(3,3)
        buffer.setCursor(1,1)
        buffer.overrideCharAtCursor('a')
        buffer.moveCursor('l',1)
        val ch = buffer.getCharAt()
        assertEquals('a', ch)
    }

    @Test
    fun GetCharAtTest() {
        val buffer = TerminalBufferCartesianTree(3,3)
        buffer.setCursor(1,1)
        buffer.overrideCharAtCursor('a')
        val ch = buffer.getCharAt(1,1)
        assertEquals('a', ch)
    }

    @Test
    fun GetCharInvalidTest() {
        val buffer = TerminalBufferCartesianTree(3,3)
        buffer.setCursor(1,1)
        buffer.overrideCharAtCursor('a')
        val ch = buffer.getCharAt(3,1)
        assertNull(ch)
        val ch1 = buffer.getCharAt(1,3)
        assertNull(ch1)
    }

    @Test
    fun GetLineAtTest() {
        val buffer = TerminalBufferCartesianTree(3,3)
        buffer.setCursor(1,1)
        buffer.overrideCharAtCursor('a')
        buffer.overrideCharAtCursor('b')
        buffer.overrideCharAtCursor('c')
        val line = buffer.getLineAt()
        assertEquals("c  ", line)
        val line1 = buffer.getLineAt(1)
        assertEquals(" ab", line1)
    }

    @Test
    fun FillLineAtCursorTest() {
        val buffer = TerminalBufferCartesianTree(3,3)
        buffer.fillCurrentLine('X')
        buffer.overrideCharAtCursor('a')
        val line = buffer.getLineAt()
        assertEquals("aXX", line)
    }

    @Test
    fun GetScreenTest() {
        val buffer = TerminalBufferCartesianTree(3,3)
        buffer.fillCurrentLine('X')
        buffer.moveCursor('d',1)
        buffer.overrideCharAtCursor('a')
        buffer.moveCursor('d',1)
        buffer.overrideCharAtCursor('b')
        val screen = buffer.getScreen()
        assertEquals("XXX\na  \n b ", screen)
    }

    @Test
    fun GetScreenAndScrollbackTest(){
        val buffer = TerminalBufferCartesianTree(3,3)
        buffer.fillCurrentLine('X')
        buffer.moveCursor('d',1)
        buffer.overrideCharAtCursor('a')
        buffer.moveCursor('d',1)
        buffer.overrideCharAtCursor('b')
        buffer.overrideCharAtCursor('c')
        buffer.overrideCharAtCursor('d')
        val all = buffer.getScreenAndScrollBack()
        assertEquals("XXX\na  \n bc\nd  ", all)
        val screen = buffer.getScreen()
        assertEquals("a  \n bc\nd  ", screen)
    }

    @Test
    fun SetAttributesTest() {
        val buffer = TerminalBufferCartesianTree(5, 5)
        val newAttrs = Attributes(background = Colour.BLACK, foreground = Colour.WHITE)
        buffer.setAttributes(newAttrs)
        buffer.overrideCharAtCursor('A')
        buffer.moveCursor('l',1)
        val attrs = buffer.getAttributesAt()
        assertEquals(newAttrs, attrs)
    }

    @Test
    fun InsertEmptyLineTest() {
        val buffer = TerminalBufferCartesianTree(3, 3)
        buffer.fillCurrentLine('1')
        buffer.moveCursor('d', 1)
        buffer.fillCurrentLine('2')
        buffer.moveCursor('d', 1)
        buffer.fillCurrentLine('3')
        buffer.insertEmptyLine()
        assertEquals("222", buffer.getLineAt(0))
        assertEquals("333", buffer.getLineAt(1))
        assertEquals("   ", buffer.getLineAt(2))
    }

    @Test
    fun InsertEmptyLineScrollbackTest() {
        val buffer = TerminalBufferCartesianTree(3, 3, maxScrollbackLines = 2)
        buffer.fillCurrentLine('X')
        buffer.moveCursor('d', 2)
        buffer.moveCursor('e')
        buffer.overrideCharAtCursor('Z')
        buffer.moveCursor('e')
        buffer.overrideCharAtCursor('Z')
        assertNull(buffer.getLineAt(-3))
        assertEquals("XXX\n   \n  Z\n  Z\n   ",buffer.getScreenAndScrollBack())
        buffer.overrideTextAtCursor("ABC")
        assertEquals("   \n  Z\n  Z\nABC\n   ",buffer.getScreenAndScrollBack())
    }

    @Test
    fun ClearScreenTest() {
        val buffer = TerminalBufferCartesianTree(3, 3, maxScrollbackLines = 2)
        buffer.fillCurrentLine('X')
        buffer.moveCursor('d', 2)
        buffer.moveCursor('e')
        buffer.overrideCharAtCursor('Z')
        buffer.moveCursor('e')
        buffer.overrideCharAtCursor('Z')
        buffer.clearScreen()
        assertEquals("XXX\n   \n   \n   \n   ", buffer.getScreenAndScrollBack())
        val (row, col) = buffer.getCursor()
        assertEquals(0, row)
        assertEquals(0, col)
        buffer.overrideTextAtCursor("ABC")
        assertEquals("XXX\n   \nABC\n   \n   ", buffer.getScreenAndScrollBack())
    }

    @Test
    fun ClearAllTest() {
        val buffer = TerminalBufferCartesianTree(3, 3, maxScrollbackLines = 2)
        buffer.fillCurrentLine('X')
        buffer.moveCursor('d', 2)
        buffer.moveCursor('e')
        buffer.overrideCharAtCursor('Z')
        buffer.moveCursor('e')
        buffer.overrideCharAtCursor('Z')
        buffer.clearAll()
        assertEquals("   \n   \n   ", buffer.getScreenAndScrollBack())
        val (row, col) = buffer.getCursor()
        assertEquals(0, row)
        assertEquals(0, col)
        buffer.overrideTextAtCursor("ABC")
        assertEquals("ABC\n   \n   ", buffer.getScreenAndScrollBack())
    }

    @Test
    fun GetCellTest() {
        val buffer = TerminalBufferCartesianTree(2, 2)
        val attrs = Attributes(background = Colour.BLACK, foreground = Colour.WHITE)
        buffer.setAttributes(attrs)
        buffer.overrideCharAtCursor('A')
        assertEquals(Cell('A', buffer.currentAttributes), buffer.getCell(0, 0))
        assertNull(buffer.getCell(2, 0))
    }

    @Test
    fun GetCellScrollbackTest() {
        val buffer = TerminalBufferCartesianTree(3, 3)
        buffer.fillCurrentLine('X')
        buffer.moveCursor('d', 2)
        buffer.moveCursor('e')
        buffer.overrideCharAtCursor('Z')
        assertEquals('X', buffer.getCell(-1, 0)?.char)
    }

    @Test
    fun GetAttributesAtTest() {
        val buffer = TerminalBufferCartesianTree(3, 3)
        val attrs = Attributes(bold = true)
        buffer.setAttributes(attrs)
        buffer.overrideCharAtCursor('B')
        buffer.moveCursor('l',1)
        assertEquals(attrs, buffer.getAttributesAt())
    }

    @Test
    fun OverrideTextAtCursorTest() {
        val buffer = TerminalBufferCartesianTree(5, 2)
        buffer.overrideTextAtCursor("HELLO")
        assertEquals("HELLO", buffer.getLineAt(0))
    }

    @Test
    fun OverrideTextWrapTest() {
        val buffer = TerminalBufferCartesianTree(3, 2)
        buffer.overrideTextAtCursor("abcde")
        assertEquals("abc", buffer.getLineAt(0))
        assertEquals("de ", buffer.getLineAt(1))
    }

    @Test
    fun GetNextEmptySimple() {
        val buffer = TerminalBufferCartesianTree(3, 2)
        val p1 = buffer.getNextEmpty()
        assertNotNull(p1)
        assertEquals(0, p1.first)
        assertEquals(0, p1.second)
        buffer.overrideCharAtCursor('A')
        val p2 = buffer.getNextEmpty()
        assertNotNull(p2)
        assertEquals(0, p2.first)
        assertEquals(1, p2.second)
        buffer.setCursor(0,2)
        buffer.overrideCharAtCursor('B')
        val p3 = buffer.getNextEmpty(0, 2)
        assertNotNull(p3)
        assertEquals(1, p3.first)
        assertEquals(0, p3.second)
    }

    @Test
    fun GetNextEmptyFullLine() {
        val buffer = TerminalBufferCartesianTree(2, 3)
        buffer.overrideTextAtCursor("AB")
        val p = buffer.getNextEmpty(0, 0)
        assertNotNull(p)
        assertEquals(1, p.first)
        assertEquals(0, p.second)
    }

    @Test
    fun GetNextEmptyNoEmpty() {
        val buffer = TerminalBufferCartesianTree(2, 2)
        buffer.insertTextAtCursor("BCD")
        buffer.setCursor(0,0)
        buffer.insertCharAtCursor('A')
        val p = buffer.getNextEmpty(0, 0)
        assertNull(p)
    }

    @Test
    fun InsertCharWhenNextEmptyExistsSameLine() {
        val buffer = TerminalBufferCartesianTree(5, 1)
        buffer.overrideTextAtCursor("A_BC")
        buffer.setCursor(0, 1)
        buffer.insertCharAtCursor('X')
        val line = buffer.getLineAt(0)
        assertNotNull(line)
        assertEquals("AX_BC", line)
    }

    @Test
    fun InsertCharNextEmptyNextLine() {
        val buffer = TerminalBufferCartesianTree(2, 3)
        buffer.overrideTextAtCursor("AB")
        buffer.overrideTextAtCursor("CD")
        buffer.setCursor(0,1)
        buffer.insertCharAtCursor('X')
        val screen = buffer.getScreen()
        assertEquals("AX\nBC\nD ", screen)
    }

    @Test
    fun insertEmptyLineMovesFirstRowToScrollback() {
        val buffer = TerminalBufferCartesianTree(3, 2)
        buffer.insertTextAtCursor("BCDEF")
        buffer.setCursor(0,0)
        buffer.insertCharAtCursor('A')
        buffer.insertEmptyLine()

        assertEquals("DEF\n   ", buffer.getScreen())
        assertEquals("ABC\nDEF\n   ", buffer.getScreenAndScrollBack())
    }

    @Test
    fun InsertCharNoNextEmptyCursorNotFirstLine() {
        val buffer = TerminalBufferCartesianTree(3, 2)
        buffer.insertTextAtCursor("BCDEF")
        buffer.setCursor(0,0)
        buffer.insertCharAtCursor('A')
        buffer.setCursor(1, 1)
        buffer.insertCharAtCursor('X')
        val screen = buffer.getScreen()
        assertEquals("DXE\nF  ", screen)
        val all = buffer.getScreenAndScrollBack()
        assertEquals("ABC\nDXE\nF  ", all)
    }

    @Test
    fun InsertCharNoNextEmptyCursorFirstLine() {
        val buffer = TerminalBufferCartesianTree(3, 2)
        buffer.insertTextAtCursor("BCDEF")
        buffer.setCursor(0,0)
        buffer.insertCharAtCursor('A')
        buffer.setCursor(0, 1)
        buffer.insertCharAtCursor('X')
        val screen = buffer.getScreen()
        assertEquals("CDE\nF  ", screen)
        val all = buffer.getScreenAndScrollBack()
        assertEquals("AXB\nCDE\nF  ", all)
    }

    @Test
    fun cursorAlwaysInBoundsAfterMove() {
        val buffer = TerminalBufferCartesianTree(4, 3)

        repeat(100) {
            buffer.moveCursorOneStep()
            val (r, c) = buffer.getCursor()
            assertTrue(r in 0 until 3)
            assertTrue(c in 0 until 4)
        }
    }

    @Test
    fun singleCellScreenAlwaysScrolls() {
        val buffer = TerminalBufferCartesianTree(1, 1)

        buffer.insertTextAtCursor("A")

        assertEquals(Pair(0, 0), buffer.getCursor())
        assertEquals(" ", buffer.getScreen())
        assertEquals("A\n ", buffer.getScreenAndScrollBack())
    }

    @Test
    fun moveRightInsideLine() {
        val buffer = TerminalBufferCartesianTree(3, 2)

        buffer.setCursor(0, 1)
        buffer.moveCursorOneStep()

        assertEquals(Pair(0, 2), buffer.getCursor())
    }

    @Test
    fun moveToNextLineFromEndOfLine() {
        val buffer = TerminalBufferCartesianTree(3, 2)

        buffer.setCursor(0, 2)
        buffer.moveCursorOneStep()

        assertEquals(Pair(1, 0), buffer.getCursor())
    }

    @Test
    fun SetBoldTest() {
        val buffer = TerminalBuffer(5, 5)
        buffer.setBold()
        buffer.overrideCharAtCursor('A')
        buffer.moveCursor('l', 1)
        assertTrue(buffer.getAttributesAt()!!.bold)
    }

    @Test
    fun SetBoldFalseTest() {
        val buffer = TerminalBuffer(5, 5)
        buffer.setBold()
        buffer.setBold(false)
        buffer.overrideCharAtCursor('A')
        buffer.moveCursor('l', 1)
        assertFalse(buffer.getAttributesAt()!!.bold)
    }

    @Test
    fun SetItalicTest() {
        val buffer = TerminalBuffer(5, 5)
        buffer.setItalic()
        buffer.overrideCharAtCursor('B')
        buffer.moveCursor('l', 1)
        assertTrue(buffer.getAttributesAt()!!.italic)
    }

    @Test
    fun SetUnderlineTest() {
        val buffer = TerminalBuffer(5, 5)
        buffer.setUnderline()
        buffer.overrideCharAtCursor('C')
        buffer.moveCursor('l', 1)
        assertTrue(buffer.getAttributesAt()!!.underline)
    }

    @Test
    fun SetForegroundTest() {
        val buffer = TerminalBuffer(5, 5)
        buffer.setForeground(Colour.RED)
        buffer.overrideCharAtCursor('D')
        buffer.moveCursor('l', 1)
        assertEquals(Colour.RED, buffer.getAttributesAt()!!.foreground)
    }

    @Test
    fun SetBackgroundTest() {
        val buffer = TerminalBuffer(5, 5)
        buffer.setBackground(Colour.BLUE)
        buffer.overrideCharAtCursor('E')
        buffer.moveCursor('l', 1)
        assertEquals(Colour.BLUE, buffer.getAttributesAt()!!.background)
    }

    @Test
    fun ResetAttributesTest() {
        val buffer = TerminalBuffer(5, 5)
        buffer.setBold()
        buffer.setItalic()
        buffer.setForeground(Colour.GREEN)
        buffer.resetAttributes()
        buffer.overrideCharAtCursor('F')
        buffer.moveCursor('l', 1)
        assertEquals(Attributes(), buffer.getAttributesAt())
    }

    @Test
    fun AttributesDoNotAffectOtherCells() {
        val buffer = TerminalBuffer(5, 5)
        buffer.setBold()
        buffer.overrideCharAtCursor('X')  // (0,0) — bold
        buffer.resetAttributes()
        buffer.overrideCharAtCursor('Y')  // (0,1) — default
        assertFalse(buffer.getAttributesAt(0, 1)!!.bold)
        assertTrue(buffer.getAttributesAt(0, 0)!!.bold)
    }

    @Test
    fun CombinedAttributesTest() {
        val buffer = TerminalBuffer(5, 5)
        buffer.setBold()
        buffer.setItalic()
        buffer.setUnderline()
        buffer.setForeground(Colour.CYAN)
        buffer.setBackground(Colour.BLACK)
        buffer.overrideCharAtCursor('Z')
        buffer.moveCursor('l', 1)
        val attrs = buffer.getAttributesAt()!!
        assertTrue(attrs.bold)
        assertTrue(attrs.italic)
        assertTrue(attrs.underline)
        assertEquals(Colour.CYAN, attrs.foreground)
        assertEquals(Colour.BLACK, attrs.background)
    }

}
