package se.ju.student.robomow.api

import retrofit2.Response
import retrofit2.http.GET
import se.ju.student.robomow.model.QuoteList

interface RoboMowApi {
    companion object {
        const val BASE_URL = "https://quotable.io/"
    }

    @GET("quotes")
    suspend fun getMowSessions(): Response<QuoteList>
}