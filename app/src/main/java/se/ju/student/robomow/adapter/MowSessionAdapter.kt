package se.ju.student.robomow.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import se.ju.student.robomow.R
import se.ju.student.robomow.model.MowSession

class MowSessionAdapter(
    private var mowSessions: List<MowSession>,
    private val onItemClicked: (MowSession) -> Unit
) : RecyclerView.Adapter<MowSessionAdapter.MowSessionViewHolder>() {
    class MowSessionViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val date: TextView = itemView.findViewById(R.id.mow_session_date)
        val status: TextView = itemView.findViewById(R.id.mow_session_status)
        val collisions: TextView = itemView.findViewById(R.id.mow_session_collisions)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MowSessionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.mow_session_item, parent, false)
        return MowSessionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MowSessionViewHolder, position: Int) {
        val mowSession = mowSessions[position]
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val date = java.util.Date(mowSession.start.seconds * 1000)

        holder.date.text = sdf.format(date)
        holder.status.text = if (mowSession.end != null) "Complete"  else "Active"
        holder.collisions.text = mowSession.avoidedCollisions.size.toString()
        holder.itemView.setOnClickListener { onItemClicked(mowSession) }
    }

    fun updateData(newData: List<MowSession>) {
        mowSessions = newData
        notifyDataSetChanged()
    }

    override fun getItemCount() = mowSessions.size
}