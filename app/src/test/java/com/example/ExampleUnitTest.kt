package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testRunningBalanceCalculation() {
    val openingBalance = 1000.0
    val creditSale = 2400.0
    val payment = 2000.0
    val finalBalance = openingBalance + creditSale - payment
    assertEquals(1400.0, finalBalance, 0.001)
  }
}
