package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rif: String,
    val businessName: String,
    val tradeName: String,
    val address: String,
    val phone: String,
    val email: String,
    val defaultTaxPercent: Double = 16.0,
    val igtfPercent: Double = 3.0,
    val isSpecialTaxpayer: Boolean = true, // Contribuyente Especial SENIAT
    val defaultIvaWithholdingPercent: Double = 75.0, // Retención IVA 75% o 100%
    val fiscalYear: String = "2026",
    val stationId: String = "ESTACION-01",
    val logoPath: String = ""
)

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rate: Double, // VES per 1 USD
    val effectiveDate: Long,
    val source: String = "BCV Oficial",
    val isCurrent: Boolean = true,
    val registeredBy: String = "SISTEMA"
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val fullName: String,
    val role: String, // ADMIN, CAJERO, FACTURADOR, AUDITOR, SUPERVISOR
    val pin: String,
    val isActive: Boolean = true
)

@Entity(tableName = "warehouses")
data class WarehouseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val location: String,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val description: String = ""
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val alternateCode: String = "", // Código Alterno / Fábrica a2
    val barcode: String = "",
    val name: String,
    val shortName: String = "", // Descripción corta para Ticket Fiscal
    val category: String, // Departamento
    val brand: String = "GENÉRICO", // Marca a2
    val model: String = "", // Modelo / Línea a2
    val location: String = "A-01", // Ubicación en Almacén
    val unit: String = "UND", // Unidad Principal (UND, KG, LTS, PAQ, METRO)
    val secondaryUnit: String = "BTO", // Unidad Secundaria / Empaque (Bulto, Caja)
    val conversionFactor: Double = 1.0, // Factor de conversión (ej: 1 Bulto = 24 UND)
    val itemType: String = "TERMINADO", // TERMINADO, MATERIA_PRIMA, SERVICIO, COMBO
    val costUsd: Double,
    val lastCostUsd: Double = 0.0, // Último Costo
    val averageCostUsd: Double = 0.0, // Costo Promedio Ponderado a2
    val margin1Percent: Double = 35.0, // Margen % P1
    val price1Usd: Double, // Precio 1 (Detal / Público)
    val margin2Percent: Double = 25.0,
    val price2Usd: Double = 0.0, // Precio 2 (Mayorista)
    val margin3Percent: Double = 18.0,
    val price3Usd: Double = 0.0, // Precio 3 (Distribuidor)
    val margin4Percent: Double = 12.0,
    val price4Usd: Double = 0.0, // Precio 4 (Mínimo / Empleado a2)
    val salesCommissionPercent: Double = 2.0, // Comisión a vendedor %
    val taxRatePercent: Double = 16.0,
    val isExempt: Boolean = false,
    val handlesLot: Boolean = false, // Maneja Lote / Vencimiento
    val handlesSerial: Boolean = false, // Maneja Seriales
    val isWeighable: Boolean = false, // Pesable en Balanza
    val minStock: Double = 5.0,
    val maxStock: Double = 1000.0,
    val currentStock: Double = 0.0,
    val stockWarehouse1: Double = 0.0, // Stock Almacén Principal
    val stockWarehouse2: Double = 0.0, // Stock Depósito Secundario
    val isActive: Boolean = true
)

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rif: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val zone: String = "CENTRO", // Zona de Venta a2
    val priceList: Int = 1, // 1, 2, 3, 4
    val creditLimitUsd: Double = 500.0,
    val creditDays: Int = 15,
    val currentBalanceUsd: Double = 0.0,
    val isSpecialTaxpayer: Boolean = false, // Contribuyente Especial
    val isActive: Boolean = true
)

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rif: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val contactPerson: String = "",
    val currentBalanceUsd: Double = 0.0,
    val defaultIvaWithholdingPercent: Double = 75.0,
    val defaultIslrPercent: Double = 2.0,
    val isActive: Boolean = true
)

@Entity(tableName = "sales_documents")
data class SalesDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docType: String, // FACTURA, NOTA_ENTREGA, COTIZACION, NOTA_CREDITO
    val docNumber: String,
    val controlNumber: String = "",
    val date: Long,
    val clientId: Long,
    val clientName: String,
    val clientRif: String,
    val clientAddress: String = "",
    val subtotalUsd: Double,
    val exemptAmountUsd: Double = 0.0,
    val baseImponibleUsd: Double,
    val taxAmountUsd: Double,
    val igtfAmountUsd: Double = 0.0,
    val totalUsd: Double,
    val bcvRate: Double,
    val totalVes: Double,
    val paidAmountUsd: Double,
    val paidAmountVes: Double = 0.0,
    val changeUsd: Double = 0.0, // Vuelto en USD
    val changeVes: Double = 0.0, // Vuelto en Bs.
    val balanceUsd: Double,
    val paymentStatus: String, // PAGADA, PENDIENTE, PARCIAL, ANULADA
    val paymentMethodsSummary: String,
    val cashierUser: String,
    val warehouseCode: String = "ALM-01",
    val notes: String = ""
)

@Entity(tableName = "sales_items")
data class SalesItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val productId: Long,
    val productCode: String,
    val productName: String,
    val unit: String = "UND",
    val quantity: Double,
    val unitPriceUsd: Double,
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double,
    val totalUsd: Double,
    val warehouseCode: String = "ALM-01"
)

