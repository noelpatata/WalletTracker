package com.example.wallettracker.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.wallettracker.R
import com.example.wallettracker.data.expenseCategory.ExpenseCategory

class RViewCategoriesAdapter(private var list: MutableList<ExpenseCategory>) :
    RecyclerView.Adapter<RViewCategoriesAdapter.ExpenseCategoryViewHolder>() {

    class ExpenseCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.label_name)
        val categoryTotal: TextView = itemView.findViewById(R.id.label_total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_rviewcategory, parent, false)
        return ExpenseCategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ExpenseCategoryViewHolder, position: Int) {
        val category = list[position]
        holder.categoryName.text = category.getName()
        holder.categoryTotal.text = String.format("%.2f", category.getTotal()) + "€"

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putLong("catId", category.getId())
            holder.itemView.findNavController().navigate(R.id.nav_categoriesexpenses, bundle)
        }
    }

    fun removeItem(position: Int) {
        if (position in list.indices) {
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, list.size) // Update UI properly
        }
    }

    fun updateData(newCategories: List<ExpenseCategory>) {
        list.clear()
        list.addAll(newCategories)
        notifyDataSetChanged()
    }
}
