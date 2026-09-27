package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ErpModule

@Composable
fun ErpToolBar(
    onNavigate: (ErpModule) -> Unit,
    onOpenRateDialog: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ToolButton(
                icon = Icons.Default.PointOfSale,
                label = "Facturar POS",
                tag = "btn_pos_toolbar",
                isPrimary = true,
                onClick = { onNavigate(ErpModule.VENTAS_POS) }
            )
            ToolButton(
                icon = Icons.Default.AddShoppingCart,
                label = "Nueva Compra",
                tag = "btn_compra_toolbar",
                onClick = { onNavigate(ErpModule.COMPRAS) }
            )
            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 2.dp))

            ToolButton(
                icon = Icons.Default.Inventory2,
                label = "Productos",
                tag = "btn_prod_toolbar",
                onClick = { onNavigate(ErpModule.PRODUCTOS) }
            )
            ToolButton(
                icon = Icons.Default.Assessment,
                label = "Kardex",
                tag = "btn_kardex_toolbar",
                onClick = { onNavigate(ErpModule.INVENTARIO_KARDEX) }
            )
            ToolButton(
                icon = Icons.Default.People,
                label = "Clientes",
                tag = "btn_cli_toolbar",
                onClick = { onNavigate(ErpModule.CLIENTES) }
            )
            ToolButton(
                icon = Icons.Default.LocalShipping,
                label = "Proveedores",
                tag = "btn_prov_toolbar",
                onClick = { onNavigate(ErpModule.PROVEEDORES) }
            )
            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 2.dp))

            ToolButton(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Cobros CxC",
                tag = "btn_cxc_toolbar",
                onClick = { onNavigate(ErpModule.CXC) }
            )
            ToolButton(
                icon = Icons.Default.Payment,
                label = "Pagos CxP",
                tag = "btn_cxp_toolbar",
                onClick = { onNavigate(ErpModule.CXP) }
            )
            ToolButton(
                icon = Icons.Default.Savings,
                label = "Caja & Bancos",
                tag = "btn_caja_toolbar",
                onClick = { onNavigate(ErpModule.CAJA_BANCOS) }
            )
            VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 2.dp))

            ToolButton(
                icon = Icons.Default.Summarize,
                label = "Reportes IVA",
                tag = "btn_rep_toolbar",
                onClick = { onNavigate(ErpModule.REPORTES_FISCALES) }
            )
            ToolButton(
                icon = Icons.Default.CurrencyExchange,
                label = "Tasa BCV",
                tag = "btn_rate_toolbar",
                onClick = onOpenRateDialog
            )
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    tag: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier
            .height(34.dp)
            .testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
