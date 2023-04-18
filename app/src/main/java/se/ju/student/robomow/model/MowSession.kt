package se.ju.student.robomow.model

data class MowSession(
    val id: String,
    val end: TimeStamp?,
    val start: TimeStamp,
    val path: List<Position>
) {
    override fun toString() = id
}

data class Position(
    val x: Int,
    val y: Int
)

data class TimeStamp(
    val seconds: Long,
    val nanoseconds: Long
)
