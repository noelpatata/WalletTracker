package com.example.wallettracker.ui.categories

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wallettracker.R
import com.example.wallettracker.data.Expense.ExpenseCategory
import com.example.wallettracker.data.Expense.ExpenseCategoryDAO
import com.example.wallettracker.data.ExpenseCategory.ExpenseDAO
import com.example.wallettracker.databinding.FragmentCategoriesBinding
import com.example.wallettracker.databinding.FragmentHomeBinding
import com.example.wallettracker.ui.adapters.RViewCategoriesAdapter
import kotlin.math.exp

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null

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
            ViewModelProvider(this).get(CategoriesViewModel::class.java)

        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        InitListeners()
        LoadData()







        val root: View = binding.root
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun LoadData() {


        try {
            var lista: List<ExpenseCategory>
            ExpenseCategoryDAO(requireContext()).use { sCat ->
                lista = sCat.getAll()!!
            }
            ExpenseDAO(requireContext()).use{expenseDB ->
                for (cat in lista){
                    val total = expenseDB.getByTotalCategory(cat.getId())

                    cat.setTotal(total)
                }
            }
            binding.rviewCategories.layoutManager = LinearLayoutManager(requireContext() )
            binding.rviewCategories.adapter = RViewCategoriesAdapter(lista)
        }
        catch (e:Exception){
            val a = e
            val b = a
        }



    }

    private fun InitListeners() {
        binding.createCategory.setOnClickListener {
            findNavController().navigate(R.id.nav_createcategories)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}