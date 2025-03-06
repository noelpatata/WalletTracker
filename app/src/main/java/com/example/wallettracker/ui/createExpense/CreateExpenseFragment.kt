package com.example.wallettracker.ui.createExpense

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
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
import androidx.navigation.fragment.findNavController
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expense.Expense
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryRepository
import com.example.wallettracker.data.expense.ExpenseRepository
import com.example.wallettracker.databinding.FragmentCreateexpenseBinding
import com.example.wallettracker.ui.adapters.ComboCategoriasAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        //default
        _binding = FragmentCreateexpenseBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //app logic
        var categoryId: Long = -1
        val args : Bundle? = arguments
        if(args != null){
            categoryId = args.getLong("catId")
            isFromCatForm = categoryId != null

            expenseId = args.getLong("expenseId")


        }


        try {
            initListeners()
            CoroutineScope(Dispatchers.Main).launch {
                loadData(categoryId, expenseId)
            }
            binding.inputPrice.requestFocus() // Focus on the EditText
            binding.root.post {
                val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                imm?.showSoftInput(binding.inputPrice, InputMethodManager.SHOW_IMPLICIT)
            }


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
    private suspend fun loadData(catId:Long, expenseId: Long) {
        binding.inputDate.isEnabled = false
        loadDefaultDateTime()
        loadComboCategorias(catId)
        if(expenseId > 0 ){
            val expenseDAO: ExpenseRepository =
                provideExpenseRepository(requireContext())
            expenseDAO.getById(
                onSuccess = { expense ->
                    loadExpense(expense)
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                },
                expenseId = expenseId
            )
        }

    }

    private fun loadExpense(expense: Expense?) {
        try {
            if (expense != null){
                //price
                binding.inputPrice.setText(expense.getPrice().toString())

                //date
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

        if (catId > 0){
            var categories = listOf<ExpenseCategory>()
            val categoryDAO: ExpenseCategoryRepository =
                provideExpenseCategoryRepository(requireContext())
            categoryDAO.getAll(
                onSuccess = {
                    loadSpinner(it)
                    selectCategory(catId)
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                }
            )
        }



    }
    private fun loadSpinner(categories: List<ExpenseCategory>) {
        binding.comboCategorias
        val spinner: Spinner = binding.comboCategorias

        if (spinner != null) {
            val adaptador = ComboCategoriasAdapter(
                requireContext(),
                categories
            )
            spinner.adapter = adaptador
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadDefaultDateTime() {

        var currentDateTime = LocalDate.now()
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
                CoroutineScope(Dispatchers.Main).launch {
                    createOrSaveChanges()
                }


                return@setOnEditorActionListener true
            }
            false
        }
        binding.createExpense.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                createOrSaveChanges()
            }


        }
        if (expenseId > 0){
            binding.delete.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    delete(expenseId)
                }
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
            val expenseDAO: ExpenseRepository =
                provideExpenseRepository(requireContext())
            expenseDAO.deleteById(
                onSuccess = { response ->
                    if (response.success)
                        findNavController().popBackStack()
                    else
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_LONG).show()
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                },
                expenseId = expenseId
            )

        }
        catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun edit() {
        try {
            val expense = getExpense()

            val expenseDAO: ExpenseRepository =
                provideExpenseRepository(requireContext())
            expenseDAO.edit(
                onSuccess = {
                    findNavController().popBackStack()

                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                },
                expense = expense
            )
        }catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun save() {
        try {
            val expense = getExpense()

            val onlineExpenseDAO: ExpenseRepository =
                provideExpenseRepository(requireContext())
            onlineExpenseDAO.create(
                onSuccess = { state ->
                    val createdExpense = state
                    findNavController().popBackStack()

                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                },
                expense = expense
            )
        }
        catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getExpense(): Expense {
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

            val adapter = binding.comboCategorias.adapter as ComboCategoriasAdapter
            val category = adapter.getItem(binding.comboCategorias.selectedItemPosition) as ExpenseCategory
            val catId: Long = category.getId()
            if (expenseId > 0){
                val expense = Expense(expenseId, price, date, catId)
                return expense
            }
            else{
                val expense = Expense(price, date, catId)
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


    }
}