package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserEntity
import com.example.ui.components.Formatters
import com.example.ui.components.StatusBadge
import com.example.viewmodel.ErpViewModel

@Composable
fun AuditSecurityScreen(viewModel: ErpViewModel) {
    val auditLogs by viewModel.auditLogs.collectAsState()
    val users by viewModel.users.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Bitácora de Auditoría, 1 = Usuarios

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(10.dp)
            .testTag("audit_security_screen"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tab Header
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Bitácora de Auditoría", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        shape = RoundedCornerShape(3.dp)
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Usuarios y Permisos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        shape = RoundedCornerShape(3.dp)
                    )
                }

                Text(
                    text = "Sesión activa: ${currentUser.username} (${currentUser.role})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            }
        }

        if (selectedTab == 0) {
            // Audit Log Grid
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
                        Text("Fecha / Hora", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(115.dp))
                        Text("Usuario", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(80.dp))
                        Text("Módulo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(85.dp))
                        Text("Acción", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(85.dp))
                        Text("Descripción de la Operación", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                        Text("Ref Doc", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(80.dp))
                    }

                    if (auditLogs.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No hay registros de auditoría.", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(auditLogs) { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(Formatters.formatDateTime(log.timestamp), fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.width(115.dp))
                                    Text(log.user, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.width(80.dp))
                                    Text(log.module, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(85.dp))
                                    Box(modifier = Modifier.width(85.dp)) {
                                        StatusBadge(status = log.action)
                                    }
                                    Text(log.description, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Text(log.documentReference, fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.width(80.dp))
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        } else {
            // Users list
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    Text("Gestión de Usuarios y Cambio de Sesión Rápido", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(users) { u ->
                            val isCurrent = u.id == currentUser.id
                            Surface(
                                color = if (isCurrent) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrent) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setCurrentUser(u) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "${u.fullName} (@${u.username})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = "Rol: ${u.role} | PIN: **** | Estado: Activo", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }

                                    if (isCurrent) {
                                        StatusBadge(status = "SESIÓN ACTIVA")
                                    } else {
                                        Button(
                                            onClick = { viewModel.setCurrentUser(u) },
                                            shape = RoundedCornerShape(3.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Cambiar a este usuario", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
