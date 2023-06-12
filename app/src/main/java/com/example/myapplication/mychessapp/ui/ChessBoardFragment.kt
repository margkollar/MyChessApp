package com.example.myapplication.mychessapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myapplication.mychessapp.Position
import com.example.myapplication.mychessapp.R
import com.example.myapplication.mychessapp.databinding.ChessboardFragmentLayoutBinding
import com.example.myapplication.mychessapp.extensions.hideKeyboard
import com.example.myapplication.mychessapp.viewModel.ChessBoardViewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ChessBoardFragment : Fragment() {
    private lateinit var binding: ChessboardFragmentLayoutBinding
    private lateinit var chessBoardView: ChessBoardView
    private val chessBoardViewModel: ChessBoardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChessboardFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chessBoardView = binding.view
        chessBoardView.userInteractionDelegate = chessBoardViewModel

        // Observe the starting position LiveData to update the infoText accordingly
        chessBoardViewModel.startingPosition.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.infoText.text = getString(R.string.starting_position)
            } else {
                binding.infoText.text = getString(R.string.target_position)
            }
        }

        // Observe the ending position LiveData to update the infoText accordingly
        chessBoardViewModel.endingPosition.observe(viewLifecycleOwner) {
            if (chessBoardViewModel.startingPosition.value == null) {
                binding.infoText.text = getString(R.string.starting_position)
            } else {
                binding.infoText.text =
                    getString(
                        R.string.selected_positions,
                        (chessBoardViewModel.startingPosition.value as Position).col,
                        (chessBoardViewModel.startingPosition.value as Position).row,
                        (chessBoardViewModel.endingPosition.value as Position).col,
                        (chessBoardViewModel.endingPosition.value as Position).row
                    )
            }
        }
        chessBoardViewModel.chessBoardDimension.observe(viewLifecycleOwner) {
            // Redraw the chessboard
            binding.view.setViewModel(chessBoardViewModel)
            chessBoardView.drawChessboard()

        }
        // Observe the noPaths LiveData to update the infoText accordingly
        chessBoardViewModel.noPaths.observe(viewLifecycleOwner) {
            if (it) {
                binding.infoText.text = getString(R.string.no_paths)
            } else {
                binding.infoText.text =
                    getString(R.string.moves, chessBoardViewModel.stringBuilder.toString())
            }
        }

        // Set an action listener for the chessBoardSizeFromInput EditText
        binding.chessBoardSizeFromInput.setOnEditorActionListener { _, actionId, _ ->
            hideKeyboard()
            if(!binding.chessBoardSizeFromInput.text.isNullOrEmpty()) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (binding.chessBoardSizeFromInput.text.toString().toInt() in 6..16) {
                        binding.view.invalidate()
                        chessBoardViewModel.setChessBoardDimension(
                            binding.chessBoardSizeFromInput.text.toString().toInt()
                        )
                        binding.view.setViewModel(chessBoardViewModel)
                        binding.view.drawChessboard()
                    } else {
                        Toast.makeText(
                            activity,
                            getString(R.string.chessboard_size_msg),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            true
        }

        // Set an action listener for the maxMovesFromInput EditText
        binding.maxMovesFromInput.setOnEditorActionListener { _, actionId, _ ->
            hideKeyboard()
            if(!binding.maxMovesFromInput.text.isNullOrEmpty()) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (binding.maxMovesFromInput.text.toString().toInt() in 1..5) {
                        binding.view.invalidate()
                        chessBoardViewModel.setMaxMoves(
                            binding.maxMovesFromInput.text.toString().toInt()
                        )
                        binding.view.drawChessboard()
                    } else {
                        Toast.makeText(
                            activity,
                            getString(R.string.max_moves_msg),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            true
        }

        // Set a click listener for the resetBtn button
        binding.resetBtn.setOnClickListener {
            reset(binding)
        }
    }

    // Resets the views and the chessboard
    private fun reset(binding: ChessboardFragmentLayoutBinding) {
        binding.chessBoardSizeFromInput.text.clear()
        binding.maxMovesFromInput.text.clear()
        chessBoardViewModel.resetChessBoard()
        chessBoardViewModel.setChessBoardDimension(6)
        chessBoardViewModel.setMaxMoves(3)
        binding.view.setViewModel(chessBoardViewModel)
        binding.view.invalidate()
        binding.view.drawChessboard()
    }
}

