package com.burhanrashid52.photoeditor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_lite_issue.view.*
import java.util.*

class IssueAdapter() : RecyclerView.Adapter<IssueAdapter.IssueViewHolder>() {
    var mList = arrayListOf<IssueModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            IssueViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_lite_issue,
                    parent, false))

    override fun getItemCount() = mList.size

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        holder.txtIssueId.text = mList.get(position).sn
    }

    fun addItem(item: IssueModel) {
        mList.add(item)
        notifyItemInserted(mList.indexOf(item))

    }

    class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtIssueId: TextView

        init {
            txtIssueId = itemView.issueId
        }
    }
}