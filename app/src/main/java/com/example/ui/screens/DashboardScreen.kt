package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DualCurrencyBadge
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.viewmodel.ErpModule
import com.example.viewmodel.ErpViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ErpViewModel,
    onNavigate: (ErpModule) -> Unit
) {
    val sales by viewModel.sales.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val lowStock by viewModel.lowStockProducts.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val activeSession by viewModel.activeCashSession.collectAsState()
    val company by viewModel.company.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    val todayMillis = System.currentTimeMillis() - 86400000
    val todaySales = sales.filter { it.date >= todayMillis && it.paymentStatus != "ANULADA" }
    val totalSalesTodayUsd = todaySales.sumOf { it.totalUsd }

    val totalCxcUsd = clients.sumOf { it.currentBalanceUsd }
    val totalCxpUsd = providers.sumOf { it.currentBalanceUsd }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(12.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Welcome & Company banner
        item {
            Surface(
                color = Color(0xFF1E3A8A), // Classic Enterprise Blue
                shape = RoundedCornerShape(6.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = company?.businessName ?: "DISTRIBUIDORA & COMERCIALIZADORA LOS ANDES C.A.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "RIF: ${company?.rif ?: "J-40123456-7"} | Período Fiscal: 2026 | Sistema Administrativo a2",
                            fontSize = 11.sp,
                            color = Color(0xFF93C5FD)
                        )
                    }

                    Surface(
                        color = Color(0xFF1E40AF),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF60A5FA))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(text = "Tasa BCV Oficial", fontSize = 9.sp, color = Color(0xFFBFDBFE))
                            Text(
                                text = "${String.format(Locale.US, "%.2f", rate)} Bs./USD",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFFFDE68A)
                            )
                        }
                    }
                }
            }
        }

        // Quick KPI Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "Ventas de Hoy (${todaySales.size})",
                    amountUsd = totalSalesTodayUsd,
                    rate = rate,
                    icon = Icons.Default.TrendingUp,
                    accentColor = Color(0xFF15803D),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ErpModule.HISTORIAL_VENTAS) }
                )
                KpiCard(
                    title = "Por Cobrar (CxC)",
                    amountUsd = totalCxcUsd,
                    rate = rate,
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ErpModule.CXC) }
                )
                KpiCard(
                    title = "Por Pagar (CxP)",
                    amountUsd = totalCxpUsd,
                    rate = rate,
                    icon = Icons.Default.Payment,
                    accentColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ErpModule.CXP) }
                )
            }
        }

        // Quick Operations Panel
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Accesos Rápidos a Módulos Operativos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionTile(
                            icon = Icons.Default.PointOfSale,
                            title = "Facturar POS",
                            subtitle = "Emisión bimoneda",
                            color = Color(0xFF1E3A8A),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(ErpModule.VENTAS_POS) }
                        )
                        ActionTile(
                            icon = Icons.Default.AddShoppingCart,
                            title = "Recepción Compra",
                            subtitle = "Factura de proveedor",
                            color = Color(0xFF0D9488),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(ErpModule.COMPRAS) }
                        )
                        ActionTile(
                            icon = Icons.Default.Inventory2,
                            title = "Artículos & Stock",
                            subtitle = "Kardex y precios",
                            color = Color(0xFF7C3AED),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(ErpModule.PRODUCTOS) }
                        )
                        ActionTile(
                            icon = Icons.Default.Summarize,
                            title = "Libros IVA",
                            subtitle = "Ventas / Compras",
                            color = Color(0xFFB45309),
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(ErpModule.REPORTES_FISCALES) }
                        )
                    }
                }
            }
        }

        // Low stock warning banner if any
        if (lowStock.isNotEmpty()) {
            item {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Alerta de Stock Mínimo (${lowStock.size} productos críticos)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        lowStock.take(3).forEach { prod ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${prod.code} - ${prod.name}", fontSize = 11.sp, color = Color(0xFF7F1D1D))
                                Text(
                                    text = "Stock: ${prod.currentStock.toInt()} / Mín: ${prod.minStock.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Invoices / Sales Table
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Últimas Ventas Registradas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF0F172A)
                        )
                        TextButton(onClick = { onNavigate(ErpModule.HISTORIAL_VENTAS) }) {
                            Text("Ver todas", fontSize = 11.sp)
                        }
                    }

                    if (sales.isEmpty()) {
                        Text(text = "No hay ventas registradas aún.", fontSize = 11.sp, color = Color(0xFF64748B))
                    } else {
                        sales.take(5).forEach { sale ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openPrintDialog(sale.id) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = sale.docNumber, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E3A8A))
                                        StatusBadge(status = sale.paymentStatus)
                                    }
                                    Text(text = "${sale.clientName} (${sale.clientRif})", fontSize = 10.sp, color = Color(0xFF475569))
                                }

                                DualCurrencyBadge(amountUsd = sale.totalUsd, bcvRate = sale.bcvRate)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    amountUsd: Double,
    rate: Double,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = Formatters.formatUsd(amountUsd),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            Text(
                text = Formatters.formatVes(amountUsd * rate),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = accentColor
            )
        }
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = color)
            Text(text = subtitle, fontSize = 9.sp, color = Color(0xFF64748B))
        }
    }
}
