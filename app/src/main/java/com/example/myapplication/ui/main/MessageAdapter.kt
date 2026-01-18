package com.example.myapplication.ui.main

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
 * Adapter for the main conversation list.
 * Displays the latest message from each sender.
 */
class MessageAdapter(private val onClick: (String) -> Unit) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(DiffCallback()) {

    // 1. Create the View by inflating the item_message XML layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    // 2. Bind data to the ViewHolder (set sender name, message content, etc.)
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)

        holder.itemView.setOnClickListener {
            onClick(message.sender)
        }
    }

    // 3. ViewHolder class to hold UI components for performance optimization
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender: TextView = itemView.findViewById(R.id.tvSender)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)

        fun bind(message: Message) {
            tvSender.text = message.sender
            tvContent.text = message.content

            // Handle avatar image display
            if (message.avatar != null) {
                val bitmap = BitmapFactory.decodeByteArray(message.avatar, 0, message.avatar.size)
                imgAvatar.setImageBitmap(bitmap)
            } else {
                imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon)
            }

            // Format timestamp (Long) to HH:mm format
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdf.format(Date(message.time))
        }
    }

    // 4. DiffCallback: Optimizes list updates by comparing items
    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
