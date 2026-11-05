package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class ExampleUnitTest {
    @Test
    fun testLiveCalculation_hourlyRate() {
        val hours = 1
        val minutes = 20
        val rate = 800.0
        val total = ((hours + (minutes / 60.0)) * rate).roundToInt()
        assertEquals(1067, total)
    }

    @Test
    fun testLiveCalculation_bighaRate() {
        val bigha = 4.5
        val rate = 600.0
        val total = (bigha * rate).roundToInt()
        assertEquals(2700, total)
    }

    @Test
    fun testKisanPhoneValidation() {
        // Valid 10-digit
        val validDigits = "9826012345"
        assertTrue(validDigits.length == 10 && validDigits.all { it.isDigit() })

        // Optional empty allowed
        val emptyDigits = ""
        assertTrue(emptyDigits.isEmpty())
    }
}
