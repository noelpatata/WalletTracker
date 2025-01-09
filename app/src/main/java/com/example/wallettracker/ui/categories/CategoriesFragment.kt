package com.example.wallettracker.ui.categories

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
import com.example.wallettracker.data.LoginRequest
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryDAO
import com.example.wallettracker.databinding.FragmentCategoriesBinding
import com.example.wallettracker.ui.adapters.RViewCategoriesAdapter

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private lateinit var expenseCategoryDAO: ExpenseCategoryDAO // Retrofit-based DAO
    private val userId = 1
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
            ViewModelProvider(this).get(CategoriesViewModel::class.java)

        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        val credentials = LoginRequest("hugo", "noel")
        expenseCategoryDAO = ExpenseCategoryDAO(credentials)
        initListeners()
        loadData()







        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadData() {
        // Log in and fetch data from the API
        expenseCategoryDAO.login(
            onSuccess = {
                expenseCategoryDAO.getExpenseCategories(userId,
                    onSuccess = { categoryList ->
                        displayCategories(categoryList)
                    },
                    onFailure = { error ->
                        showError("Error fetching categories: $error")
                    })
            },
            onFailure = { error ->
                showError("Login error: $error")
            }
        )
    }

    private fun displayCategories(categories: List<ExpenseCategory>) {


        // Bind the categories to the RecyclerView
        binding.rviewCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rviewCategories.adapter = RViewCategoriesAdapter(categories)

        // Display the total
        val totalSum = categories.sumOf { it.getTotal() }
        binding.lblTotal.text = String.format("%.2f", totalSum) + "€"
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }


    private fun initListeners() {
        binding.createCategory.setOnClickListener {
            findNavController().navigate(R.id.nav_createcategories)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}