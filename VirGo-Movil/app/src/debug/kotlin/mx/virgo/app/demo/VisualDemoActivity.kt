package mx.virgo.app.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import mx.virgo.core.designsystem.theme.VirGoTheme

/** Separate debug-only, offline showcase. Never creates an authenticated session. */
class VisualDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VirGoTheme {
                VisualDemoApp(onExit = { finish() })
            }
        }
    }
}
