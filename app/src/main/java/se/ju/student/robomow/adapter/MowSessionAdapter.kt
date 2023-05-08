package se.ju.student.robomow.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import se.ju.student.robomow.R
import se.ju.student.robomow.model.MowSession
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MowSessionAdapter(
    private val context: Context,
    private var mowSessions: List<MowSession>,
    private val onItemClicked: (MowSession) -> Unit
) : RecyclerView.Adapter<MowSessionAdapter.MowSessionViewHolder>() {
    class MowSessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.mow_session_date)
        val status: View = itemView.findViewById(R.id.mow_session_status_indicator)
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
        holder.date.text = getDateTimeFromTimeStamp(mowSession.start.seconds)
        holder.collisions.text = context.getString(
            R.string.avoided_collisions,
            mowSession.avoidedCollisions.size
        )
        holder.itemView.setOnClickListener { onItemClicked(mowSession) }
        holder.mowingTime.text = getMowingTime(mowSession)
        holder.status.background = ColorDrawable(getStatusIndicatorColor(mowSession))
    }

    fun updateData(newData: List<MowSession>) {
        mowSessions = newData
        notifyDataSetChanged()
    }

    private fun mowSessionIsComplete(mowSession: MowSession): Boolean {
        return mowSession.end != null
    }

    private fun getStatusIndicatorColor(mowSession: MowSession): Int{
        return if (mowSessionIsComplete(mowSession)) {
            ContextCompat.getColor(context, R.color.completed_session_indicator_color)
        } else {
            ContextCompat.getColor(context, R.color.success_green)
        }
    }

    private fun convertToHoursMinutes(startTimeStamp: Long, endTimeStamp: Long): Pair<Int, Int> {
        val mowingTime = endTimeStamp - startTimeStamp
        val totalMinutes = mowingTime.div(60)
        val hours = totalMinutes.div(60)
        val minutes = totalMinutes % 60
        return Pair(hours.toInt(), minutes.toInt())
    }

    private fun getMowingTime(mowSession: MowSession): String {
        if (mowSessionIsComplete(mowSession)) {
            val (hours, min) = convertToHoursMinutes(
                mowSession.start.seconds,
                mowSession.end!!.seconds
            )
            return context.getString(
                R.string.mowing_time,
                hours,
                min
            )
        }
        val (hours, min) = convertToHoursMinutes(
            mowSession.start.seconds,
            Instant.now().epochSecond
        )
        return context.getString(
            R.string.mowing_time,
            hours,
            min
        )

    }

    private fun getDateTimeFromTimeStamp(timeStamp: Long): String {
        val date = LocalDateTime.ofEpochSecond(
            timeStamp,
            0,
            ZoneId.systemDefault().rules.getOffset(Instant.now())
        )
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return date.format(formatter)
    }

    override fun getItemCount() = mowSessions.size
}