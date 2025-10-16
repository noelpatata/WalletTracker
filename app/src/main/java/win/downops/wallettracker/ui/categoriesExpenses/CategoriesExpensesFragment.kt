package win.downops.wallettracker.ui.categoriesExpenses

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import win.downops.wallettracker.R
import win.downops.wallettracker.data.online.expenseCategory.ExpenseCategoryRepository
import win.downops.wallettracker.data.online.expense.ExpenseRepository
import win.downops.wallettracker.databinding.FragmentCategoriesexpensesBinding
import win.downops.wallettracker.ui.adapters.RViewExpensesAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import provideExpenseCategoryRepository
import provideExpenseRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Expense
import win.downops.wallettracker.data.models.ExpenseCategory
import win.downops.wallettracker.util.AppResultHandler
import win.downops.wallettracker.util.Logger
import win.downops.wallettracker.util.Messages.unexpectedError

class CategoriesExpensesFragment() : Fragment() {
    var categoryId: Long = 0


    private var _binding: FragmentCategoriesexpensesBinding? = null
    private val binding get() = _binding!!
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private var snackbar: Snackbar? = null
    private lateinit var viewModel: CategoriesExpensesViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CategoriesExpensesViewModel::class.java]

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AppResult.Success -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                is AppResult.Error -> {
                    AppResultHandler.handleError(requireContext(), result)
                }
            }
        }
    }


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
        viewLifecycleOwner.lifecycleScope.launch {
            loadData()
        }

        val root: View = binding.root
        return root
    }




    @SuppressLint("NewApi")
    private suspend fun loadData() {
        binding.loadingPanel.visibility = View.VISIBLE
        binding.form.visibility = View.GONE


        val expenseCategoryDAO = provideExpenseCategoryRepository(requireContext())
        when (val result = expenseCategoryDAO.getById(categoryId)) {
            is AppResult.Success -> {
                val category = result.data
                if (category != null) {
                    binding.inputName.setText(category.getName())
                    loadExpenses()
                }
            }
            is AppResult.Error -> {
                AppResultHandler.handleError(requireContext(), result)
            }
        }

        binding.loadingPanel.visibility = View.GONE
        binding.form.visibility = View.VISIBLE
    }

    @SuppressLint("NewApi")
    private suspend fun loadExpenses() {
        binding.loadingPanel.visibility = View.VISIBLE
        binding.form.visibility = View.GONE

        val expenseDAO: ExpenseRepository = provideExpenseRepository(requireContext())

        when (val result = expenseDAO.getByCatId(categoryId)) {
            is AppResult.Success -> {
                displayExpenses(result.data)
            }

            is AppResult.Error -> {
                AppResultHandler.handleError(requireContext(), result)
            }
        }

        binding.loadingPanel.visibility = View.GONE
        binding.form.visibility = View.VISIBLE
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
                val backupExpense = delExpense
                val backupPosition = viewHolder.adapterPosition

                (binding.rviewExpenses.adapter as RViewExpensesAdapter).removeItem(backupPosition)

                if (isAdded) {
                    snackbar = Snackbar.make(
                        binding.form,
                        "Expense deleted",
                        Snackbar.LENGTH_LONG
                    )

                    snackbar!!.setAction("Undo") {
                        (binding.rviewExpenses.adapter as RViewExpensesAdapter).addItem(backupPosition, backupExpense)
                    }

                    snackbar!!.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION && isAdded) {
                                mainScope.launch {
                                    viewModel.deleteExpense(requireContext(), delExpense.getId())
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
        binding.inputName.setOnEditorActionListener { v, actionId, event ->
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
            Logger.log("Invalid data")
            Toast.makeText(requireContext(), unexpectedError, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkValidation(category: ExpenseCategory): Boolean {
        return category.getName().isNotEmpty()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun saveChanges() {
        binding.loadingPanel.visibility = View.VISIBLE

        try {
            val category = GetCategory()
            val categoryDAO: ExpenseCategoryRepository =
                provideExpenseCategoryRepository(requireContext())

            when (val result = categoryDAO.edit(category)) {
                is AppResult.Success -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.nav_categories)
                }

                is AppResult.Error -> {
                    AppResultHandler.handleError(requireContext(), result)
                }
            }
        } catch (e: Exception) {
            Logger.log(e)
            Toast.makeText(requireContext(), unexpectedError, Toast.LENGTH_LONG).show()
        } finally {
            binding.loadingPanel.visibility = View.GONE
        }
    }

    private fun GetCategory(): ExpenseCategory {
        val cat = ExpenseCategory(categoryId)
        cat.setName(binding.inputName.text.toString())
        return cat
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun deleteCategory() {
        val categoryDAO: ExpenseCategoryRepository = provideExpenseCategoryRepository(requireContext())

        binding.loadingPanel.visibility = View.VISIBLE

        try {
            when (val result = categoryDAO.deleteById(categoryId)) {
                is AppResult.Success -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.nav_categories)
                }

                is AppResult.Error -> {
                    AppResultHandler.handleError(requireContext(), result)
                }
            }
        } catch (e: Exception) {
            Logger.log(e)
            Toast.makeText(requireContext(), unexpectedError, Toast.LENGTH_LONG).show()
        } finally {
            binding.loadingPanel.visibility = View.GONE
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