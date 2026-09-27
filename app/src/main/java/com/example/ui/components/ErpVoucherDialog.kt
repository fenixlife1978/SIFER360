package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.CompanyEntity
import com.example.data.entity.SalesDocumentEntity
import com.example.data.entity.SalesItemEntity
import com.example.viewmodel.ErpViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ErpVoucherDialog(
    docId: Long,
    viewModel: ErpViewModel,
    onDismiss: () -> Unit
) {
    val company by viewModel.company.collectAsState()
    var doc by remember { mutableStateOf<SalesDocumentEntity?>(null) }
    var items by remember { mutableStateOf<List<SalesItemEntity>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(docId) {
        val result = viewModel.repository.getSaleDetails(docId)
        doc = result.first
        items = result.second
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxHeight(0.9f)
                .padding(16.dp)
                .testTag("voucher_dialog"),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header action bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMPROBANTE FISCAL / DOCUMENTO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF64748B))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Scrollable Voucher Ticket Content
                if (doc == null) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                } else {
                    val currentDoc = doc!!
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Company Header
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = company?.businessName ?: "DISTRIBUIDORA LOS ANDES C.A.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "RIF: ${company?.rif ?: "J-40123456-7"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = company?.address ?: "Caracas, Venezuela",
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Tel: ${company?.phone ?: "0212-5551234"}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFCBD5E1))
                        }

                        // Doc Type & Number
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${currentDoc.docType}: ${currentDoc.docNumber}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Text(
                                        text = "Control: ${currentDoc.controlNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = "Fecha: ${Formatters.formatDateTime(currentDoc.date)}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF475569)
                                )
                                Text(
                                    text = "Tasa BCV Aplicada: ${String.format(Locale.US, "%.2f", currentDoc.bcvRate)} Bs/$",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        // Client Info
                        item {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "CLIENTE: ${currentDoc.clientName}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E3A8A))
                                    Text(text = "RIF / C.I.: ${currentDoc.clientRif}", fontSize = 10.sp, color = Color(0xFF1E293B))
                                    if (currentDoc.clientAddress.isNotBlank()) {
                                        Text(text = "Dirección: ${currentDoc.clientAddress}", fontSize = 10.sp, color = Color(0xFF475569))
                                    }
                                }
                            }
                        }

                        // Items Table Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE2E8F0))
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Cant", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                                Text(text = "Descripción", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(text = "P.Unit", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp), textAlign = TextAlign.End)
                                Text(text = "Total $", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp), textAlign = TextAlign.End)
                            }
                        }

                        // Items Rows
                        items(items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${item.quantity.toInt()}", fontSize = 10.sp, modifier = Modifier.width(36.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.productName, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "${item.productCode} ${if (item.taxRatePercent == 0.0) "(E)" else "(${item.taxRatePercent}%)"}",
                                        fontSize = 9.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = Formatters.formatUsd(item.unitPriceUsd),
                                    fontSize = 10.sp,
                                    modifier = Modifier.width(55.dp),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    text = Formatters.formatUsd(item.totalUsd),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(60.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        item {
                            HorizontalDivider(color = Color(0xFFCBD5E1))
                        }

                        // Totals Summary
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                SummaryRow("Subtotal:", Formatters.formatUsd(currentDoc.subtotalUsd), Formatters.formatVes(currentDoc.subtotalUsd * currentDoc.bcvRate))
                                if (currentDoc.exemptAmountUsd > 0) {
                                    SummaryRow("Monto Exento:", Formatters.formatUsd(currentDoc.exemptAmountUsd), Formatters.formatVes(currentDoc.exemptAmountUsd * currentDoc.bcvRate))
                                }
                                SummaryRow("Base Imponible:", Formatters.formatUsd(currentDoc.baseImponibleUsd), Formatters.formatVes(currentDoc.baseImponibleUsd * currentDoc.bcvRate))
                                SummaryRow("IVA (16%):", Formatters.formatUsd(currentDoc.taxAmountUsd), Formatters.formatVes(currentDoc.taxAmountUsd * currentDoc.bcvRate))
                                if (currentDoc.igtfAmountUsd > 0) {
                                    SummaryRow("IGTF (3% Divisas):", Formatters.formatUsd(currentDoc.igtfAmountUsd), Formatters.formatVes(currentDoc.igtfAmountUsd * currentDoc.bcvRate))
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.5.dp, color = Color(0xFF0F172A))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "TOTAL A PAGAR:", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color(0xFF0F172A))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = Formatters.formatUsd(currentDoc.totalUsd), fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF15803D))
                                        Text(text = Formatters.formatVes(currentDoc.totalVes), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E40AF))
                                    }
                                }
                            }
                        }

                        // Payment Methods
                        item {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "FORMAS DE PAGO / CANCELACIÓN:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF334155))
                                    Text(text = currentDoc.paymentMethodsSummary, fontSize = 10.sp, color = Color(0xFF0F172A))
                                    Text(text = "Estado: ${currentDoc.paymentStatus} | Cajero: ${currentDoc.cashierUser}", fontSize = 9.sp, color = Color(0xFF64748B))
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Gracias por su compra. Documento emitido conforme a normativas SENIAT.",
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                // Dialog Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Cerrar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.showMessage("Comprobante enviado a cola de impresión fiscal/térmica.")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("btn_print_voucher"),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Imprimir", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, valUsd: String, valVes: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF475569))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = valUsd, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF15803D))
            Text(text = valVes, fontSize = 10.sp, color = Color(0xFF1E40AF))
        }
    }
}
