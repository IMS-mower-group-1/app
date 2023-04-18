package se.ju.student.robomow.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MowSession(
    val id: String,
    val end: TimeStamp?,
    val start: TimeStamp,
    val path: List<Position>
) : Parcelable {
    override fun toString() = id
}

@Parcelize
data class Position(
    val x: Int,
    val y: Int
) : Parcelable

@Parcelize
data class TimeStamp(
    val seconds: Long,
    val nanoseconds: Long
) : Parcelable
