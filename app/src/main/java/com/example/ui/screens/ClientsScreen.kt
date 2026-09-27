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
import com.example.data.entity.ClientEntity
import com.example.ui.components.DualCurrencyBadge
import com.example.ui.components.Formatters
import com.example.viewmodel.ErpViewModel

@Composable
fun ClientsScreen(viewModel: ErpViewModel) {
    val clients by viewModel.clients.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    var searchQuery by remember { mutableStateOf("") }
    var editingClient by remember { mutableStateOf<ClientEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val filtered = clients.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.rif.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
            .testTag("clients_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Actions & Search
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        editingClient = null
                        showDialog = true
                    },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(38.dp).testTag("btn_new_client")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Cliente (Ficha RIF)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar cliente por RIF, Razón Social o Teléfono...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }

        // Table
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
                    Text("RIF / C.I.", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(95.dp))
                    Text("Nombre / Razón Social", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                    Text("Teléfono", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(100.dp))
                    Text("Lista", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                    Text("Límite ($)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                    Text("Saldo Deuda ($ / Bs)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(140.dp), textAlign = TextAlign.End)
                    Text("Acción", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron clientes.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(filtered) { cli ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingClient = cli
                                        showDialog = true
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = cli.rif, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.width(95.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = cli.name, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text(text = cli.address, fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                                Text(text = cli.phone, fontSize = 10.sp, color = Color(0xFF475569), modifier = Modifier.width(100.dp))
                                Text(text = "P${cli.priceList}", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                                Text(text = Formatters.formatUsd(cli.creditLimitUsd), fontSize = 10.sp, modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                                Box(modifier = Modifier.width(140.dp), contentAlignment = Alignment.CenterEnd) {
                                    DualCurrencyBadge(amountUsd = cli.currentBalanceUsd, bcvRate = rate, isHighlighted = cli.currentBalanceUsd > 0)
                                }
                                Row(modifier = Modifier.width(60.dp), horizontalArrangement = Arrangement.Center) {
                                    IconButton(
                                        onClick = {
                                            editingClient = cli
                                            showDialog = true
                                        },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteClient(cli.id) },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }

    // Client Form Dialog
    if (showDialog) {
        ClientFormDialog(
            initial = editingClient,
            onSave = { c ->
                viewModel.saveClient(c)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun ClientFormDialog(
    initial: ClientEntity?,
    onSave: (ClientEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var rif by remember { mutableStateOf(initial?.rif ?: "V-") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "0414-") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var priceList by remember { mutableStateOf(initial?.priceList?.toString() ?: "1") }
    var creditLimit by remember { mutableStateOf(initial?.creditLimitUsd?.toString() ?: "500.00") }
    var creditDays by remember { mutableStateOf(initial?.creditDays?.toString() ?: "15") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 450.dp).padding(12.dp).testTag("client_form_dialog")
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (initial == null) "Nuevo Cliente (Ficha RIF)" else "Modificar Ficha de Cliente",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = rif,
                        onValueChange = { rif = it },
                        label = { Text("RIF / C.I. (V-, J-, E-, G-)", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(44.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(44.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Razón Social o Nombre Completo", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección Fiscal", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = priceList,
                        onValueChange = { priceList = it },
                        label = { Text("Lista Precios (1, 2, 3)", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(44.dp)
                    )
                    OutlinedTextField(
                        value = creditLimit,
                        onValueChange = { creditLimit = it },
                        label = { Text("Límite Crédito ($)", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(44.dp)
                    )
                    OutlinedTextField(
                        value = creditDays,
                        onValueChange = { creditDays = it },
                        label = { Text("Días Crédito", fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(44.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) {
                        Text("Cancelar", fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val entity = (initial ?: ClientEntity(rif = rif, name = name, phone = phone, email = email, address = address)).copy(
                                    rif = rif,
                                    name = name,
                                    phone = phone,
                                    email = email,
                                    address = address,
                                    priceList = priceList.toIntOrNull() ?: 1,
                                    creditLimitUsd = creditLimit.toDoubleOrNull() ?: 500.0,
                                    creditDays = creditDays.toIntOrNull() ?: 15,
                                    isActive = true
                                )
                                onSave(entity)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Guardar Cliente", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
