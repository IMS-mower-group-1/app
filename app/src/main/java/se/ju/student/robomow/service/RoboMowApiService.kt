package se.ju.student.robomow.service

import retrofit2.Response
import se.ju.student.robomow.api.RoboMowApi
import se.ju.student.robomow.model.QuoteList

class RoboMowApiService : RoboMowApi {
    private val roboMowApi = RoboMowApiClient.getClient()

    override suspend fun getMowSessions(): Response<QuoteList> {
        return roboMowApi.getMowSessions()
    }
}