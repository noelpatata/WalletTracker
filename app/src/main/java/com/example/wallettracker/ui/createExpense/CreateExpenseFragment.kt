package com.example.wallettracker.ui.createExpense

import android.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import com.example.wallettracker.ui.pickers.TimePickerFragment
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.wallettracker.databinding.FragmentCreateexpenseBinding
import com.example.wallettracker.ui.adapters.ComboCategoriasAdapter
import com.example.wallettracker.ui.pickers.DatePickerFragment
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CreateExpenseFragment : Fragment() {

    private var _binding: FragmentCreateexpenseBinding? = null

    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //default
        _binding = FragmentCreateexpenseBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //app logic
        InitListeners()
        LoadData()



        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun LoadData() {
        LoadDefaultDateTime()
        LoadComboCategorias()
    }

    private fun LoadComboCategorias() {
        //todo replace hardcoded list with sqlite registries
        val categorias = listOf<String>("Ocio", "Importantes", "Necesarios")

        binding.comboCategorias
        val spinner: Spinner = binding.comboCategorias

        if (spinner != null) {
            val adaptador = ComboCategoriasAdapter(
                requireContext(),
                categorias
            )
            spinner.adapter = adaptador
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun LoadDefaultDateTime() {

        var currentDateTime = LocalDateTime.now()

        binding.inputDate.setText(currentDateTime.toLocalDate().toString())
        binding.inputTime.setText(currentDateTime.toLocalTime().toString())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun InitListeners() {
        binding.pickDate.setOnClickListener {
            ShowDatePickerDialog()
        }
        binding.pickTime.setOnClickListener {
            ShowTimePickerDialog()
        }
        binding.createExpense.setOnClickListener {
            Save()
        }

    }

    private fun Save() {
        val selectedCategory = binding.comboCategorias.adapter.getItem(binding.comboCategorias.selectedItemPosition) as String
        val toast = Toast.makeText(requireContext(), selectedCategory, Toast.LENGTH_SHORT) // in Activity
        toast.show()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ShowDatePickerDialog() {
        //calback -> defining anonymous function
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->

            val selectedDate = LocalDate.of(year, month + 1, day) // Adjust month as it is 0-based
            binding.inputDate.setText(selectedDate.toString())
        }

        //actual execution
        val currDate = LocalDate.now()
        val datePicker = DatePickerDialog(
            requireContext(),
            dateSetListener, //calling anonymous function
            currDate.year,
            currDate.monthValue,
            currDate.dayOfMonth
        )
        datePicker.show()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun ShowTimePickerDialog() {
        //calback -> defining anonymous function
        val dateSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

            val selectedTime = LocalTime.of(hourOfDay, minute) // Adjust month as it is 0-based
            binding.inputTime.setText(selectedTime.toString())
        }

        //actual execution
        val currTime = LocalTime.now()
        val datePicker = TimePickerDialog(
            requireContext(),
            dateSetListener, //calling anonymous function
            currTime.hour,
            currTime.minute,
            DateFormat.is24HourFormat(activity)
        )
        datePicker.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}