package dev.calb456.iptimewol.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DiscoveredRoutersCard(
    gatewayIp: String?,
    productNames: Map<String, String>,
    loadingIps: Set<String>,
    onIpClick: (String) -> Unit,
    onRefreshClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),// Light grey card
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RectangleShape
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "검색된 공유기",
                    fontSize = 16.sp, // Same as list text size
                    color = Color(0xFF333333), // Dark grey
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRefreshClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
            gatewayIp?.let { ip ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onIpClick(ip) }
                        .padding(vertical = 4.dp) // Adjusted padding
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val productName = productNames[ip]
                        Text(
                            text = productName ?: "Unknown Router",
                            fontSize = 16.sp,
                            color = Color.Black, // White text
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ip,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                    if (loadingIps.contains(ip)) {
                        Spacer(modifier = Modifier.size(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                }
            } ?: Text(
                text = "게이트웨이 IP를 찾을 수 없습니다.",
                fontSize = 16.sp,
                color = Color.Black, // White text
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
