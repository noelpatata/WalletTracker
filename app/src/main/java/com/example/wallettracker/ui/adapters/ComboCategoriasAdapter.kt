package com.example.wallettracker.ui.adapters
import android.content.ClipData
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.wallettracker.R

class ComboCategoriasAdapter(private val context: Context, private val items: List<String>) : BaseAdapter() {
    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.element_combocategorias, parent, false)

        val item = getItem(position) as String

        // Bind data to views
        val text = view.findViewById<TextView>(R.id.spinner_text)
        text.text = item

        return view
    }
}