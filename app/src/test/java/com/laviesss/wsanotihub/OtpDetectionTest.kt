package com.laviesss.wsanotihub

import org.junit.Test
import org.junit.Assert.*
import java.util.regex.Pattern

class OtpDetectionTest {
    @Test
    fun testOtpRegex() {
        val pattern = Pattern.compile("\\b\\d{4,8}\\b")

        val text1 = "Your code is 482910"
        val matcher1 = pattern.matcher(text1)
        assertTrue(matcher1.find())
        assertEquals("482910", matcher1.group())

        val text2 = "No numbers here"
        val matcher2 = pattern.matcher(text2)
        assertFalse(matcher2.find())

        val text3 = "Code: 123" // Too short
        val matcher3 = pattern.matcher(text3)
        assertFalse(matcher3.find())

        val text4 = "Code: 123456789" // Too long
        val matcher4 = pattern.matcher(text4)
        assertFalse(matcher4.find())
    }
}
