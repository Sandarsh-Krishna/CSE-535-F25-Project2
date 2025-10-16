package edu.asu.cse535.cse535_meseretictactoe

/**
 * Build a 3x3 board from a string using only 'X', 'O', '.' (or '_') for empty.
 * Any other characters (spaces/newlines/commas) are ignored for readability.
 *
 * Example:
 *   boardOf("""
 *     XX.
 *     .O.
 *     ...
 *   """)
 */
fun boardOf(pattern: String): List<Cell> {
    val cleaned = pattern.filter { it == 'X' || it == 'x' || it == 'O' || it == 'o' || it == '.' || it == '_' }
    require(cleaned.length == 9) { "Need 9 marks (X/O/.) but got ${cleaned.length}: '$pattern'" }
    return cleaned.map {
        when (it) {
            'X', 'x' -> Cell.X
            'O', 'o' -> Cell.O
            '.', '_' -> Cell.E
            else -> error("Bad char: $it")
        }
    }
}

/** Convenience state builder with custom board and next player. */
fun stateOf(pattern: String, next: Player): GameState =
    GameState(board = boardOf(pattern), next = next)
