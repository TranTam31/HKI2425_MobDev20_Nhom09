package com.example.hope

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
@RequiresApi(Build.VERSION_CODES.O)
data class Note constructor(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: LocalDate = LocalDate.now(),
    val content: String = "",
)
