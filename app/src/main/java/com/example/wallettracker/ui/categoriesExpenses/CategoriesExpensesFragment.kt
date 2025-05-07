package com.example.wallettracker.ui.categoriesExpenses

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wallettracker.R
import com.example.wallettracker.data.expense.Expense
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryRepository
import com.example.wallettracker.data.expense.ExpenseRepository
import com.example.wallettracker.databinding.FragmentCategoriesexpensesBinding
import com.example.wallettracker.ui.adapters.RViewCategoriesAdapter
import com.example.wallettracker.ui.adapters.RViewExpensesAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import provideExpenseCategoryRepository
import provideExpenseRepository
import java.util.Collections

class CategoriesExpensesFragment() : Fragment() {
    var categoryId: Long = 0


    private var _binding: FragmentCategoriesexpensesBinding? = null
    private val binding get() = _binding!!
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private var snackbar: Snackbar? = null
    private lateinit var viewModel: CategoriesExpensesViewModel


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[CategoriesExpensesViewModel::class.java]


        _binding = FragmentCategoriesexpensesBinding.inflate(inflater, container, false)

        val args : Bundle = requireArguments()
        categoryId = args.getLong("catId")
        initListeners()
        CoroutineScope(Dispatchers.Main).launch {
            loadData()
        }




        val root: View = binding.root
        return root
    }


    @SuppressLint("NewApi")
    private suspend fun loadData() {
        binding.loadingPanel.visibility = View.VISIBLE
        binding.form.visibility = View.GONE

        val expenseCategoryDAO: ExpenseCategoryRepository =
            provideExpenseCategoryRepository(requireContext())
        expenseCategoryDAO.getById(
            onSuccess = { category ->
                binding.inputName.setText(category.getName())
                CoroutineScope(Dispatchers.Main).launch {
                    loadExpenses()
                }

            },
            onFailure = { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            },
            catId = categoryId
        )


    }

    @SuppressLint("NewApi")
    private suspend fun loadExpenses() {
        try {
            val expenseDAO: ExpenseRepository =
                provideExpenseRepository(requireContext())
            expenseDAO.getByCatId(
                onSuccess = { lista ->
                    displayExpenses(lista)
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

    private fun displayExpenses(lista: List<Expense>) {
        val mutableExpenses = lista.toMutableList()
        if(binding.rviewExpenses.layoutManager == null || binding.rviewExpenses.adapter == null){
            binding.rviewExpenses.layoutManager = LinearLayoutManager(requireContext() )
            binding.rviewExpenses.adapter = RViewExpensesAdapter(mutableExpenses)
        }else{
            (binding.rviewExpenses.adapter as RViewExpensesAdapter).updateData(mutableExpenses)
        }


        val simpleCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.START or ItemTouchHelper.END) {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                return false
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val delExpense = mutableExpenses[viewHolder.adapterPosition]
                val backupExpense = delExpense // Save the item being deleted, not the position
                val backupPosition = viewHolder.adapterPosition // Store position for restoring

                // Remove item from the list
                (binding.rviewExpenses.adapter as RViewExpensesAdapter).removeItem(backupPosition)

                if (isAdded) {
                    snackbar = Snackbar.make(
                        binding.form,
                        "Expense deleted",
                        Snackbar.LENGTH_LONG
                    )

                    snackbar!!.setAction("Undo") {
                        // Undo action: restore the deleted expense at the correct position
                        (binding.rviewExpenses.adapter as RViewExpensesAdapter).addItem(backupPosition, backupExpense)
                    }

                    snackbar!!.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            Log.e("noel", "dismissed")
                            Log.e("noel", event.toString())
                            // Only delete if fragment is still attached
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION && isAdded) {
                                mainScope.launch {
                                    Log.e("noel", "tried deleting")
                                    viewModel.deleteExpense(requireContext(), delExpense.getId())
                                    Log.e("noel", "tried deleted")
                                }
                            }
                        }
                    })

                    snackbar!!.show()
                }
            }

        }


        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.rviewExpenses)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        binding.saveChanges.setOnClickListener {
            saveIfValid()

        }
        binding.inputName.setOnEditorActionListener { v, actionId, event -> //cuando se presiona enter
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                saveIfValid()

                return@setOnEditorActionListener true
            }
            false
        }
        binding.addExpense.setOnClickListener{
            val bundle = Bundle()
            bundle.putLong("catId", categoryId)
            findNavController().navigate(R.id.nav_createexpense, bundle)
        }
        binding.delete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext(), R.style.ButtonsCustomColor)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Delete") { _, _ ->
                    CoroutineScope(Dispatchers.Main).launch {
                        deleteCategory()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveIfValid() {
        val category = GetCategory()
        val isValid = checkValidation(category)
        if (isValid){
            CoroutineScope(Dispatchers.Main).launch {
                saveChanges()
            }

        }
        else{
            Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkValidation(category: ExpenseCategory): Boolean {
        return category.getName().isNotEmpty()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun saveChanges() {
        try {
            val category = GetCategory()
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

    private fun GetCategory(): ExpenseCategory {
        val cat = ExpenseCategory(categoryId)
        cat.setName(binding.inputName.text.toString())
        return cat
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun deleteCategory() {
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
            findNavController().navigate(R.id.nav_categories)
        }catch (e:Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
        snackbar?.dismiss()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()

    }
}