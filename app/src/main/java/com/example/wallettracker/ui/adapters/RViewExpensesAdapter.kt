package com.example.wallettracker.ui.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.wallettracker.R
import com.example.wallettracker.data.Expense.ExpenseCategory
import com.example.wallettracker.data.ExpenseCategory.Expense
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class RViewExpensesAdapter(list: List<Expense>) : RecyclerView.Adapter<RViewExpensesAdapter.ExpenseViewHolder>() {
    val list = list
    class ExpenseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val expensePrice: TextView = itemView.findViewById(R.id.label_price)
        val expenseDate: TextView = itemView.findViewById(R.id.label_date)
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_rviewexpense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.expensePrice.text = list[position].getPrice().toString()
        val format = SimpleDateFormat("yyyy-MM-dd")
        holder.expenseDate.text = format.format(list[position].getDate())

    }
}