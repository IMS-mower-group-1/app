package se.ju.student.robomow.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.ju.student.robomow.BuildConfig
import se.ju.student.robomow.api.AuthorizationInterceptor
import se.ju.student.robomow.api.RoboMowApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule{
    @Provides
    @Singleton
    fun provideRoboMowApi(): RoboMowApi {
        val okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(AuthorizationInterceptor)
            .build()

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BuildConfig.ROBO_MOW_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RoboMowApi::class.java)
    }
}