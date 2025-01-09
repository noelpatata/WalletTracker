package com.example.wallettracker.ui.createCategories

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
import com.example.wallettracker.R
import com.example.wallettracker.data.LoginRequest
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expense.bakExpenseCategoryDAO
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryDAO
import com.example.wallettracker.databinding.FragmentCreatecategoriesBinding

class CreateCategoriesFragment : Fragment() {

    private var _binding: FragmentCreatecategoriesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var expenseCategoryDAO: ExpenseCategoryDAO
    private val userId = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(CreateCategoriesViewModel::class.java)

        _binding = FragmentCreatecategoriesBinding.inflate(inflater, container, false)
        val credentials = LoginRequest("hugo", "noel")
        expenseCategoryDAO = ExpenseCategoryDAO(credentials)

        InitListeners()




        val root: View = binding.root
        return root
    }

    private fun InitListeners() {
        binding.createCategory.setOnClickListener {
            val category = GetCategory()
            val isValid = CheckValidation(category)
            if(isValid){
                Save()
            }else{
                Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_LONG).show()

            }


        }
    }

    private fun CheckValidation(category: ExpenseCategory): Boolean {
        if(category.getName().isEmpty()){
            return false
        }

        return true
    }

    private fun Save() {
        try{
            val cat = GetCategory()
            expenseCategoryDAO.login(
                onSuccess = {
                    expenseCategoryDAO.createExpenseCategories(
                        userId,
                        cat,
                        onSuccess = { categoryList ->
                            findNavController().navigate(R.id.nav_categories)
                        },
                        onFailure = { error ->
                            showError("Error creating category: $error")
                        })
                },
                onFailure = { error ->
                    showError("Login error: $error")
                }
            )
        }
        catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun GetCategory(): ExpenseCategory {
        return ExpenseCategory(binding.inputName.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}