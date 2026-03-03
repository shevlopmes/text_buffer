package org.example
import kotlin.test.*


class TerminalBufferTest {
    @Test
    fun InBoundsCheck(){
        val buffer = TerminalBuffer(10,40)
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
        val buffer = TerminalBuffer(80, 24)
        val (row, col) = buffer.getCursor()
        assertEquals(0, row)
        assertEquals(0, col)
    }

    @Test
    fun SetCursorValid() {
        val buffer = TerminalBuffer(80, 24)
        assertTrue(buffer.setCursor(21,32))
        val (row, col) = buffer.getCursor()
        assertEquals(32, col)
        assertEquals(21, row)
    }

    @Test
    fun SetCursorInvalid() {
        val buffer = TerminalBuffer(80, 24)
        assertFalse(buffer.setCursor(32,21))
    }

    @Test
    fun SetCursorValidEdgeCases() {
        val buffer = TerminalBuffer(10,40)
        assertTrue(buffer.setCursor(0,0))
        assertTrue(buffer.setCursor(0,9))
        assertTrue(buffer.setCursor(39,9))
        assertTrue(buffer.setCursor(39,0))
    }

    @Test
    fun SetCursorInvalidEdgeCases() {
        val buffer = TerminalBuffer(10,40)
        assertFalse(buffer.setCursor(-1,0))
        assertFalse(buffer.setCursor(0,-1))
        assertFalse(buffer.setCursor(39,-1))
        assertFalse(buffer.setCursor(39,10))
        assertFalse(buffer.setCursor(0,10))
        assertFalse(buffer.setCursor(40,0))
    }

    @Test
    fun MoveCursorValid() {
        val buffer = TerminalBuffer(80, 24)
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
        val buffer = TerminalBuffer(80, 24)
        assertTrue(buffer.setCursor(11,10))
        assertFalse(buffer.moveCursor('l',11))
        assertFalse(buffer.moveCursor('w',1))
    }

    @Test
    fun OverrideCharMovesCursor() {
        val buffer = TerminalBuffer(3,3)
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
        val buffer = TerminalBuffer(3,3)
        buffer.setCursor(1,1)
        buffer.overrideCharAtCursor('a')
        buffer.moveCursor('l',1)
        val ch = buffer.getCharAt()
        assertEquals('a', ch)
    }

    @Test
    fun GetCharAtTest() {
        val buffer = TerminalBuffer(3,3)
        buffer.setCursor(1,1)
        buffer.overrideCharAtCursor('a')
        val ch = buffer.getCharAt(1,1)
        assertEquals('a', ch)
    }

    @Test
    fun GetCharInvalidTest() {
        val buffer = TerminalBuffer(3,3)
        buffer.setCursor(1,1)
        buffer.overrideCharAtCursor('a')
        val ch = buffer.getCharAt(3,1)
        assertNull(ch)
        val ch1 = buffer.getCharAt(1,3)
        assertNull(ch1)
    }

    @Test
    fun GetLineAtTest() {
        val buffer = TerminalBuffer(3,3)
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
        val buffer = TerminalBuffer(3,3)
        buffer.fillCurrentLine('X')
        buffer.overrideCharAtCursor('a')
        val line = buffer.getLineAt()
        assertEquals("aXX", line)
    }

    @Test
    fun GetScreenTest() {
        val buffer = TerminalBuffer(3,3)
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
        val buffer = TerminalBuffer(3,3)
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
        val buffer = TerminalBuffer(5, 5)
        val newAttrs = Attributes(background = Colour.BLACK, foreground = Colour.WHITE)
        buffer.setAttributes(newAttrs)
        buffer.overrideCharAtCursor('A')
        buffer.moveCursor('l',1)
        val attrs = buffer.getAttributesAt()
        assertEquals(newAttrs, attrs)
    }

    @Test
    fun InsertEmptyLineTest() {
        val buffer = TerminalBuffer(3, 3)
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
        val buffer = TerminalBuffer(3, 3, maxScrollbackLines = 2)
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
        val buffer = TerminalBuffer(3, 3, maxScrollbackLines = 2)
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
        val buffer = TerminalBuffer(3, 3, maxScrollbackLines = 2)
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
        val buffer = TerminalBuffer(2, 2)
        val attrs = Attributes(background = Colour.BLACK, foreground = Colour.WHITE)
        buffer.setAttributes(attrs)
        buffer.overrideCharAtCursor('A')
        assertEquals(Cell('A', buffer.currentAttributes), buffer.getCell(0, 0))
        assertNull(buffer.getCell(2, 0))
    }

    @Test
    fun GetCellScrollbackTest() {
        val buffer = TerminalBuffer(3, 3)
        buffer.fillCurrentLine('X')
        buffer.moveCursor('d', 2)
        buffer.moveCursor('e')
        buffer.overrideCharAtCursor('Z')
        assertEquals('X', buffer.getCell(-1, 0)?.char)
    }

    @Test
    fun GetAttributesAtTest() {
        val buffer = TerminalBuffer(3, 3)
        val attrs = Attributes(bold = true)
        buffer.setAttributes(attrs)
        buffer.overrideCharAtCursor('B')
        buffer.moveCursor('l',1)
        assertEquals(attrs, buffer.getAttributesAt())
    }

    @Test
    fun OverrideTextAtCursorTest() {
        val buffer = TerminalBuffer(5, 2)
        buffer.overrideTextAtCursor("HELLO")
        assertEquals("HELLO", buffer.getLineAt(0))
    }

    @Test
    fun OverrideTextWrapTest() {
        val buffer = TerminalBuffer(3, 2)
        buffer.overrideTextAtCursor("abcde")
        assertEquals("abc", buffer.getLineAt(0))
        assertEquals("de ", buffer.getLineAt(1))
    }
}
