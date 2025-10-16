package win.downops.wallettracker.ui.categories

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import win.downops.wallettracker.R
import win.downops.wallettracker.data.online.expenseCategory.ExpenseCategoryRepository
import win.downops.wallettracker.databinding.FragmentCategoriesBinding
import win.downops.wallettracker.ui.adapters.RViewCategoriesAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import provideExpenseCategoryRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.ExpenseCategory
import win.downops.wallettracker.util.AppResultHandler
import win.downops.wallettracker.util.Logger
import win.downops.wallettracker.util.Messages.unexpectedError
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

        initListeners()

        viewLifecycleOwner.lifecycleScope.launch {
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

        when (val result = expenseCategoryRepository.getAll()) {
            is AppResult.Success -> {
                displayCategories(result.data)
            }
            is AppResult.Error -> {
                AppResultHandler.handleError(requireContext(), result)
            }
        }

        binding.loadingPanel.visibility = View.GONE
        binding.rviewCategories.visibility = View.VISIBLE
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
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.START or ItemTouchHelper.END
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

                Logger.log("${categories[fromPosition].getName()} moved from $fromPosition to $toPosition")

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
                return true
            }

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
                return makeMovementFlags(dragFlags, swipeFlags)
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.rviewCategories)


        loadTotal()
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun loadTotal() {
        try {
            val adapter = binding.rviewCategories.adapter as RViewCategoriesAdapter
            val categories = adapter.list
            val totalSum = categories.sumOf { it.getTotal() }
            binding.lblTotal.text = String.format("%.2f", totalSum) + "€"
        } catch (ex: Exception) {
            Logger.log(ex)
            Toast.makeText(requireContext(), unexpectedError, Toast.LENGTH_LONG).show()
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
        val categoryDAO: ExpenseCategoryRepository = provideExpenseCategoryRepository(requireContext())

        when (val result = categoryDAO.edit(category)) {
            is AppResult.Success -> {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            }
            is AppResult.Error -> {
                AppResultHandler.handleError(requireContext(), result)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun deleteCategory(categoryId: Long) {
        val categoryDAO: ExpenseCategoryRepository = provideExpenseCategoryRepository(requireContext())

        when (val result = categoryDAO.deleteById(categoryId)) {
            is AppResult.Success -> {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            }
            is AppResult.Error -> {
                AppResultHandler.handleError(requireContext(), result)
            }
        }
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
