package org.example
import kotlin.random.Random
import kotlin.test.*
class CartesianTreeTest {
    @Test
    fun insertAndGet() {
        val t = CartesianTree()
        t.insert(0, Cell('A'))
        t.insert(1, Cell('B'))
        t.insert(2, Cell('C'))

        assertEquals('A', t.get(0)?.char)
        assertEquals('B', t.get(1)?.char)
        assertEquals('C', t.get(2)?.char)
    }

    @Test
    fun insertInMiddle() {
        val t = CartesianTree()
        t.insert(0, Cell('A'))
        t.insert(1, Cell('C'))
        t.insert(1, Cell('B'))

        assertEquals("ABC", t.getScreen())
    }

    @Test
    fun deleteElement() {
        val t = CartesianTree()
        "ABCDE".forEachIndexed { i, c ->
            t.insert(i, Cell(c))
        }

        t.delete(2)

        assertEquals("ABDE", t.getScreen())
    }

    @Test
    fun setElement() {
        val t = CartesianTree()
        "ABC".forEachIndexed { i, c ->
            t.insert(i, Cell(c))
        }

        t.set(1, Cell('X'))

        assertEquals("AXC", t.getScreen())
    }

    @Test
    fun splitAndMergeIdentity() {
        val t = CartesianTree()
        "ABCDE".forEachIndexed { i, c ->
            t.insert(i, Cell(c))
        }

        val (l, r) = t.split(t.root, 2)
        val merged = t.merge(l, r)

        val t2 = CartesianTree()
        t2.root = merged

        assertEquals("ABCDE", t2.getScreen())
    }

    @Test
    fun sizeInvariant() {
        val t = CartesianTree()
        repeat(100) { i ->
            t.insert(i, Cell(('a'.code + i % 26).toChar()))
        }

        assertEquals(100, t.getSize(t.root))
        assertEquals(100, t.root?.size)
    }

    @Test
    fun randomOperationsMatchList() {
        val t = CartesianTree()
        val list = mutableListOf<Char?>()
        val rnd = Random(1)

        repeat(1000) {
            when (rnd.nextInt(3)) {
                0 -> { // insert
                    val idx = if (list.isEmpty()) 0 else rnd.nextInt(list.size + 1)
                    val c = ('A'.code + rnd.nextInt(26)).toChar()
                    list.add(idx, c)
                    t.insert(idx, Cell(c))
                }
                1 -> if (list.isNotEmpty()) { // delete
                    val idx = rnd.nextInt(list.size)
                    list.removeAt(idx)
                    t.delete(idx)
                }
                2 -> if (list.isNotEmpty()) { // get
                    val idx = rnd.nextInt(list.size)
                    assertEquals(list[idx], t.get(idx)?.char)
                }
            }
            assertEquals(list.joinToString("") , t.getScreen())
            assertEquals(list.size, t.getSize(t.root))
        }
    }
}