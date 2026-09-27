package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ErpViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: ErpViewModel) {
    val company by viewModel.company.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()
    val users by viewModel.users.collectAsState()
    val rate = latestRate?.rate ?: 62.50

    val companyOptions = listOf(
        "DISTRIBUIDORA & COMERCIALIZADORA LOS ANDES C.A. (RIF: J-40123456-7) - SEDE PRINCIPAL",
        "SUCURSAL 02 MARACAY (RIF: J-40123456-7) - ZONA INDUSTRIAL",
        "EMPRESA DEMO / CAPACITACIÓN VENEZUELA C.A."
    )

    var selectedCompany by remember { mutableStateOf(companyOptions[0]) }
    var companyExpanded by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("ADMIN") }
    var password by remember { mutableStateOf("1234") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberUser by remember { mutableStateOf(true) }
    var fastPosMode by remember { mutableStateOf(false) }
    var fiscalYear by remember { mutableStateOf("2026") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun doLogin() {
        if (username.isBlank()) {
            errorMessage = "Debe indicar el nombre de usuario."
            return
        }
        val success = viewModel.login(username.trim(), password.trim())
        if (!success) {
            errorMessage = "Usuario o clave inválida. Pruebe ADMIN con PIN 1234."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Desktop MDI Login Modal Window
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .wrapContentHeight()
                .testTag("login_window"),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Classic Desktop Title Bar
                Surface(
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "a2 ERP Administrativo - Acceso al Sistema",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        // Window Control Icons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF38BDF8), RoundedCornerShape(2.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFFEF4444), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }

                // Header Banner inside window
                Surface(
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E3A8A),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE68A),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "a2 Profesional Venezuela Pro",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Motor ERP Bimoneda USD/VES • Tasa BCV: ${String.format(Locale.US, "%.2f", rate)} Bs/$",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Form Fields Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Company Selection (Exposed Dropdown)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Empresa / Razón Social:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )

                        ExposedDropdownMenuBox(
                            expanded = companyExpanded,
                            onExpandedChange = { companyExpanded = !companyExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCompany,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = companyExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("login_company_select"),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                shape = RoundedCornerShape(4.dp),
                                singleLine = true
                            )

                            ExposedDropdownMenu(
                                expanded = companyExpanded,
                                onDismissRequest = { companyExpanded = false }
                            ) {
                                companyOptions.forEach { comp ->
                                    DropdownMenuItem(
                                        text = { Text(comp, fontSize = 11.sp) },
                                        onClick = {
                                            selectedCompany = comp
                                            companyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Fiscal Year & Station
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fiscalYear,
                            onValueChange = { fiscalYear = it },
                            label = { Text("Ejercicio Fiscal", fontSize = 10.sp) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(4.dp)
                        )

                        OutlinedTextField(
                            value = "ESTACIÓN-01 (Mostrador)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Terminal / Estación", fontSize = 10.sp) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                            modifier = Modifier.weight(1.4f).height(46.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }

                    // Username Field
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Usuario / Operador:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                errorMessage = null
                            },
                            placeholder = { Text("Ingrese usuario (ej. ADMIN, CAJERO1)", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(16.dp))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("login_username_field"),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }

                    // Quick User Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Rápido:", fontSize = 10.sp, color = Color(0xFF64748B))
                        listOf("ADMIN", "CAJERO1", "SUPERVISOR").forEach { u ->
                            FilterChip(
                                selected = username.equals(u, ignoreCase = true),
                                onClick = {
                                    username = u
                                    password = if (u == "ADMIN") "1234" else if (u == "CAJERO1") "0000" else "7777"
                                    errorMessage = null
                                },
                                label = { Text(u, fontSize = 9.sp) },
                                shape = RoundedCornerShape(3.dp),
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }

                    // Password / PIN Field
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Contraseña / Clave de Acceso:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            placeholder = { Text("Ingrese contraseña", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(16.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Ver contraseña",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { doLogin() }),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("login_password_field"),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }

                    // Checkboxes & Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberUser,
                                onCheckedChange = { rememberUser = it },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Recordar usuario", fontSize = 10.sp, color = Color(0xFF334155))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = fastPosMode,
                                onCheckedChange = { fastPosMode = it },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Modo POS Rápido", fontSize = 10.sp, color = Color(0xFF334155))
                        }
                    }

                    // Error Message
                    if (errorMessage != null) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                Text(text = errorMessage!!, fontSize = 10.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                username = ""
                                password = ""
                                errorMessage = null
                            },
                            modifier = Modifier.weight(0.7f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Limpiar", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { doLogin() },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(40.dp)
                                .testTag("btn_submit_login"),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Iniciar Sesión (F9)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // Footer Info
                Surface(
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Motor SQLite Room • Modo Offline Activo",
                            fontSize = 9.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "a2 ERP v2026.1",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }
        }
    }
}
