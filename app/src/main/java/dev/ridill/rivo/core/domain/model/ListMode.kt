package dev.ridill.rivo.core.domain.model

enum class ListMode {
    LIST, GRID;

    operator fun not(): ListMode = when (this) {
        LIST -> GRID
        GRID -> LIST
    }
}