package info.benjaminhill.geochat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.benjaminhill.geochat.ui.radar.PostWithMeta
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProximityMessageRow(
    postWithMeta: PostWithMeta,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = dateFormat.format(postWithMeta.post.timestamp)
    val distanceStr = "${postWithMeta.distanceMeters.toInt()}m"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .alpha(postWithMeta.alpha)
    ) {
        // Message Text
        Text(
            text = postWithMeta.post.text,
            fontSize = postWithMeta.fontSize.sp,
            fontWeight = if (postWithMeta.relevance > 0.7f) FontWeight.Bold else FontWeight.Normal,
            lineHeight = (postWithMeta.fontSize * 1.2).sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Metadata (Caption)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = postWithMeta.post.plusCode,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = distanceStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
