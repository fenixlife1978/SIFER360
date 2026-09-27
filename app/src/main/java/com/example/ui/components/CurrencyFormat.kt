package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

object Formatters {
    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    val dfVes = DecimalFormat("#,##0.00", symbols)
    val dfUsd = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    val dateDf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
    val shortDateDf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatUsd(amount: Double): String = "$${dfUsd.format(amount)}"
    fun formatVes(amount: Double): String = "Bs. ${dfVes.format(amount)}"
    fun formatDateTime(timestamp: Long): String = dateDf.format(Date(timestamp))
    fun formatDate(timestamp: Long): String = shortDateDf.format(Date(timestamp))
}

@Composable
fun DualCurrencyBadge(
    amountUsd: Double,
    bcvRate: Double,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    val amountVes = amountUsd * bcvRate
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = if (isHighlighted) Color(0xFF1E3A8A).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHighlighted) Color(0xFF1E3A8A).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = Formatters.formatUsd(amountUsd),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF15803D) // Emerald / Green for USD
            )
            Text(
                text = "|",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = Formatters.formatVes(amountVes),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Color(0xFF1E40AF) // Blue for VES
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "PAGADA", "ABIERTA", "ACTIVO", "EMITIDA", "REGISTRADA" -> Pair(Color(0xFFDCFCE7), Color(0xFF166534))
        "PENDIENTE", "PARCIAL" -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
        "ANULADA", "CERRADA", "INACTIVO" -> Pair(Color(0xFFFEE2E2), Color(0xFF991B1B))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF334155))
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(3.dp))
            .border(0.5.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = status,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
