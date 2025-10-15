package com.example.wallettracker.ui.createExpense

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.wallettracker.R
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expense.Expense
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryRepository
import com.example.wallettracker.data.expense.ExpenseRepository
import com.example.wallettracker.data.login.AppResult
import com.example.wallettracker.databinding.FragmentCreateexpenseBinding
import com.example.wallettracker.ui.adapters.ComboCategoriasAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import provideExpenseCategoryRepository
import provideExpenseRepository
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CreateExpenseFragment : Fragment() {

    var isFromCatForm: Boolean = false
    var expenseId: Long = 0

    private var _binding: FragmentCreateexpenseBinding? = null

    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCreateexpenseBinding.inflate(inflater, container, false)
        val root: View = binding.root


        var categoryId: Long = -1
        val args : Bundle? = arguments
        if(args != null){
            categoryId = args.getLong("catId")
            isFromCatForm = categoryId > 0

            expenseId = args.getLong("expenseId")


        }


        try {
            initListeners()
            viewLifecycleOwner.lifecycleScope.launch {
                loadData(categoryId, expenseId)
            }

            binding.root.post {
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.showSoftInput(binding.inputPrice, InputMethodManager.SHOW_IMPLICIT)
            }

            if (expenseId <= 0)
                binding.inputPrice.requestFocus()

        }
        catch(e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }




        return root
    }

    private fun selectCategory(categoryId: Long) {
        try{
            if(categoryId > 0){
                val adapter = binding.comboCategorias.adapter as ComboCategoriasAdapter
                val pos = adapter.getById(categoryId)
                if(pos >= 0)
                    binding.comboCategorias.setSelection(pos)
            }

        }catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadData(catId: Long, expenseId: Long) {
        binding.inputDate.isEnabled = false
        loadDefaultDateTime()
        loadComboCategorias(catId)

        if (expenseId > 0) {
            val expenseDAO: ExpenseRepository = provideExpenseRepository(requireContext())

            when (val result = expenseDAO.getById(expenseId)) {
                is AppResult.Success -> {
                    val expense = result.data
                    if (expense != null) {
                        loadExpense(expense)
                    } else {
                        Toast.makeText(requireContext(), "Expense not found", Toast.LENGTH_LONG).show()
                    }
                }

                is AppResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadExpense(expense: Expense?) {
        try {
            if (expense != null){
                binding.inputPrice.setText(expense.getPrice().toString())

                binding.inputDesc.setText(expense.getDescription())
                val format = SimpleDateFormat("yyyy-MM-dd")
                val stringDate = format.format(expense.getDate())
                binding.inputDate.setText(stringDate)

            }
        }
        catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadComboCategorias(catId: Long) {
        if (catId <= 0) return

        val categoryDAO: ExpenseCategoryRepository = provideExpenseCategoryRepository(requireContext())

        when (val result = categoryDAO.getAll()) {
            is AppResult.Success -> {
                val categories = result.data
                loadSpinner(categories)
                selectCategory(catId)
            }

            is AppResult.Error -> {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadSpinner(categories: List<ExpenseCategory>) {
        binding.comboCategorias
        val spinner: Spinner = binding.comboCategorias

        val adaptador = ComboCategoriasAdapter(
            requireContext(),
            categories
        )
        spinner.adapter = adaptador
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDefaultDateTime() {

        val currentDateTime = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        binding.inputDate.setText(formatter.format(currentDateTime))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        binding.pickDate.setOnClickListener {
            showDatePickerDialog()
        }
        binding.inputPrice.setOnEditorActionListener { v, actionId, event -> //cuando se presiona enter
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                (actionId == EditorInfo.IME_ACTION_DONE)) {
                viewLifecycleOwner.lifecycleScope.launch {
                    createOrSaveChanges()
                }


                return@setOnEditorActionListener true
            }
            false
        }
        binding.createExpense.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                createOrSaveChanges()
            }


        }
        if (expenseId > 0){
            binding.delete.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext(), R.style.ButtonsCustomColor)
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            delete(expenseId)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            }
        }
        else{
            binding.delete.visibility = View.GONE
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createOrSaveChanges() {
        val expense = getExpense()
        val isValid = checkValidation(expense)
        if(isValid){
            if(expenseId > 0){
                edit()
            }else{
                save()
            }
        }else{
            Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkValidation(expense: Expense): Boolean {
        if(expense.getPrice() <= 0){
            return false
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun delete(expenseId: Long) {
        try {
            val expenseDAO: ExpenseRepository = provideExpenseRepository(requireContext())

            when (val result = expenseDAO.deleteById(expenseId)) {
                is AppResult.Success -> {
                    findNavController().popBackStack()
                }

                is AppResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun edit() {
        try {
            val expense = getExpense()
            val expenseDAO: ExpenseRepository = provideExpenseRepository(requireContext())

            when (val result = expenseDAO.edit(expense)) {
                is AppResult.Success -> {
                    findNavController().popBackStack()
                }

                is AppResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message ?: "Unknown error", Toast.LENGTH_LONG).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun save() {
        try {
            val expense = getExpense()
            val expenseDAO: ExpenseRepository = provideExpenseRepository(requireContext())

            when (val result = expenseDAO.create(expense)) {
                is AppResult.Success -> {
                    findNavController().popBackStack()
                }

                is AppResult.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message ?: "Unexpected error", Toast.LENGTH_LONG).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getExpense(): Expense {
        try {
            val priceString = binding.inputPrice.text.toString()
            var price: Double = 0.0
            if (priceString.isNotEmpty()){
                price = priceString.toDouble()
            }

            val descString = binding.inputDesc.text.toString()

            val dateString = binding.inputDate.text.toString()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            var date = Date.valueOf(formatter.format(LocalDate.now()))


            if (dateString.isNotEmpty()){
                date = Date.valueOf(dateString)
            }

            val adapter = binding.comboCategorias.adapter as ComboCategoriasAdapter
            val category = adapter.getItem(binding.comboCategorias.selectedItemPosition)
            val catId: Long = category.getId()
            if (expenseId > 0){
                val expense = Expense(expenseId, price, date, catId, descString)
                return expense
            }
            else{
                val expense = Expense(price, date, catId, descString)
                return expense
            }

        }
        catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            return Expense()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->

            val selectedDate = LocalDate.of(year, month + 1, day)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            binding.inputDate.setText(formatter.format(selectedDate))
        }

        val currDate = LocalDate.now()
        val datePicker = DatePickerDialog(
            requireContext(),
            dateSetListener,
            currDate.year,
            currDate.monthValue,
            currDate.dayOfMonth
        )
        datePicker.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null


    }
}