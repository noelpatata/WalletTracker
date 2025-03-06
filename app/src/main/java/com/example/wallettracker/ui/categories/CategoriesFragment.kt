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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wallettracker.R
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryRepository
import com.example.wallettracker.databinding.FragmentCategoriesBinding
import com.example.wallettracker.ui.adapters.RViewCategoriesAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import provideExpenseCategoryRepository
import java.util.Collections


class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private var snackbar: Snackbar? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(CategoriesViewModel::class.java)

        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        mainScope.launch {
            initListeners()
            loadData()
        }

        return binding.root
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
        if (binding.rviewCategories.layoutManager == null || binding.rviewCategories.adapter == null) {
            binding.rviewCategories.layoutManager = LinearLayoutManager(requireContext())
            val adapter = RViewCategoriesAdapter(mutableCategories)
            adapter.setHasStableIds(true)
            binding.rviewCategories.adapter = adapter

        } else {
            (binding.rviewCategories.adapter as RViewCategoriesAdapter).updateData(mutableCategories)
        }

        val simpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, // Drag only up/down
            ItemTouchHelper.START or ItemTouchHelper.END // Swipe left/right
        ) {
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

                Toast.makeText(
                    requireContext(),
                    "${categories[fromPosition].getName()} moved from $fromPosition to $toPosition",
                    Toast.LENGTH_SHORT
                ).show()

                mainScope.launch {
                    updateCategoriesSortOrder(mutableCategories)
                }
                return true
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val delCat = mutableCategories[viewHolder.adapterPosition]
                val backupPosition = viewHolder.adapterPosition
                (binding.rviewCategories.adapter as RViewCategoriesAdapter).removeItem(viewHolder.adapterPosition)
                if (isAdded) {
                    if(snackbar != null) { snackbar?.dismiss() }
                    snackbar = Snackbar.make(
                        binding.linearLayo,
                        "Category deleted",
                        Snackbar.LENGTH_LONG
                    )
                    var undo = false
                    snackbar!!.setAction("Undo") {
                        snackbar = null
                        (binding.rviewCategories.adapter as RViewCategoriesAdapter).addItem(viewHolder.adapterPosition, categories[backupPosition])

                    }

                    snackbar!!.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                snackbar = null
                                mainScope.launch {
                                    deleteCategory(delCat.getId())
                                }
                                loadTotal()
                            }
                        }
                    })

                    snackbar!!.show()
                }


            }

            override fun isItemViewSwipeEnabled(): Boolean {
                return true // Keep swipe functionality
            }

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN // Allow only vertical movement
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END // Allow swipe in both directions
                return makeMovementFlags(dragFlags, swipeFlags)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.rviewCategories)


        loadTotal()
    }

    private fun loadTotal() {
        try {
            val adapter = binding.rviewCategories.adapter as RViewCategoriesAdapter
            val categories = adapter.list
            val totalSum = categories.sumOf { it.getTotal() }
            binding.lblTotal.text = String.format("%.2f", totalSum) + "€"
        } catch (ex: Exception) {
            showError(ex.message.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateCategoriesSortOrder(categories: List<ExpenseCategory>) {
        for (i in categories.indices) {
            val updatedCategory = categories[i]
            updatedCategory.setOrder(i)
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
                }
            )
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun deleteCategory(categoryId: Long) {
        try {
            val categoryDAO: ExpenseCategoryRepository =
                provideExpenseCategoryRepository(requireContext())
            categoryDAO.deleteById(
                onSuccess = { },
                onFailure = { error ->
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
                },
                catId = categoryId
            )
        } catch (e: Exception) {
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
            snackbar?.dismiss()
            mainScope.launch {
                loadData()
            }
            binding.swiperefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (isAdded) {
            snackbar?.dismiss()
        }

        mainScope.coroutineContext.cancelChildren()

    }
}
