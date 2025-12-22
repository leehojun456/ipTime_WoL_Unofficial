package dev.calb456.iptimewol.ui.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.calb456.iptimewol.data.Router
import dev.calb456.iptimewol.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRouterSheet(
    onDismiss: () -> Unit,
    onConfirm: (Router) -> Unit,
    initialIpAddress: String,
    initialRouterName: String,
    viewModel: MainViewModel
) {
    var routerName by remember(initialRouterName) { mutableStateOf(initialRouterName) }
    var ipAddress by remember(initialIpAddress) { mutableStateOf(initialIpAddress) }
    var managementPort by remember { mutableStateOf("80") } // Default port
    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val (nameFocus, ipFocus, portFocus, idFocus, passwordFocus) = remember { FocusRequester.createRefs() }

    Surface {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(), // Add vertical scrolling
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "공유기 추가",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = routerName,
                onValueChange = { routerName = it },
                label = { Text("공유기 이름") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .focusRequester(nameFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { ipFocus.requestFocus() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary, // Color for the focused bottom line
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant, // Color for the unfocused bottom line
                    disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    errorIndicatorColor = MaterialTheme.colorScheme.error
                )
            )

            TextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("내부 IP 주소") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { portFocus.requestFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .focusRequester(ipFocus),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    errorIndicatorColor = MaterialTheme.colorScheme.error
                )
            )

            TextField(
                value = managementPort,
                onValueChange = { managementPort = it },
                label = { Text("관리 포트") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { idFocus.requestFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .focusRequester(portFocus),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    errorIndicatorColor = MaterialTheme.colorScheme.error
                )
            )

            TextField(
                value = loginId,
                onValueChange = { loginId = it },
                label = { Text("로그인 ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .focusRequester(idFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    errorIndicatorColor = MaterialTheme.colorScheme.error
                )
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("암호") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .focusRequester(passwordFocus),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    errorIndicatorColor = MaterialTheme.colorScheme.error
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val port = managementPort.toIntOrNull() ?: 80
                    val newRouter = Router(
                        name = routerName,
                        ipAddress = ipAddress,
                        managementPort = port,
                        loginId = loginId,
                        password = password
                    )
                    onConfirm(newRouter)
                },
                modifier = Modifier.fillMaxWidth(), // Make the button fill max width
                enabled = routerName.isNotBlank() && ipAddress.isNotBlank() && managementPort.isNotBlank()
            ) {
                Text("추가")
            }
        }
    }
}
