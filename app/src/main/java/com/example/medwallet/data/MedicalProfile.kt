package com.example.medwallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Single-row table (id is always 1): the owner's emergency medical profile. */
@Entity(tableName = "medical_profile")
data class MedicalProfile(
    @PrimaryKey val id: Int,
    val fullName: String,
    val bloodGroup: String,
    val allergies: String,
    val chronicIllnesses: String,
    val organDonor: Boolean,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    /** Opt-in: the home-screen widget is readable without unlocking, so the user decides. */
    val showOnWidget: Boolean
) {
    companion object {
        const val SINGLETON_ID = 1
        fun empty() = MedicalProfile(SINGLETON_ID, "", "", "", "", false, "", "", true)
    }
}
