package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ErpModule

@Composable
fun ErpTopMenuBar(
    onNavigate: (ErpModule) -> Unit,
    onShowRateDialog: () -> Unit,
    onShowAboutDialog: () -> Unit,
    onLogout: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf<String?>(null) }

    Surface(
        color = Color(0xFF1E293B), // Classic Slate Navy Dark Menu Bar
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Brand Emblem
            Row(
                modifier = Modifier
                    .padding(end = 12.dp, start = 4.dp)
                    .clickable { onNavigate(ErpModule.DASHBOARD) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = "a2 ERP Logo",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "a2 ERP Pro",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            // Menu Items
            MenuItem(
                title = "Archivo",
                isOpen = expandedMenu == "Archivo",
                onToggle = { expandedMenu = if (expandedMenu == "Archivo") null else "Archivo" },
                items = listOf(
                    MenuAction("Tablero General", Icons.Default.Dashboard) { onNavigate(ErpModule.DASHBOARD) },
                    MenuAction("Configuración de Empresa", Icons.Default.CorporateFare) { onNavigate(ErpModule.CONFIGURACION) },
                    MenuAction("Usuarios y Seguridad", Icons.Default.AdminPanelSettings) { onNavigate(ErpModule.AUDITORIA) },
                    MenuAction("Bloquear / Cerrar Sesión", Icons.Default.Lock) { onLogout() }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "Maestros",
                isOpen = expandedMenu == "Maestros",
                onToggle = { expandedMenu = if (expandedMenu == "Maestros") null else "Maestros" },
                items = listOf(
                    MenuAction("Productos y Servicios", Icons.Default.Inventory2) { onNavigate(ErpModule.PRODUCTOS) },
                    MenuAction("Clientes (Ficha RIF)", Icons.Default.People) { onNavigate(ErpModule.CLIENTES) },
                    MenuAction("Proveedores", Icons.Default.LocalShipping) { onNavigate(ErpModule.PROVEEDORES) },
                    MenuAction("Almacenes y Sucursales", Icons.Default.Store) { onNavigate(ErpModule.INVENTARIO_KARDEX) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "Inventario",
                isOpen = expandedMenu == "Inventario",
                onToggle = { expandedMenu = if (expandedMenu == "Inventario") null else "Inventario" },
                items = listOf(
                    MenuAction("Existencias y Kardex Bimoneda", Icons.Default.Assessment) { onNavigate(ErpModule.INVENTARIO_KARDEX) },
                    MenuAction("Ajustes de Inventario (+/-)", Icons.Default.Tune) { onNavigate(ErpModule.AJUSTES_INVENTARIO) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "Ventas",
                isOpen = expandedMenu == "Ventas",
                onToggle = { expandedMenu = if (expandedMenu == "Ventas") null else "Ventas" },
                items = listOf(
                    MenuAction("Facturación POS / Nueva Venta", Icons.Default.PointOfSale) { onNavigate(ErpModule.VENTAS_POS) },
                    MenuAction("Historial de Facturas y Ventas", Icons.Default.ReceiptLong) { onNavigate(ErpModule.HISTORIAL_VENTAS) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "Compras",
                isOpen = expandedMenu == "Compras",
                onToggle = { expandedMenu = if (expandedMenu == "Compras") null else "Compras" },
                items = listOf(
                    MenuAction("Recepción de Factura de Compra", Icons.Default.ShoppingCartCheckout) { onNavigate(ErpModule.COMPRAS) },
                    MenuAction("Historial de Compras", Icons.Default.History) { onNavigate(ErpModule.HISTORIAL_COMPRAS) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "CxC",
                isOpen = expandedMenu == "CxC",
                onToggle = { expandedMenu = if (expandedMenu == "CxC") null else "CxC" },
                items = listOf(
                    MenuAction("Cuentas por Cobrar & Abonos", Icons.Default.AccountBalanceWallet) { onNavigate(ErpModule.CXC) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "CxP",
                isOpen = expandedMenu == "CxP",
                onToggle = { expandedMenu = if (expandedMenu == "CxP") null else "CxP" },
                items = listOf(
                    MenuAction("Cuentas por Pagar & Pagos", Icons.Default.Payment) { onNavigate(ErpModule.CXP) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "Caja y Bancos",
                isOpen = expandedMenu == "Caja",
                onToggle = { expandedMenu = if (expandedMenu == "Caja") null else "Caja" },
                items = listOf(
                    MenuAction("Caja, Arqueos & Cuentas Bancarias", Icons.Default.Savings) { onNavigate(ErpModule.CAJA_BANCOS) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "Contabilidad",
                isOpen = expandedMenu == "Contabilidad",
                onToggle = { expandedMenu = if (expandedMenu == "Contabilidad") null else "Contabilidad" },
                items = listOf(
                    MenuAction("Libro Diario y Asientos", Icons.Default.MenuBook) { onNavigate(ErpModule.CONTABILIDAD) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "Reportes",
                isOpen = expandedMenu == "Reportes",
                onToggle = { expandedMenu = if (expandedMenu == "Reportes") null else "Reportes" },
                items = listOf(
                    MenuAction("Libro Fiscal IVA y Gerencial", Icons.Default.Summarize) { onNavigate(ErpModule.REPORTES_FISCALES) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "Seguridad",
                isOpen = expandedMenu == "Seguridad",
                onToggle = { expandedMenu = if (expandedMenu == "Seguridad") null else "Seguridad" },
                items = listOf(
                    MenuAction("Auditoría y Bitácora", Icons.Default.Shield) { onNavigate(ErpModule.AUDITORIA) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "Configuración",
                isOpen = expandedMenu == "Config",
                onToggle = { expandedMenu = if (expandedMenu == "Config") null else "Config" },
                items = listOf(
                    MenuAction("Tasa BCV y Parámetros", Icons.Default.CurrencyExchange) { onShowRateDialog() },
                    MenuAction("Configuración General", Icons.Default.Settings) { onNavigate(ErpModule.CONFIGURACION) }
                ),
                onDismiss = { expandedMenu = null }
            )

            MenuItem(
                title = "Ayuda",
                isOpen = expandedMenu == "Ayuda",
                onToggle = { expandedMenu = if (expandedMenu == "Ayuda") null else "Ayuda" },
                items = listOf(
                    MenuAction("Acerca de a2 ERP Venezuela", Icons.Default.Info) { onShowAboutDialog() }
                ),
                onDismiss = { expandedMenu = null }
            )
        }
    }
}

data class MenuAction(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun MenuItem(
    title: String,
    isOpen: Boolean,
    onToggle: () -> Unit,
    items: List<MenuAction>,
    onDismiss: () -> Unit
) {
    Box {
        TextButton(
            onClick = onToggle,
            modifier = Modifier.padding(horizontal = 1.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = title,
                color = if (isOpen) Color(0xFF38BDF8) else Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = onDismiss,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            items.forEach { action ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.title,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(text = action.title, fontSize = 12.sp)
                        }
                    },
                    onClick = {
                        onDismiss()
                        action.onClick()
                    }
                )
            }
        }
    }
}
