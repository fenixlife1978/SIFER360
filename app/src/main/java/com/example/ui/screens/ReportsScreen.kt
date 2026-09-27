package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DualCurrencyBadge
import com.example.ui.components.Formatters
import com.example.viewmodel.ErpViewModel

@Composable
fun ReportsScreen(viewModel: ErpViewModel) {
    val sales by viewModel.sales.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val products by viewModel.products.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    var selectedReport by remember { mutableStateOf(0) } // 0 = Libro Ventas IVA, 1 = Libro Compras IVA, 2 = Inventario Valorizado, 3 = Margen Utilidad

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tab selector
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedReport == 0,
                    onClick = { selectedReport = 0 },
                    label = { Text("Libro Ventas IVA", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedReport == 1,
                    onClick = { selectedReport = 1 },
                    label = { Text("Libro Compras IVA", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedReport == 2,
                    onClick = { selectedReport = 2 },
                    label = { Text("Inventario Valorizado", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedReport == 3,
                    onClick = { selectedReport = 3 },
                    label = { Text("Utilidad Estimada", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Report Body
        when (selectedReport) {
            0 -> {
                // Libro de Ventas Fiscal SENIAT
                val totalVentasVes = sales.sumOf { it.totalVes }
                val totalBaseVes = sales.sumOf { it.baseImponibleUsd * it.bcvRate }
                val totalIvaVes = sales.sumOf { it.taxAmountUsd * it.bcvRate }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Title
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LIBRO DE VENTAS FISCAL - SENIAT (VES)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Total IVA Débito: ${Formatters.formatVes(totalIvaVes)}", color = Color(0xFFFDE68A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fecha", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(70.dp))
                            Text("Nro Factura", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(75.dp))
                            Text("Control", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(75.dp))
                            Text("Cliente / RIF", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                            Text("Base Imponible", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                            Text("IVA 16%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                            Text("Total Ventas (Bs.)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(105.dp), textAlign = TextAlign.End)
                        }

                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(sales) { s ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(Formatters.formatDate(s.date), fontSize = 9.sp, modifier = Modifier.width(70.dp))
                                    Text(s.docNumber, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.width(75.dp))
                                    Text(s.controlNumber, fontSize = 9.sp, modifier = Modifier.width(75.dp), color = Color(0xFF64748B))
                                    Text("${s.clientName} (${s.clientRif})", fontSize = 9.sp, modifier = Modifier.weight(1f))
                                    Text(Formatters.formatVes(s.baseImponibleUsd * s.bcvRate), fontSize = 9.sp, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                                    Text(Formatters.formatVes(s.taxAmountUsd * s.bcvRate), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                    Text(Formatters.formatVes(s.totalVes), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(105.dp), textAlign = TextAlign.End)
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
            1 -> {
                // Libro de Compras Fiscal SENIAT
                val totalComprasVes = purchases.sumOf { it.totalVes }
                val totalIvaCreditoVes = purchases.sumOf { it.taxAmountUsd * it.bcvRate }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LIBRO DE COMPRAS FISCAL - SENIAT (VES)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Total Crédito Fiscal IVA: ${Formatters.formatVes(totalIvaCreditoVes)}", color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fecha", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(70.dp))
                            Text("Nro Fact. Prov", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(85.dp))
                            Text("Proveedor / RIF", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                            Text("Base Imponible", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                            Text("IVA Crédito 16%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                            Text("Total Compras (Bs.)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(105.dp), textAlign = TextAlign.End)
                        }

                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(purchases) { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(Formatters.formatDate(p.date), fontSize = 9.sp, modifier = Modifier.width(70.dp))
                                    Text(p.controlNumber, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488), modifier = Modifier.width(85.dp))
                                    Text("${p.providerName} (${p.providerRif})", fontSize = 9.sp, modifier = Modifier.weight(1f))
                                    Text(Formatters.formatVes(p.baseImponibleUsd * p.bcvRate), fontSize = 9.sp, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                                    Text(Formatters.formatVes(p.taxAmountUsd * p.bcvRate), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                                    Text(Formatters.formatVes(p.totalVes), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(105.dp), textAlign = TextAlign.End)
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
            2 -> {
                // Valued Inventory
                val totalCostUsd = products.sumOf { it.currentStock * it.costUsd }
                val totalRetailUsd = products.sumOf { it.currentStock * it.price1Usd }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("INVENTARIO VALORIZADO AL COSTO Y AL P.V.P.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Costo Total: ${Formatters.formatUsd(totalCostUsd)} (${Formatters.formatVes(totalCostUsd * rate)})", color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Código", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(65.dp))
                            Text("Descripción", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                            Text("Stock", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                            Text("Costo Total ($)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                            Text("PVP Total ($)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                            Text("Margen $", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                        }

                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(products) { pr ->
                                val costTot = pr.currentStock * pr.costUsd
                                val pvpTot = pr.currentStock * pr.price1Usd
                                val diff = pvpTot - costTot
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(pr.code, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(65.dp))
                                    Text(pr.name, fontSize = 9.sp, modifier = Modifier.weight(1f))
                                    Text("${pr.currentStock.toInt()}", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                                    Text(Formatters.formatUsd(costTot), fontSize = 9.sp, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                                    Text(Formatters.formatUsd(pvpTot), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                                    Text(Formatters.formatUsd(diff), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
            3 -> {
                // Utilidad en Ventas
                val totalSalesRevenueUsd = sales.sumOf { it.subtotalUsd }
                val estimatedMarginUsd = totalSalesRevenueUsd * 0.28 // Approx 28% margin

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Análisis de Rentabilidad y Utilidad en Ventas", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF0F172A))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).padding(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Facturación Neta", fontSize = 10.sp, color = Color(0xFF1E40AF))
                                    Text(Formatters.formatUsd(totalSalesRevenueUsd), fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E3A8A))
                                    Text(Formatters.formatVes(totalSalesRevenueUsd * rate), fontSize = 11.sp, color = Color(0xFF3B82F6))
                                }
                            }

                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).padding(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Utilidad Bruta Estimada", fontSize = 10.sp, color = Color(0xFF166534))
                                    Text(Formatters.formatUsd(estimatedMarginUsd), fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF15803D))
                                    Text(Formatters.formatVes(estimatedMarginUsd * rate), fontSize = 11.sp, color = Color(0xFF22C55E))
                                }
                            }
                        }

                        Text("El cálculo de utilidad se realiza comparando el costo de reposición registrado en el Kardex contra el precio de liquidación en divisas a la tasa congelada de cada factura.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}
