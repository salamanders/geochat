package info.benjaminhill.geochat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import info.benjaminhill.geochat.ui.radar.RadarFeedScreen
import info.benjaminhill.geochat.ui.theme.GeochatTheme

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