@Entity(tableName = "purchase_documents")
data class PurchaseDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docType: String = "FACTURA_COMPRA",
    val docNumber: String, // Nro Interno ERP
    val providerInvoiceNumber: String, // Nro Factura del Proveedor
    val controlNumber: String = "", // Nro Control Fiscal Proveedor (00-000000)
    val date: Long,
    val expirationDate: Long = 0, // Fecha de Vencimiento
    val providerId: Long,
    val providerName: String,
    val providerRif: String,
    val warehouseCode: String = "ALM-01",
    val subtotalUsd: Double,
    val baseImponibleUsd: Double,
    val taxAmountUsd: Double,
    val ivaWithholdingPercent: Double = 0.0, // Retención IVA % (75% o 100%)
    val ivaWithholdingUsd: Double = 0.0, // Monto Retenido IVA $
    val islrPercent: Double = 0.0, // Retención ISLR %
    val islrUsd: Double = 0.0, // Monto Retenido ISLR $
    val totalUsd: Double,
    val totalNetToPayUsd: Double = 0.0, // Total Factura - Retenciones
    val bcvRate: Double,
    val totalVes: Double,
    val paidAmountUsd: Double,
    val balanceUsd: Double,
    val status: String, // REGISTRADA, ANULADA, PAGADA, PENDIENTE
    val cashierUser: String,
    val notes: String = ""
)

@Entity(tableName = "purchase_items")
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val productId: Long,
    val productCode: String,
    val productName: String,
    val unit: String = "UND",
    val quantity: Double,
    val unitCostUsd: Double,
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double,
    val totalUsd: Double
)

@Entity(tableName = "inventory_movements")
data class InventoryMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val movementType: String, // VENTA, COMPRA, AJUSTE_POS, AJUSTE_NEG, TRANSFERENCIA, DEVOLUCION
    val documentReference: String,
    val productId: Long,
    val productCode: String,
    val productName: String,
    val warehouseCode: String,
    val quantityChange: Double,
    val previousStock: Double,
    val newStock: Double,
    val costUsd: Double,
    val bcvRate: Double,
    val user: String,
    val reason: String = ""
)

@Entity(tableName = "cxc_movements")
data class CxcMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val clientId: Long,
    val clientName: String,
    val docReference: String,
    val movementType: String,
    val amountUsd: Double,
    val bcvRate: Double,
    val amountVes: Double,
    val paymentMethod: String,
    val referenceNumber: String = "",
    val balanceAfterUsd: Double,
    val user: String,
    val notes: String = ""
)

@Entity(tableName = "cxp_movements")
data class CxpMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val providerId: Long,
    val providerName: String,
    val docReference: String,
    val movementType: String,
    val amountUsd: Double,
    val bcvRate: Double,
    val amountVes: Double,
    val paymentMethod: String,
    val referenceNumber: String = "",
    val balanceAfterUsd: Double,
    val user: String,
    val notes: String = ""
)

@Entity(tableName = "cash_sessions")
data class CashSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val registerName: String = "CAJA 01 - MOSTRADOR",
    val openedAt: Long,
    val closedAt: Long? = null,
    val openingUsd: Double = 0.0,
    val openingVes: Double = 0.0,
    val closingUsd: Double = 0.0,
    val closingVes: Double = 0.0,
    val expectedUsd: Double = 0.0,
    val expectedVes: Double = 0.0,
    val differenceUsd: Double = 0.0,
    val differenceVes: Double = 0.0,
    val status: String = "ABIERTA",
    val openedByUser: String = "ADMIN",
    val closedByUser: String = ""
)

@Entity(tableName = "cash_movements")
data class CashMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long,
    val type: String,
    val category: String,
    val description: String,
    val amountUsd: Double,
    val amountVes: Double,
    val currency: String,
    val paymentMethod: String,
    val referenceNumber: String = "",
    val user: String
)

@Entity(tableName = "bank_accounts")
data class BankAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bankName: String,
    val accountNumber: String,
    val accountType: String,
    val currency: String,
    val balance: Double = 0.0,
    val holderName: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "bank_movements")
data class BankMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bankAccountId: Long,
    val timestamp: Long,
    val type: String,
    val description: String,
    val amount: Double,
    val rateBcv: Double = 1.0,
    val referenceNumber: String,
    val isReconciled: Boolean = false
)

@Entity(tableName = "accounting_entries")
data class AccountingEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryNumber: String,
    val date: Long,
    val concept: String,
    val documentReference: String,
    val totalDebitUsd: Double,
    val totalCreditUsd: Double,
    val isAutomated: Boolean = true
)

@Entity(tableName = "accounting_lines")
data class AccountingLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val accountCode: String,
    val accountName: String,
    val debitUsd: Double = 0.0,
    val creditUsd: Double = 0.0,
    val debitVes: Double = 0.0,
    val creditVes: Double = 0.0
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val user: String,
    val module: String,
    val action: String,
    val description: String,
    val documentReference: String = ""
)

@Entity(tableName = "correlatives")
data class CorrelativeEntity(
    @PrimaryKey val docType: String,
    val prefix: String,
    val nextNumber: Long
)
