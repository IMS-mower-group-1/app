package se.ju.student.robomow.api

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Response
import retrofit2.http.GET
import se.ju.student.robomow.BuildConfig
import se.ju.student.robomow.model.QuoteList
interface RoboMowApi {
    companion object {
        const val BASE_URL = BuildConfig.ROBO_MOW_API_URL
    }

    @GET("quotes")
    suspend fun getMowSessions(): Response<QuoteList>
}