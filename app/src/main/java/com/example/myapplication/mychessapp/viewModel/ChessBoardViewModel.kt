package com.example.myapplication.mychessapp.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.mychessapp.UserInteractionDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChessBoardViewModel(application: Application) : AndroidViewModel(application),
    UserInteractionDelegate {
    private val sharedPreferences =
        application.getSharedPreferences("ChessBoardPrefs", Context.MODE_PRIVATE)
    private val _startingPosition = MutableLiveData<Position?>()
    val startingPosition: LiveData<Position?> = _startingPosition
    private val _endingPosition = MutableLiveData<Position?>()
    val endingPosition: LiveData<Position?> = _endingPosition
    private val _noPaths = MutableLiveData<Boolean>()
    val noPaths: LiveData<Boolean> = _noPaths
    private val _chessBoardDimension = MutableLiveData<Int>()
    val chessBoardDimension: LiveData<Int> = _chessBoardDimension
    private val _maxMoves = MutableLiveData<Int>()
    var maxMoves: LiveData<Int> = _maxMoves

    val stringBuilder = StringBuilder()

    init {
        // Retrieve the saved chessboard size and max moves from SharedPreferences
        val savedChessBoardSize = sharedPreferences.getInt("ChessBoardSize", 6)
        val savedMaxMoves = sharedPreferences.getInt("MaxMoves", 3)

        // Check if the saved values are different from the default values
        if (savedChessBoardSize != _chessBoardDimension.value || savedMaxMoves != _maxMoves.value) {
            // Update the LiveData with the saved values
            _chessBoardDimension.value = savedChessBoardSize
            _maxMoves.value = savedMaxMoves
        }
    }

    // Sets the chessboard dimension value
    fun setChessBoardDimension(size: Int) {
        _chessBoardDimension.value = size
        sharedPreferences.edit().putInt("ChessBoardSize", size).apply()
    }

    // Sets the maxMoves value
    fun setMaxMoves(moves: Int) {
        _maxMoves.value = moves
        sharedPreferences.edit().putInt("MaxMoves", moves).apply()
    }

    // Handles the user's selection of a position
    override fun didSelectPosition(row: Int, col: Int) {
        if (_startingPosition.value == null) {
            _startingPosition.value = Position(row, col)
        } else if (_endingPosition.value == null) {
            _endingPosition.value = Position(row, col)
            calculateNextPossibleMoves()
        }
    }

    // Resets the chessboard by clearing the starting and ending positions
    fun resetChessBoard() {
        _startingPosition.value = null
        _endingPosition.value = null
    }

    // Calculates the next possible moves based on the starting and ending positions
    private fun calculateNextPossibleMoves() {
        val start = startingPosition.value ?: return
        val target = endingPosition.value ?: return

        GlobalScope.launch(Dispatchers.Default) {
            val paths = maxMoves.value?.let { findKnightPaths(start, target, it) }
            _noPaths.postValue(paths?.isEmpty())
            stringBuilder.clear()
            if (paths != null) {
                for (path in paths) {
                    stringBuilder.append(path)
                    stringBuilder.append("\n")
                    println(path)
                }
            }
        }
    }

    // Finds all possible knight paths from the start position to the target position
    private suspend fun findKnightPaths(
        start: Position,
        target: Position,
        maxMoves: Int
    ): List<List<Position>> = withContext(Dispatchers.Default) {
        val paths = mutableListOf<List<Position>>()

        val queue = ArrayDeque<List<Position>>()
        queue.addLast(listOf(start))

        var depth = 1

        while (queue.isNotEmpty() && depth <= maxMoves) {
            val levelSize = queue.size

            repeat(levelSize) {
                val currentPath = queue.removeFirst()
                val currentPos = currentPath.last()

                if (currentPos == target) {
                    // Found a path to the target
                    paths.add(currentPath)
                } else {
                    val nextMoves = currentPos.getNext()

                    for (move in nextMoves) {
                        if (move !in currentPath && chessBoardDimension.value?.let { it1 ->
                                move.isValidMove(
                                    it1
                                )
                            } == true) {
                            val newPath = currentPath.toMutableList()
                            newPath.add(move)
                            queue.addLast(newPath)
                        }
                    }
                }
            }

            depth++
        }

        return@withContext paths
    }
}

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