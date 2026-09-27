package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CashSessionEntity
import com.example.data.entity.CompanyEntity
import com.example.data.entity.ExchangeRateEntity
import com.example.data.entity.UserEntity
import java.util.Locale

@Composable
fun ErpStatusBar(
    latestRate: ExchangeRateEntity?,
    company: CompanyEntity?,
    user: UserEntity,
    activeSession: CashSessionEntity?,
    onOpenRateDialog: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A), // Deep Slate Navy for bottom status bar
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live BCV Rate Badge (Clickable to change)
            Row(
                modifier = Modifier
                    .clickable { onOpenRateDialog() }
                    .background(Color(0xFF1E293B), shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("status_bcv_rate"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CurrencyExchange,
                    contentDescription = "Tasa BCV",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "BCV: ${String.format(Locale.US, "%.2f", latestRate?.rate ?: 62.50)} Bs/$",
                    color = Color(0xFFFDE68A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Company RIF & Trade Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "${company?.tradeName ?: "LOS ANDES C.A."} (${company?.rif ?: "J-40123456-7"})",
                    color = Color(0xFFE2E8F0),
                    fontSize = 10.sp
                )
            }

            // User Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Usuario: ${user.username} [${user.role}]",
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp
                )
            }

            // Cash Register Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isOpen = activeSession != null
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isOpen) Color(0xFF22C55E) else Color(0xFFEF4444),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
                Text(
                    text = if (isOpen) "CAJA: ${activeSession.registerName} (ABIERTA)" else "CAJA: CERRADA",
                    color = if (isOpen) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Fiscal Year / IVA
            Text(
                text = "IVA: ${company?.defaultTaxPercent ?: 16.0}% | IGTF: ${company?.igtfPercent ?: 3.0}%",
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}
