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
import com.example.ui.components.DualCurrencyBadge
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.viewmodel.ErpViewModel

@Composable
fun SalesHistoryScreen(
    viewModel: ErpViewModel,
    onViewDocument: (Long) -> Unit
) {
    val sales by viewModel.sales.collectAsState()
    var searchFilter by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("TODOS") }

    val filteredSales = sales.filter { s ->
        (selectedTypeFilter == "TODOS" || s.docType == selectedTypeFilter) &&
        (s.docNumber.contains(searchFilter, ignoreCase = true) ||
         s.clientName.contains(searchFilter, ignoreCase = true) ||
         s.clientRif.contains(searchFilter, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
            .testTag("sales_history_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Filter Bar
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
                OutlinedTextField(
                    value = searchFilter,
                    onValueChange = { searchFilter = it },
                    placeholder = { Text("Buscar por Nro Documento, Cliente o RIF...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(4.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("TODOS", "FACTURA", "NOTA_ENTREGA", "COTIZACION").forEach { filter ->
                        FilterChip(
                            selected = selectedTypeFilter == filter,
                            onClick = { selectedTypeFilter = filter },
                            label = { Text(filter.replace("_", " "), fontSize = 10.sp) },
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }
        }

        // Dense Grid Data Table
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nro Doc", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp))
                    Text("Fecha / Hora", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(110.dp))
                    Text("Cliente / RIF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                    Text("Estado", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(70.dp))
                    Text("Tasa BCV", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                    Text("Total ($ / Bs)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(150.dp), textAlign = TextAlign.End)
                    Text("Acción", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                }

                if (filteredSales.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron documentos de venta.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(filteredSales) { sale ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onViewDocument(sale.id) }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = sale.docNumber, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.width(90.dp))
                                Text(text = Formatters.formatDate(sale.date), fontSize = 10.sp, color = Color(0xFF475569), modifier = Modifier.width(110.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = sale.clientName, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Text(text = sale.clientRif, fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                                Box(modifier = Modifier.width(70.dp)) {
                                    StatusBadge(status = sale.paymentStatus)
                                }
                                Text(
                                    text = "${sale.bcvRate} Bs/$",
                                    fontSize = 10.sp,
                                    modifier = Modifier.width(70.dp),
                                    textAlign = TextAlign.End,
                                    color = Color(0xFF92400E)
                                )
                                Box(modifier = Modifier.width(150.dp), contentAlignment = Alignment.CenterEnd) {
                                    DualCurrencyBadge(amountUsd = sale.totalUsd, bcvRate = sale.bcvRate)
                                }
                                Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.Center) {
                                    IconButton(
                                        onClick = { onViewDocument(sale.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = "Imprimir", tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
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
}
