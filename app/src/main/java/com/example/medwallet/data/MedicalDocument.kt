package com.example.medwallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Metadata only. The image itself lives encrypted in filesDir/documents/[fileName]. */
@Entity(tableName = "medical_documents")
data class MedicalDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String,
    val fileName: String,
    val addedAt: Long
)

object DocTypes {
    const val PRESCRIPTION = "Prescription"
    const val REPORT = "Medical report"
    val all = listOf(PRESCRIPTION, REPORT)
}
