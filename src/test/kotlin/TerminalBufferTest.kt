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
    }

    @Test
    fun MoveCursorInvalid() {
        val buffer = TerminalBuffer(80, 24)
        assertTrue(buffer.setCursor(11,10))
        assertFalse(buffer.moveCursor('l',11))
        assertFalse(buffer.moveCursor('w',1))
    }
}
