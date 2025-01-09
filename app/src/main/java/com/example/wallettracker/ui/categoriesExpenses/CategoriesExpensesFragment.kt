package com.example.wallettracker.ui.categoriesExpenses

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wallettracker.R
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expense.bakExpenseCategoryDAO
import com.example.wallettracker.data.expense.Expense
import com.example.wallettracker.data.expense.ExpenseDAO
import com.example.wallettracker.databinding.FragmentCategoriesexpensesBinding
import com.example.wallettracker.ui.adapters.RViewExpensesAdapter

class CategoriesExpensesFragment() : Fragment() {
    var categoryId: Long = 0


    private var _binding: FragmentCategoriesexpensesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(CategoriesExpensesViewModel::class.java)

        _binding = FragmentCategoriesexpensesBinding.inflate(inflater, container, false)


        val args : Bundle = requireArguments()
        categoryId = args.getLong("catId")
        InitListeners()
        LoadData()



        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun LoadData() {
        LoadCategoryInfo()
        LoadExpenses()
    }

    @SuppressLint("NewApi")
    private fun LoadCategoryInfo() {
        var catName: String = ""
        try {
            var cat: ExpenseCategory
            bakExpenseCategoryDAO(requireContext()).use { sCat ->
                cat = sCat.getById(categoryId)
            }
            catName = cat.getName()

        }
        catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }

        binding.inputName.setText(catName)
    }

    @SuppressLint("NewApi")
    private fun LoadExpenses() {
        try {
            var lista: List<Expense>
            ExpenseDAO(requireContext()).use { sCat ->
                lista = sCat.getByCategory(categoryId)!!
            }
            binding.rviewExpenses.layoutManager = LinearLayoutManager(requireContext() )
            binding.rviewExpenses.adapter = RViewExpensesAdapter(lista)
        }
        catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun InitListeners() {
        binding.saveChanges.setOnClickListener {
            val category = GetCategory()
            val isValid = CheckValidation(category)
            if (isValid){
                SaveChanges()
                LoadData()
            }
            else{
                Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_LONG).show()
            }

        }
        binding.addExpense.setOnClickListener{
            val bundle = Bundle()
            bundle.putLong("catId", categoryId)
            findNavController().navigate(R.id.nav_createexpense, bundle)
        }
        binding.delete.setOnClickListener {
            DeleteCategory()
        }
    }

    private fun CheckValidation(category: ExpenseCategory): Boolean {
        if(category.getName().isNullOrEmpty()){
            return false
        }
        return true
    }

    private fun SaveChanges() {
        try {
            val cat = GetCategory()
            bakExpenseCategoryDAO(requireContext()).use { sCat ->
                sCat.update(cat)
            }
        }catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun GetCategory(): ExpenseCategory {
        val cat = ExpenseCategory(categoryId)
        cat.setName(binding.inputName.text.toString())
        return cat
    }

    private fun DeleteCategory() {
        try {
            bakExpenseCategoryDAO(requireContext()).use { sCat ->
                sCat.delete(categoryId)
            }
            findNavController().navigate(R.id.nav_categories)
        }catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}