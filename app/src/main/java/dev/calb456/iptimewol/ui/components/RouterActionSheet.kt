package dev.calb456.iptimewol.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.calb456.iptimewol.data.Router

@Composable
fun RouterActionSheet(
    router: Router,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAccessManagement: (Router) -> Unit,
    onShowInfo: () -> Unit
) {
    Surface {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(vertical = 8.dp)
        ) {
            ListItem(
                headlineContent = { Text("Access Management Console") },
                leadingContent = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Access Management"
                    )
                },
                modifier = Modifier.clickable { onAccessManagement(router) }
            )
            ListItem(
                headlineContent = { Text("Edit") },
                leadingContent = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                modifier = Modifier.clickable { onEdit() }
            )
            ListItem(
                headlineContent = { Text("Show Info") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = "Show Info") },
                modifier = Modifier.clickable { onShowInfo() }
            )
            ListItem(
                headlineContent = {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable { onDelete() }
            )
        }
    }
}
