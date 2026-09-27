package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.components.StatusBadge
import com.example.viewmodel.ErpViewModel

@Composable
fun PurchasesHistoryScreen(viewModel: ErpViewModel) {
    val purchases by viewModel.purchases.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
            .testTag("purchases_history_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
                    Text("Nro Compra", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(85.dp))
                    Text("Factura Proveedor", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(100.dp))
                    Text("Fecha", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(90.dp))
                    Text("Proveedor / RIF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                    Text("Estado", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(70.dp))
                    Text("Tasa BCV", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                    Text("Total Compra ($ / Bs)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(150.dp), textAlign = TextAlign.End)
                }

                if (purchases.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No se han registrado compras.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(purchases) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = p.docNumber, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488), modifier = Modifier.width(85.dp))
                                Text(text = p.controlNumber, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(100.dp))
                                Text(text = Formatters.formatDate(p.date), fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.width(90.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = p.providerName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text(text = p.providerRif, fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                                Box(modifier = Modifier.width(70.dp)) {
                                    StatusBadge(status = p.status)
                                }
                                Text(text = "${p.bcvRate} Bs", fontSize = 10.sp, modifier = Modifier.width(65.dp), textAlign = TextAlign.End)
                                Box(modifier = Modifier.width(150.dp), contentAlignment = Alignment.CenterEnd) {
                                    DualCurrencyBadge(amountUsd = p.totalUsd, bcvRate = p.bcvRate)
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
