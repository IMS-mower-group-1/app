package se.ju.student.robomow.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import se.ju.student.robomow.model.MowSession

interface RoboMowApi {
    @GET("position/{mowerId}")
    suspend fun getPosition(@Path("mowerId") mowerId: String): Response<String>

    @GET("mow-session/mower/{mowerId}")
    suspend fun getMowSessions(@Path("mowerId") mowerId: String): Response<List<MowSession>>
}