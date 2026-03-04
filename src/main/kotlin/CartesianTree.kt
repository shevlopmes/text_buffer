package org.example
import kotlin.random.Random

data class Node(
    val cell: Cell,
    val priority: Int = Random.nextInt(),
    var size: Int = 1,
    var left: Node? = null,
    var right: Node? = null
)
class CartesianTree {
    var root: Node? = null

    fun getSize(node: Node?): Int = node?.size ?: 0

    private fun updateSize(node: Node?) {
        node?.size = 1 + getSize(node.left) + getSize(node.right)
    }

    fun split(node: Node?, key: Int): Pair<Node?, Node?> {
        if (node == null) return Pair(null, null)
        val curKey = getSize(node.left)
        return if (key <= curKey) {
            val (L,R) = split(node.left,key)
            node.left = R
            updateSize(node)
            Pair(L, node)
        } else {
            val (L, R) = split(node.right,key - curKey - 1)
            node.right = L
            updateSize(node)
            Pair(node,R)
        }
    }

    fun merge(L: Node?, R: Node?): Node? {
        if (L == null) return R
        if (R == null) return L
        return if (L.priority > R.priority) {
            L.right = merge(L.right,R)
            updateSize(L)
            L
        } else {
            R.left = merge(L, R.left)
            updateSize(R)
            R
        }
    }

    fun insert(index: Int, cell: Cell) {
        val (l, r) = split(root, index)
        val newNode = Node(cell)
        root = merge(merge(l, newNode), r)
    }

    fun set(index: Int, cell: Cell) {
        delete(index)
        insert(index, cell)
    }

    fun delete(index: Int) {
        val (l, mr) = split(root, index)
        val (_, r) = split(mr, 1)
        root = merge(l, r)
    }

    fun get(index: Int): Cell? {
        val (l, mr) = split(root, index)
        val (m, r) = split(mr, 1)
        val result = m?.cell
        root = merge(l, merge(m, r))
        return result
    }

    fun getScreen(): String {
        val sb = StringBuilder()
        traverse(root, sb)
        return sb.toString()
    }

    private fun traverse(node: Node?, sb: StringBuilder) {
        if (node == null) return

        traverse(node.left, sb)
        sb.append(node.cell.char ?: ' ')
        traverse(node.right, sb)
    }
}