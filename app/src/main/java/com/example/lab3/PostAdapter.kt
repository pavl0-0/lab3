package com.example.lab3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostAdapter(
    private val postList: List<Post>,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.postTitle)
        val contentTextView: TextView = itemView.findViewById(R.id.postContent)
        val categoryTextView: TextView = itemView.findViewById(R.id.postCategory)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val commentsTextView: TextView = itemView.findViewById(R.id.commentsTextView)
        val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        val btnSendComment: Button = itemView.findViewById(R.id.btnSendComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = postList[position]
        holder.titleTextView.text = currentPost.title
        holder.contentTextView.text = currentPost.content
        holder.categoryTextView.text = currentPost.category

        if (currentPost.comments.isEmpty()) {
            holder.commentsTextView.text = "Коментарі відсутні"
        } else {
            holder.commentsTextView.text = currentPost.comments.joinToString("\n- ", prefix = "- ")
        }

        holder.btnSendComment.setOnClickListener {
            val commentText = holder.commentInput.text.toString()
            if (commentText.isNotEmpty()) {
                currentPost.comments.add(commentText)
                notifyItemChanged(holder.bindingAdapterPosition)
                holder.commentInput.text.clear()
            }
        }

        holder.btnEdit.setOnClickListener { onEditClick(holder.bindingAdapterPosition) }
        holder.btnDelete.setOnClickListener { onDeleteClick(holder.bindingAdapterPosition) }
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}