package se.ju.student.robomow.model

data class MowSession(
    val id: String,
    val end: String,
    val start: String,
    val path: List<String>
)
