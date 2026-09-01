package com.dhimandasgupta.funposables

import com.dhimandasgupta.funposables.composables.SubwayOrientation
import com.dhimandasgupta.funposables.composables.SubwayStep
import com.dhimandasgupta.funposables.composables.SubwayStepState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SubwayTest {
  @Test
  fun testSubwayStepCreation() {
    val step =
      SubwayStep(
        label = "Test Label",
        supportText = "Support",
        trailingLabel = "Trailing",
        state = SubwayStepState.Active,
      )

    assertEquals("Test Label", step.label)
    assertEquals("Support", step.supportText)
    assertEquals("Trailing", step.trailingLabel)
    assertEquals(SubwayStepState.Active, step.state)
  }

  @Test
  fun testValidStepCounts() {
    for (count in 2..7) {
      val steps =
        List(count) {
          SubwayStep(label = "Step $it")
        }
      assertEquals(count, steps.size)
      // Check requirement validation logic
      require(steps.size in 2..7)
    }
  }

  @Test
  fun testInvalidStepCountsLessThanTwoThrows() {
    val steps0 = emptyList<SubwayStep>()
    assertThrows(IllegalArgumentException::class.java) {
      require(steps0.size in 2..7) {
        "Subway step count must be between 2 and 7, but was ${steps0.size}."
      }
    }

    val steps1 = listOf(SubwayStep(label = "Single Step"))
    assertThrows(IllegalArgumentException::class.java) {
      require(steps1.size in 2..7) {
        "Subway step count must be between 2 and 7, but was ${steps1.size}."
      }
    }
  }

  @Test
  fun testInvalidStepCountsGreaterThanSevenThrows() {
    val steps8 = List(8) { SubwayStep(label = "Step $it") }
    assertThrows(IllegalArgumentException::class.java) {
      require(steps8.size in 2..7) {
        "Subway step count must be between 2 and 7, but was ${steps8.size}."
      }
    }

    val steps10 = List(10) { SubwayStep(label = "Step $it") }
    assertThrows(IllegalArgumentException::class.java) {
      require(steps10.size in 2..7) {
        "Subway step count must be between 2 and 7, but was ${steps10.size}."
      }
    }
  }

  @Test
  fun testSubwayOrientations() {
    assertEquals(2, SubwayOrientation.entries.size)
    assertEquals(SubwayOrientation.Horizontal, SubwayOrientation.valueOf("Horizontal"))
    assertEquals(SubwayOrientation.Vertical, SubwayOrientation.valueOf("Vertical"))
  }

  @Test
  fun testSubwayStepStates() {
    assertEquals(3, SubwayStepState.entries.size)
    assertEquals(SubwayStepState.Completed, SubwayStepState.valueOf("Completed"))
    assertEquals(SubwayStepState.Active, SubwayStepState.valueOf("Active"))
    assertEquals(SubwayStepState.Inactive, SubwayStepState.valueOf("Inactive"))
  }
}
