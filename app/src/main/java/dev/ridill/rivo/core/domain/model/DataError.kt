package dev.ridill.rivo.core.domain.model

interface DataError : Error {
    enum class Network : DataError {
        REQUEST_TIMEOUT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        RESPONSE_ERROR,
        SERVER_ERROR,
        UNKNOWN
    }

//    enum class Local : DataError {
//        DISK_FULL
//    }
}