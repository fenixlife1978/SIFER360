package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ClientEntity
import com.example.data.entity.ProductEntity
import com.example.data.repository.CartItem
import com.example.data.repository.PaymentEntry
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel
import java.util.Locale

@Composable
fun PosSalesScreen(
    viewModel: ErpViewModel,
    onSaleCompleted: (Long) -> Unit
) {
    val posState by viewModel.posState.collectAsState()
    val products by viewModel.products.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val company by viewModel.company.collectAsState()
    val heldTickets by viewModel.heldTickets.collectAsState()

    val rate = if (posState.isCustomRate && posState.customRate > 0) posState.customRate else (latestRate?.rate ?: 62.50)
    val igtfPercent = company?.igtfPercent ?: 3.0

    var barcodeInput by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("TODAS") }
    var showClientDialog by remember { mutableStateOf(false) }
    var showPaymentModal by remember { mutableStateOf(false) }
    var showCatalogPicker by remember { mutableStateOf(false) }
    var showHeldTicketsModal by remember { mutableStateOf(false) }
    var itemQuantityInput by remember { mutableStateOf("1") }

    // Computations
    val subtotalUsd = posState.cartItems.sumOf { it.quantity * it.unitPriceUsd * (1.0 - it.discountPercent / 100.0) }
    val exemptUsd = posState.cartItems.filter { it.taxRatePercent <= 0.0 }.sumOf { it.quantity * it.unitPriceUsd * (1.0 - it.discountPercent / 100.0) }
    val baseImponibleUsd = subtotalUsd - exemptUsd
    val taxUsd = posState.cartItems.sumOf { (it.quantity * it.unitPriceUsd * (1.0 - it.discountPercent / 100.0)) * (it.taxRatePercent / 100.0) }

    val foreignPayments = posState.payments.filter { it.method == "EFECTIVO_USD" || it.method == "ZELLE" }.sumOf { it.amountUsd }
    val igtfUsd = if (foreignPayments > 0) foreignPayments * (igtfPercent / 100.0) else 0.0
    val totalUsd = subtotalUsd + taxUsd + igtfUsd
    val totalVes = totalUsd * rate

    fun handleAddBarcode() {
        if (barcodeInput.isNotBlank()) {
            val query = barcodeInput.trim()
            val match = products.find {
                it.barcode.equals(query, ignoreCase = true) ||
                it.code.equals(query, ignoreCase = true) ||
                it.alternateCode.equals(query, ignoreCase = true) ||
                it.name.contains(query, ignoreCase = true)
            }
            if (match != null) {
                val qty = itemQuantityInput.toDoubleOrNull() ?: 1.0
                viewModel.addProductToCart(match, qty)
                barcodeInput = ""
                itemQuantityInput = "1"
            } else {
                viewModel.showMessage("Artículo no encontrado: $query")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(A2WindowBg)
            .padding(6.dp)
            .testTag("a2_pos_screen"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Top Header: Client Info & Fluorescent Green LCD Display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left: Client & Document Info Box
            Surface(
                color = A2SurfaceWhite,
                shape = RoundedCornerShape(3.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Document Type Selector Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            listOf("FACTURA", "NOTA_ENTREGA", "COTIZACION").forEach { dt ->
                                Surface(
                                    color = if (posState.docType == dt) A2TitleBlue else A2ControlFace,
                                    shape = RoundedCornerShape(2.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (posState.docType == dt) A2TitleNavyDark else A2BorderMid),
                                    modifier = Modifier
                                        .clickable { viewModel.setPosDocType(dt) }
                                        .padding(vertical = 1.dp)
                                ) {
                                    Text(
                                        text = dt.replace("_", " "),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (posState.docType == dt) Color.White else Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "ESTACIÓN: 01",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = A2TitleNavyDark
                        )
                    }

                    // Client Selector Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClientDialog = true }
                            .background(Color(0xFFE8EEF5), RoundedCornerShape(2.dp))
                            .border(1.dp, Color(0xFFB0C4DE), RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = A2TitleBlue, modifier = Modifier.size(16.dp))
                            Text(
                                text = posState.selectedClient?.let { "${it.name} | RIF: ${it.rif} (Lista P${it.priceList})" } ?: "[F4] Seleccionar Cliente / RIF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = A2TitleNavyDark
                            )
                        }
                        Text(
                            text = "Saldo: ${Formatters.formatUsd(posState.selectedClient?.currentBalanceUsd ?: 0.0)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if ((posState.selectedClient?.currentBalanceUsd ?: 0.0) > 0) A2RedAlert else Color(0xFF15803D)
                        )
                    }
                }
            }

            // Right: Authentic a2 Fluorescent Green/Yellow Digital LCD Display
            Surface(
                color = A2LcdBackground,
                shape = RoundedCornerShape(3.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1E3A8A)),
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "TOTAL A PAGAR (USD)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF86EFAC)
                        )
                        Text(
                            text = Formatters.formatUsd(totalUsd),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = A2LcdGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "TOTAL EN BOLÍVARES (VES)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD)
                        )
                        Text(
                            text = Formatters.formatVes(totalVes),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF60A5FA),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "TASA BCV: ${String.format(Locale.US, "%.2f", rate)} Bs/$ • ÍTEMS: ${posState.cartItems.sumOf { it.quantity }.toInt()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = A2LcdYellow
                        )
                    }
                }
            }
        }

        // Fast Barcode Input & Category Department Bar
        Surface(
            color = A2SurfaceWhite,
            shape = RoundedCornerShape(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Quantity input
                OutlinedTextField(
                    value = itemQuantityInput,
                    onValueChange = { itemQuantityInput = it },
                    label = { Text("Cant [F3]", fontSize = 9.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                    modifier = Modifier.width(70.dp).height(42.dp),
                    shape = RoundedCornerShape(2.dp)
                )

                // Barcode input
                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { barcodeInput = it },
                    placeholder = { Text("Escanee código de barras o ingrese código de producto [F2 Buscar]...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = A2TitleBlue, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleAddBarcode() }),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f).height(42.dp).testTag("pos_barcode_input"),
                    shape = RoundedCornerShape(2.dp)
                )

                Button(
                    onClick = { handleAddBarcode() },
                    shape = RoundedCornerShape(2.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = A2TitleBlue),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cargar Ítem", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showCatalogPicker = true },
                    shape = RoundedCornerShape(2.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Catálogo F2", fontSize = 11.sp)
                }
            }
        }

        // Quick Department Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                color = if (selectedCategoryFilter == "TODAS") A2TitleNavyDark else A2ControlFace,
                shape = RoundedCornerShape(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderMid),
                modifier = Modifier.clickable { selectedCategoryFilter = "TODAS" }
            ) {
                Text(
                    text = "TODOS LOS DEPARTAMENTOS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedCategoryFilter == "TODAS") Color.White else Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            categories.forEach { cat ->
                Surface(
                    color = if (selectedCategoryFilter == cat.name) A2TitleNavyDark else A2ControlFace,
                    shape = RoundedCornerShape(2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderMid),
                    modifier = Modifier.clickable { selectedCategoryFilter = cat.name }
                ) {
                    Text(
                        text = cat.name.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCategoryFilter == cat.name) Color.White else Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Main Body: Classic a2 Dense Items Grid & Summary Column
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Cart Items Grid (Classic Windows Grid)
            Surface(
                color = A2SurfaceWhite,
                shape = RoundedCornerShape(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // a2 Grid Header
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
                        Text("Descripción del Artículo", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                        Text("Unid", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(38.dp), textAlign = TextAlign.Center)
                        Text("Cant.", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(65.dp), textAlign = TextAlign.Center)
                        Text("Precio Unit $", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                        Text("Desc %", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                        Text("IVA", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                        Text("Total ($)", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                        Text("Total (Bs.)", fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(75.dp), textAlign = TextAlign.End)
                        Text("", modifier = Modifier.width(28.dp))
                    }

                    if (posState.cartItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(36.dp))
                                Text(
                                    text = "CAJA EN ESPERA DE ARTÍCULOS\nEscanee el código de barras o presione F2 para abrir el catálogo.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(posState.cartItems.size) { idx ->
                                val item = posState.cartItems[idx]
                                val lineSubUsd = item.quantity * item.unitPriceUsd * (1.0 - item.discountPercent / 100.0)
                                val lineSubVes = lineSubUsd * rate
                                val isEven = idx % 2 == 0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isEven) A2SurfaceWhite else A2GridAltRow)
                                        .padding(horizontal = 4.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "${idx + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                                    Text(text = item.productCode, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = A2TitleNavyDark, modifier = Modifier.width(65.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.productName, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                    }
                                    Text(text = item.unit, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.width(38.dp), textAlign = TextAlign.Center)

                                    // Quantity Selector
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.width(65.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.updateCartItemQuantity(item.productId, item.quantity - 1) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                        }
                                        Text(text = "${item.quantity.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        IconButton(
                                            onClick = { viewModel.updateCartItemQuantity(item.productId, item.quantity + 1) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                        }
                                    }

                                    Text(text = Formatters.formatUsd(item.unitPriceUsd), fontSize = 10.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                                    Text(text = "${item.discountPercent.toInt()}%", fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                                    Text(
                                        text = if (item.isExempt) "E" else "${item.taxRatePercent.toInt()}%",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.isExempt) Color(0xFF16A34A) else Color(0xFF2563EB),
                                        modifier = Modifier.width(45.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(text = Formatters.formatUsd(lineSubUsd), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF15803D), modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                                    Text(text = Formatters.formatVes(lineSubVes), fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E40AF), modifier = Modifier.width(75.dp), textAlign = TextAlign.End)

                                    IconButton(
                                        onClick = { viewModel.removeCartItem(item.productId) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = A2RedAlert, modifier = Modifier.size(13.dp))
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // Right Panel: Fiscal Totals & Fast Action Buttons
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Liquidación Fiscal Box
                Surface(
                    color = A2SurfaceWhite,
                    shape = RoundedCornerShape(2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "DESGLOSE FISCAL (a2 LIQUIDACIÓN)",
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = A2TitleNavyDark
                        )
                        HorizontalDivider(color = A2BorderMid)

                        SummaryLine("Subtotal Bruto:", Formatters.formatUsd(subtotalUsd), Formatters.formatVes(subtotalUsd * rate))
                        if (exemptUsd > 0) {
                            SummaryLine("Monto Exento (0%):", Formatters.formatUsd(exemptUsd), Formatters.formatVes(exemptUsd * rate))
                        }
                        SummaryLine("Base Imponible (16%):", Formatters.formatUsd(baseImponibleUsd), Formatters.formatVes(baseImponibleUsd * rate))
                        SummaryLine("IVA Débito Fiscal (16%):", Formatters.formatUsd(taxUsd), Formatters.formatVes(taxUsd * rate))
                        if (igtfUsd > 0) {
                            SummaryLine("IGTF Divisas (3%):", Formatters.formatUsd(igtfUsd), Formatters.formatVes(igtfUsd * rate))
                        }

                        HorizontalDivider(color = A2TitleNavyDark, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL VENTA:", fontSize = 11.sp, fontWeight = FontWeight.Black, color = A2TitleNavyDark)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(Formatters.formatUsd(totalUsd), fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF15803D))
                                Text(Formatters.formatVes(totalVes), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                            }
                        }
                    }
                }

                // Payments Added List
                Surface(
                    color = A2SurfaceWhite,
                    shape = RoundedCornerShape(2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Formas de Pago [F7]", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = A2TitleNavyDark)
                            TextButton(
                                onClick = { showPaymentModal = true },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("+ Agregar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (posState.payments.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Sin pagos registrados.\n(Presione F9 para totalizar y cobrar)", fontSize = 9.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(posState.payments.size) { idx ->
                                    val p = posState.payments[idx]
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = p.method.replace("_", " "), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = if (p.method == "EFECTIVO_USD" || p.method == "ZELLE") Formatters.formatUsd(p.amountUsd) else Formatters.formatVes(p.amountVes),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = A2TitleNavyDark
                                        )
                                        IconButton(onClick = { viewModel.removePayment(idx) }, modifier = Modifier.size(16.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = A2RedAlert, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Totalize & Emit Buttons
                Button(
                    onClick = {
                        if (posState.payments.isEmpty()) {
                            showPaymentModal = true
                        } else {
                            viewModel.processSale { docId ->
                                onSaleCompleted(docId)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_pos_totalize"),
                    shape = RoundedCornerShape(3.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TOTALIZAR Y COBRAR [F9]", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }

        // Classic a2 Function Keys Bar at Bottom
        Surface(
            color = A2TitleNavyDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FunctionKeyBadge("F2", "Catálogo") { showCatalogPicker = true }
                FunctionKeyBadge("F3", "Cantidad") {}
                FunctionKeyBadge("F4", "Cliente") { showClientDialog = true }
                FunctionKeyBadge("F5", "En Espera (${heldTickets.size})") { showHeldTicketsModal = true }
                FunctionKeyBadge("F7", "Pagos") { showPaymentModal = true }
                FunctionKeyBadge("F8", "Limpiar") { viewModel.clearCart() }
                FunctionKeyBadge("F9", "Cobrar") {
                    if (posState.payments.isEmpty()) showPaymentModal = true
                    else viewModel.processSale { onSaleCompleted(it) }
                }
            }
        }
    }

    // Modal Formas de Pago con Calculadora de Vuelto
    if (showPaymentModal) {
        A2PaymentModal(
            totalDueUsd = totalUsd,
            bcvRate = rate,
            onAddPayment = { entry ->
                viewModel.addPayment(entry)
            },
            onFinalizeSale = {
                viewModel.processSale { docId ->
                    showPaymentModal = false
                    onSaleCompleted(docId)
                }
            },
            onDismiss = { showPaymentModal = false }
        )
    }

    // Held Tickets Modal
    if (showHeldTicketsModal) {
        A2HeldTicketsModal(
            heldTickets = heldTickets,
            hasActiveCart = posState.cartItems.isNotEmpty(),
            onHoldCurrent = {
                viewModel.holdCurrentTicket()
                showHeldTicketsModal = false
            },
            onRecall = { id ->
                viewModel.recallTicket(id)
                showHeldTicketsModal = false
            },
            onDelete = { id ->
                viewModel.deleteHeldTicket(id)
            },
            onDismiss = { showHeldTicketsModal = false }
        )
    }

    // Catalog Picker Modal
    if (showCatalogPicker) {
        A2CatalogPickerModal(
            products = products,
            bcvRate = rate,
            onSelect = { prod ->
                val qty = itemQuantityInput.toDoubleOrNull() ?: 1.0
                viewModel.addProductToCart(prod, qty)
                showCatalogPicker = false
            },
            onDismiss = { showCatalogPicker = false }
        )
    }

    // Client Picker Modal
    if (showClientDialog) {
        A2ClientPickerModal(
            clients = clients,
            onSelect = { cli ->
                viewModel.setPosClient(cli)
                showClientDialog = false
            },
            onDismiss = { showClientDialog = false }
        )
    }
}

@Composable
private fun SummaryLine(label: String, usdVal: String, vesVal: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 9.sp, color = Color(0xFF475569))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = usdVal, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
            Text(text = "($vesVal)", fontSize = 8.sp, color = Color(0xFF1E40AF))
        }
    }
}

@Composable
private fun FunctionKeyBadge(key: String, label: String, onClick: () -> Unit) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = key, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFFACC15))
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

@Composable
private fun A2PaymentModal(
    totalDueUsd: Double,
    bcvRate: Double,
    onAddPayment: (PaymentEntry) -> Unit,
    onFinalizeSale: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf("PAGO_MOVIL") }
    var amountInput by remember { mutableStateOf("") }
    var refInput by remember { mutableStateOf("") }
    var receivedAmountUsd by remember { mutableStateOf("") }

    val methods = listOf(
        "PAGO_MOVIL" to "Pago Móvil (Bs.)",
        "EFECTIVO_USD" to "Efectivo Divisas ($)",
        "EFECTIVO_VES" to "Efectivo Bolívares (Bs.)",
        "ZELLE" to "Zelle (USD)",
        "TRANSFERENCIA" to "Transferencia Bancaria (Bs.)",
        "CREDITO" to "Crédito a Cuenta Corriente"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = A2WindowBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
            modifier = Modifier.widthIn(max = 480.dp).padding(8.dp).testTag("a2_payment_modal")
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                Text(
                    text = "a2 FORMAS DE PAGO & CALCULADORA DE CAMBIO",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = A2TitleNavyDark
                )

                // Fluorescent Display
                Surface(
                    color = A2LcdBackground,
                    shape = RoundedCornerShape(2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL FACTURA:", fontSize = 9.sp, color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold)
                            Text(Formatters.formatUsd(totalDueUsd), fontSize = 18.sp, fontWeight = FontWeight.Black, color = A2LcdGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EQUIVALENCIA BS:", fontSize = 9.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Bold)
                            Text(Formatters.formatVes(totalDueUsd * bcvRate), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                        }
                    }
                }

                // Payment Methods
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    methods.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { (k, label) ->
                                Surface(
                                    color = if (selectedMethod == k) A2TitleBlue else A2SurfaceWhite,
                                    shape = RoundedCornerShape(2.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedMethod == k) A2TitleNavyDark else A2BorderMid),
                                    modifier = Modifier.weight(1f).clickable { selectedMethod = k }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedMethod == k) Color.White else Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                val isUsd = selectedMethod == "EFECTIVO_USD" || selectedMethod == "ZELLE" || selectedMethod == "CREDITO"

                // Quick preset buttons for tender
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val exactAmount = if (isUsd) String.format(Locale.US, "%.2f", totalDueUsd) else String.format(Locale.US, "%.2f", totalDueUsd * bcvRate)
                    Surface(
                        color = Color(0xFF1E3A8A),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.clickable { amountInput = exactAmount }
                    ) {
                        Text("Exacto ($exactAmount)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    if (isUsd) {
                        listOf(1.0, 5.0, 10.0, 20.0, 50.0, 100.0).forEach { bill ->
                            Surface(
                                color = Color(0xFF0F766E),
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier.clickable {
                                    amountInput = bill.toInt().toString()
                                    if (selectedMethod == "EFECTIVO_USD") receivedAmountUsd = bill.toInt().toString()
                                }
                            ) {
                                Text("\$$bill", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text(if (isUsd) "Monto a Aplicar ($)" else "Monto a Aplicar (Bs.)", fontSize = 9.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(44.dp)
                    )

                    OutlinedTextField(
                        value = refInput,
                        onValueChange = { refInput = it },
                        label = { Text("Referencia / Comprobante", fontSize = 9.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(44.dp)
                    )
                }

                // Cashier Change Calculator
                if (selectedMethod == "EFECTIVO_USD") {
                    OutlinedTextField(
                        value = receivedAmountUsd,
                        onValueChange = { receivedAmountUsd = it },
                        label = { Text("Monto Recibido del Cliente ($ Billete)", fontSize = 9.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    )

                    val rec = receivedAmountUsd.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val changeUsd = (rec - totalDueUsd).coerceAtLeast(0.0)
                    val changeVes = changeUsd * bcvRate

                    if (rec > totalDueUsd) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF93C5FD)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("VUELTO A ENTREGAR:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = A2TitleNavyDark)
                                Text("${Formatters.formatUsd(changeUsd)} (${Formatters.formatVes(changeVes)})", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF15803D))
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            val parsed = amountInput.replace(',', '.').toDoubleOrNull() ?: totalDueUsd
                            val finalUsd = if (isUsd) parsed else (parsed / bcvRate)
                            val finalVes = if (isUsd) (parsed * bcvRate) else parsed
                            onAddPayment(PaymentEntry(selectedMethod, finalUsd, finalVes, refInput))
                            amountInput = ""
                            refInput = ""
                        },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = A2TitleBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Registrar Pago", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (amountInput.isNotBlank()) {
                                val parsed = amountInput.replace(',', '.').toDoubleOrNull() ?: totalDueUsd
                                val finalUsd = if (isUsd) parsed else (parsed / bcvRate)
                                val finalVes = if (isUsd) (parsed * bcvRate) else parsed
                                onAddPayment(PaymentEntry(selectedMethod, finalUsd, finalVes, refInput))
                            }
                            onFinalizeSale()
                        },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("EMITIR FACTURA (F9)", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun A2CatalogPickerModal(
    products: List<ProductEntity>,
    bcvRate: Double,
    onSelect: (ProductEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = products.filter {
        it.name.contains(search, ignoreCase = true) ||
        it.code.contains(search, ignoreCase = true) ||
        it.barcode.contains(search, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = A2WindowBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
            modifier = Modifier.widthIn(max = 550.dp).padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("CATÁLOGO DE ARTÍCULOS - a2 SOFTWAY", fontWeight = FontWeight.Black, fontSize = 12.sp, color = A2TitleNavyDark)
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Filtrar por código, descripción o barra...", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.heightIn(max = 320.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered) { p ->
                        Surface(
                            color = A2SurfaceWhite,
                            shape = RoundedCornerShape(2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderMid),
                            modifier = Modifier.clickable { onSelect(p) }
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1)
                                Text(text = "Cód: ${p.code} | Ubic: ${p.location}", fontSize = 8.sp, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(Formatters.formatUsd(p.price1Usd), fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF15803D))
                                    Text("Stock: ${p.currentStock.toInt()}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(2.dp)) {
                    Text("Cerrar", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun A2ClientPickerModal(
    clients: List<ClientEntity>,
    onSelect: (ClientEntity) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = A2WindowBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
            modifier = Modifier.widthIn(max = 450.dp).padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("SELECCIÓN DE CLIENTE / FICHA RIF (a2)", fontWeight = FontWeight.Black, fontSize = 12.sp, color = A2TitleNavyDark)
                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    items(clients) { c ->
                        Surface(
                            color = A2SurfaceWhite,
                            shape = RoundedCornerShape(2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderMid),
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(c) }.padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = c.name, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text(text = "RIF: ${c.rif} • Lista: P${c.priceList} • Días: ${c.creditDays}", fontSize = 8.sp, color = Color(0xFF64748B))
                                }
                                Text("Saldo: ${Formatters.formatUsd(c.currentBalanceUsd)}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (c.currentBalanceUsd > 0) A2RedAlert else Color(0xFF15803D))
                            }
                        }
                    }
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(2.dp)) {
                    Text("Cancelar", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun A2HeldTicketsModal(
    heldTickets: List<com.example.viewmodel.HoldTicket>,
    hasActiveCart: Boolean,
    onHoldCurrent: () -> Unit,
    onRecall: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = A2WindowBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderDark),
            modifier = Modifier.widthIn(max = 500.dp).padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIQUETES EN ESPERA (HOLD / RECALL)",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = A2TitleNavyDark
                    )
                    Text(
                        text = "${heldTickets.size} en cola",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }

                if (hasActiveCart) {
                    Button(
                        onClick = onHoldCurrent,
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Poner Carrito Actual en Espera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = A2BorderMid)

                if (heldTickets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No hay tiquetes en espera guardados.", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 260.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(heldTickets) { t ->
                            Surface(
                                color = A2SurfaceWhite,
                                shape = RoundedCornerShape(2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, A2BorderMid),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(t.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = A2TitleNavyDark)
                                        Text("Cliente: ${t.clientName} • ${t.itemCount} ítems", fontSize = 9.sp, color = Color(0xFF475569))
                                        Text("Total: \$${String.format(java.util.Locale.US, "%.2f", t.totalUsd)}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF15803D))
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = { onRecall(t.id) },
                                            shape = RoundedCornerShape(2.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Recuperar", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        IconButton(
                                            onClick = { onDelete(t.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = A2RedAlert, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = A2ControlFace),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", fontSize = 10.sp, color = Color.Black)
                }
            }
        }
    }
}
