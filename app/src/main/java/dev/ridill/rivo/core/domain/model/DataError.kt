package dev.ridill.rivo.core.domain.model

sealed interface DataError : Error {
    enum class Network : DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        SERVER_ERROR,
        UNKNOWN
    }

//    enum class Local : DataError {
//        DISK_FULL
//    }
}