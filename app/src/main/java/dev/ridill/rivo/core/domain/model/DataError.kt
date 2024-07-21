package dev.ridill.rivo.core.domain.model

interface DataError : Error {
    enum class Network : DataError {
        REQUEST_TIMEOUT,
        NO_INTERNET,
        SERVER_ERROR,
        UNKNOWN
    }

//    enum class Local : DataError {
//        DISK_FULL
//    }
}