package win.downops.wallettracker.ui.createCategories

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import win.downops.wallettracker.R
import win.downops.wallettracker.data.ExpenseCategoryRepository
import win.downops.wallettracker.databinding.FragmentCreatecategoriesBinding
import kotlinx.coroutines.launch
import provideExpenseCategoryRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.ExpenseCategory
import win.downops.wallettracker.util.AppResultHandler
import win.downops.wallettracker.util.Logger
import win.downops.wallettracker.util.Messages.unexpectedError


class CreateCategoriesFragment : Fragment() {

    private var _binding: FragmentCreatecategoriesBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(CreateCategoriesViewModel::class.java)

        _binding = FragmentCreatecategoriesBinding.inflate(inflater, container, false)

        initListeners()

        binding.inputName.requestFocus()
        binding.root.post {
            val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(binding.inputName, InputMethodManager.SHOW_IMPLICIT)
        }



        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListeners() {
        binding.createCategory.setOnClickListener {
            saveChanges()
        }
        binding.inputName.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                saveChanges()

                return@setOnEditorActionListener true
            }
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveChanges() {
        val category = GetCategory()
        val isValid = checkValidation(category)
        if(isValid){
            viewLifecycleOwner.lifecycleScope.launch {
                save()
            }

        }else{
            Logger.log("Category fields are invalid")
            Toast.makeText(requireContext(), unexpectedError, Toast.LENGTH_LONG).show()

        }
    }

    private fun checkValidation(category: ExpenseCategory): Boolean {
        return category.getName().isNotEmpty()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun save() {
        try {
            val category = GetCategory()
            val expenseCategoryDAO: ExpenseCategoryRepository =
                provideExpenseCategoryRepository(requireContext())

            when (val result = expenseCategoryDAO.create(category)) {
                is AppResult.Success -> {
                    findNavController().navigate(R.id.nav_categories)
                }
                is AppResult.Error -> {
                    AppResultHandler.handleError(requireContext(), result)
                }
            }
        } catch (e: Exception) {
            Logger.log(e)
            Toast.makeText(requireContext(), unexpectedError, Toast.LENGTH_LONG).show()
        }
    }

    private fun GetCategory(): ExpenseCategory {
        return ExpenseCategory(binding.inputName.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}