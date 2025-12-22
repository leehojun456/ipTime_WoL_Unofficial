package dev.calb456.iptimewol.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import dev.calb456.iptimewol.data.Router

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedRoutersCard(
    savedRouters: List<Router>,
    productNames: Map<String, String>,
    onMoreClick: (Router) -> Unit,
    onItemClick: (Router) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RectangleShape
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = "저장된 공유기",
                fontSize = 16.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Bold,
            )
            if (savedRouters.isEmpty()) {
                Text(
                    text = "저장된 공유기가 없습니다.",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                savedRouters.forEach { router ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onItemClick(router) },
                                onLongClick = { onMoreClick(router) }
                            )
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = router.name,
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val originalName = productNames[router.ipAddress]
                                if (originalName != null) {
                                    Text(
                                        text = "$originalName (${router.ipAddress})",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                } else {
                                    Text(
                                        text = router.ipAddress,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { onMoreClick(router) }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "더보기"
                            )
                        }
                    }
                }
            }
        }
    }
}
