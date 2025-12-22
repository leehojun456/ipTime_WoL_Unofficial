package dev.calb456.iptimewol.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.calb456.iptimewol.R
import dev.calb456.iptimewol.data.Router
import dev.calb456.iptimewol.ui.components.DiscoveredRoutersCard
import dev.calb456.iptimewol.ui.components.Header
import dev.calb456.iptimewol.ui.components.SavedRoutersCard
import dev.calb456.iptimewol.ui.sheet.AddRouterSheet
import dev.calb456.iptimewol.ui.sheet.RouterActionSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier, viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    var currentGatewayIp by remember { mutableStateOf<String?>(null) }
    val savedRouters by viewModel.allRouters.collectAsState(initial = emptyList())
    val gatewayIp by viewModel.gatewayIp.collectAsState()
    val productNames by viewModel.productNames.collectAsState()
    val loadingIps by viewModel.loadingIps.collectAsState()
    val context = LocalContext.current
    val addRouterResult by viewModel.addRouterResult.collectAsState()
    val preLoginCheckState by viewModel.preLoginCheckState.collectAsState()

    val addRouterSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
    val routerActionSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var selectedRouter by remember { mutableStateOf<Router?>(null) }

    var isCaptchaRequiredForSheet by remember { mutableStateOf(false) }
    var captchaRelativeUrlForSheet by remember { mutableStateOf<String?>(null) }
    var captchaFullUrlForSheet by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(addRouterResult) {
        when (val result = addRouterResult) {
            is AddRouterResult.Success -> {
                scope.launch { addRouterSheetState.hide() }
                viewModel.resetAddRouterResult()
            }
            is AddRouterResult.Error -> {
                scope.launch {
                    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val layout = inflater.inflate(R.layout.custom_toast, null)
                    val text = layout.findViewById<TextView>(R.id.toast_text)
                    text.text = result.message

                    with(Toast(context)) {
                        duration = Toast.LENGTH_LONG
                        view = layout
                        show()
                    }
                }
                viewModel.resetAddRouterResult()
            }
            else -> {
                // InProgress or Idle
            }
        }
    }

    LaunchedEffect(preLoginCheckState) {
        when (val state = preLoginCheckState) {
            is PreLoginCheckState.Ready -> {
                isCaptchaRequiredForSheet = state.isCaptchaRequired
                captchaRelativeUrlForSheet = state.captchaRelativeUrl
                captchaFullUrlForSheet = state.captchaRelativeUrl?.let { "http://${currentGatewayIp}$it" }
                scope.launch { addRouterSheetState.show() }
                viewModel.resetPreLoginState()
            }
            is PreLoginCheckState.Error -> {
                scope.launch {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                viewModel.resetPreLoginState()
            }
            else -> { /* Idle or Loading */
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("삭제 확인") },
            text = { Text("정말로 '${selectedRouter?.name}' 공유기를 삭제하시겠습니까?") },
            confirmButton = {
                Button(onClick = {
                    selectedRouter?.let { viewModel.deleteRouterById(it.id) }
                    showDeleteConfirmationDialog = false
                    selectedRouter = null
                }) { Text("삭제") }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmationDialog = false }) { Text("취소") }
            }
        )
    }

    if (addRouterSheetState.isVisible) {
        BackHandler(true) { scope.launch { addRouterSheetState.hide() } }
    }
    if (routerActionSheetState.isVisible) {
        BackHandler(true) { scope.launch { routerActionSheetState.hide() } }
    }

    LaunchedEffect(addRouterSheetState.isVisible) {
        if (!addRouterSheetState.isVisible) {
            focusManager.clearFocus()
            isCaptchaRequiredForSheet = false
            captchaRelativeUrlForSheet = null
            captchaFullUrlForSheet = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getGatewayIp(context)
    }

    ModalBottomSheetLayout(
        sheetState = addRouterSheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            AddRouterSheet(
                onDismiss = { scope.launch { addRouterSheetState.hide() } },
                onConfirm = { router, captchaText ->
                    viewModel.addRouter(router, captchaText, captchaRelativeUrlForSheet)
                },
                initialIpAddress = currentGatewayIp ?: "",
                initialRouterName = productNames[currentGatewayIp] ?: "",
                viewModel = viewModel,
                addRouterResult = addRouterResult,
                isCaptchaRequired = isCaptchaRequiredForSheet,
                captchaImageUrl = captchaFullUrlForSheet
            )
        }
    ) {
        ModalBottomSheetLayout(
            sheetState = routerActionSheetState,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetContent = {
                selectedRouter?.let { router ->
                    RouterActionSheet(
                        router = router,
                        onDismiss = { scope.launch { routerActionSheetState.hide() } },
                        onEdit = { /*TODO*/ },
                        onDelete = {
                            scope.launch { routerActionSheetState.hide() }
                            showDeleteConfirmationDialog = true
                        },
                        onAccessManagement = { selectedRouter ->
                            val url = "http://${selectedRouter.ipAddress}:${selectedRouter.managementPort}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                            scope.launch { routerActionSheetState.hide() }
                        },
                        onShowInfo = { /*TODO*/ }
                    )
                }
            }
        ) {
            Column(modifier = modifier.fillMaxSize()) {
                Header()
                LazyColumn {
                    item {
                        SavedRoutersCard(
                            savedRouters = savedRouters,
                            productNames = productNames,
                            onMoreClick = { router ->
                                selectedRouter = router
                                scope.launch { routerActionSheetState.show() }
                            },
                            onItemClick = { router -> Log.d("MainScreen", "Router clicked: $router") }
                        )
                    }
                    item {
                        DiscoveredRoutersCard(
                            gatewayIp = gatewayIp,
                            productNames = productNames,
                            loadingIps = loadingIps,
                            onIpClick = {
                                currentGatewayIp = it
                                viewModel.handleDiscoveredRouterClick(it)
                            },
                            onRefreshClick = { viewModel.getGatewayIp(context) }
                        )
                    }
                }
            }
        }
    }
}