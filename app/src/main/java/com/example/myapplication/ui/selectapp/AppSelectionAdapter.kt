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

data class AppInfo(val name: String, val packageName: String, val icon: Drawable, var isSelected: Boolean)

class AppSelectionAdapter(
    private var appList: List<AppInfo>, // Đổi val thành var
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<AppSelectionAdapter.ViewHolder>() {

    // --- HÀM MỚI: CẬP NHẬT DANH SÁCH KHI TÌM KIẾM ---
    fun updateList(newList: List<AppInfo>) {
        appList = newList
        notifyDataSetChanged()
    }
    // -------------------------------------------------

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

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = app.isSelected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            app.isSelected = isChecked
            onSelectionChanged()
        }

        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    override fun getItemCount() = appList.size
}