package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ProviderEntity
import com.example.ui.components.DualCurrencyBadge
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.viewmodel.ErpViewModel

@Composable
fun CxpScreen(viewModel: ErpViewModel) {
    val providers by viewModel.providers.collectAsState()
    val movements by viewModel.cxpMovements.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    var selectedTab by remember { mutableStateOf(0) } // 0 = Deudas Pendientes, 1 = Historial de Pagos
    var payingProvider by remember { mutableStateOf<ProviderEntity?>(null) }
    var showPayDialog by remember { mutableStateOf(false) }

    val debtorProviders = providers.filter { it.currentBalanceUsd > 0.01 }
    val totalDebtUsd = debtorProviders.sumOf { it.currentBalanceUsd }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
            .testTag("cxp_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Summary & Tab Header
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Proveedores por Pagar (${debtorProviders.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        shape = RoundedCornerShape(3.dp)
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Historial de Pagos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        shape = RoundedCornerShape(3.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "Total Deuda a Proveedores:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    DualCurrencyBadge(amountUsd = totalDebtUsd, bcvRate = rate, isHighlighted = true)
                }
            }
        }

        // Content
        if (selectedTab == 0) {
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
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RIF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(95.dp))
                        Text("Proveedor / Razón Social", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                        Text("Contacto", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(110.dp))
                        Text("Teléfono", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(100.dp))
                        Text("Saldo a Pagar ($ / Bs)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(150.dp), textAlign = TextAlign.End)
                        Text("Acción", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp), textAlign = TextAlign.Center)
                    }

                    if (debtorProviders.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No existen cuentas pendientes por pagar a proveedores.", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(debtorProviders) { prov ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = prov.rif, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.width(95.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = prov.name, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        Text(text = prov.address, fontSize = 9.sp, color = Color(0xFF64748B))
                                    }
                                    Text(text = prov.contactPerson, fontSize = 10.sp, color = Color(0xFF475569), modifier = Modifier.width(110.dp))
                                    Text(text = prov.phone, fontSize = 10.sp, color = Color(0xFF475569), modifier = Modifier.width(100.dp))
                                    Box(modifier = Modifier.width(150.dp), contentAlignment = Alignment.CenterEnd) {
                                        DualCurrencyBadge(amountUsd = prov.currentBalanceUsd, bcvRate = rate, isHighlighted = true)
                                    }
                                    Box(modifier = Modifier.width(90.dp), contentAlignment = Alignment.Center) {
                                        Button(
                                            onClick = {
                                                payingProvider = prov
                                                showPayDialog = true
                                            },
                                            shape = RoundedCornerShape(3.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Pagar", fontSize = 10.sp)
                                        }
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 1: Payments History
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
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fecha", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp))
                        Text("Tipo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(85.dp))
                        Text("Comprobante", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(80.dp))
                        Text("Proveedor", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                        Text("Forma Pago", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(95.dp))
                        Text("Monto ($ / Bs)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(140.dp), textAlign = TextAlign.End)
                        Text("Saldo Result", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                    }

                    if (movements.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No se han registrado pagos a proveedores.", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(movements) { mov ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = Formatters.formatDate(mov.timestamp), fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.width(90.dp))
                                    Box(modifier = Modifier.width(85.dp)) {
                                        StatusBadge(status = mov.movementType)
                                    }
                                    Text(text = mov.docReference, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488), modifier = Modifier.width(80.dp))
                                    Text(text = mov.providerName, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                    Text(text = mov.paymentMethod.replace("_", " "), fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.width(95.dp))
                                    Box(modifier = Modifier.width(140.dp), contentAlignment = Alignment.CenterEnd) {
                                        DualCurrencyBadge(amountUsd = mov.amountUsd, bcvRate = mov.bcvRate)
                                    }
                                    Text(text = Formatters.formatUsd(mov.balanceAfterUsd), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPayDialog && payingProvider != null) {
        PayProviderDialog(
            provider = payingProvider!!,
            bcvRate = rate,
            onConfirm = { amountUsd, method, ref, notes ->
                viewModel.registerProviderPayment(payingProvider!!, amountUsd, method, ref, notes)
                showPayDialog = false
            },
            onDismiss = { showPayDialog = false }
        )
    }
}

@Composable
private fun PayProviderDialog(
    provider: ProviderEntity,
    bcvRate: Double,
    onConfirm: (Double, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountInput by remember { mutableStateOf(provider.currentBalanceUsd.toString()) }
    var selectedMethod by remember { mutableStateOf("TRANSFERENCIA") }
    var refInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("Cancelación factura proveedor") }

    val methods = listOf(
        "TRANSFERENCIA" to "Transferencia Bancaria (Bs.)",
        "PAGO_MOVIL" to "Pago Móvil (Bs.)",
        "EFECTIVO_USD" to "Efectivo Divisas ($)",
        "EFECTIVO_VES" to "Efectivo Bolívares (Bs.)",
        "ZELLE" to "Zelle (USD)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 440.dp).padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Registrar Pago a Proveedor", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Surface(color = Color(0xFFFFFBEB), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Proveedor: ${provider.name} (${provider.rif})", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF92400E))
                        Text(
                            text = "Deuda Actual: ${Formatters.formatUsd(provider.currentBalanceUsd)} (${Formatters.formatVes(provider.currentBalanceUsd * bcvRate)})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                Text("Medio de Pago Utilizado:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    methods.take(3).forEach { (m, label) ->
                        FilterChip(
                            selected = selectedMethod == m,
                            onClick = { selectedMethod = m },
                            label = { Text(label, fontSize = 9.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    methods.drop(3).forEach { (m, label) ->
                        FilterChip(
                            selected = selectedMethod == m,
                            onClick = { selectedMethod = m },
                            label = { Text(label, fontSize = 9.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val isUsd = selectedMethod == "EFECTIVO_USD" || selectedMethod == "ZELLE"
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text(if (isUsd) "Monto a Pagar ($)" else "Monto a Pagar (Bs.)", fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )

                OutlinedTextField(
                    value = refInput,
                    onValueChange = { refInput = it },
                    label = { Text("Número de Referencia / Transferencia", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Concepto / Observaciones", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) {
                        Text("Cancelar", fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            val parsed = amountInput.replace(',', '.').toDoubleOrNull() ?: 0.0
                            if (parsed > 0) {
                                val finalUsd = if (isUsd) parsed else (parsed / bcvRate)
                                onConfirm(finalUsd, selectedMethod, refInput, notesInput)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                    ) {
                        Text("Confirmar Pago", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
