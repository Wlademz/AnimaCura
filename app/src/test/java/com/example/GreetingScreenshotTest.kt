package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.entity.Pet
import com.example.ui.components.PetHeroSummaryCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val samplePet = Pet(
      id = 1,
      name = "Bary",
      species = "Pes",
      breed = "Zlatý Retríver",
      birthDate = "15.04.2021",
      gender = "Samec",
      chipNumber = "941000024819201",
      passportNumber = "SK 192847",
      vetName = "MVDr. Peter Novák",
      vetClinic = "Veterinárna Poliklinika",
      vetPhone = "+421 905 123 456",
      vetEmail = "doktor@vetklinika.sk",
      targetWeightKg = 31.0,
      avatarColorHex = 0xFF0D9488
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        PetHeroSummaryCard(
          pet = samplePet,
          latestWeight = 31.4,
          activeMedsCount = 2,
          onExportClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
