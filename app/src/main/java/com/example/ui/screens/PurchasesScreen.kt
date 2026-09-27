package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.entity.ProductEntity
import com.example.data.entity.ProviderEntity
import com.example.ui.components.DualCurrencyBadge
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@Composable
fun PurchasesScreen(
    viewModel: ErpViewModel,
    onPurchaseSuccess: () -> Unit
) {
    val purchaseState by viewModel.purchaseState.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val products by viewModel.products.collectAsState()
    val warehouses by viewModel.warehouses.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    var showProviderPicker by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    var invoiceNumberInput by remember { mutableStateOf(purchaseState.invoiceNumber) }
    var controlNumberInput by remember { mutableStateOf(purchaseState.controlNumber.ifBlank { "00-${(100000..999999).random()}" }) }
    var warehouseCodeSelected by remember { mutableStateOf("ALM-01") }
    var ivaWithholdingPercentInput by remember { mutableStateOf("75") } // 75% o 100%
    var islrPercentInput by remember { mutableStateOf("2.0") } // 2% o 3%
    var isCredit by remember { mutableStateOf(false) }
    var notesInput by remember { mutableStateOf("") }

    val subtotalUsd = purchaseState.items.sumOf { it.quantity * it.unitCostUsd * (1.0 - it.discountPercent / 100.0) }
    val taxUsd = purchaseState.items.sumOf { (it.quantity * it.unitCostUsd * (1.0 - it.discountPercent / 100.0)) * (it.taxRatePercent / 100.0) }
    val totalFacturaUsd = subtotalUsd + taxUsd

    val ivaWithholdingPct = ivaWithholdingPercentInput.toDoubleOrNull() ?: 75.0
    val islrPct = islrPercentInput.toDoubleOrNull() ?: 2.0

    val ivaWithholdingUsd = if (ivaWithholdingPct > 0) taxUsd * (ivaWithholdingPct / 100.0) else 0.0
    val islrUsd = if (islrPct > 0) subtotalUsd * (islrPct / 100.0) else 0.0
    val totalNetoAPagarUsd = (totalFacturaUsd - ivaWithholdingUsd - islrUsd).coerceAtLeast(0.0)

    val totalFacturaVes = totalFacturaUsd * rate
    val totalNetoVes = totalNetoAPagarUsd * rate

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A2WindowBg)
            .padding(6.dp)
            .testTag("a2_purchases_screen"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // a2 Document Header Box
        Surface(
            color = A2SurfaceWhite,
            shape = RoundedCornerShape(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(A2TitleNavyDark).padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "a2 ADMINISTRATIVO - RECEPCIÓN DE FACTURA DE COMPRA / PROVEEDOR",
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                    Text(
                        text = "TASA BCV: ${String.format(java.util.Locale.US, "%.2f", rate)} Bs/$",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFFFDE68A)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Provider Selector
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                        modifier = Modifier
                            .weight(1.3f)
                            .clickable { showProviderPicker = true }
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = A2TitleBlue, modifier = Modifier.size(16.dp))
                                Text(
                                    text = purchaseState.selectedProvider?.let { "${it.name} | RIF: ${it.rif}" } ?: "Seleccionar Proveedor (F4)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = A2TitleNavyDark
                                )
                            }
                            Text(
                                text = "Deuda: ${Formatters.formatUsd(purchaseState.selectedProvider?.currentBalanceUsd ?: 0.0)}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = A2RedAlert
                            )
                        }
                    }

                    // Invoice and Control
                    OutlinedTextField(
                        value = invoiceNumberInput,
                        onValueChange = { invoiceNumberInput = it },
                        label = { Text("Nro. Factura Proveedor", fontSize = 8.sp) },
                        singleLine = true,
                        modifier = Modifier.width(150.dp).height(40.dp)
                    )

                    OutlinedTextField(
                        value = controlNumberInput,
                        onValueChange = { controlNumberInput = it },
                        label = { Text("Nro. Control (00-000000)", fontSize = 8.sp) },
                        singleLine = true,
                        modifier = Modifier.width(150.dp).height(40.dp)
                    )

                    // Warehouse destination
                    OutlinedTextField(
                        value = warehouseCodeSelected,
                        onValueChange = { warehouseCodeSelected = it },
                        label = { Text("Alm. Destino", fontSize = 8.sp) },
                        singleLine = true,
                        modifier = Modifier.width(90.dp).height(40.dp)
                    )
                }
            }
        }

        // Main Grid & Totals
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Purchases Items Grid
            Surface(
                color = A2SurfaceWhite,
                shape = RoundedCornerShape(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
                modifier = Modifier.weight(1.5f).fillMaxHeight()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(A2GridHeader)
                            .border(0.5.dp, A2BorderMid)
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ítem", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                        Text("Código", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(65.dp))
                        Text("Descripción del Artículo Recibido", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Text("Unid", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(35.dp), textAlign = TextAlign.Center)
                        Text("Cant.", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(55.dp), textAlign = TextAlign.Center)
                        Text("Costo Unit $", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                        Text("Desc %", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                        Text("IVA", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                        Text("Total ($)", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                        Text("Total (Bs.)", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                        Text("", modifier = Modifier.width(28.dp))
                    }

                    if (purchaseState.items.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No hay renglones cargados a la recepción de compra.", fontSize = 11.sp, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { showAddItemDialog = true },
                                    shape = RoundedCornerShape(2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = A2TitleBlue)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Añadir Renglón de Compra", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(purchaseState.items.size) { idx ->
                                val itm = purchaseState.items[idx]
                                val lineTot = itm.quantity * itm.unitCostUsd * (1.0 - itm.discountPercent / 100.0)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${idx + 1}", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                                    Text(itm.productCode, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = A2TitleNavyDark, modifier = Modifier.width(65.dp))
                                    Text(itm.productName, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1)
                                    Text(itm.unit, fontSize = 8.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.Center)
                                    Text("${itm.quantity.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(55.dp), textAlign = TextAlign.Center)
                                    Text(Formatters.formatUsd(itm.unitCostUsd), fontSize = 9.sp, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                                    Text("${itm.discountPercent.toInt()}%", fontSize = 9.sp, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                                    Text("${itm.taxRatePercent.toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                                    Text(Formatters.formatUsd(lineTot), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF15803D), modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                                    Text(Formatters.formatVes(lineTot * rate), fontSize = 9.sp, color = Color(0xFF1E40AF), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                    IconButton(
                                        onClick = { viewModel.removePurchaseItem(itm.productId) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = A2RedAlert, modifier = Modifier.size(12.dp))
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // Right: Fiscal Withholdings & Totals
            Column(
                modifier = Modifier.width(300.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = A2SurfaceWhite,
                    shape = RoundedCornerShape(2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("LIQUIDACIÓN & RETENCIONES SENIAT", fontWeight = FontWeight.Black, fontSize = 10.sp, color = A2TitleNavyDark)
                        HorizontalDivider(color = A2BorderMid)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal Gravable:", fontSize = 9.sp, color = Color(0xFF475569))
                            Text(Formatters.formatUsd(subtotalUsd), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IVA Crédito Fiscal (16%):", fontSize = 9.sp, color = Color(0xFF475569))
                            Text(Formatters.formatUsd(taxUsd), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL FACTURA BRUTO:", fontSize = 9.sp, fontWeight = FontWeight.Black)
                            Text(Formatters.formatUsd(totalFacturaUsd), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        HorizontalDivider(color = A2BorderMid)

                        // Retención IVA & ISLR Inputs
                        Text("Retenciones Fiscales de Ley:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = A2TitleNavyDark)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = ivaWithholdingPercentInput,
                                onValueChange = { ivaWithholdingPercentInput = it },
                                label = { Text("Ret. IVA % (75/100)", fontSize = 7.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).height(38.dp)
                            )
                            OutlinedTextField(
                                value = islrPercentInput,
                                onValueChange = { islrPercentInput = it },
                                label = { Text("Ret. ISLR %", fontSize = 7.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).height(38.dp)
                            )
                        }

                        if (ivaWithholdingUsd > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("(-) Retención IVA Comprobante:", fontSize = 8.sp, color = A2RedAlert)
                                Text("-${Formatters.formatUsd(ivaWithholdingUsd)}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = A2RedAlert)
                            }
                        }

                        if (islrUsd > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("(-) Retención ISLR:", fontSize = 8.sp, color = A2RedAlert)
                                Text("-${Formatters.formatUsd(islrUsd)}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = A2RedAlert)
                            }
                        }

                        HorizontalDivider(color = A2TitleNavyDark, thickness = 1.dp)

                        // Net to Pay Box
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TOTAL NETO A PAGAR PROVEEDOR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF166534))
                                Text(Formatters.formatUsd(totalNetoAPagarUsd), fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF15803D))
                                Text(Formatters.formatVes(totalNetoVes), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                            }
                        }

                        // Credit toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isCredit, onCheckedChange = { isCredit = it })
                            Text("Compra a Crédito (Genera CxP Neto)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { showAddItemDialog = true },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = A2TitleBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Añadir Renglón", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.processPurchase {
                                onPurchaseSuccess()
                            }
                        },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                        modifier = Modifier.weight(1.3f).testTag("btn_confirm_a2_purchase")
                    ) {
                        Text("PROCESAR RECEPCIÓN (F9)", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    // Provider Picker Modal
    if (showProviderPicker) {
        Dialog(onDismissRequest = { showProviderPicker = false }) {
            Surface(shape = RoundedCornerShape(4.dp), color = A2WindowBg, modifier = Modifier.widthIn(max = 480.dp).padding(8.dp)) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("SELECCIÓN DE PROVEEDOR (a2 SOFTWAY)", fontWeight = FontWeight.Black, fontSize = 12.sp, color = A2TitleNavyDark)
                    LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                        items(providers) { prov ->
                            Surface(
                                color = A2SurfaceWhite,
                                shape = RoundedCornerShape(2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderMid),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.setPurchaseProvider(prov)
                                    showProviderPicker = false
                                }.padding(vertical = 2.dp)
                            ) {
                                Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(prov.name, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        Text("RIF: ${prov.rif} • Contacto: ${prov.contactPerson}", fontSize = 8.sp, color = Color(0xFF64748B))
                                    }
                                    Text("Deuda: ${Formatters.formatUsd(prov.currentBalanceUsd)}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = A2RedAlert)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Item to Purchase Dialog
    if (showAddItemDialog) {
        A2PurchaseItemDialog(
            products = products,
            onAdd = { p, qty, cost, disc ->
                viewModel.addProductToPurchase(p, qty, cost, disc)
                showAddItemDialog = false
            },
            onDismiss = { showAddItemDialog = false }
        )
    }
}

@Composable
private fun A2PurchaseItemDialog(
    products: List<ProductEntity>,
    onAdd: (ProductEntity, Double, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var qtyInput by remember { mutableStateOf("10") }
    var costInput by remember { mutableStateOf(selectedProduct?.costUsd?.toString() ?: "1.00") }
    var discountInput by remember { mutableStateOf("0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(4.dp), color = A2WindowBg, modifier = Modifier.widthIn(max = 460.dp).padding(8.dp)) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("CARGA DE RENGLÓN EN FACTURA DE COMPRA", fontWeight = FontWeight.Black, fontSize = 11.sp, color = A2TitleNavyDark)

                LazyColumn(modifier = Modifier.heightIn(max = 140.dp)) {
                    items(products) { p ->
                        Surface(
                            color = if (selectedProduct?.id == p.id) Color(0xFFEFF6FF) else A2SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedProduct?.id == p.id) A2TitleBlue else A2BorderMid),
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedProduct = p
                                costInput = p.costUsd.toString()
                            }.padding(vertical = 1.dp)
                        ) {
                            Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${p.code} - ${p.name}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("Costo actual: ${Formatters.formatUsd(p.costUsd)}", fontSize = 8.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(value = qtyInput, onValueChange = { qtyInput = it }, label = { Text("Cantidad Recibida", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(40.dp))
                    OutlinedTextField(value = costInput, onValueChange = { costInput = it }, label = { Text("Costo Unitario ($)", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(40.dp))
                    OutlinedTextField(value = discountInput, onValueChange = { discountInput = it }, label = { Text("Desc %", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(0.7f).height(40.dp))
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(2.dp)) { Text("Cancelar", fontSize = 10.sp) }
                    Button(
                        onClick = {
                            val q = qtyInput.toDoubleOrNull() ?: 1.0
                            val c = costInput.replace(',', '.').toDoubleOrNull() ?: 1.0
                            val d = discountInput.toDoubleOrNull() ?: 0.0
                            selectedProduct?.let { p -> onAdd(p, q, c, d) }
                        },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = A2TitleBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Agregar Renglón", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
