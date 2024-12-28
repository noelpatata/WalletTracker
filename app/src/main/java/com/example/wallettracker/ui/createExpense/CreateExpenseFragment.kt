package com.example.wallettracker.ui.createExpense

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wallettracker.R
import com.example.wallettracker.data.Expense.Expense
import com.example.wallettracker.data.Expense.ExpenseDAO
import com.example.wallettracker.databinding.FragmentCreateexpenseBinding
import com.example.wallettracker.ui.adapters.ComboCategoriasAdapter
import java.sql.Date
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.exp

class CreateExpenseFragment : Fragment() {

    private var _binding: FragmentCreateexpenseBinding? = null
    private var expenseDB: ExpenseDAO? = null

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
        try {
            InitDatabase()
            InitListeners()
            LoadData()
        }
        catch(e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }




        return root
    }

    private fun InitDatabase() {
        expenseDB = ExpenseDAO(requireContext())
        expenseDB!!.open()
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

        var currentDateTime = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        binding.inputDate.setText(formatter.format(currentDateTime))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun InitListeners() {
        binding.pickDate.setOnClickListener {
            ShowDatePickerDialog()
        }
        binding.createExpense.setOnClickListener {
            Save()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Save() {
        try {
            val selectedCategory = binding.comboCategorias.adapter.getItem(binding.comboCategorias.selectedItemPosition) as String

            val expense = GetExpense()
            val id = expenseDB!!.insertExpense(expense)
            if (id > 0 ){
                Toast.makeText(requireContext(), "Expense ${id} saved successfully", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.nav_home)
            }

        }
        catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun GetExpense(): Expense {
        try {
            val pricestring = binding.inputPrice.text.toString()
            var price: Double = 0.0
            if (!pricestring.isNullOrEmpty()){
                price = pricestring.toDouble()
            }

            val dateString = binding.inputDate.text.toString()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            var date = Date.valueOf(formatter.format(LocalDate.now()))


            if (!dateString.isNullOrEmpty()){
                date = Date.valueOf(dateString)
            }

            //todo deal with harcoded categoryId
            val catId: Long = 0

            val expense = Expense(price, date, catId)
            return expense
        }
        catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            return Expense()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ShowDatePickerDialog() {
        //calback -> defining anonymous function
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->

            val selectedDate = LocalDate.of(year, month + 1, day) // Adjust month as it is 0-basedç
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            binding.inputDate.setText(formatter.format(selectedDate))
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if(expenseDB != null)
            expenseDB!!.close()


    }
}