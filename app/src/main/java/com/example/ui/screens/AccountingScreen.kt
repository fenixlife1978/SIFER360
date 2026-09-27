package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun AccountingScreen(viewModel: ErpViewModel) {
    val entries by viewModel.accountingEntries.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
            .testTag("accounting_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(22.dp))
                    Column {
                        Text(text = "Libro Diario General / Asientos Contables", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Generación automática vinculada a ventas, compras, cobros y pagos", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
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
                    Text("Nro Asiento", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(85.dp))
                    Text("Fecha", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(85.dp))
                    Text("Concepto del Asiento", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                    Text("Referencia", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp))
                    Text("Debe / Haber ($)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
                    Text("Debe / Haber (Bs.)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                }

                if (entries.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No hay asientos contables generados.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(entries) { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = entry.entryNumber, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.width(85.dp))
                                Text(text = Formatters.formatDate(entry.date), fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.width(85.dp))
                                Text(text = entry.concept, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                Text(text = entry.documentReference, fontSize = 9.sp, color = Color(0xFF475569), modifier = Modifier.width(90.dp))
                                Text(text = Formatters.formatUsd(entry.totalDebitUsd), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
                                Text(text = Formatters.formatVes(entry.totalDebitUsd * rate), fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E40AF), modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }
}
