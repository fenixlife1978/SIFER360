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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ProviderEntity
import com.example.ui.components.DualCurrencyBadge
import com.example.ui.components.Formatters
import com.example.viewmodel.ErpViewModel

@Composable
fun ProvidersScreen(viewModel: ErpViewModel) {
    val providers by viewModel.providers.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    var searchQuery by remember { mutableStateOf("") }
    var editingProvider by remember { mutableStateOf<ProviderEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val filtered = providers.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.rif.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
            .testTag("providers_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Action Bar
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
                        editingProvider = null
                        showDialog = true
                    },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(38.dp).testTag("btn_new_provider")
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Proveedor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar proveedor por RIF, Razón Social...", fontSize = 11.sp) },
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
                    Text("RIF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(95.dp))
                    Text("Proveedor / Razón Social", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                    Text("Teléfono", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(105.dp))
                    Text("Contacto", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(120.dp))
                    Text("Saldo Deuda ($ / Bs)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(140.dp), textAlign = TextAlign.End)
                    Text("Acción", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron proveedores.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(filtered) { prov ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingProvider = prov
                                        showDialog = true
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = prov.rif, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.width(95.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = prov.name, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text(text = prov.address, fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                                Text(text = prov.phone, fontSize = 10.sp, color = Color(0xFF475569), modifier = Modifier.width(105.dp))
                                Text(text = prov.contactPerson, fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.width(120.dp))
                                Box(modifier = Modifier.width(140.dp), contentAlignment = Alignment.CenterEnd) {
                                    DualCurrencyBadge(amountUsd = prov.currentBalanceUsd, bcvRate = rate, isHighlighted = prov.currentBalanceUsd > 0)
                                }
                                Row(modifier = Modifier.width(60.dp), horizontalArrangement = Arrangement.Center) {
                                    IconButton(
                                        onClick = {
                                            editingProvider = prov
                                            showDialog = true
                                        },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF2563EB), modifier = Modifier.size(13.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteProvider(prov.id) },
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

    if (showDialog) {
        ProviderFormDialog(
            initial = editingProvider,
            onSave = { p ->
                viewModel.saveProvider(p)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun ProviderFormDialog(
    initial: ProviderEntity?,
    onSave: (ProviderEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var rif by remember { mutableStateOf(initial?.rif ?: "J-") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "0212-") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var contact by remember { mutableStateOf(initial?.contactPerson ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 450.dp).padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (initial == null) "Nuevo Proveedor" else "Modificar Proveedor",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = rif,
                        onValueChange = { rif = it },
                        label = { Text("RIF (J-, G-, V-)", fontSize = 10.sp) },
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
                    label = { Text("Razón Social de la Empresa", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Persona de Contacto / Vendedor", fontSize = 10.sp) },
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

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) {
                        Text("Cancelar", fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val entity = (initial ?: ProviderEntity(rif = rif, name = name, phone = phone, email = email, address = address, contactPerson = contact)).copy(
                                    rif = rif,
                                    name = name,
                                    phone = phone,
                                    email = email,
                                    address = address,
                                    contactPerson = contact,
                                    isActive = true
                                )
                                onSave(entity)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Guardar Proveedor", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
