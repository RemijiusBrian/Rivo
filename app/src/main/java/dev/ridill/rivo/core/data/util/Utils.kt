package dev.ridill.rivo.core.data.util

import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.model.DataError
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.domain.util.logE
import dev.ridill.rivo.core.ui.util.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import retrofit2.HttpException
import java.net.SocketTimeoutException

suspend inline fun <T, E : DataError> tryNetworkCall(
    crossinline call: suspend () -> Result<T, E>
): Result<T, DataError> = withContext(Dispatchers.IO) {
    try {
        call()
    } catch (e: IOException) {
        logE(e) { "Network call failed" }
        Result.Error(
            DataError.Network.NO_INTERNET,
            UiText.StringResource(R.string.error_no_internet_connection)
        )
    } catch (e: SocketTimeoutException) {
        logE(e) { "Network call failed" }
        Result.Error(
            DataError.Network.REQUEST_TIMEOUT,
            UiText.StringResource(R.string.error_request_timeout)
        )
    } catch (e: HttpException) {
        logE(e) { "Network call failed" }
        when (e.code()) {
            in (500..599) -> Result.Error(
                DataError.Network.SERVER_ERROR,
                UiText.StringResource(R.string.error_server_error)
            )

            else -> Result.Error(
                DataError.Network.UNKNOWN,
                UiText.StringResource(R.string.error_unknown)
            )
        }
    } catch (t: Throwable) {
        if (t is CancellationException) throw t
        logE(t) { "Network call failed" }
        val message = t.message?.let {
            UiText.DynamicString(it)
        } ?: UiText.StringResource(R.string.error_unknown)

        Result.Error(DataError.Network.UNKNOWN, message)
    }
}