package dev.calb456.iptimewol.ui.sheet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.calb456.iptimewol.data.Router
import dev.calb456.iptimewol.ui.AddRouterResult
import dev.calb456.iptimewol.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRouterSheet(
    onDismiss: () -> Unit,
    onConfirm: (Router, String?) -> Unit,
    initialIpAddress: String,
    initialRouterName: String,
    viewModel: MainViewModel,
    addRouterResult: AddRouterResult,
    isCaptchaRequired: Boolean,
    captchaImageUrl: String?
) {
    var routerName by remember(initialRouterName) { mutableStateOf(initialRouterName) }
    var ipAddress by remember(initialIpAddress) { mutableStateOf(initialIpAddress) }
    var managementPort by remember { mutableStateOf("80") } // Default port
    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var captchaInput by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val (nameFocus, ipFocus, portFocus, idFocus, passwordFocus, captchaFocus) = remember { FocusRequester.createRefs() }

    Surface {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "공유기 추가",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                errorIndicatorColor = MaterialTheme.colorScheme.error
            )

            TextField(
                value = routerName,
                onValueChange = { routerName = it },
                label = { Text("공유기 이름") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(nameFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { ipFocus.requestFocus() }),
                colors = textFieldColors
            )

            TextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("내부 IP 주소") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { portFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(ipFocus),
                singleLine = true,
                colors = textFieldColors
            )

            TextField(
                value = managementPort,
                onValueChange = { managementPort = it },
                label = { Text("관리 포트") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { idFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(portFocus),
                singleLine = true,
                colors = textFieldColors
            )

            TextField(
                value = loginId,
                onValueChange = { loginId = it },
                label = { Text("로그인 ID") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(idFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                colors = textFieldColors
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("암호") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if(isCaptchaRequired) ImeAction.Next else ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onNext = { captchaFocus.requestFocus() },
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(passwordFocus),
                singleLine = true,
                colors = textFieldColors
            )

            if (isCaptchaRequired) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = captchaImageUrl,
                    contentDescription = "Captcha Image",
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                )
                TextField(
                    value = captchaInput,
                    onValueChange = { captchaInput = it },
                    label = { Text("보안문자") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(captchaFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = textFieldColors
                )
            }

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
                    onConfirm(newRouter, if (isCaptchaRequired) captchaInput else null)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = routerName.isNotBlank() && ipAddress.isNotBlank() && managementPort.isNotBlank() && addRouterResult !is AddRouterResult.InProgress
            ) {
                if (addRouterResult is AddRouterResult.InProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("추가")
                }
            }
        }
    }
}
