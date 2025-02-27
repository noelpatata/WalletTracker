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
import com.example.wallettracker.MainActivity
import com.example.wallettracker.R
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryDAO
import com.example.wallettracker.databinding.FragmentCategoriesBinding
import com.example.wallettracker.ui.adapters.RViewCategoriesAdapter


class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
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

        initListeners()
        loadData()



        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadData() {
        binding.loadingPanel.visibility = View.VISIBLE
        binding.rviewCategories.visibility = View.GONE
        val expenseCategoryDAO = ExpenseCategoryDAO(this.requireContext())
        expenseCategoryDAO.getExpenseCategories(
            onSuccess = { categoryList ->
                displayCategories(categoryList)
                binding.loadingPanel.visibility = View.GONE
                binding.rviewCategories.visibility = View.VISIBLE
            },
            onFailure = { error ->
                showError("$error")
                binding.loadingPanel.visibility = View.GONE
                binding.rviewCategories.visibility = View.VISIBLE
            })
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        binding.createCategory.setOnClickListener {
            findNavController().navigate(R.id.nav_createcategories)
        }
        binding.swiperefresh.setOnRefreshListener {
            loadData()
            binding.swiperefresh.isRefreshing = false

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}