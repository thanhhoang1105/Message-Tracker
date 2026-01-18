package com.example.myapplication.ui.main

import android.content.pm.PackageManager
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

/**
 * Adapter for the horizontal app list in the main header.
 * Allows users to filter messages by selecting a specific application.
 */
class HeaderAdapter(
    private val packageNames: List<String>,
    private val packageManager: PackageManager,
    initialSelection: String?,
    private val onAppClick: (String) -> Unit
) : RecyclerView.Adapter<HeaderAdapter.ViewHolder>() {

    private var selectedPackage: String? = initialSelection

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgIcon: ImageView = view.findViewById(R.id.imgAppIcon)
        val tvName: TextView = view.findViewById(R.id.tvAppName)
        val imgCheck: ImageView = view.findViewById(R.id.imgCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val packageName = packageNames[position]

        // Fetch and display app information (Name and Icon)
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            holder.tvName.text = packageManager.getApplicationLabel(appInfo)
            holder.imgIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo))
        } catch (e: Exception) {
            holder.tvName.text = "Unknown"
            holder.imgIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        // Selection indicator (green checkmark) logic
        if (packageName == selectedPackage) {
            holder.imgCheck.visibility = View.VISIBLE
            holder.imgCheck.setColorFilter(Color.parseColor("#4CAF50"))
        } else {
            holder.imgCheck.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            // If the currently selected app is clicked, do nothing (prevent deselecting)
            if (selectedPackage != packageName) {
                selectedPackage = packageName
                onAppClick(packageName)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = packageNames.size
}
