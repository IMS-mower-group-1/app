package se.ju.student.robomow.api

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import se.ju.student.robomow.BuildConfig
import se.ju.student.robomow.model.QuoteList
interface RoboMowApi {
    @GET("position/{mowerId}")
    suspend fun getPosition(@Path("mowerId") mowerId: String): Response<String>
}