package com.example

import com.example.ui.localization.AppStrings
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleFinancialTest {

  @Test
  fun testBengaliDigitConversion() {
    val english = "12345"
    val bengali = AppStrings.toBengaliDigits(english)
    assertEquals("১২৩৪৫", bengali)
  }

  @Test
  fun testCurrencyFormatting() {
    val amount = 2400.0
    val formatted = AppStrings.formatCurrency(amount, "bn")
    assertEquals("৳ ২,৪০০.০০", formatted)
  }
}
