package com.example.myapplication.mychessapp


interface UserInteractionDelegate {
    fun didSelectPosition(row: Int, col: Int)
}