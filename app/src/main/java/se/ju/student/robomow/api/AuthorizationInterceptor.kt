package se.ju.student.robomow.api

import okhttp3.Interceptor
import okhttp3.Response
import se.ju.student.robomow.BuildConfig

object AuthorizationInterceptor : Interceptor{
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestWithHeader = chain.request()
            .newBuilder()
            .header(
                "x-api-key", BuildConfig.ROBO_MOW_API_KEY
            ).build()
        return chain.proceed(requestWithHeader)
    }
}