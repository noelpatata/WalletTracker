package com.example.wallettracker.ui.adapters

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.wallettracker.R
import com.example.wallettracker.data.expense.Expense
import java.text.SimpleDateFormat
import java.util.Locale

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
        holder.expensePrice.text = String.format("%.2f", list[position].getPrice())+"€"
        val format = SimpleDateFormat("dd 'de' MMMM 'del' yyyy", Locale("es", "ES"))
        holder.expenseDate.text = format.format(list[position].getDate())

        //listeners
        holder.itemView.setOnClickListener {
            val expenseId = list[position].getId()
            val bundle = Bundle()
            bundle.putLong("expenseId", expenseId)
            holder.itemView.findNavController().navigate(R.id.nav_createexpense, bundle)
        }

    }
}