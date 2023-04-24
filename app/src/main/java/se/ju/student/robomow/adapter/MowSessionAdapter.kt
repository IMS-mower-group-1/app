package se.ju.student.robomow.adapter

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import se.ju.student.robomow.R
import se.ju.student.robomow.model.MowSession
import se.ju.student.robomow.model.TimeStamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class MowSessionAdapter(
    private val context: Context,
    private var mowSessions: List<MowSession>,
    private val onItemClicked: (MowSession) -> Unit
) : RecyclerView.Adapter<MowSessionAdapter.MowSessionViewHolder>() {
    class MowSessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.mow_session_date)
        val status: TextView = itemView.findViewById(R.id.mow_session_status)
        val collisions: TextView = itemView.findViewById(R.id.mow_session_collisions)
        val mowingTime: TextView = itemView.findViewById(R.id.mow_session_mowing_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MowSessionViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.mow_session_item, parent, false)
        return MowSessionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MowSessionViewHolder, position: Int) {
        val mowSession = mowSessions[position]
        holder.date.text = getDateFromTimeStamp(mowSession.start.seconds)
        holder.status.text = getMowingStatus(mowSession)
        holder.collisions.text = context.getString(
            R.string.avoided_collisions,
            mowSession.avoidedCollisions.size.toString()
        )
        holder.itemView.setOnClickListener { onItemClicked(mowSession) }
        if (mowSessionIsComplete(mowSession)) {
            holder.mowingTime.text = context.getString(
                R.string.mowing_time,
                getMowingTimeMinutes(mowSession.start.seconds, mowSession.end!!.seconds)
            )
        } else {
            holder.mowingTime.text = context.getString(
                R.string.mowing_time,
                getMowingTimeMinutes(
                    mowSession.start.seconds,
                    Instant.now().epochSecond
                )
            )
        }
    }

    fun updateData(newData: List<MowSession>) {
        mowSessions = newData
        notifyDataSetChanged()
    }

    private fun getMowingStatus(mowSession: MowSession): String {
        if (mowSessionIsComplete(mowSession)) {
            return context.getString(R.string.mowing_status_complete)
        }
        return context.getString(R.string.mowing_status_active)
    }

    private fun mowSessionIsComplete(mowSession: MowSession): Boolean {
        return mowSession.end != null
    }

    private fun getMowingTimeMinutes(startTimeStamp: Long, endTimeStamp: Long): String {
        val mowingTime = endTimeStamp - startTimeStamp
        return mowingTime.div(60).toString()
    }

    private fun getDateFromTimeStamp(timeStamp: Long): String {
        val date = LocalDateTime.ofEpochSecond(timeStamp, 0, ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return date.format(formatter)
    }

    override fun getItemCount() = mowSessions.size
}