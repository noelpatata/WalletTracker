package com.example.wallettracker.ui.createCategories

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
import androidx.navigation.fragment.findNavController
import com.example.wallettracker.MainActivity
import com.example.wallettracker.R
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryDAO
import com.example.wallettracker.databinding.FragmentCreatecategoriesBinding

class CreateCategoriesFragment : Fragment() {

    private var _binding: FragmentCreatecategoriesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var TOKEN: String = ""
    var USER_ID: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(CreateCategoriesViewModel::class.java)

        _binding = FragmentCreatecategoriesBinding.inflate(inflater, container, false)
        val mainActivity = requireActivity() as MainActivity
        TOKEN = mainActivity.TOKEN
        USER_ID = mainActivity.USER_ID

        InitListeners()




        val root: View = binding.root
        return root
    }

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

    private fun SaveChanges() {
        val category = GetCategory()
        val isValid = CheckValidation(category)
        if(isValid){
            Save()
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

    private fun Save() {
        try{
            val cat = GetCategory()
            val expenseCategoryDAO = ExpenseCategoryDAO(TOKEN, USER_ID)
            expenseCategoryDAO.createExpenseCategories(
                cat,
                onSuccess = {
                    findNavController().navigate(R.id.nav_categories)
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