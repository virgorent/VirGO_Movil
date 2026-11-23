package mx.virgo.app.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import mx.virgo.app.R
import mx.virgo.app.ui.guest.VenueSample
import mx.virgo.app.ui.guest.VenueSamples
import mx.virgo.app.ui.login.LoginScreen
import mx.virgo.app.ui.login.LoginViewModel
import mx.virgo.app.ui.recover.RecoverScreen
import mx.virgo.app.ui.recover.RecoverViewModel
import mx.virgo.app.ui.register.RegisterHero
import mx.virgo.app.ui.register.RegisterRole
import mx.virgo.app.ui.register.RegisterScreen
import mx.virgo.app.ui.register.RegisterViewModel
import mx.virgo.app.ui.register.RolePicker
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.GenerateRecoveryCodeUseCase
import mx.virgo.core.domain.LoginUseCase
import mx.virgo.core.domain.RegisterUseCase
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateLoginFormUseCase
import mx.virgo.core.domain.ValidateRecoveryUseCase
import mx.virgo.core.domain.ValidateRegisterFormUseCase
import mx.virgo.core.testing.FakeAuthRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h640dp")
class CompactAccessTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun loginFitsAtDoubleScaleInLightTheme() = assertLoginFits(darkTheme = false)

    @Test
    fun loginFitsAtDoubleScaleInDarkTheme() = assertLoginFits(darkTheme = true)

    @Test
    fun registerFitsAtDoubleScaleInLightTheme() = assertRegisterFits(darkTheme = false)

    @Test
    fun registerFitsAtDoubleScaleInDarkTheme() = assertRegisterFits(darkTheme = true)

    @Test
    fun recoverFitsAtDoubleScaleInLightTheme() = assertRecoverFits(darkTheme = false)

    @Test
    fun recoverFitsAtDoubleScaleInDarkTheme() = assertRecoverFits(darkTheme = true)

    @Test
    fun loginFitsAtIntermediateScaleInLightTheme() = assertLoginFits(darkTheme = false, fontScale = 1.3f)

    @Test
    fun loginFitsAtIntermediateScaleInDarkTheme() = assertLoginFits(darkTheme = true, fontScale = 1.3f)

    @Test
    fun registerFitsAtIntermediateScaleInLightTheme() = assertRegisterFits(darkTheme = false, fontScale = 1.3f)

    @Test
    fun registerFitsAtIntermediateScaleInDarkTheme() = assertRegisterFits(darkTheme = true, fontScale = 1.3f)

    @Test
    fun recoverFitsAtIntermediateScaleInLightTheme() = assertRecoverFits(darkTheme = false, fontScale = 1.3f)

    @Test
    fun recoverFitsAtIntermediateScaleInDarkTheme() = assertRecoverFits(darkTheme = true, fontScale = 1.3f)

    @Test
    fun registerHeroSeparatesBackAndCopyAtDoubleScale() = assertHeroSeparated(venue = null)

    @Test
    fun venueHeroSeparatesBackAndCopyAtDoubleScale() = assertHeroSeparated(venue = VenueSamples.first())

    @Test
    fun recoverSecondStepAndErrorFitAtDoubleScaleInDarkTheme() {
        val viewModel =
            RecoverViewModel(
                ValidateEmailUseCase(),
                GenerateRecoveryCodeUseCase(),
                ValidateRecoveryUseCase(),
            )
        viewModel.onEmailChange("demo@example.invalid")
        viewModel.sendCode(nowMillis = 1L)
        viewModel.onCodeChange(viewModel.state.value.demoCode)
        viewModel.save(nowMillis = 1L)
        compose.setContent {
            LargeTextTheme(darkTheme = true) { RecoverScreen(viewModel = viewModel) }
        }
        compose.onRoot().assertWidthIsEqualTo(320.dp)
        val heading = compose.onAllNodesWithText(string(R.string.recover_title_step2))[0]
        heading.performScrollTo().assertIsDisplayed()
        assertTextDoesNotOverflow(R.string.recover_title_step2, heading)
        assertTextFitsAfterScroll(R.string.recover_error_password)
        assertTextFitsAfterScroll(R.string.recover_resend)
        compose.onNodeWithText(string(R.string.recover_save)).assertIsDisplayed()
        assertTextDoesNotOverflow(R.string.recover_save)
    }

    @Test
    fun rolesHave48DpTargetsAndKeepTheirCallbacksAtNormalScale() {
        var chosen: RegisterRole? = null
        compose.setContent {
            VirGoTheme {
                var selected by remember { mutableStateOf(RegisterRole.CLIENT) }
                RolePicker(
                    selected = selected,
                    onSelected = {
                        chosen = it
                        selected = it
                    },
                )
            }
        }
        RegisterRole.entries.forEach { role ->
            compose
                .onNodeWithText(string(role.labelRes))
                .assertIsDisplayed()
                .assertHeightIsAtLeast(48.dp)
                .assertWidthIsAtLeast(48.dp)
        }
        compose
            .onNodeWithContentDescription(string(R.string.register_role_group))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup))
        compose.onNodeWithText(string(R.string.register_role_client)).assertIsSelected()
        compose.onNodeWithText(string(R.string.register_role_provider)).performClick()
        compose.onNodeWithText(string(R.string.register_role_provider)).assertIsSelected()
        compose.onNodeWithText(string(R.string.register_role_client)).assertIsNotSelected()
        compose.onNodeWithText(string(R.string.register_role_owner)).assertIsNotSelected()
        compose.runOnIdle { assertEquals(RegisterRole.PROVIDER, chosen) }
    }

    private fun assertLoginFits(
        darkTheme: Boolean,
        fontScale: Float = 2f,
    ) {
        val viewModel =
            LoginViewModel(
                ValidateLoginFormUseCase(ValidateEmailUseCase()),
                LoginUseCase(FakeAuthRepository()),
            )
        compose.setContent {
            LargeTextTheme(darkTheme, fontScale) { LoginScreen(viewModel = viewModel) }
        }
        compose.onRoot().assertWidthIsEqualTo(320.dp)
        listOf(
            R.string.login_hero_title,
            R.string.login_title,
            R.string.social_google,
            R.string.social_apple,
            R.string.auth_divider,
            R.string.login_forgot,
            R.string.login_submit,
            R.string.register_no_account,
            R.string.register_switch_register,
        ).forEach(::assertTextFitsAfterScroll)
    }

    private fun assertRegisterFits(
        darkTheme: Boolean,
        fontScale: Float = 2f,
    ) {
        val viewModel =
            RegisterViewModel(
                ValidateRegisterFormUseCase(ValidateEmailUseCase()),
                RegisterUseCase(FakeAuthRepository()),
            )
        compose.setContent {
            LargeTextTheme(darkTheme, fontScale) { RegisterScreen(venueId = null, viewModel = viewModel) }
        }
        compose.onRoot().assertWidthIsEqualTo(320.dp)
        listOf(
            R.string.register_hero_title,
            R.string.register_title,
            R.string.register_benefit_prices,
            R.string.register_benefit_ai,
            R.string.register_benefit_event,
            R.string.social_google,
            R.string.social_apple,
            R.string.register_role_client,
            R.string.register_role_owner,
            R.string.register_role_provider,
            R.string.register_submit,
            R.string.register_have_account,
            R.string.register_switch_login,
            R.string.register_terms,
        ).forEach(::assertTextFitsAfterScroll)
    }

    private fun assertRecoverFits(
        darkTheme: Boolean,
        fontScale: Float = 2f,
    ) {
        val viewModel =
            RecoverViewModel(
                ValidateEmailUseCase(),
                GenerateRecoveryCodeUseCase(),
                ValidateRecoveryUseCase(),
            )
        compose.setContent {
            LargeTextTheme(darkTheme, fontScale) { RecoverScreen(viewModel = viewModel) }
        }
        compose.onRoot().assertWidthIsEqualTo(320.dp)
        assertTextFitsAfterScroll(R.string.recover_title)
        assertTextFitsAfterScroll(R.string.recover_lead)
        compose.onNodeWithText(string(R.string.recover_send)).assertIsDisplayed()
        assertTextDoesNotOverflow(R.string.recover_send)
    }

    private fun assertHeroSeparated(venue: VenueSample?) {
        compose.setContent {
            LargeTextTheme(darkTheme = true) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    RegisterHero(venue = venue, onBackClick = {})
                }
            }
        }
        compose.onRoot().assertWidthIsEqualTo(320.dp)
        val back = compose.onNodeWithContentDescription(string(R.string.action_back))
        back.assertIsDisplayed().assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
        val titleText = venue?.name ?: string(R.string.register_hero_title)
        val title = compose.onNodeWithText(titleText).assertIsDisplayed()
        assertTrue(
            "Back action must not intersect the hero copy",
            back.getUnclippedBoundsInRoot().bottom <= title.getUnclippedBoundsInRoot().top,
        )
    }

    private fun assertTextFitsAfterScroll(resId: Int) {
        compose.onNodeWithText(string(resId)).performScrollTo().assertIsDisplayed()
        assertTextDoesNotOverflow(resId)
    }

    private fun assertTextDoesNotOverflow(
        resId: Int,
        node: SemanticsNodeInteraction = compose.onNodeWithText(string(resId), useUnmergedTree = true),
    ) {
        val layouts = mutableListOf<TextLayoutResult>()
        node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            assertTrue(action(layouts))
        }
        assertEquals(1, layouts.size)
        val layout = layouts.single()
        val message = "Text must fit at 320 dp and the configured font scale: ${string(resId)}"
        assertFalse(message, layout.didOverflowHeight)
        assertFalse(message, layout.multiParagraph.didExceedMaxLines)
        // A cached paragraph can retain a wider allocation than the measured text.
        // Check actual line extents, not the unused paragraph width.
        repeat(layout.lineCount) { line ->
            assertFalse(message, layout.isLineEllipsized(line))
            assertTrue(message, layout.getLineLeft(line) >= 0f)
            assertTrue(message, layout.getLineRight(line) <= layout.size.width)
        }
    }

    private fun string(resId: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(resId)

    @Composable
    private fun LargeTextTheme(
        darkTheme: Boolean,
        fontScale: Float = 2f,
        content: @Composable () -> Unit,
    ) {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
            VirGoTheme(darkTheme = darkTheme, content = content)
        }
    }
}
