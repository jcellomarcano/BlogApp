package com.example.rodriguez.blogapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rodriguez.blogapp.R
import com.example.rodriguez.blogapp.activities.PostDetailActivity
import com.example.rodriguez.blogapp.adapters.PostAdapter.MyViewHolder
import com.example.rodriguez.blogapp.models.Post

class PostAdapter(var mContext: Context, var mData: MutableList<Post?>?) :
    RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val row = LayoutInflater.from(mContext).inflate(R.layout.row_post_item, parent, false)
        return MyViewHolder(row)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvTitle.text = mData!![position]?.title
        Glide.with(mContext).load(mData!![position]?.picture).into(holder.imgPost)
        Glide.with(mContext).load(mData!![position]?.userPhoto).into(holder.imgPostProfile)
    }

    override fun getItemCount(): Int {
        return mData!!.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = itemView.findViewById(R.id.row_post_title)
        var imgPost: ImageView = itemView.findViewById(R.id.row_post_img)
        var imgPostProfile: ImageView = itemView.findViewById(R.id.row_post_profile_img)

        init {
            itemView.setOnClickListener {
                val postDetailActivity = Intent(mContext, PostDetailActivity::class.java)
                val position = adapterPosition
                postDetailActivity.putExtra("title", mData!![position]?.title)
                postDetailActivity.putExtra("postImage", mData!![position]?.picture)
                postDetailActivity.putExtra("description", mData!![position]?.description)
                postDetailActivity.putExtra("postKey", mData!![position]?.postKey)
                postDetailActivity.putExtra("userPhoto", mData!![position]?.userPhoto)
                // will fix this later i forgot to add user name to post object
                // postDetailActivity.putExtra("userName",mData.get(position).getUsername);
                val timestamp = mData!![position]?.timeStamp as Long
                postDetailActivity.putExtra("postDate", timestamp)
                mContext.startActivity(postDetailActivity)
            }
        }
    }
}
