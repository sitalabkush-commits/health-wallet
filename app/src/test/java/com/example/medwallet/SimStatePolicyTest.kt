package com.example.medwallet

import com.example.medwallet.security.SimStatePolicy
import com.example.medwallet.security.SimStatePolicy.Decision
import org.junit.Assert.assertEquals
import org.junit.Test

class SimStatePolicyTest {
    @Test fun absentWhenArmedAndWasPresent_alarms() =
        assertEquals(Decision.ALARM, SimStatePolicy.decide("ABSENT", true, false, true))

    @Test fun notReadyWhenArmedAndWasPresent_alarms() =
        assertEquals(Decision.ALARM, SimStatePolicy.decide("NOT_READY", true, false, true))

    @Test fun notReadyInAirplaneMode_ignored() =
        assertEquals(Decision.IGNORE, SimStatePolicy.decide("NOT_READY", true, true, true))

    @Test fun absentInAirplaneMode_stillAlarms() =
        assertEquals(Decision.ALARM, SimStatePolicy.decide("ABSENT", true, true, true))

    @Test fun notArmed_ignored() =
        assertEquals(Decision.IGNORE, SimStatePolicy.decide("ABSENT", false, false, true))

    @Test fun phoneNeverHadSim_ignored() =
        assertEquals(Decision.IGNORE, SimStatePolicy.decide("ABSENT", true, false, false))

    @Test fun simLoaded_marksPresentEvenWhenDisarmed() =
        assertEquals(Decision.MARK_PRESENT, SimStatePolicy.decide("LOADED", false, false, false))

    @Test fun unknownOrNull_ignored() {
        assertEquals(Decision.IGNORE, SimStatePolicy.decide("UNKNOWN", true, false, true))
        assertEquals(Decision.IGNORE, SimStatePolicy.decide(null, true, false, true))
    }
}
