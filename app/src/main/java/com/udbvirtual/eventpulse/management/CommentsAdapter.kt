package com.udbvirtual.eventpulse.management

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.udbvirtual.eventpulse.R
import com.udbvirtual.eventpulse.model.Comment

class CommentsAdapter(
    private var comments: List<Comment>
) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int = comments.size

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val commentTextView: TextView = itemView.findViewById(R.id.textViewCommentText)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)

        fun bind(comment: Comment) {
            commentTextView.text = comment.text
            ratingBar.rating = comment.rating.toFloat()
        }
    }
}
