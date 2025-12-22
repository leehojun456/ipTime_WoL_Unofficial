package dev.calb456.iptimewol

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dev.calb456.iptimewol.ui.MainScreen
import dev.calb456.iptimewol.ui.theme.IptimeWolTheme

class MainActivity : ComponentActivity() {
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this) {
            if (System.currentTimeMillis() > backPressedTime + 2000) {
                backPressedTime = System.currentTimeMillis()
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout = inflater.inflate(R.layout.custom_toast, null)
                val text = layout.findViewById<TextView>(R.id.toast_text)
                text.text = "한번 더 뒤로가기 하시면 앱이 종료됩니다."

                with(Toast(applicationContext)) {
                    duration = Toast.LENGTH_SHORT
                    view = layout
                    show()
                }
            } else {
                finish()
            }
        }

        setContent {
            IptimeWolTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
