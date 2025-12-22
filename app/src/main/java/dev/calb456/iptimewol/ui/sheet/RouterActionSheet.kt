package dev.calb456.iptimewol.ui.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.calb456.iptimewol.data.Router

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouterActionSheet(
    router: Router,
    onDismiss: () -> Unit,
    onEdit: (Router) -> Unit,
    onDelete: (Router) -> Unit,
    onAccessManagement: (Router) -> Unit,
    onShowInfo: (Router) -> Unit
) {
    Surface {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth().navigationBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onEdit(router) })
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Edit, contentDescription = "수정")
                Spacer(modifier = Modifier.width(16.dp))
                Text("수정")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onDelete(router) })
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Delete, contentDescription = "삭제")
                Spacer(modifier = Modifier.width(16.dp))
                Text("삭제")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onAccessManagement(router) })
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Settings, contentDescription = "관리화면 접속")
                Spacer(modifier = Modifier.width(16.dp))
                Text("관리화면 접속")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onShowInfo(router) })
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = "공유기, 내부네트워크 정보")
                Spacer(modifier = Modifier.width(16.dp))
                Text("공유기, 내부네트워크 정보")
            }
        }
    }
}
