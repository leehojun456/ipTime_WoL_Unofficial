package dev.calb456.iptimewol.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
fun AddRouterScreen(
    onBack: () -> Unit,
    onConfirm: (Router, String?, isFinalAdd: Boolean) -> Unit,
    initialIpAddress: String,
    initialRouterName: String,
    viewModel: MainViewModel,
    addRouterResult: AddRouterResult,
    isCaptchaRequired: Boolean,
    captchaImageUrl: String?,
    partialRouter: Router? = null,
    showExtendedFields: Boolean
) {
    var routerName by remember(initialRouterName) { mutableStateOf(initialRouterName) }
    var ipAddress by remember(initialIpAddress) { mutableStateOf(initialIpAddress) }
    
    // These fields reset when a new router is picked (initialIpAddress changes)
    var managementPort by remember(initialIpAddress) { mutableStateOf(partialRouter?.managementPort?.toString() ?: "80") }
    var loginId by remember(initialIpAddress) { mutableStateOf(partialRouter?.loginId ?: "") }
    var password by remember(initialIpAddress) { mutableStateOf(partialRouter?.password ?: "") }
    var captchaInput by remember(initialIpAddress) { mutableStateOf("") }
    
    // These fields are for the extended section
    var externalIpAddress by remember(initialIpAddress) { mutableStateOf("") }
    var ddnsAddress by remember(initialIpAddress) { mutableStateOf("") }
    var ddnsStatus by remember(initialIpAddress) { mutableStateOf("") }
    var remoteAccessPort by remember(initialIpAddress) { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val (nameFocus, ipFocus, portFocus, idFocus, passwordFocus, captchaFocus, extIpFocus, ddnsFocus, ddnsStatusFocus, remotePortFocus) = remember { FocusRequester.createRefs() }

    LaunchedEffect(showExtendedFields) {
        if (showExtendedFields) {
            // This will be triggered when MainScreen sets showExtendedFields to true
            extIpFocus.requestFocus()
            // If we have a partial router, populate the fields from it
            partialRouter?.let {
                routerName = it.name
                ipAddress = it.ipAddress
                managementPort = it.managementPort.toString()
                loginId = it.loginId
                password = it.password
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("공유기 추가") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.imePadding()) {
                Button(
                    onClick = {
                        if (!showExtendedFields) {
                            val port = managementPort.toIntOrNull() ?: 80
                            val routerToLogin = Router(
                                name = routerName,
                                ipAddress = ipAddress,
                                managementPort = port,
                                loginId = loginId,
                                password = password
                            )
                            onConfirm(routerToLogin, if (isCaptchaRequired) captchaInput else null, false)
                            // ViewModel will handle setting showExtendedFields through MainScreen -> AddRouterResult.ShowExtendedFields
                        } else {
                            val port = managementPort.toIntOrNull() ?: 80
                            val remotePort = remoteAccessPort.toIntOrNull()
                            val finalRouter = Router(
                                name = routerName,
                                ipAddress = ipAddress,
                                managementPort = port,
                                loginId = loginId,
                                password = password,
                                externalIpAddress = externalIpAddress.ifBlank { null },
                                ddnsAddress = ddnsAddress.ifBlank { null },
                                ddnsStatus = ddnsStatus.ifBlank { null },
                                remoteAccessPort = remotePort
                            )
                            onConfirm(finalRouter, null, true)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    enabled = routerName.isNotBlank() && ipAddress.isNotBlank() && managementPort.isNotBlank() && addRouterResult !is AddRouterResult.InProgress
                ) {
                    if (addRouterResult is AddRouterResult.InProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (showExtendedFields) "추가" else "다음")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                colors = textFieldColors,
                enabled = !showExtendedFields
            )

            TextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("내부 IP 주소") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { portFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(ipFocus),
                singleLine = true,
                colors = textFieldColors,
                enabled = !showExtendedFields
            )

            TextField(
                value = managementPort,
                onValueChange = { managementPort = it },
                label = { Text("관리 포트") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { idFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(portFocus),
                singleLine = true,
                colors = textFieldColors,
                enabled = !showExtendedFields
            )

            TextField(
                value = loginId,
                onValueChange = { loginId = it },
                label = { Text("로그인 ID") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(idFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                colors = textFieldColors,
                enabled = !showExtendedFields
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("암호") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if(isCaptchaRequired || showExtendedFields) ImeAction.Next else ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (isCaptchaRequired) captchaFocus.requestFocus()
                        else if (showExtendedFields) extIpFocus.requestFocus()
                        else focusManager.clearFocus()
                    },
                    onDone = {
                        if (showExtendedFields) extIpFocus.requestFocus()
                        else focusManager.clearFocus()
                    }
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(passwordFocus),
                singleLine = true,
                colors = textFieldColors,
                enabled = !showExtendedFields
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
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = if (showExtendedFields) ImeAction.Next else ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (showExtendedFields) extIpFocus.requestFocus() else focusManager.clearFocus() }),
                    colors = textFieldColors,
                    enabled = !showExtendedFields
                )
            }

            if (showExtendedFields) {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = "추가 정보 입력",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )

                TextField(
                    value = externalIpAddress,
                    onValueChange = { externalIpAddress = it },
                    label = { Text("외부 IP 주소") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(extIpFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { ddnsFocus.requestFocus() }),
                    colors = textFieldColors
                )

                TextField(
                    value = ddnsAddress,
                    onValueChange = { ddnsAddress = it },
                    label = { Text("DDNS 주소") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(ddnsFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { ddnsStatusFocus.requestFocus() }),
                    colors = textFieldColors
                )

                TextField(
                    value = ddnsStatus,
                    onValueChange = { ddnsStatus = it },
                    label = { Text("DDNS 등록상태") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(ddnsStatusFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { remotePortFocus.requestFocus() }),
                    colors = textFieldColors
                )

                TextField(
                    value = remoteAccessPort,
                    onValueChange = { remoteAccessPort = it },
                    label = { Text("원격접속 포트") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).focusRequester(remotePortFocus),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = textFieldColors
                )
            }
        }
    }
}
