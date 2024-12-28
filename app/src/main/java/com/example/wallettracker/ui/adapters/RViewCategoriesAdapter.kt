package com.example.wallettracker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wallettracker.R
import com.example.wallettracker.data.Expense.ExpenseCategory

class RViewCategoriesAdapter(list: List<ExpenseCategory>) : RecyclerView.Adapter<RViewCategoriesAdapter.ExpenseCategoryViewHolder>() {
    val list = list
    class ExpenseCategoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.label_name)
        val categoryTotal: TextView = itemView.findViewById(R.id.label_total)
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_rviewcategory, parent, false)
        return ExpenseCategoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    override fun onBindViewHolder(holder: ExpenseCategoryViewHolder, position: Int) {
        holder.categoryName.text = list[position].getName()
        holder.categoryTotal.text = list[position].getTotal().toString()

    }
}