package com.example.myapplication.mychessapp.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.myapplication.mychessapp.R
import com.example.myapplication.mychessapp.UserInteractionDelegate
import com.example.myapplication.mychessapp.viewModel.ChessBoardViewModel

class ChessBoardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()
    private var cellSide = 0f
    private var dimensionX = 0f
    private var dimensionY = 0f
    private var defaultChessboardSize: Int = 6
    lateinit var chessBoardViewModel: ChessBoardViewModel
    var userInteractionDelegate: UserInteractionDelegate? = null
    var canvas: Canvas? = null

    // Set the ChessBoardViewModel for the view
    fun setViewModel(viewModel: ChessBoardViewModel) {
        this.chessBoardViewModel = viewModel

        // Observe the chessBoardSize property and update the defaultChessboardSize accordingly
        viewModel.chessBoardDimension.observeForever { size ->
            defaultChessboardSize = size
            drawChessboard()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        if (defaultChessboardSize == null) return
        val chessBoardSide = width * 0.95f
        cellSide = chessBoardSide / defaultChessboardSize.toFloat()
        dimensionX = (width - chessBoardSide) / 2f
        dimensionY = (height - chessBoardSide) / 2f

        this.canvas = canvas
        drawChessboard()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        val col = ((event.x - dimensionX) / cellSide).toInt()
        val row = ((event.y - dimensionY) / cellSide).toInt()
        if (event.action == MotionEvent.ACTION_DOWN) {
            userInteractionDelegate?.didSelectPosition(col, row)
        }
        return true
    }

    // Draw a square at the specified column and row
    private fun drawSquareAt(canvas: Canvas, col: Int, row: Int) {
        val isSquarePositionOddNumber = (col + row) % 2 == 1
        val squareColor = if (isSquarePositionOddNumber) resources.getColor(
            R.color.black,
            context.theme
        ) else resources.getColor(R.color.white, context.theme)
        val squareLeftSide = dimensionX + col * cellSide
        val squareTopSide = dimensionY + row * cellSide
        val squareRightSide = dimensionX + (col + 1) * cellSide
        val squareBottomSide = dimensionY + (row + 1) * cellSide

        paint.color = squareColor
        canvas.drawRect(
            squareLeftSide,
            squareTopSide,
            squareRightSide,
            squareBottomSide,
            paint
        )
    }

    // Draw the entire chessboard
    fun drawChessboard() {
        paint.color =
            resources.getColor(androidx.appcompat.R.color.material_blue_grey_800, context.theme)
        canvas?.drawRect(
            0f,
            (height - width) / 2f,
            width.toFloat(),
            (height - width) / 2f + width,
            paint
        )
        for (row in 0 until defaultChessboardSize)
            for (col in 0 until defaultChessboardSize)
                this.canvas?.let { drawSquareAt(it, col, row) }
    }
}
