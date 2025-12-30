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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.calb456.iptimewol.R
import dev.calb456.iptimewol.data.Router
import dev.calb456.iptimewol.ui.components.DiscoveredRoutersCard
import dev.calb456.iptimewol.ui.components.Header
import dev.calb456.iptimewol.ui.components.RouterActionSheet
import dev.calb456.iptimewol.ui.components.SavedRoutersCard
import dev.calb456.iptimewol.ui.screens.AddRouterScreen
import kotlinx.coroutines.launch
import java.io.Serializable

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier, viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val savedRouters by viewModel.allRouters.collectAsState(initial = emptyList())
    val gatewayIp by viewModel.gatewayIp.collectAsState()
    val productNames by viewModel.productNames.collectAsState()
    val loadingIps by viewModel.loadingIps.collectAsState()
    val context = LocalContext.current
    val addRouterResult by viewModel.addRouterResult.collectAsState()
    val preLoginCheckState by viewModel.preLoginCheckState.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentGatewayIp by viewModel.currentGatewayIp.collectAsState()
    val isCaptchaRequired by viewModel.isCaptchaRequired.collectAsState()
    val captchaFullUrl by viewModel.captchaFullUrl.collectAsState()
    val routerForExtendedFields by viewModel.routerForExtendedFields.collectAsState()
    val showExtendedFields by viewModel.showExtendedFields.collectAsState()
    val isWifiConnected by viewModel.isWifiConnected.collectAsState() // Observe Wi-Fi status

    val routerActionSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
    val scope = rememberCoroutineScope()
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var selectedRouter by remember { mutableStateOf<Router?>(null) }
    
    LaunchedEffect(addRouterResult) {
        when (val result = addRouterResult) {
            is AddRouterResult.ShowExtendedFields -> {
                // This is now handled in the ViewModel
            }
            is AddRouterResult.Success -> {
                viewModel.navigateTo(Screen.Home)
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
        when (preLoginCheckState) {
            is PreLoginCheckState.Ready -> {
                viewModel.navigateTo(Screen.AddRouter)
                viewModel.resetPreLoginState()
            }
            is PreLoginCheckState.Error -> {
                scope.launch {
                    Toast.makeText(context, (preLoginCheckState as PreLoginCheckState.Error).message, Toast.LENGTH_LONG).show()
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

    if (routerActionSheetState.isVisible) {
        BackHandler(true) { scope.launch { routerActionSheetState.hide() } }
    }

    if (currentScreen != Screen.Home) {
        BackHandler(true) {
            viewModel.onBackToHome()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getGatewayIp(context)
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == Screen.Home) { // Going back to Home
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut()
            } else { // Going to another screen
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut()
            }
        }, label = ""
    ) { screen ->
        when (screen) {
            is Screen.Home -> {
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
                                onAccessManagement = { routerForManagement ->
                                    val url = "http://${routerForManagement.ipAddress}:${routerForManagement.managementPort}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                    scope.launch { routerActionSheetState.hide() }
                                },
                                onShowInfo = { /*TODO*/ }
                            )
                        }
                    }
                ) {
                    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Column {
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
                                        onItemClick = { router ->
                                            Log.d(
                                                "MainScreen",
                                                "Router clicked: $router"
                                            )
                                        }
                                    )
                                }
                                item {
                                    DiscoveredRoutersCard(
                                        gatewayIp = gatewayIp,
                                        productNames = productNames,
                                        loadingIps = loadingIps,
                                        onIpClick = {
                                            viewModel.setCurrentGatewayIp(it)
                                            viewModel.handleDiscoveredRouterClick(it)
                                        },
                                        onRefreshClick = { viewModel.getGatewayIp(context) }
                                    )
                                }
                            }
                        }
                        // Display Wi-Fi status at the very bottom
                        Text(
                            text = if (isWifiConnected) "Wi-Fi 연결됨" else "Wi-Fi 연결 안 됨",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            is Screen.AddRouter -> {
                AddRouterScreen(
                    onBack = { viewModel.onBackToHome() },
                    onConfirm = { router, captchaText, isFinalAdd ->
                        viewModel.addRouter(router, captchaText, null, isFinalAdd)
                    },
                    initialIpAddress = currentGatewayIp ?: "",
                    initialRouterName = productNames[currentGatewayIp] ?: "",
                    viewModel = viewModel,
                    addRouterResult = addRouterResult,
                    isCaptchaRequired = isCaptchaRequired,
                    captchaImageUrl = captchaFullUrl,
                    partialRouter = routerForExtendedFields,
                    showExtendedFields = showExtendedFields
                )
            }
        }
    }
}