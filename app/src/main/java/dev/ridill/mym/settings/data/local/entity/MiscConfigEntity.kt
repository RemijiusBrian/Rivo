package dev.ridill.mym.settings.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class MiscConfigEntity(
    @PrimaryKey(autoGenerate = false)
    val configKey: String,
    val configValue: String
)