package com.example.wallettracker.ui.createCategories

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
import com.example.wallettracker.R
import com.example.wallettracker.data.Expense.ExpenseCategory
import com.example.wallettracker.data.Expense.ExpenseCategoryDAO
import com.example.wallettracker.data.ExpenseCategory.ExpenseDAO
import com.example.wallettracker.databinding.FragmentCategoriesBinding
import com.example.wallettracker.databinding.FragmentCreatecategoriesBinding

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




        val root: View = binding.root
        return root
    }

    private fun InitListeners() {
        binding.createCategory.setOnClickListener {
            val category = GetCategory()
            val isValid = CheckValidation(category)
            if(isValid){
                Save()
            }else{
                Toast.makeText(requireContext(), "Invalid data", Toast.LENGTH_LONG).show()

            }


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
            ExpenseCategoryDAO(requireContext()).use { categoryDB ->
                val id = categoryDB.insert(cat)
                if (id > 0 ){
                    findNavController().navigate(R.id.nav_categories)
                }
            }
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
}