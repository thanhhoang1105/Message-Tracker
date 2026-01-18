package com.example.myapplication.ui.selectapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.Drawable
import com.example.myapplication.R

/**
 * Data class representing an application's information for selection.
 */
data class AppInfo(val name: String, val packageName: String, val icon: Drawable, var isSelected: Boolean)

/**
 * Adapter for the app selection list.
 * Allows users to choose which applications to monitor for notifications.
 */
class AppSelectionAdapter(
    private var appList: List<AppInfo>, // Changed to var to allow list updates during search
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<AppSelectionAdapter.ViewHolder>() {

    /**
     * Updates the displayed list with new data (e.g., during search/filtering).
     */
    fun updateList(newList: List<AppInfo>) {
        appList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgIcon: ImageView = view.findViewById(R.id.imgAppIcon)
        val tvName: TextView = view.findViewById(R.id.tvAppName)
        val checkBox: CheckBox = view.findViewById(R.id.cbSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = appList[position]
        holder.tvName.text = app.name
        holder.imgIcon.setImageDrawable(app.icon)

        // Clear listener before setting isChecked to avoid triggering callback during binding
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = app.isSelected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            app.isSelected = isChecked
            onSelectionChanged()
        }

        // Allow toggling the checkbox by clicking the entire item row
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    override fun getItemCount() = appList.size
}
