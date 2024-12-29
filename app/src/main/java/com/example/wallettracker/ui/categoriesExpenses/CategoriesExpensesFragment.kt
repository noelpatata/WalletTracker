package com.example.wallettracker.ui.categoriesExpenses

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wallettracker.data.ExpenseCategory.Expense
import com.example.wallettracker.data.ExpenseCategory.ExpenseDAO
import com.example.wallettracker.databinding.FragmentCategoriesexpensesBinding
import com.example.wallettracker.ui.adapters.RViewExpensesAdapter

class CategoriesExpensesFragment() : Fragment() {
    var categoryId: Long = 0


    private var _binding: FragmentCategoriesexpensesBinding? = null

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
            ViewModelProvider(this).get(CategoriesExpensesViewModel::class.java)

        _binding = FragmentCategoriesexpensesBinding.inflate(inflater, container, false)
        val args : Bundle = requireArguments()
        categoryId = args.getLong("catId")
        InitListeners()
        LoadData()







        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun LoadData() {


        try {
            var lista: List<Expense>
            ExpenseDAO(requireContext()).use { sCat ->
                lista = sCat.getByCategory(categoryId)!!
            }
            binding.rviewExpenses.layoutManager = LinearLayoutManager(requireContext() )
            binding.rviewExpenses.adapter = RViewExpensesAdapter(lista)
        }
        catch (e:Exception){
            val a = e
            val b = a
        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun InitListeners() {
        binding.saveChanges.setOnClickListener {
            LoadData()
        }
        binding.delete.setOnClickListener {
            DeleteCategory()
        }
    }

    private fun DeleteCategory() {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}