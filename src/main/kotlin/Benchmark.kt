package org.example

import kotlin.system.measureNanoTime

private fun usedMemoryBytes(): Long {
    val rt = Runtime.getRuntime()
    return rt.totalMemory() - rt.freeMemory()
}

private fun forceGc() {
    val before = System.nanoTime()
    System.gc()
    System.gc()
    // Wait until GC actually ran (heuristic)
    Thread.sleep(50)
}

data class BenchResult(
    val name: String,
    val avgTimeMs: Double,
    val minTimeMs: Double,
    val maxTimeMs: Double,
    val memDeltaKB: Long
)

/**
 * Runs [block] [warmup] times (ignored), then [iters] times measured.
 * Returns timing stats + rough heap delta.
 */
private fun bench(
    name: String,
    warmup: Int = 5,
    iters: Int = 20,
    block: () -> Unit
): BenchResult {
    // Warmup
    repeat(warmup) { block() }

    forceGc()
    val memBefore = usedMemoryBytes()

    val times = LongArray(iters)
    repeat(iters) { i ->
        times[i] = measureNanoTime { block() }
    }

    forceGc()
    val memAfter = usedMemoryBytes()

    val timesMs = times.map { it / 1_000_000.0 }
    return BenchResult(
        name = name,
        avgTimeMs = timesMs.average(),
        minTimeMs = timesMs.min(),
        maxTimeMs = timesMs.max(),
        memDeltaKB = (memAfter - memBefore) / 1024
    )
}

private fun printResult(r: BenchResult) {
    println(
        "%-55s | avg=%7.3f ms | min=%7.3f ms | max=%7.3f ms | memΔ=%+6d KB".format(
            r.name, r.avgTimeMs, r.minTimeMs, r.maxTimeMs, r.memDeltaKB
        )
    )
}


/** Single large block of text — no newlines, fills screen sequentially */
private fun patternSequentialFill(width: Int, height: Int) =
    "A".repeat(width * height)

/** Text with frequent newlines (code-like, many short lines) */
private fun patternShortLines(width: Int, height: Int): String {
    val lineLen = width / 4
    return buildString {
        repeat(height * 4) {
            append("x".repeat(lineLen))
            append("\n")
        }
    }
}

/** Simulates writing code: keywords, spaces, brackets */
private fun patternCodeLike(width: Int, height: Int): String {
    val line = "fun foo() { val x = bar(42); return x + 1 }"
        .padEnd(width).take(width)
    return buildString { repeat(height) { append(line) } }
}

/** Sparse writes: mostly spaces with occasional real chars */
private fun patternSparse(width: Int, height: Int): String {
    return buildString {
        repeat(height) { row ->
            repeat(width) { col ->
                append(if ((row * width + col) % 17 == 0) 'X' else ' ')
            }
        }
    }
}

/** Repeated inserts near the cursor (stress-tests insertCharAtCursor) */
private fun patternInsertMiddle(width: Int, height: Int): String =
    "Hello, World! ".repeat((width * height / 14) + 1)
        .take(width * height / 2)

// ─────────────────────────────────────────────────────────────────────────────
// Benchmark suites
// ─────────────────────────────────────────────────────────────────────────────

private data class ScreenSize(val width: Int, val height: Int) {
    override fun toString() = "${width}x${height}"
}

private val SCREEN_SIZES = listOf(
    ScreenSize(80, 24),    // classic VT100
    ScreenSize(220, 50),   // modern large terminal
)

