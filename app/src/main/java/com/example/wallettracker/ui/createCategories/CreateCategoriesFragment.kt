package com.example.wallettracker.ui.createCategories

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
import androidx.navigation.fragment.findNavController
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryRepository
import com.example.wallettracker.databinding.FragmentCreatecategoriesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import provideExpenseCategoryRepository


class CreateCategoriesFragment : Fragment() {

    private var _binding: FragmentCreatecategoriesBinding? = null

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
            ViewModelProvider(this).get(CreateCategoriesViewModel::class.java)

        _binding = FragmentCreatecategoriesBinding.inflate(inflater, container, false)

        InitListeners()

        binding.inputName.requestFocus() // Focus on the EditText
        binding.root.post {
            val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(binding.inputName, InputMethodManager.SHOW_IMPLICIT)
        }



        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun InitListeners() {
        binding.createCategory.setOnClickListener {
            SaveChanges()


        }
        binding.inputName.setOnEditorActionListener { v, actionId, event -> //cuando se presiona enter
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                SaveChanges()

                return@setOnEditorActionListener true
            }
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun SaveChanges() {
        val category = GetCategory()
        val isValid = CheckValidation(category)
        if(isValid){
            CoroutineScope(Dispatchers.Main).launch {
                save()
            }

        }else{
            Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_LONG).show()

        }
    }

    private fun CheckValidation(category: ExpenseCategory): Boolean {
        if(category.getName().isEmpty()){
            return false
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun save() {
        try{
            val cat = GetCategory()
            val expenseCategoryDAO: ExpenseCategoryRepository =
                provideExpenseCategoryRepository(requireContext())
            expenseCategoryDAO.create(
                cat,
                onSuccess = {
                    findNavController().navigate(com.example.wallettracker.R.id.nav_categories)
                },
                onFailure = { error ->
                    showError("Error creating category: $error")
                })
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