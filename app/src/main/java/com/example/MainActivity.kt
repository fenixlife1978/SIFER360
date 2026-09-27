package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.ErpTheme
import com.example.viewmodel.ErpModule
import com.example.viewmodel.ErpViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ErpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ErpTheme {
                ErpApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ErpApp(viewModel: ErpViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentModule by viewModel.currentModule.collectAsState()
    val openTabs by viewModel.openTabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val allRates by viewModel.allRates.collectAsState()
    val company by viewModel.company.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeSession by viewModel.activeCashSession.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val printedDocId by viewModel.printedDocumentId.collectAsState()

    var showRateDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Column {
                    // Top Classic Menu Bar
                    ErpTopMenuBar(
                        onNavigate = { mod -> viewModel.openModule(mod) },
                        onShowRateDialog = { showRateDialog = true },
                        onShowAboutDialog = { showAboutDialog = true },
                        onLogout = { viewModel.logout() }
                    )
                    // Toolbar
                    ErpToolBar(
                        onNavigate = { mod -> viewModel.openModule(mod) },
                        onOpenRateDialog = { showRateDialog = true }
                    )
                    // MDI Tab Bar
                    ErpTabBar(
                        tabs = openTabs,
                        activeTabId = activeTabId,
                        onSelectTab = { tabId -> viewModel.selectTab(tabId) },
                        onCloseTab = { tabId -> viewModel.closeTab(tabId) }
                    )
                }
            },
            bottomBar = {
                // Enterprise Bottom Status Bar
                ErpStatusBar(
                    latestRate = latestRate,
                    company = company,
                    user = currentUser,
                    activeSession = activeSession,
                    onOpenRateDialog = { showRateDialog = true }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF1F5F9))
            ) {
                when (currentModule) {
                    ErpModule.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { mod -> viewModel.openModule(mod) }
                    )
                    ErpModule.VENTAS_POS -> PosSalesScreen(
                        viewModel = viewModel,
                        onSaleCompleted = { docId ->
                            viewModel.openPrintDialog(docId)
                        }
                    )
                    ErpModule.HISTORIAL_VENTAS -> SalesHistoryScreen(
                        viewModel = viewModel,
                        onViewDocument = { docId -> viewModel.openPrintDialog(docId) }
                    )
                    ErpModule.PRODUCTOS -> ProductsScreen(viewModel = viewModel)
                    ErpModule.INVENTARIO_KARDEX, ErpModule.AJUSTES_INVENTARIO -> InventoryScreen(viewModel = viewModel)
                    ErpModule.COMPRAS -> PurchasesScreen(
                        viewModel = viewModel,
                        onPurchaseSuccess = { viewModel.openModule(ErpModule.HISTORIAL_COMPRAS) }
                    )
                    ErpModule.HISTORIAL_COMPRAS -> PurchasesHistoryScreen(viewModel = viewModel)
                    ErpModule.CLIENTES -> ClientsScreen(viewModel = viewModel)
                    ErpModule.PROVEEDORES -> ProvidersScreen(viewModel = viewModel)
                    ErpModule.CXC -> CxcScreen(viewModel = viewModel)
                    ErpModule.CXP -> CxpScreen(viewModel = viewModel)
                    ErpModule.CAJA_BANCOS -> CashBankScreen(viewModel = viewModel)
                    ErpModule.CONTABILIDAD -> AccountingScreen(viewModel = viewModel)
                    ErpModule.REPORTES_FISCALES -> ReportsScreen(viewModel = viewModel)
                    ErpModule.AUDITORIA -> AuditSecurityScreen(viewModel = viewModel)
                    ErpModule.CONFIGURACION -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Overlays
    if (showRateDialog) {
        ExchangeRateDialog(
            currentRate = latestRate,
            rateHistory = allRates,
            onUpdateRate = { newRate, source ->
                viewModel.updateBcvRate(newRate, source)
            },
            onDismiss = { showRateDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    printedDocId?.let { docId ->
        ErpVoucherDialog(
            docId = docId,
            viewModel = viewModel,
            onDismiss = { viewModel.dismissPrintDialog() }
        )
    }
}
