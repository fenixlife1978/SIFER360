package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Save
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
import com.example.data.entity.CompanyEntity
import com.example.viewmodel.ErpViewModel

@Composable
fun SettingsScreen(viewModel: ErpViewModel) {
    val company by viewModel.company.collectAsState()
    val latestRate by viewModel.latestRate.collectAsState()

    var rif by remember(company) { mutableStateOf(company?.rif ?: "J-40123456-7") }
    var businessName by remember(company) { mutableStateOf(company?.businessName ?: "DISTRIBUIDORA & COMERCIALIZADORA LOS ANDES C.A.") }
    var tradeName by remember(company) { mutableStateOf(company?.tradeName ?: "LOS ANDES C.A. - SEDE PRINCIPAL") }
    var address by remember(company) { mutableStateOf(company?.address ?: "Av. Francisco de Miranda, Chacao, Caracas") }
    var phone by remember(company) { mutableStateOf(company?.phone ?: "0212-5551234") }
    var email by remember(company) { mutableStateOf(company?.email ?: "administracion@losandeserp.com.ve") }
    var taxPercent by remember(company) { mutableStateOf(company?.defaultTaxPercent?.toString() ?: "16.0") }
    var igtfPercent by remember(company) { mutableStateOf(company?.igtfPercent?.toString() ?: "3.0") }
    var fiscalYear by remember(company) { mutableStateOf(company?.fiscalYear ?: "2026") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(12.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(22.dp))
                        Text(text = "Configuración de la Empresa y Parámetros Fiscales", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rif,
                            onValueChange = { rif = it },
                            label = { Text("RIF Fiscal (e.g. J-40123456-7)", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(44.dp)
                        )
                        OutlinedTextField(
                            value = fiscalYear,
                            onValueChange = { fiscalYear = it },
                            label = { Text("Ejercicio Fiscal", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f).height(44.dp)
                        )
                    }

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Razón Social (Para Facturación SENIAT)", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    )

                    OutlinedTextField(
                        value = tradeName,
                        onValueChange = { tradeName = it },
                        label = { Text("Nombre Comercial / Sucursal", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección Fiscal Principal", fontSize = 10.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfonos", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(44.dp)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electrónico Administrativo", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(44.dp)
                        )
                    }

                    Text("Impuestos y Alícuotas de Ley (Venezuela):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E3A8A))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = taxPercent,
                            onValueChange = { taxPercent = it },
                            label = { Text("Alícuota IVA General (%)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(44.dp)
                        )
                        OutlinedTextField(
                            value = igtfPercent,
                            onValueChange = { igtfPercent = it },
                            label = { Text("Alícuota IGTF Divisas (%)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(44.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val tax = taxPercent.replace(',', '.').toDoubleOrNull() ?: 16.0
                            val igtf = igtfPercent.replace(',', '.').toDoubleOrNull() ?: 3.0
                            val updated = (company ?: CompanyEntity(rif = rif, businessName = businessName, tradeName = tradeName, address = address, phone = phone, email = email)).copy(
                                rif = rif,
                                businessName = businessName,
                                tradeName = tradeName,
                                address = address,
                                phone = phone,
                                email = email,
                                defaultTaxPercent = tax,
                                igtfPercent = igtf,
                                fiscalYear = fiscalYear
                            )
                            viewModel.updateCompany(updated)
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.align(Alignment.End).testTag("btn_save_company_settings")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar Cambios de Configuración", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
