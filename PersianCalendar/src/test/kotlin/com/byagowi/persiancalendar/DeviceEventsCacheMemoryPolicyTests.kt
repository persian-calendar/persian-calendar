package com.byagowi.persiancalendar

import com.byagowi.persiancalendar.ui.calendar.DeviceEventsCacheMemoryPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceEventsCacheMemoryPolicyTests {
    @Test
    fun `pressure disables preloading and retains a reduced budget`() {
        val policy = DeviceEventsCacheMemoryPolicy()
        assertTrue(policy.preloadingAllowed)
        assertEquals(90, policy.capacity)
        policy.onMemoryPressure()
        repeat(100) {
            assertFalse(policy.preloadingAllowed)
            assertEquals(12, policy.capacity)
        }
    }

    @Test
    fun `resume does not recover while memory is still low`() {
        val policy = DeviceEventsCacheMemoryPolicy()
        policy.onMemoryPressure()
        policy.onResume(lowMemory = true)
        assertFalse(policy.preloadingAllowed)
        assertEquals(12, policy.capacity)
    }

    @Test
    fun `healthy resume recovers and subsequent pressure reduces again`() {
        val policy = DeviceEventsCacheMemoryPolicy()
        policy.onMemoryPressure()
        policy.onResume(lowMemory = false)
        assertTrue(policy.preloadingAllowed)
        assertEquals(90, policy.capacity)
        policy.onMemoryPressure()
        assertFalse(policy.preloadingAllowed)
        assertEquals(12, policy.capacity)
    }

    @Test
    fun `low memory resume reduces even without an earlier trim callback`() {
        val policy = DeviceEventsCacheMemoryPolicy()
        policy.onResume(lowMemory = true)
        assertFalse(policy.preloadingAllowed)
        assertEquals(12, policy.capacity)
    }
}
