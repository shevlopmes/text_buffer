# Terminal Text Buffer

I implemented the `TerminalBuffer` class according to the task requirements.

I defined a `Cell` data class holding a character and an `Attributes` data class with fields for colors and font styling (bold, italic, underline). Both can be found in `Utils.kt`.

In the main class, the screen is represented as a 2D array and the scrollback as a deque of arrays, which allows efficient insertion at the front and removal from the back.

The cursor is stored as a pair of integers. `setCursor` moves it to an absolute position and `moveCursor` moves it in a given direction, including `'b'` (beginning of line) and `'e'` (end of line). Both functions perform bounds checking and return a `Boolean` indicating whether the move was valid — this guarantees the cursor never leaves the screen.

`insertEmptyLine` shifts all existing lines one row up, pushes the top line into the scrollback, and appends a new empty line at the bottom. The scrollback size is capped at `maxScrollbackLines`.

`overrideCharAtCursor` writes a character at the cursor, then advances it: right if possible, to the beginning of the next line if at the end of a line, or inserts a new empty line if at the bottom-right corner.

`insertCharAtCursor` is more complex. It finds the first empty cell after the cursor and shifts all cells one position to the right to make room. If no empty cell exists and the cursor is not on the first row, a new line is inserted and the shift is performed similarly. If the screen is completely full and the cursor is on the first row, the inserted character goes directly into the scrollback, and the cursor resets to `(0, 0)`.

Since shifting every character one position seemed time-inefficient, I implemented an alternative `TerminalBufferCartesianTree` that stores screen cells in a Cartesian Tree, which theoretically gives O(log n) insertions. However, after writing `Benchmark.kt` to measure performance, the naive implementation was consistently **~10× faster**. The most likely reason is cache locality — the 2D array is contiguous in memory and benefits from CPU prefetching, while the tree nodes are scattered across the heap causing frequent cache misses.

For the bonus task, I implemented `resize`. It collects all cells (scrollback + screen) into a flat list, trims it at the last non-empty cell, rebuilds the buffer with the new dimensions, and fills the screen from the end backwards. Any remaining cells that don't fit on the new screen are pushed into the scrollback, respecting the `maxScrollbackLines` limit.
