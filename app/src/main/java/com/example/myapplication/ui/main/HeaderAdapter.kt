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

        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            holder.tvName.text = packageManager.getApplicationLabel(appInfo)
            holder.imgIcon.setImageDrawable(packageManager.getApplicationIcon(appInfo))
        } catch (e: Exception) {
            holder.tvName.text = "Unknown"
            holder.imgIcon.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        // Logic tích xanh
        if (packageName == selectedPackage) {
            holder.imgCheck.visibility = View.VISIBLE
            holder.imgCheck.setColorFilter(Color.parseColor("#4CAF50"))
        } else {
            holder.imgCheck.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            // NẾU CLICK VÀO APP ĐANG ĐƯỢC CHỌN THÌ KHÔNG LÀM GÌ CẢ (KHÔNG CHO BỎ CHỌN)
            if (selectedPackage != packageName) {
                selectedPackage = packageName
                onAppClick(packageName)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = packageNames.size
}