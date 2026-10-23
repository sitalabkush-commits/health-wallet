package com.example.medwallet.security

/** Pure decision logic (no Android classes) so it can be unit-tested on the JVM. */
object SimStatePolicy {
    enum class Decision { ALARM, MARK_PRESENT, IGNORE }

    private val PRESENT = setOf("READY", "LOADED", "IMSI", "LOCKED")
    private val REMOVED = setOf("ABSENT", "NOT_READY")

    /**
     * @param state       value of the "ss" extra of SIM_STATE_CHANGED
     * @param armed       user enabled anti-theft protection
     * @param airplaneMode NOT_READY is normal while the radio is off
     * @param wasPresent  a SIM was seen present before (so a removal is real, not a phone without SIM)
     */
    fun decide(state: String?, armed: Boolean, airplaneMode: Boolean, wasPresent: Boolean): Decision = when {
        state != null && state in PRESENT -> Decision.MARK_PRESENT
        state == null || state !in REMOVED -> Decision.IGNORE
        !armed || !wasPresent -> Decision.IGNORE
        state == "NOT_READY" && airplaneMode -> Decision.IGNORE
        else -> Decision.ALARM
    }
}
