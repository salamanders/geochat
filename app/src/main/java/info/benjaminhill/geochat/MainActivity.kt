package info.benjaminhill.geochat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import info.benjaminhill.geochat.ui.radar.RadarFeedScreen
import info.benjaminhill.geochat.ui.theme.GeochatTheme

/**
 * The single Activity for the application.
 *
 * **Purpose:**
 * In a "Single Activity Architecture", this class serves as the host for the UI content.
 * It initializes the Jetpack Compose environment and sets the root content view.
 *
 * **Architecture:**
 * - **Layer:** Application Framework / UI Entry Point.
 * - **Relations:**
 *   - Hosts [RadarFeedScreen].
 *   - Wraps content in [GeochatTheme].
 *   - Annotated with `@AndroidEntryPoint` to allow Hilt to inject dependencies into the ViewModels hosted here.
 *
 * **Why keep it?**
 * It is the bridge between the Android OS and our Compose UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeochatTheme {
                RadarFeedScreen()
            }
        }
    }
}
