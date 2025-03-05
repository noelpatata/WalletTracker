package com.example.wallettracker.ui.categories

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wallettracker.R
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryRepository
import com.example.wallettracker.databinding.FragmentCategoriesBinding
import com.example.wallettracker.ui.adapters.RViewCategoriesAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import provideExpenseCategoryRepository
import java.util.Collections


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


        CoroutineScope(Dispatchers.Main).launch {
            initListeners()
            loadData()
        }



        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadData() {
        binding.loadingPanel.visibility = View.VISIBLE
        binding.rviewCategories.visibility = View.GONE
        val expenseCategoryRepository: ExpenseCategoryRepository =
            provideExpenseCategoryRepository(requireContext())

        expenseCategoryRepository.getAll(
            onSuccess = { categoryList ->
                displayCategories(categoryList)
                binding.loadingPanel.visibility = View.GONE
                binding.rviewCategories.visibility = View.VISIBLE
            },
            onFailure = { error ->
                showError("$error")
                binding.loadingPanel.visibility = View.GONE
                binding.rviewCategories.visibility = View.VISIBLE
            }
        )
    }

    private fun displayCategories(categories: List<ExpenseCategory>) {

        val mutableCategories = categories.toMutableList()

        binding.rviewCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rviewCategories.adapter as? RViewCategoriesAdapter
        binding.rviewCategories.adapter = RViewCategoriesAdapter(mutableCategories)


        val simpleCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START or ItemTouchHelper.END) {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition: Int = viewHolder.adapterPosition
                val toPosition: Int = target.adapterPosition
                Collections.swap(mutableCategories, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                Toast.makeText(requireContext(), "${categories.get(fromPosition).getName()} moved from $fromPosition to $toPosition", Toast.LENGTH_SHORT).show()
                CoroutineScope(Dispatchers.Main).launch {
                    updateCategoriesSortOrder(mutableCategories)
                }



                return true
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val delCat = categories[viewHolder.adapterPosition]
                (binding.rviewCategories.adapter as RViewCategoriesAdapter).removeItem(viewHolder.adapterPosition)
                CoroutineScope(Dispatchers.Main).launch {
                    deleteCategory(delCat.getId())
                }
            }
        }


        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.rviewCategories)


        // Display the total
        val totalSum = categories.sumOf { it.getTotal() }
        binding.lblTotal.text = String.format("%.2f", totalSum) + "€"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateCategoriesSortOrder(categories: List<ExpenseCategory>) {
        // Iterate through categories and update their sortOrder in the database
        for (i in categories.indices) {
            val updatedCategory = categories[i]
            updatedCategory.setOrder(i)

            // Call suspend function to edit the category in the database
            editCategory(updatedCategory)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun editCategory(category: ExpenseCategory) {
        try {
            val categoryDAO: ExpenseCategoryRepository =
                provideExpenseCategoryRepository(requireContext())
            categoryDAO.edit(
                category,
                onSuccess = { },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                },

                )
        }catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun deleteCategory(categoryId: Long) {
        try {
            val categoryDAO: ExpenseCategoryRepository =
                provideExpenseCategoryRepository(requireContext())
            categoryDAO.deleteById(
                onSuccess = { lista ->
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                },
                catId = categoryId
            )
        }catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
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
            CoroutineScope(Dispatchers.Main).launch {
                loadData()
            }
            binding.swiperefresh.isRefreshing = false

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}