package info.benjaminhill.geochat.ui.radar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import info.benjaminhill.geochat.ui.components.ProximityMessageRow

/**
 * The main screen of the application displaying the "Radar" feed.
 *
 * **Purpose:**
 * This Composable is the visual container for the chat experience. It assembles the list of
 * messages, the input bar, and the debug controls into a coherent screen.
 *
 * **Architecture:**
 * - **Layer:** UI (Screen).
 * - **Relations:**
 *   - Observes [RadarViewModel] for state changes.
 *   - Renders a list of [ProximityMessageRow]s.
 *   - Handles user input and delegates actions (send, move) back to the ViewModel.
 *
 * **Why keep it?**
 * It is the primary "View" in the MVVM pattern for the chat feature.
 */
@Composable
fun RadarFeedScreen(
    viewModel: RadarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var messageText by remember { mutableStateOf("") }

    // Auto-scroll to bottom (newest) when new items arrive?
    // Spec says: "reverseLayout = true (newest at bottom)"
    // And "Live Mode: The list auto-scrolls to the bottom when new messages arrive unless the user is scrolling up."

    // With reverseLayout=true, index 0 is at the bottom.
    // So usually adding a new item at index 0 (which is bottom) naturally keeps it visible if we are at the bottom.
    // However, we sorted by timestamp descending [Newest, ..., Oldest].
    // So Index 0 is Newest.
    // If reverseLayout=true, Index 0 is at the Bottom. Perfect.

    Scaffold(
        bottomBar = {
            InputBar(
                text = messageText,
                onTextChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            Column(Modifier.fillMaxSize()) {
                // Info Bar
                if (uiState.debugRangeInfo.isNotEmpty()) {
                    Text(
                        text = uiState.debugRangeInfo,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Debug Controls
                DebugControls(
                    onMove = { lat, lng -> viewModel.debugMoveUser(lat, lng) }
                )

                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.posts, key = { it.post.id }) { postMeta ->
                        ProximityMessageRow(postWithMeta = postMeta)
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}

/**
 * A UI component for typing and sending messages.
 *
 * **Purpose:**
 * Captures user text input and triggers the send action.
 */
@Composable
fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Broadcast to nearby...") },
            maxLines = 3
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSend) {
            Icon(Icons.Default.Send, contentDescription = "Send")
        }
    }
}

/**
 * A temporary UI component for simulating movement in the mock environment.
 *
 * **Purpose:**
 * Allows the developer to "walk" around the virtual world by modifying the mock location
 * provided by [info.benjaminhill.geochat.data.repository.MockLocationRepository].
 */
@Composable
fun DebugControls(
    onMove: (Double, Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Debug: Move User (Simulate Walking)", style = MaterialTheme.typography.labelMedium)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onMove(0.0005, 0.0) }) { // ~50m North
                    Icon(Icons.Default.KeyboardArrowUp, "North")
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onMove(0.0, -0.0005) }) { // ~50m West
                    Icon(Icons.Default.ArrowBack, "West")
                }
                IconButton(onClick = { onMove(0.0, 0.0005) }) { // ~50m East
                    Icon(Icons.Default.ArrowForward, "East")
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onMove(-0.0005, 0.0) }) { // ~50m South
                    Icon(Icons.Default.KeyboardArrowDown, "South")
                }
            }
        }
    }
}
