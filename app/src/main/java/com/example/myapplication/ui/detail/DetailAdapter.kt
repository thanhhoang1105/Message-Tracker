package com.example.myapplication.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.local.entities.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter for displaying detailed messages in a conversation.
 * Uses ListAdapter for efficient list updates.
 */
class DetailAdapter : ListAdapter<Message, DetailAdapter.DetailViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        // Inflate the chat bubble layout for each message item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail_message, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContent: TextView = itemView.findViewById(R.id.tvBubbleContent)
        private val tvTime: TextView = itemView.findViewById(R.id.tvBubbleTime)

        /**
         * Binds message data to the chat bubble view.
         */
        fun bind(message: Message) {
            tvContent.text = message.content
            // Format timestamp to HH:mm (e.g., 14:30)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdf.format(Date(message.time))
        }
    }

    /**
     * Callback for calculating the diff between two non-null items in a list.
     */
    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
    }
}
