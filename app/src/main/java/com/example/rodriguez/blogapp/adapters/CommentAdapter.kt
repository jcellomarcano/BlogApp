package com.example.rodriguez.blogapp.adapters

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rodriguez.blogapp.R
import com.example.rodriguez.blogapp.adapters.CommentAdapter.CommentViewHolder
import com.example.rodriguez.blogapp.models.Comment
import java.util.*

class CommentAdapter(private val mContext: Context, private val mData: List<Comment>) :
    RecyclerView.Adapter<CommentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val row = LayoutInflater.from(mContext).inflate(R.layout.row_comment, parent, false)
        return CommentViewHolder(row)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        Glide.with(mContext).load(mData[position].uimg).into(holder.img_user)
        holder.tv_name.text = mData[position].uname
        holder.tv_content.text = mData[position].content
        holder.tv_date.text = timestampToString(mData[position].timestamp as Long)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img_user: ImageView = itemView.findViewById(R.id.comment_user_img)
        var tv_name: TextView = itemView.findViewById(R.id.comment_username)
        var tv_content: TextView = itemView.findViewById(R.id.comment_content)
        var tv_date: TextView = itemView.findViewById(R.id.comment_date)
    }

    private fun timestampToString(time: Long): String {
        val calendar =
            Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = time
        return DateFormat.format("hh:mm", calendar).toString()
    }
}
