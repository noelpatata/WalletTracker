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
import com.example.wallettracker.data.Expense.ExpenseCategory
import com.example.wallettracker.data.Expense.ExpenseCategoryDAO
import com.example.wallettracker.data.ExpenseCategory.Expense
import com.example.wallettracker.data.ExpenseCategory.ExpenseDAO
import com.example.wallettracker.databinding.FragmentCreateexpenseBinding
import com.example.wallettracker.ui.adapters.ComboCategoriasAdapter
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
            InitListeners()
            LoadData(expenseId)
            SelectCategory(categoryId)
        }
        catch(e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }




        return root
    }

    private fun SelectCategory(categoryId: Long) {
        try{
            val adapter = binding.comboCategorias.adapter as ComboCategoriasAdapter
            val pos = adapter.getById(categoryId)
            if(pos >= 0)
                binding.comboCategorias.setSelection(pos)
        }catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun LoadData(expenseId: Long) {
        LoadDefaultDateTime()
        LoadComboCategorias()
        if(expenseId > 0 ){
            val expense = GetExpenseFromSQL()
            if(expense != null){
                LoadExpense(expense)
            }
        }

        }

    private fun LoadExpense(expense: Expense) {
        try {
            //price
            binding.inputPrice.setText(expense.getPrice().toString())

            //date
            val format = SimpleDateFormat("yyyy-MM-dd")
            val stringDate = format.format(expense.getDate())
            binding.inputDate.setText(stringDate)

            //category
            val adapter = binding.comboCategorias.adapter as ComboCategoriasAdapter
            val position = adapter.getById(expense.getCategoryId())
            if(position >= 0){
                binding.comboCategorias.setSelection(position)
            }

        }
        catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
}

    @RequiresApi(Build.VERSION_CODES.O)
    private fun GetExpenseFromSQL(): Expense? {
        try {
            var expense: Expense? = null
            ExpenseDAO(requireContext()).use { expenseDB ->
                expense = expenseDB.getById(expenseId)
            }
            return expense
        }catch (e: Exception){

            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            return null

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun LoadComboCategorias() {
        var categories = listOf<ExpenseCategory>()
        ExpenseCategoryDAO(requireContext()).use { categoryDB ->
            categories = categoryDB.getAll()!!
        }

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
            val expense = GetExpense()
            val isValid = CheckValidation(expense)
            if(isValid){
                if(expenseId > 0){
                    Edit()
                }else{
                    Save()
                }
            }else{
                Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_LONG).show()
            }


        }
        if (expenseId > 0){
            binding.delete.setOnClickListener {
                Delete(expenseId)
            }
        }


    }

    private fun CheckValidation(expense: Expense): Boolean {
        if(expense.getPrice() <= 0){
            return false
        }

        return true
    }

    private fun Delete(expenseId: Long) {
        try {
            ExpenseDAO(requireContext()).use { expenseDB ->
                expenseDB.delete(expenseId)
            }
            findNavController().navigate(R.id.nav_categories)
        }
        catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Edit() {
        try {
            val expense = GetExpense()

            ExpenseDAO(requireContext()).use { expenseDB ->
                expenseDB.update(expense)
                findNavController().navigate(R.id.nav_categories)
            }
        }catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Save() {
        try {
            val expense = GetExpense()

            ExpenseDAO(requireContext()).use { expenseDB ->
                val id = expenseDB.insert(expense)
                if (id > 0 ){
                    findNavController().navigate(R.id.nav_categories)


                }
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

            val adapter = binding.comboCategorias.adapter as ComboCategoriasAdapter
            val category = adapter.getItem(binding.comboCategorias.selectedItemPosition) as ExpenseCategory
            val catId: Long = category.getId()
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


    }
}