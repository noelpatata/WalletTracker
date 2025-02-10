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
import com.example.wallettracker.MainActivity
import com.example.wallettracker.R
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expense.bakExpenseCategoryDAO
import com.example.wallettracker.data.expense.Expense
import com.example.wallettracker.data.expense.ExpenseDAO
import com.example.wallettracker.data.expense.bakExpenseDAO
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryDAO
import com.example.wallettracker.databinding.FragmentCategoriesexpensesBinding
import com.example.wallettracker.ui.adapters.RViewExpensesAdapter

class CategoriesExpensesFragment() : Fragment() {
    var categoryId: Long = 0


    private var _binding: FragmentCategoriesexpensesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var TOKEN: String = ""
    var USER_ID: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(CategoriesExpensesViewModel::class.java)

        _binding = FragmentCategoriesexpensesBinding.inflate(inflater, container, false)
        val mainActivity = requireActivity() as MainActivity
        TOKEN = mainActivity.TOKEN
        USER_ID = mainActivity.USER_ID

        val args : Bundle = requireArguments()
        categoryId = args.getLong("catId")
        InitListeners()
        LoadData()



        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun LoadData() {
        binding.loadingPanel.visibility = View.VISIBLE
        binding.form.visibility = View.GONE
        LoadCategoryInfo()
        LoadExpenses()
    }

    @SuppressLint("NewApi")
    private fun LoadCategoryInfo() {

        val expenseCategoryDAO = ExpenseCategoryDAO(TOKEN, USER_ID)
        expenseCategoryDAO.getExpenseCategoryById(
            onSuccess = { category ->
                binding.inputName.setText(category.getName())
            },
            onFailure = { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            },
            catId = categoryId
        )

    }

    @SuppressLint("NewApi")
    private fun LoadExpenses() {
        try {
            val expenseDAO = ExpenseDAO(TOKEN, USER_ID)
            expenseDAO.getById(
                onSuccess = { lista ->
                    binding.rviewExpenses.layoutManager = LinearLayoutManager(requireContext() )
                    binding.rviewExpenses.adapter = RViewExpensesAdapter(lista)
                    binding.loadingPanel.visibility = View.GONE
                    binding.form.visibility = View.VISIBLE
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    binding.loadingPanel.visibility = View.GONE
                    binding.form.visibility = View.VISIBLE
                },
                catId = categoryId
            )

        }
        catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            binding.loadingPanel.visibility = View.GONE
            binding.form.visibility = View.VISIBLE
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
            val category = GetCategory()
            val categoryDAO = ExpenseCategoryDAO(TOKEN, USER_ID)
            categoryDAO.editName(
                onSuccess = { },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                },
                category
            )
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
            val categoryDAO = ExpenseCategoryDAO(TOKEN, USER_ID)
            categoryDAO.deleteById(
                onSuccess = { lista ->
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                },
                catId = categoryId
            )
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