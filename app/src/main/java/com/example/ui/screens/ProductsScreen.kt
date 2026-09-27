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
import com.example.ui.components.DualCurrencyBadge
import com.example.ui.components.Formatters
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel
import java.util.Locale

@Composable
fun ProductsScreen(viewModel: ErpViewModel) {
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("TODAS") }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val filtered = products.filter { p ->
        (selectedCategory == "TODAS" || p.category == selectedCategory) &&
        (p.name.contains(searchQuery, ignoreCase = true) ||
         p.code.contains(searchQuery, ignoreCase = true) ||
         p.alternateCode.contains(searchQuery, ignoreCase = true) ||
         p.barcode.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A2WindowBg)
            .padding(6.dp)
            .testTag("a2_products_screen"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // a2 Action Toolbar
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        editingProduct = null
                        showDialog = true
                    },
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = A2TitleBlue),
                    modifier = Modifier.height(34.dp).testTag("btn_a2_new_product")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo Artículo [Ficha a2]", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por código, alterno, barra, descripción o marca...", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = A2TitleBlue, modifier = Modifier.size(15.dp)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(2.dp)
                )

                // Dept filter
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf("TODAS", "Alimentos y Víveres", "Bebidas y Refrescos", "Limpieza y Hogar").forEach { cat ->
                        Surface(
                            color = if (selectedCategory == cat) A2TitleNavyDark else A2ControlFace,
                            shape = RoundedCornerShape(2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderMid),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat.take(12),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCategory == cat) Color.White else Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // a2 Windows Data Grid
        Surface(
            color = A2SurfaceWhite,
            shape = RoundedCornerShape(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(A2GridHeader)
                        .border(0.5.dp, A2BorderMid)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Código", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(65.dp))
                    Text("Cód. Alterno", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(70.dp))
                    Text("Descripción del Artículo", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Text("Departamento", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(95.dp))
                    Text("Ubic.", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                    Text("Stock", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(55.dp), textAlign = TextAlign.Center)
                    Text("Costo ($)", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                    Text("P1 Detal ($ / Bs)", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(130.dp), textAlign = TextAlign.End)
                    Text("P2 Mayor ($)", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                    Text("IVA", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                    Text("Acciones", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(55.dp), textAlign = TextAlign.Center)
                }

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron artículos en el inventario.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(filtered) { prod ->
                            val isLow = prod.currentStock <= prod.minStock
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingProduct = prod
                                        showDialog = true
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = prod.code, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = A2TitleNavyDark, modifier = Modifier.width(65.dp))
                                Text(text = prod.alternateCode.ifBlank { "N/A" }, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.width(70.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = prod.name, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Text(text = "Marca: ${prod.brand} • Barra: ${prod.barcode.ifBlank { "N/A" }}", fontSize = 8.sp, color = Color(0xFF64748B))
                                }
                                Text(text = prod.category, fontSize = 9.sp, color = Color(0xFF334155), modifier = Modifier.width(95.dp))
                                Text(text = prod.location, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                                Text(
                                    text = "${prod.currentStock.toInt()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isLow) A2RedAlert else Color(0xFF15803D),
                                    modifier = Modifier.width(55.dp),
                                    textAlign = TextAlign.Center
                                )
                                Text(text = Formatters.formatUsd(prod.costUsd), fontSize = 9.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                                Box(modifier = Modifier.width(130.dp), contentAlignment = Alignment.CenterEnd) {
                                    DualCurrencyBadge(amountUsd = prod.price1Usd, bcvRate = rate)
                                }
                                Text(text = Formatters.formatUsd(if (prod.price2Usd > 0) prod.price2Usd else prod.price1Usd), fontSize = 9.sp, color = Color(0xFF1E3A8A), modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                                Text(
                                    text = if (prod.isExempt) "Exento" else "${prod.taxRatePercent.toInt()}%",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (prod.isExempt) Color(0xFF16A34A) else Color(0xFF2563EB),
                                    modifier = Modifier.width(45.dp),
                                    textAlign = TextAlign.Center
                                )
                                Row(modifier = Modifier.width(55.dp), horizontalArrangement = Arrangement.Center) {
                                    IconButton(
                                        onClick = {
                                            editingProduct = prod
                                            showDialog = true
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = A2TitleBlue, modifier = Modifier.size(12.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteProduct(prod.id) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = A2RedAlert, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }

    // Multi-tab a2 Product Card Dialog
    if (showDialog) {
        A2ProductDialog(
            initial = editingProduct,
            categories = categories.map { it.name },
            bcvRate = rate,
            onSave = { p ->
                viewModel.saveProduct(p)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun A2ProductDialog(
    initial: ProductEntity?,
    categories: List<String>,
    bcvRate: Double,
    onSave: (ProductEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = General, 1 = Costos & Precios, 2 = Almacenes & Lotes

    // Tab 1 Fields
    var code by remember { mutableStateOf(initial?.code ?: "ART-${(100..999).random()}") }
    var altCode by remember { mutableStateOf(initial?.alternateCode ?: "") }
    var barcode by remember { mutableStateOf(initial?.barcode ?: "") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var shortName by remember { mutableStateOf(initial?.shortName ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: (categories.firstOrNull() ?: "Alimentos y Víveres")) }
    var brand by remember { mutableStateOf(initial?.brand ?: "GENÉRICO") }
    var model by remember { mutableStateOf(initial?.model ?: "") }
    var location by remember { mutableStateOf(initial?.location ?: "A-01") }
    var unit by remember { mutableStateOf(initial?.unit ?: "UND") }
    var secondaryUnit by remember { mutableStateOf(initial?.secondaryUnit ?: "BTO") }
    var conversionFactor by remember { mutableStateOf(initial?.conversionFactor?.toInt()?.toString() ?: "1") }
    var itemType by remember { mutableStateOf(initial?.itemType ?: "TERMINADO") }
    var isExempt by remember { mutableStateOf(initial?.isExempt ?: false) }
    var handlesLot by remember { mutableStateOf(initial?.handlesLot ?: false) }
    var handlesSerial by remember { mutableStateOf(initial?.handlesSerial ?: false) }
    var isWeighable by remember { mutableStateOf(initial?.isWeighable ?: false) }

    // Tab 2 Fields
    var costUsd by remember { mutableStateOf(initial?.costUsd?.toString() ?: "1.00") }
    var margin1 by remember { mutableStateOf(initial?.margin1Percent?.toString() ?: "35.0") }
    var price1 by remember { mutableStateOf(initial?.price1Usd?.toString() ?: "1.35") }
    var margin2 by remember { mutableStateOf(initial?.margin2Percent?.toString() ?: "25.0") }
    var price2 by remember { mutableStateOf(initial?.price2Usd?.toString() ?: "1.25") }
    var margin3 by remember { mutableStateOf(initial?.margin3Percent?.toString() ?: "18.0") }
    var price3 by remember { mutableStateOf(initial?.price3Usd?.toString() ?: "1.18") }
    var margin4 by remember { mutableStateOf(initial?.margin4Percent?.toString() ?: "12.0") }
    var price4 by remember { mutableStateOf(initial?.price4Usd?.toString() ?: "1.12") }
    var commission by remember { mutableStateOf(initial?.salesCommissionPercent?.toString() ?: "2.0") }

    // Tab 3 Fields
    var minStock by remember { mutableStateOf(initial?.minStock?.toInt()?.toString() ?: "10") }
    var maxStock by remember { mutableStateOf(initial?.maxStock?.toInt()?.toString() ?: "500") }
    var stockWh1 by remember { mutableStateOf(initial?.stockWarehouse1?.toInt()?.toString() ?: "40") }
    var stockWh2 by remember { mutableStateOf(initial?.stockWarehouse2?.toInt()?.toString() ?: "10") }

    // Recalculate Prices from Margin
    fun recalcFromCost(newCost: Double) {
        val m1 = margin1.toDoubleOrNull() ?: 35.0
        val m2 = margin2.toDoubleOrNull() ?: 25.0
        val m3 = margin3.toDoubleOrNull() ?: 18.0
        val m4 = margin4.toDoubleOrNull() ?: 12.0
        price1 = String.format(Locale.US, "%.2f", newCost * (1.0 + m1 / 100.0))
        price2 = String.format(Locale.US, "%.2f", newCost * (1.0 + m2 / 100.0))
        price3 = String.format(Locale.US, "%.2f", newCost * (1.0 + m3 / 100.0))
        price4 = String.format(Locale.US, "%.2f", newCost * (1.0 + m4 / 100.0))
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = A2WindowBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
            modifier = Modifier.widthIn(max = 560.dp).padding(6.dp).testTag("a2_product_dialog")
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Title bar
                Row(
                    modifier = Modifier.fillMaxWidth().background(A2TitleNavyDark).padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initial == null) "a2 ADMINISTRATIVO - FICHA DEL ARTÍCULO (NUEVO)" else "a2 ADMINISTRATIVO - FICHA DEL ARTÍCULO (${initial.code})",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // a2 Folder Tabs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf("1. General & Identificación", "2. Costos & Precios 1-4", "3. Almacenes & Lotes").forEachIndexed { index, label ->
                        Surface(
                            color = if (activeTab == index) A2SurfaceWhite else A2ControlFace,
                            shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (activeTab == index) A2TitleBlue else A2BorderMid),
                            modifier = Modifier.weight(1f).clickable { activeTab = index }
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = if (activeTab == index) FontWeight.Black else FontWeight.Bold,
                                color = if (activeTab == index) A2TitleNavyDark else Color(0xFF64748B),
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Tab Content Body
                Surface(
                    color = A2SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderMid),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp).padding(top = 2.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        when (activeTab) {
                            0 -> {
                                // Tab 1: General & Identificación
                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Código Principal", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                        OutlinedTextField(value = altCode, onValueChange = { altCode = it }, label = { Text("Cód. Alterno / Fábrica", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                        OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Código de Barras EAN", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1.2f).height(42.dp))
                                    }
                                }

                                item {
                                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Descripción Completa del Artículo", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth().height(42.dp))
                                }

                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(value = shortName, onValueChange = { shortName = it }, label = { Text("Descripción Corta (Ticket Fiscal)", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1.4f).height(42.dp))
                                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Departamento", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                    }
                                }

                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Marca / Fabricante", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                        OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modelo / Línea", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Ubicación en Almacén", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(0.8f).height(42.dp))
                                    }
                                }

                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unid. Principal", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                        OutlinedTextField(value = secondaryUnit, onValueChange = { secondaryUnit = it }, label = { Text("Unid. Empaque", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                        OutlinedTextField(value = conversionFactor, onValueChange = { conversionFactor = it }, label = { Text("Factor Empaque", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                    }
                                }

                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = isExempt, onCheckedChange = { isExempt = it })
                                            Text("Exento de IVA", fontSize = 9.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = handlesLot, onCheckedChange = { handlesLot = it })
                                            Text("Maneja Lote / Vencimiento", fontSize = 9.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = isWeighable, onCheckedChange = { isWeighable = it })
                                            Text("Pesable en Balanza", fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                            1 -> {
                                // Tab 2: Costos & Precios 1-4
                                item {
                                    Text("Costos Base (Bimoneda Tasa: $bcvRate Bs/$):", fontSize = 10.sp, fontWeight = FontWeight.Black, color = A2TitleNavyDark)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(
                                            value = costUsd,
                                            onValueChange = {
                                                costUsd = it
                                                it.toDoubleOrNull()?.let { c -> recalcFromCost(c) }
                                            },
                                            label = { Text("Costo Moneda Extranjera ($)", fontSize = 9.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f).height(42.dp)
                                        )
                                        val cUsd = costUsd.toDoubleOrNull() ?: 0.0
                                        OutlinedTextField(
                                            value = Formatters.formatVes(cUsd * bcvRate),
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Costo Nacional (Bs.)", fontSize = 9.sp) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f).height(42.dp)
                                        )
                                    }
                                }

                                item {
                                    Text("Listas de Precios a2 Softway (P1 Detal, P2 Mayor, P3 Distribuidor, P4 Empleado):", fontSize = 10.sp, fontWeight = FontWeight.Black, color = A2TitleNavyDark)
                                }

                                // P1 & P2
                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(value = margin1, onValueChange = { margin1 = it; costUsd.toDoubleOrNull()?.let { c -> recalcFromCost(c) } }, label = { Text("Margen 1 %", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(0.7f).height(42.dp))
                                        OutlinedTextField(value = price1, onValueChange = { price1 = it }, label = { Text("P1 Detal ($)", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                        OutlinedTextField(value = margin2, onValueChange = { margin2 = it; costUsd.toDoubleOrNull()?.let { c -> recalcFromCost(c) } }, label = { Text("Margen 2 %", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(0.7f).height(42.dp))
                                        OutlinedTextField(value = price2, onValueChange = { price2 = it }, label = { Text("P2 Mayor ($)", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                    }
                                }

                                // P3 & P4
                                item {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(value = margin3, onValueChange = { margin3 = it; costUsd.toDoubleOrNull()?.let { c -> recalcFromCost(c) } }, label = { Text("Margen 3 %", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(0.7f).height(42.dp))
                                        OutlinedTextField(value = price3, onValueChange = { price3 = it }, label = { Text("P3 Distrib. ($)", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                        OutlinedTextField(value = margin4, onValueChange = { margin4 = it; costUsd.toDoubleOrNull()?.let { c -> recalcFromCost(c) } }, label = { Text("Margen 4 %", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(0.7f).height(42.dp))
                                        OutlinedTextField(value = price4, onValueChange = { price4 = it }, label = { Text("P4 Mínimo ($)", fontSize = 8.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                    }
                                }

                                item {
                                    OutlinedTextField(value = commission, onValueChange = { commission = it }, label = { Text("Comisión a Vendedor (%)", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.width(180.dp).height(42.dp))
                                }
                            }
                            2 -> {
                                // Tab 3: Almacenes & Lotes
                                item {
                                    Text("Control de Stock y Parámetros por Almacén:", fontSize = 10.sp, fontWeight = FontWeight.Black, color = A2TitleNavyDark)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(value = minStock, onValueChange = { minStock = it }, label = { Text("Stock Mínimo Alerta", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                        OutlinedTextField(value = maxStock, onValueChange = { maxStock = it }, label = { Text("Stock Máximo Capacidad", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                    }
                                }

                                item {
                                    Text("Existencias Iniciales por Depósito:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(value = stockWh1, onValueChange = { stockWh1 = it }, label = { Text("Alm. Principal Caracas", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                        OutlinedTextField(value = stockWh2, onValueChange = { stockWh2 = it }, label = { Text("Depósito Maracay", fontSize = 9.sp) }, singleLine = true, modifier = Modifier.weight(1f).height(42.dp))
                                    }
                                }

                                item {
                                    val totalStk = (stockWh1.toDoubleOrNull() ?: 0.0) + (stockWh2.toDoubleOrNull() ?: 0.0)
                                    Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(2.dp), modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("TOTAL EXISTENCIA GLOBAL:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("${totalStk.toInt()} $unit", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF15803D))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // a2 Buttons Bar
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(2.dp)) {
                        Text("Cancelar", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val c = costUsd.replace(',', '.').toDoubleOrNull() ?: 1.0
                                val p1 = price1.replace(',', '.').toDoubleOrNull() ?: 1.35
                                val p2 = price2.replace(',', '.').toDoubleOrNull() ?: 1.25
                                val p3 = price3.replace(',', '.').toDoubleOrNull() ?: 1.18
                                val p4 = price4.replace(',', '.').toDoubleOrNull() ?: 1.12
                                val minS = minStock.toDoubleOrNull() ?: 5.0
                                val maxS = maxStock.toDoubleOrNull() ?: 500.0
                                val w1 = stockWh1.toDoubleOrNull() ?: 0.0
                                val w2 = stockWh2.toDoubleOrNull() ?: 0.0
                                val totStock = w1 + w2

                                val entity = (initial ?: ProductEntity(
                                    code = code,
                                    alternateCode = altCode,
                                    barcode = barcode,
                                    name = name,
                                    category = category,
                                    costUsd = c,
                                    price1Usd = p1
                                )).copy(
                                    code = code,
                                    alternateCode = altCode,
                                    barcode = barcode,
                                    name = name,
                                    shortName = shortName.ifBlank { name.take(20) },
                                    category = category,
                                    brand = brand,
                                    model = model,
                                    location = location,
                                    unit = unit,
                                    secondaryUnit = secondaryUnit,
                                    conversionFactor = conversionFactor.toDoubleOrNull() ?: 1.0,
                                    itemType = itemType,
                                    costUsd = c,
                                    margin1Percent = margin1.toDoubleOrNull() ?: 35.0,
                                    price1Usd = p1,
                                    margin2Percent = margin2.toDoubleOrNull() ?: 25.0,
                                    price2Usd = p2,
                                    margin3Percent = margin3.toDoubleOrNull() ?: 18.0,
                                    price3Usd = p3,
                                    margin4Percent = margin4.toDoubleOrNull() ?: 12.0,
                                    price4Usd = p4,
                                    salesCommissionPercent = commission.toDoubleOrNull() ?: 2.0,
                                    taxRatePercent = if (isExempt) 0.0 else 16.0,
                                    isExempt = isExempt,
                                    handlesLot = handlesLot,
                                    handlesSerial = handlesSerial,
                                    isWeighable = isWeighable,
                                    minStock = minS,
                                    maxStock = maxS,
                                    stockWarehouse1 = w1,
                                    stockWarehouse2 = w2,
                                    currentStock = totStock,
                                    isActive = true
                                )
                                onSave(entity)
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("btn_save_a2_product"),
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = A2TitleBlue)
                    ) {
                        Text("Guardar Ficha (F9)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
