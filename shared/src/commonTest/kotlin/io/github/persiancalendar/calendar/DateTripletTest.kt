package io.github.persiancalendar.calendar

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTripletTest {
    @Test
    fun `simple smoke test`() {
        DateTriplet(-231, 23, -42).also { (y, m, d) ->
            assertEquals(-231, y)
            assertEquals(23, m)
            assertEquals(-42, d)
        }
    }

    private fun randomByte(): Int =
        Random.nextInt(Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt() + 1)
    private fun randomShort(): Int =
        Random.nextInt(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt() + 1)

    @Test
    fun `random generated test`() {
        repeat(1000) {
            val val1 = randomShort()
            val val2 = randomByte()
            val val3 = randomByte()
            val date = DateTriplet(val1, val2, val3)
            val (y, m, d) = date
            assertEquals(val1, y)
            assertEquals(val2, m)
            assertEquals(val3, d)
        }
    }
}
