package com.example.wallettracker.ui.settings

import com.example.wallettracker.ui.pickers.TimePickerFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wallettracker.databinding.FragmentCreateexpenseBinding

class CreateExpenseFragment : Fragment() {

    private var _binding: FragmentCreateexpenseBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCreateexpenseBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.pickTime.setOnClickListener {
            TimePickerFragment().show(parentFragmentManager, "timePicker")
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}