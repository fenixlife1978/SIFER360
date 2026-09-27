package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.ExchangeRateEntity
import com.example.viewmodel.ErpViewModel
import java.util.Locale

@Composable
fun ExchangeRateDialog(
    currentRate: ExchangeRateEntity?,
    rateHistory: List<ExchangeRateEntity>,
    onUpdateRate: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var rateInput by remember { mutableStateOf(currentRate?.rate?.toString() ?: "62.50") }
    var sourceSelected by remember { mutableStateOf("BCV Oficial") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(max = 440.dp)
                .padding(8.dp)
                .testTag("rate_dialog")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Gestión de Tasa de Cambio (Bimoneda)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "La tasa del Banco Central de Venezuela (BCV) rige la conversión automática en presupuestos, facturación, compras y cuentas por cobrar.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Current Rate Box
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Tasa Activa del Sistema", fontSize = 10.sp, color = Color(0xFF92400E))
                            Text(
                                text = "1 USD = ${String.format(Locale.US, "%.2f", currentRate?.rate ?: 62.50)} Bs.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF78350F)
                            )
                        }
                        Text(
                            text = currentRate?.source ?: "BCV Oficial",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }

                // Input Field
                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = { Text("Nueva Tasa de Cambio (Bs./USD)", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("input_new_rate"),
                    shape = RoundedCornerShape(4.dp)
                )

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = sourceSelected == "BCV Oficial",
                        onClick = { sourceSelected = "BCV Oficial" },
                        label = { Text("BCV Oficial", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = sourceSelected == "Tasa Manual",
                        onClick = { sourceSelected = "Tasa Manual" },
                        label = { Text("Ajuste Manual", fontSize = 10.sp) }
                    )
                }

                // History Preview
                if (rateHistory.isNotEmpty()) {
                    Text(
                        text = "Historial Reciente de Tasas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(rateHistory.take(5)) { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = Formatters.formatDateTime(r.effectiveDate),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", r.rate)} Bs/$ (${r.source})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Cancelar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val parsed = rateInput.replace(',', '.').toDoubleOrNull()
                            if (parsed != null && parsed > 0) {
                                onUpdateRate(parsed, sourceSelected)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("btn_save_rate"),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Aplicar Tasa", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CurrencyExchange,
                    contentDescription = null,
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "a2 ERP Administrativo Pro",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Edición Venezuela Bimoneda (USD / Bs)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFF3B82F6)
                )
                Text(
                    text = "Inspirado en el flujo clásico administrativo de a2 Profesional. Desarrollado con arquitectura nativa y persistencia completa en base de datos SQLite/Room.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("• Núcleo financiero Bimoneda USD + Bs (Tasa BCV)", fontSize = 10.sp, color = Color(0xFF334155))
                        Text("• IVA 16%, IGTF 3%, y exentos por renglón", fontSize = 10.sp, color = Color(0xFF334155))
                        Text("• Inventario multialmacén, kardex y ajustes", fontSize = 10.sp, color = Color(0xFF334155))
                        Text("• Cuentas por cobrar (CxC) y cuentas por pagar (CxP)", fontSize = 10.sp, color = Color(0xFF334155))
                        Text("• Arqueo de caja X/Z y conciliación bancaria", fontSize = 10.sp, color = Color(0xFF334155))
                        Text("• Libros fiscales IVA y asientos contables automáticos", fontSize = 10.sp, color = Color(0xFF334155))
                    }
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Aceptar", fontSize = 12.sp)
                }
            }
        }
    }
}
