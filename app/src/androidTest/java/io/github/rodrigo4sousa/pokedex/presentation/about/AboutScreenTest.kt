package io.github.rodrigo4sousa.pokedex.presentation.about

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import io.github.rodrigo4sousa.pokedex.R
import io.github.rodrigo4sousa.pokedex.presentation.theme.PokedexTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** The About screen shows the fan-project disclaimer and opens the legal pages and the source code. */
class AboutScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val openedUrls = mutableListOf<String>()
    private var backClicks = 0

    private val recordingUriHandler = object : UriHandler {
        override fun openUri(uri: String) {
            openedUrls += uri
        }
    }

    private fun string(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Before
    fun setUp() {
        composeRule.setContent {
            CompositionLocalProvider(LocalUriHandler provides recordingUriHandler) {
                PokedexTheme {
                    AboutScreen(onBack = { backClicks++ })
                }
            }
        }
    }

    @Test
    fun theDisclaimerIsVisible() {
        composeRule.onNodeWithText(string(R.string.about_disclaimer_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.about_disclaimer_body)).assertIsDisplayed()
    }

    @Test
    fun privacyPolicyOpensItsPage() = assertLinkOpens(R.string.about_privacy_policy, AboutLinks.PRIVACY_POLICY_URL)

    @Test
    fun termsOpenTheirPage() = assertLinkOpens(R.string.about_terms, AboutLinks.TERMS_URL)

    @Test
    fun sourceCodeOpensTheRepository() = assertLinkOpens(R.string.about_source_code, AboutLinks.SOURCE_CODE_URL)

    @Test
    fun theBackButtonLeavesTheScreen() {
        composeRule.onNodeWithContentDescription(string(R.string.about_back)).performClick()

        assertEquals(1, backClicks)
    }

    private fun assertLinkOpens(label: Int, expectedUrl: String) {
        val link = composeRule.onNodeWithText(string(label))
        link.performScrollTo().assertIsDisplayed().assertHasClickAction().performClick()

        assertEquals(listOf(expectedUrl), openedUrls)
    }
}
