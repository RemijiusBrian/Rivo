package dev.ridill.rivo.settings.data.remote.interceptors

import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.domain.util.tryOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class GoogleAccessTokenInterceptor(
    private val googleSignInService: GoogleSignInService
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
        val token = tryOrNull { googleSignInService.getAccessToken() }.orEmpty()
        val original = chain.request()
        val updatedRequest = original.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        chain.proceed(updatedRequest)
    }
}