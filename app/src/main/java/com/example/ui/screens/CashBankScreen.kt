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
import com.example.data.entity.CashSessionEntity
import com.example.ui.components.DualCurrencyBadge
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.viewmodel.ErpViewModel

@Composable
fun CashBankScreen(viewModel: ErpViewModel) {
    val activeSession by viewModel.activeCashSession.collectAsState()
    val cashMovements by viewModel.cashMovements.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    var showOpenDialog by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }
    var showMovementDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
            .testTag("cash_bank_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cash Register Status Card
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = if (activeSession != null) Color(0xFF15803D) else Color(0xFFDC2626),
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = activeSession?.registerName ?: "CAJA 01 - MOSTRADOR",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            StatusBadge(status = if (activeSession != null) "ABIERTA" else "CERRADA")
                        }
                        if (activeSession != null) {
                            Text(
                                text = "Apertura: ${Formatters.formatDateTime(activeSession!!.openedAt)} por ${activeSession!!.openedByUser}",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        } else {
                            Text(text = "La caja se encuentra cerrada. Debe abrir turno para registrar ventas en efectivo.", fontSize = 10.sp, color = Color(0xFFDC2626))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (activeSession == null) {
                        Button(
                            onClick = { showOpenDialog = true },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534))
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Apertura de Caja", fontSize = 11.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showMovementDialog = true },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ingreso / Gasto", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showCloseDialog = true },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B))
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cierre Z / Arqueo", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Bank Accounts Strip
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "Cuentas Bancarias y Pasarelas de Pago", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bankAccounts.forEach { b ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = b.bankName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                Text(text = b.accountNumber, fontSize = 9.sp, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (b.currency == "USD") Formatters.formatUsd(b.balance) else Formatters.formatVes(b.balance),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (b.currency == "USD") Color(0xFF15803D) else Color(0xFF1E40AF)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cash Movements Table
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
                    Text("Hora", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(75.dp))
                    Text("Tipo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(80.dp))
                    Text("Concepto / Descripción", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                    Text("Forma Pago", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(95.dp))
                    Text("Monto ($)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                    Text("Monto (Bs.)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                    Text("Cajero", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(65.dp), textAlign = TextAlign.Center)
                }

                if (cashMovements.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No hay movimientos de caja en este turno.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(cashMovements) { mov ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = Formatters.formatDate(mov.timestamp), fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.width(75.dp))
                                Box(modifier = Modifier.width(80.dp)) {
                                    StatusBadge(status = mov.type)
                                }
                                Text(text = mov.description, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                Text(text = mov.paymentMethod.replace("_", " "), fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.width(95.dp))
                                Text(text = Formatters.formatUsd(mov.amountUsd), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                                Text(text = Formatters.formatVes(mov.amountVes), fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E40AF), modifier = Modifier.width(90.dp), textAlign = TextAlign.End)
                                Text(text = mov.user, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.width(65.dp), textAlign = TextAlign.Center)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }

    // Open Register Dialog
    if (showOpenDialog) {
        OpenRegisterDialog(
            onConfirm = { name, usd, ves ->
                viewModel.openCashRegister(name, usd, ves)
                showOpenDialog = false
            },
            onDismiss = { showOpenDialog = false }
        )
    }

    // Close Register Dialog
    if (showCloseDialog && activeSession != null) {
        CloseRegisterDialog(
            session = activeSession!!,
            onConfirm = { usd, ves ->
                viewModel.closeCashRegister(usd, ves)
                showCloseDialog = false
            },
            onDismiss = { showCloseDialog = false }
        )
    }

    // Add Movement Dialog
    if (showMovementDialog) {
        AddCashMovementDialog(
            bcvRate = rate,
            onConfirm = { type, cat, desc, usd, ves, meth ->
                viewModel.addCashMovement(type, cat, desc, usd, ves, meth)
                showMovementDialog = false
            },
            onDismiss = { showMovementDialog = false }
        )
    }
}

@Composable
private fun OpenRegisterDialog(onConfirm: (String, Double, Double) -> Unit, onDismiss: () -> Unit) {
    var regName by remember { mutableStateOf("CAJA 01 - MOSTRADOR") }
    var usdOpening by remember { mutableStateOf("50.00") }
    var vesOpening by remember { mutableStateOf("1500.00") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.widthIn(max = 400.dp).padding(12.dp)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Apertura de Turno de Caja", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = regName, onValueChange = { regName = it }, label = { Text("Nombre de Caja", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth().height(44.dp))
                OutlinedTextField(value = usdOpening, onValueChange = { usdOpening = it }, label = { Text("Fondo Inicial ($ Dólares)", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth().height(44.dp))
                OutlinedTextField(value = vesOpening, onValueChange = { vesOpening = it }, label = { Text("Fondo Inicial (Bs. Bolívares)", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth().height(44.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) { Text("Cancelar", fontSize = 11.sp) }
                    Button(onClick = {
                        val u = usdOpening.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val v = vesOpening.replace(',', '.').toDoubleOrNull() ?: 0.0
                        onConfirm(regName, u, v)
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) { Text("Abrir Caja", fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
private fun CloseRegisterDialog(session: CashSessionEntity, onConfirm: (Double, Double) -> Unit, onDismiss: () -> Unit) {
    var usdClosing by remember { mutableStateOf(session.expectedUsd.toString()) }
    var vesClosing by remember { mutableStateOf(session.expectedVes.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.widthIn(max = 420.dp).padding(12.dp)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cierre de Caja Z / Arqueo Final", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Caja: ${session.registerName}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(text = "Fondo Apertura: ${Formatters.formatUsd(session.openingUsd)} / ${Formatters.formatVes(session.openingVes)}", fontSize = 10.sp)
                    }
                }

                OutlinedTextField(value = usdClosing, onValueChange = { usdClosing = it }, label = { Text("Efectivo Físico USD Contado ($)", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth().height(44.dp))
                OutlinedTextField(value = vesClosing, onValueChange = { vesClosing = it }, label = { Text("Efectivo Físico Bs. Contado (Bs.)", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth().height(44.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) { Text("Cancelar", fontSize = 11.sp) }
                    Button(onClick = {
                        val u = usdClosing.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val v = vesClosing.replace(',', '.').toDoubleOrNull() ?: 0.0
                        onConfirm(u, v)
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)), shape = RoundedCornerShape(4.dp)) { Text("Cerrar Turno Z", fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
private fun AddCashMovementDialog(bcvRate: Double, onConfirm: (String, String, String, Double, Double, String) -> Unit, onDismiss: () -> Unit) {
    var type by remember { mutableStateOf("GASTO") } // INGRESO, EGRESO, GASTO
    var category by remember { mutableStateOf("GASTO_OPERATIVO") }
    var description by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("10.00") }
    var method by remember { mutableStateOf("EFECTIVO_USD") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.widthIn(max = 420.dp).padding(12.dp)) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Registrar Movimiento de Caja", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("GASTO", "EGRESO", "INGRESO").forEach { t ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t, fontSize = 10.sp) }, modifier = Modifier.weight(1f))
                    }
                }

                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Concepto del Movimiento", fontSize = 10.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth().height(44.dp))
                OutlinedTextField(value = amountInput, onValueChange = { amountInput = it }, label = { Text("Monto", fontSize = 10.sp) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth().height(44.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) { Text("Cancelar", fontSize = 11.sp) }
                    Button(onClick = {
                        val parsed = amountInput.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (parsed > 0 && description.isNotBlank()) {
                            val usd = if (method.contains("USD")) parsed else (parsed / bcvRate)
                            val ves = if (method.contains("USD")) (parsed * bcvRate) else parsed
                            onConfirm(type, category, description, usd, ves, method)
                        }
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) { Text("Guardar", fontSize = 11.sp) }
                }
            }
        }
    }
}
