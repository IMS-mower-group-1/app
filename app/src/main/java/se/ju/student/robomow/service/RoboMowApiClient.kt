package se.ju.student.robomow.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.ju.student.robomow.api.RoboMowApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoboMowApiClient{
    @Provides
    @Singleton
    fun getClient(): RoboMowApi {
        return Retrofit.Builder()
            .baseUrl(RoboMowApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoboMowApi::class.java)
    }
}