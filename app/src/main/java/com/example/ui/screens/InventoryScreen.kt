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
import com.example.data.entity.InventoryMovementEntity
import com.example.data.entity.ProductEntity
import com.example.ui.components.DualCurrencyBadge
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@Composable
fun InventoryScreen(viewModel: ErpViewModel) {
    val products by viewModel.products.collectAsState()
    val movements by viewModel.inventoryMovements.collectAsState()
    val warehouses by viewModel.warehouses.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    var selectedTab by remember { mutableStateOf(0) } // 0 = Existencias Multialmacén, 1 = Kardex Bimoneda
    var showAdjustmentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A2WindowBg)
            .padding(6.dp)
            .testTag("a2_inventory_screen"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Tab header & action bar
        Surface(
            color = A2SurfaceWhite,
            shape = RoundedCornerShape(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = if (selectedTab == 0) A2TitleNavyDark else A2ControlFace,
                        shape = RoundedCornerShape(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderMid),
                        modifier = Modifier.clickable { selectedTab = 0 }
                    ) {
                        Text(
                            text = "1. Existencias y Multialmacén a2",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = if (selectedTab == 1) A2TitleNavyDark else A2ControlFace,
                        shape = RoundedCornerShape(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderMid),
                        modifier = Modifier.clickable { selectedTab = 1 }
                    ) {
                        Text(
                            text = "2. Kardex Bimoneda Histórico",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Button(
                    onClick = { showAdjustmentDialog = true },
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = A2TitleBlue),
                    modifier = Modifier.height(34.dp).testTag("btn_a2_inventory_adj")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajuste de Stock (+ / -)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Table Content
        if (selectedTab == 0) {
            // Multialmacén Grid
            Surface(
                color = A2SurfaceWhite,
                shape = RoundedCornerShape(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(A2GridHeader)
                            .border(0.5.dp, A2BorderMid)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Código", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(65.dp))
                        Text("Descripción del Artículo", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Text("Ubicación", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                        Text("Alm. Caracas", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(75.dp), textAlign = TextAlign.Center)
                        Text("Dep. Maracay", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(75.dp), textAlign = TextAlign.Center)
                        Text("Stock Total", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(70.dp), textAlign = TextAlign.Center)
                        Text("Costo Val. ($)", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                        Text("Total Valorizado ($ / Bs)", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(140.dp), textAlign = TextAlign.End)
                    }

                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(products) { prod ->
                            val isLow = prod.currentStock <= prod.minStock
                            val totalValUsd = prod.currentStock * prod.costUsd
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(prod.code, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = A2TitleNavyDark, modifier = Modifier.width(65.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Text("Depto: ${prod.category} • Marca: ${prod.brand}", fontSize = 8.sp, color = Color(0xFF64748B))
                                }
                                Text(prod.location, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                                Text("${prod.stockWarehouse1.toInt()}", fontSize = 9.sp, modifier = Modifier.width(75.dp), textAlign = TextAlign.Center)
                                Text("${prod.stockWarehouse2.toInt()}", fontSize = 9.sp, modifier = Modifier.width(75.dp), textAlign = TextAlign.Center)
                                Text(
                                    text = "${prod.currentStock.toInt()} ${prod.unit}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isLow) A2RedAlert else Color(0xFF15803D),
                                    modifier = Modifier.width(70.dp),
                                    textAlign = TextAlign.Center
                                )
                                Text(Formatters.formatUsd(prod.costUsd), fontSize = 9.sp, modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                                Box(modifier = Modifier.width(140.dp), contentAlignment = Alignment.CenterEnd) {
                                    DualCurrencyBadge(amountUsd = totalValUsd, bcvRate = rate)
                                }
                            }
                            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                        }
                    }
                }
            }
        } else {
            // Kardex Grid
            Surface(
                color = A2SurfaceWhite,
                shape = RoundedCornerShape(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(A2GridHeader)
                            .border(0.5.dp, A2BorderMid)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fecha / Hora", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(105.dp))
                        Text("Tipo", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(75.dp))
                        Text("Doc. Ref", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(80.dp))
                        Text("Artículo / Producto", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Text("Movimiento", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(75.dp), textAlign = TextAlign.Center)
                        Text("Saldo", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(65.dp), textAlign = TextAlign.Center)
                        Text("Costo ($)", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                        Text("Tasa BCV", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                    }

                    if (movements.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No hay movimientos registrados en el Kardex.", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(movements) { mov ->
                                val isNeg = mov.quantityChange < 0
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(Formatters.formatDate(mov.timestamp), fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.width(105.dp))
                                    Box(modifier = Modifier.width(75.dp)) {
                                        StatusBadge(status = mov.movementType)
                                    }
                                    Text(mov.documentReference, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = A2TitleNavyDark, modifier = Modifier.width(80.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(mov.productName, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                        Text("Motivo: ${mov.reason.ifBlank { "Comercial" }}", fontSize = 8.sp, color = Color(0xFF64748B))
                                    }
                                    Text(
                                        text = "${if (mov.quantityChange > 0) "+" else ""}${mov.quantityChange.toInt()}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isNeg) A2RedAlert else Color(0xFF15803D),
                                        modifier = Modifier.width(75.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Text("${mov.newStock.toInt()}", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(65.dp), textAlign = TextAlign.Center)
                                    Text(Formatters.formatUsd(mov.costUsd), fontSize = 9.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                                    Text("${mov.bcvRate} Bs", fontSize = 9.sp, color = Color(0xFF92400E), modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                                }
                                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Adjustment Modal
    if (showAdjustmentDialog) {
        A2InventoryAdjustmentModal(
            products = products,
            onSave = { prodId, type, qty, reason ->
                viewModel.registerInventoryAdjustment(prodId, type, qty, reason)
                showAdjustmentDialog = false
            },
            onDismiss = { showAdjustmentDialog = false }
        )
    }
}

@Composable
private fun A2InventoryAdjustmentModal(
    products: List<ProductEntity>,
    onSave: (Long, String, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProductId by remember { mutableStateOf(products.firstOrNull()?.id ?: 0L) }
    var adjustmentType by remember { mutableStateOf("AJUSTE_POS") }
    var qtyInput by remember { mutableStateOf("1") }
    var reasonInput by remember { mutableStateOf("Conteo físico a2") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = A2WindowBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
            modifier = Modifier.widthIn(max = 450.dp).padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("AJUSTE DE INVENTARIO a2 SOFTWAY", fontWeight = FontWeight.Black, fontSize = 11.sp, color = A2TitleNavyDark)

                LazyColumn(modifier = Modifier.heightIn(max = 130.dp)) {
                    items(products) { p ->
                        Surface(
                            color = if (selectedProductId == p.id) Color(0xFFEFF6FF) else A2SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedProductId == p.id) A2TitleBlue else A2BorderMid),
                            modifier = Modifier.fillMaxWidth().clickable { selectedProductId = p.id }.padding(vertical = 1.dp)
                        ) {
                            Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${p.code} - ${p.name}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("Stock: ${p.currentStock.toInt()}", fontSize = 9.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = if (adjustmentType == "AJUSTE_POS") A2TitleBlue else A2ControlFace,
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.weight(1f).clickable { adjustmentType = "AJUSTE_POS" }
                    ) {
                        Text("Ajuste Positivo (+)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (adjustmentType == "AJUSTE_POS") Color.White else Color.Black, modifier = Modifier.padding(vertical = 6.dp), textAlign = TextAlign.Center)
                    }
                    Surface(
                        color = if (adjustmentType == "AJUSTE_NEG") A2RedAlert else A2ControlFace,
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.weight(1f).clickable { adjustmentType = "AJUSTE_NEG" }
                    ) {
                        Text("Ajuste Negativo (-)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (adjustmentType == "AJUSTE_NEG") Color.White else Color.Black, modifier = Modifier.padding(vertical = 6.dp), textAlign = TextAlign.Center)
                    }
                }

                OutlinedTextField(value = qtyInput, onValueChange = { qtyInput = it }, label = { Text("Cantidad a Ajustar", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth().height(40.dp))
                OutlinedTextField(value = reasonInput, onValueChange = { reasonInput = it }, label = { Text("Justificación / Motivo", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth().height(40.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(2.dp)) { Text("Cancelar", fontSize = 10.sp) }
                    Button(
                        onClick = {
                            val q = qtyInput.toDoubleOrNull() ?: 1.0
                            if (selectedProductId != 0L && q > 0) onSave(selectedProductId, adjustmentType, q, reasonInput)
                        },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = A2TitleBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aplicar Ajuste", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
