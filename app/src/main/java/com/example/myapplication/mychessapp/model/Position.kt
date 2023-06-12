package com.example.myapplication.mychessapp

class Position(var col: Int, var row: Int) {

    // Returns a list of possible next positions for the knight
    fun getNext(): List<Position> {
        var nextPositions = mutableListOf<Position>()
        for (direction in Direction.values()) {
            when (direction) {
                Direction.NORTH -> {
                    nextPositions.add(Position(col + 1, row + 2))
                    nextPositions.add(Position(col - 1, row + 2))
                }
                Direction.SOUTH -> {
                    nextPositions.add(Position(col + 1, row - 2))
                    nextPositions.add(Position(col - 1, row - 2))
                }
                Direction.EAST -> {
                    nextPositions.add(Position(col + 2, row + 1))
                    nextPositions.add(Position(col + 2, row - 1))
                }
                Direction.WEST -> {
                    nextPositions.add(Position(col - 2, row + 1))
                    nextPositions.add(Position(col - 2, row - 1))
                }
            }
        }
        return nextPositions.filter {
            it.isValidMove(20)
        }.toList()
    }

    // Checks if the position is a valid move on the chessboard
    fun isValidMove(chessBoardDimension: Int): Boolean {
        val validRow = row >= 0 && row <= (chessBoardDimension - 1)
        val validCol = col >= 0 && col <= (chessBoardDimension - 1)
        return validCol && validRow
    }

    // Overrides the equals() function to compare two Position objects
    override fun equals(other: Any?): Boolean {
        val equalRows = row == (other as Position).row
        val equalCols = col == (other as Position).col
        return equalCols && equalRows
    }

    override fun hashCode(): Int {
        return col + row
    }

    // Overrides the toString() function to display the position in a algebraic notation
    override fun toString(): String {
        val row = "♞" + ('a' + row).toString()
        val col = (col + 1).toString()
        return row + col
    }
}

// Enum class for different directions of the knight's movement
enum class Direction {
    NORTH, SOUTH, WEST, EAST;
}