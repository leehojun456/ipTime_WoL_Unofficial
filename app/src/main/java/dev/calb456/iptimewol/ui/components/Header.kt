package dev.calb456.iptimewol.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.calb456.iptimewol.R

@Composable
fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = R.drawable.logo,
            contentDescription = "logo",
            Modifier.height(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp)) // 간격
        Text(
            text = "WOL",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
