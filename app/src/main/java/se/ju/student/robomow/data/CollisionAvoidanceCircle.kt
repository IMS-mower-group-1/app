package se.ju.student.robomow.data

data class CollisionAvoidanceCircle(
    val x: Float,
    val y: Float,
    val avoidedObject: String,
    val radius:Float = 25f
)
