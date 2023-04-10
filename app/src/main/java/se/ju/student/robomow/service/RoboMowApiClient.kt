package se.ju.student.robomow.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.ju.student.robomow.api.RoboMowApi

object RoboMowApiClient {
    fun getClient(): RoboMowApi =
        Retrofit.Builder()
            .baseUrl(RoboMowApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoboMowApi::class.java)
}