private fun runSuite(size: ScreenSize): List<BenchResult> {
    val (w, h) = size
    val results = mutableListOf<BenchResult>()

    // ── overrideTextAtCursor ──────────────────────────────────────────────

    results += bench("[${size}] Naive  | overrideText | sequential") {
        val buf = TerminalBuffer(w, h)
        buf.overrideTextAtCursor(patternSequentialFill(w, h))
    }
    results += bench("[${size}] Opt    | overrideText | sequential") {
        val buf = TerminalBufferCartesianTree(w, h)
        buf.overrideTextAtCursor(patternSequentialFill(w, h))
    }

    results += bench("[${size}] Naive  | overrideText | short lines") {
        val buf = TerminalBuffer(w, h)
        buf.overrideTextAtCursor(patternShortLines(w, h))
    }
    results += bench("[${size}] Opt    | overrideText | short lines") {
        val buf = TerminalBufferCartesianTree(w, h)
        buf.overrideTextAtCursor(patternShortLines(w, h))
    }

    results += bench("[${size}] Naive  | overrideText | code-like") {
        val buf = TerminalBuffer(w, h)
        buf.overrideTextAtCursor(patternCodeLike(w, h))
    }
    results += bench("[${size}] Opt    | overrideText | code-like") {
        val buf = TerminalBufferCartesianTree(w, h)
        buf.overrideTextAtCursor(patternCodeLike(w, h))
    }

    results += bench("[${size}] Naive  | overrideText | sparse") {
        val buf = TerminalBuffer(w, h)
        buf.overrideTextAtCursor(patternSparse(w, h))
    }
    results += bench("[${size}] Opt    | overrideText | sparse") {
        val buf = TerminalBufferCartesianTree(w, h)
        buf.overrideTextAtCursor(patternSparse(w, h))
    }

    // ── insertTextAtCursor ────────────────────────────────────────────────

    results += bench("[${size}] Naive  | insertText   | sequential") {
        val buf = TerminalBuffer(w, h)
        buf.insertTextAtCursor(patternSequentialFill(w, h))
    }
    results += bench("[${size}] Opt    | insertText   | sequential") {
        val buf = TerminalBufferCartesianTree(w, h)
        buf.insertTextAtCursor(patternSequentialFill(w, h))
    }

    results += bench("[${size}] Naive  | insertText   | insert-middle") {
        val buf = TerminalBuffer(w, h)
        buf.insertTextAtCursor(patternInsertMiddle(w, h))
    }
    results += bench("[${size}] Opt    | insertText   | insert-middle") {
        val buf = TerminalBufferCartesianTree(w, h)
        buf.insertTextAtCursor(patternInsertMiddle(w, h))
    }

    // ── insertEmptyLine (scrollback stress) ──────────────────────────────

    results += bench("[${size}] Naive  | insertEmptyLine x${h * 5}") {
        val buf = TerminalBuffer(w, h)
        repeat(h * 5) { buf.insertEmptyLine() }
    }
    results += bench("[${size}] Opt    | insertEmptyLine x${h * 5}") {
        val buf = TerminalBufferCartesianTree(w, h)
        repeat(h * 5) { buf.insertEmptyLine() }
    }

    // ── getScreen / getScreenAndScrollBack ────────────────────────────────

    results += bench("[${size}] Naive  | getScreen (after fill)") {
        val buf = TerminalBuffer(w, h)
        buf.overrideTextAtCursor(patternCodeLike(w, h))
        buf.getScreen()
    }
    results += bench("[${size}] Opt    | getScreen (after fill)") {
        val buf = TerminalBufferCartesianTree(w, h)
        buf.overrideTextAtCursor(patternCodeLike(w, h))
        buf.getScreen()
    }

    results += bench("[${size}] Naive  | getScreenAndScrollBack") {
        val buf = TerminalBuffer(w, h, maxScrollbackLines = 200)
        repeat(h * 3) { buf.insertEmptyLine() }
        buf.getScreenAndScrollBack()
    }
    results += bench("[${size}] Opt    | getScreenAndScrollBack") {
        val buf = TerminalBufferCartesianTree(w, h, maxScrollbackLines = 200)
        repeat(h * 3) { buf.insertEmptyLine() }
        buf.getScreenAndScrollBack()
    }

    return results
}

// ─────────────────────────────────────────────────────────────────────────────
// Main
// ─────────────────────────────────────────────────────────────────────────────

fun main() {
    println("=".repeat(100))
    println("TerminalBuffer vs TerminalBufferOptimized — Benchmark")
    println("JVM: ${System.getProperty("java.version")}  |  Warmup=5  Iters=20")
    println("=".repeat(100))

    for (size in SCREEN_SIZES) {
        println("\n>>> Screen size: $size")
        println("-".repeat(100))
        val results = runSuite(size)
        results.forEach(::printResult)
    }

    println("\n" + "=".repeat(100))
    println("Done.")
}
