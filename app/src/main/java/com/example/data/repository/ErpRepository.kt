package com.example.data.repository

import com.example.data.database.ErpDatabase
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class ErpRepository(private val db: ErpDatabase) {

    // Company & Config
    val company: Flow<CompanyEntity?> = db.companyDao().getCompany()
    suspend fun getCompanyDirect(): CompanyEntity? = db.companyDao().getCompanyDirect()
    suspend fun updateCompany(company: CompanyEntity) = db.companyDao().insertOrUpdate(company)

    // Exchange Rate BCV
    val latestRate: Flow<ExchangeRateEntity?> = db.exchangeRateDao().getLatestRate()
    val allRates: Flow<List<ExchangeRateEntity>> = db.exchangeRateDao().getAllRates()
    suspend fun getLatestRateDirect(): ExchangeRateEntity? = db.exchangeRateDao().getLatestRateDirect()
    suspend fun setExchangeRate(rateValue: Double, source: String = "BCV Oficial", user: String = "ADMIN") {
        val entity = ExchangeRateEntity(
            rate = rateValue,
            effectiveDate = System.currentTimeMillis(),
            source = source,
            isCurrent = true,
            registeredBy = user
        )
        db.exchangeRateDao().insertRate(entity)
        logAudit(user, "CONFIG", "CAMBIO_TASA", "Nueva tasa de cambio establecida: $rateValue Bs/USD", "")
    }

    // Users & Security
    val users: Flow<List<UserEntity>> = db.userDao().getAllUsers()
    suspend fun findUser(username: String): UserEntity? = db.userDao().findByUsername(username)
    suspend fun saveUser(user: UserEntity) = db.userDao().insertUser(user)
    suspend fun deleteUser(user: UserEntity) = db.userDao().deleteUser(user)

    // Warehouses & Categories
    val warehouses: Flow<List<WarehouseEntity>> = db.warehouseDao().getAllWarehouses()
    suspend fun saveWarehouse(warehouse: WarehouseEntity) = db.warehouseDao().insertWarehouse(warehouse)
    suspend fun deleteWarehouse(id: Long) = db.warehouseDao().deleteWarehouse(id)

    val categories: Flow<List<CategoryEntity>> = db.categoryDao().getAllCategories()
    suspend fun saveCategory(category: CategoryEntity) = db.categoryDao().insertCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = db.categoryDao().deleteCategory(category)

    // Products
    val products: Flow<List<ProductEntity>> = db.productDao().getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = db.productDao().getLowStockProducts()
    suspend fun getProductById(id: Long) = db.productDao().getProductById(id)
    suspend fun findProductByCode(code: String) = db.productDao().findByCodeOrBarcode(code)
    suspend fun saveProduct(product: ProductEntity, user: String = "ADMIN") {
        if (product.id == 0L) {
            val id = db.productDao().insertProduct(product)
            logAudit(user, "PRODUCTOS", "CREAR", "Producto creado: ${product.code} - ${product.name}", product.code)
        } else {
            db.productDao().updateProduct(product)
            logAudit(user, "PRODUCTOS", "MODIFICAR", "Producto actualizado: ${product.code} - ${product.name}", product.code)
        }
    }
    suspend fun deleteProduct(id: Long, user: String = "ADMIN") {
        db.productDao().softDeleteProduct(id)
        logAudit(user, "PRODUCTOS", "ELIMINAR", "Producto desactivado ID: $id", "")
    }

    // Clients
    val clients: Flow<List<ClientEntity>> = db.clientDao().getAllClients()
    suspend fun getClientById(id: Long) = db.clientDao().getClientById(id)
    suspend fun saveClient(client: ClientEntity, user: String = "ADMIN") {
        if (client.id == 0L) {
            db.clientDao().insertClient(client)
            logAudit(user, "CLIENTES", "CREAR", "Cliente creado: ${client.rif} - ${client.name}", client.rif)
        } else {
            db.clientDao().updateClient(client)
            logAudit(user, "CLIENTES", "MODIFICAR", "Cliente modificado: ${client.rif} - ${client.name}", client.rif)
        }
    }
    suspend fun deleteClient(id: Long, user: String = "ADMIN") {
        db.clientDao().softDeleteClient(id)
        logAudit(user, "CLIENTES", "ELIMINAR", "Cliente eliminado ID: $id", "")
    }

    // Providers
    val providers: Flow<List<ProviderEntity>> = db.providerDao().getAllProviders()
    suspend fun getProviderById(id: Long) = db.providerDao().getProviderById(id)
    suspend fun saveProvider(provider: ProviderEntity, user: String = "ADMIN") {
        if (provider.id == 0L) {
            db.providerDao().insertProvider(provider)
            logAudit(user, "PROVEEDORES", "CREAR", "Proveedor creado: ${provider.rif} - ${provider.name}", provider.rif)
        } else {
            db.providerDao().updateProvider(provider)
            logAudit(user, "PROVEEDORES", "MODIFICAR", "Proveedor modificado: ${provider.rif} - ${provider.name}", provider.rif)
        }
    }
    suspend fun deleteProvider(id: Long, user: String = "ADMIN") {
        db.providerDao().softDeleteProvider(id)
        logAudit(user, "PROVEEDORES", "ELIMINAR", "Proveedor eliminado ID: $id", "")
    }

    // Correlatives
    suspend fun getNextCorrelative(docType: String): String {
        val corr = db.correlativeDao().getCorrelative(docType)
        return if (corr != null) {
            val formatted = "${corr.prefix}${corr.nextNumber.toString().padStart(6, '0')}"
            db.correlativeDao().incrementCorrelative(docType)
            formatted
        } else {
            "${docType.take(3).uppercase()}-000001"
        }
    }

    // Sales / Invoicing Process (Full Venezuelan bimonetary & stock deduction logic)
    val allSales: Flow<List<SalesDocumentEntity>> = db.salesDao().getAllSales()
    val invoices: Flow<List<SalesDocumentEntity>> = db.salesDao().getInvoices()

    suspend fun getSaleDetails(documentId: Long): Pair<SalesDocumentEntity?, List<SalesItemEntity>> {
        val doc = db.salesDao().getSaleById(documentId)
        val items = db.salesDao().getItemsForDocumentDirect(documentId)
        return Pair(doc, items)
    }

    suspend fun registerSaleTransaction(
        docType: String,
        client: ClientEntity,
        items: List<CartItem>,
        bcvRate: Double,
        igtfPercent: Double,
        payments: List<PaymentEntry>,
        cashierUser: String,
        notes: String
    ): Long {
        val now = System.currentTimeMillis()
        val docNumber = getNextCorrelative(docType)
        val controlNumber = "00-" + (100000..999999).random()

        var subtotalUsd = 0.0
        var exemptAmountUsd = 0.0
        var baseImponibleUsd = 0.0
        var taxAmountUsd = 0.0

        for (it in items) {
            val lineTotal = it.quantity * it.unitPriceUsd
            subtotalUsd += lineTotal
            if (it.taxRatePercent <= 0.0) {
                exemptAmountUsd += lineTotal
            } else {
                baseImponibleUsd += lineTotal
                taxAmountUsd += lineTotal * (it.taxRatePercent / 100.0)
            }
        }

        // Calculate IGTF if any payment is in foreign currency
        val foreignCurrencyPaymentUsd = payments
            .filter { it.method == "EFECTIVO_USD" || it.method == "ZELLE" }
            .sumOf { it.amountUsd }
        val igtfAmountUsd = if (foreignCurrencyPaymentUsd > 0 && igtfPercent > 0) {
            foreignCurrencyPaymentUsd * (igtfPercent / 100.0)
        } else {
            0.0
        }

        val totalUsd = subtotalUsd + taxAmountUsd + igtfAmountUsd
        val totalVes = totalUsd * bcvRate
        val totalPaidUsd = payments.sumOf { it.amountUsd }
        val balanceUsd = (totalUsd - totalPaidUsd).coerceAtLeast(0.0)

        val paymentStatus = if (balanceUsd <= 0.01) {
            "PAGADA"
        } else if (totalPaidUsd > 0.0) {
            "PARCIAL"
        } else {
            "PENDIENTE"
        }

        val paymentSummary = payments.joinToString(" | ") {
            when (it.method) {
                "EFECTIVO_USD" -> "Efectivo USD $${String.format(Locale.US, "%.2f", it.amountUsd)}"
                "EFECTIVO_VES" -> "Efectivo Bs. ${String.format(Locale.US, "%.2f", it.amountVes)}"
                "PAGO_MOVIL" -> "Pago Móvil ${String.format(Locale.US, "%.2f", it.amountVes)} Bs. (Ref: ${it.reference})"
                "TRANSFERENCIA" -> "Transf. ${String.format(Locale.US, "%.2f", it.amountVes)} Bs. (Ref: ${it.reference})"
                "ZELLE" -> "Zelle $${String.format(Locale.US, "%.2f", it.amountUsd)} (Ref: ${it.reference})"
                "CREDITO" -> "A Crédito $${String.format(Locale.US, "%.2f", it.amountUsd)}"
                else -> "${it.method} $${String.format(Locale.US, "%.2f", it.amountUsd)}"
            }
        }

        val saleDoc = SalesDocumentEntity(
            docType = docType,
            docNumber = docNumber,
            controlNumber = controlNumber,
            date = now,
            clientId = client.id,
            clientName = client.name,
            clientRif = client.rif,
            clientAddress = client.address,
            subtotalUsd = subtotalUsd,
            exemptAmountUsd = exemptAmountUsd,
            baseImponibleUsd = baseImponibleUsd,
            taxAmountUsd = taxAmountUsd,
            igtfAmountUsd = igtfAmountUsd,
            totalUsd = totalUsd,
            bcvRate = bcvRate,
            totalVes = totalVes,
            paidAmountUsd = totalPaidUsd,
            balanceUsd = balanceUsd,
            paymentStatus = paymentStatus,
            paymentMethodsSummary = paymentSummary,
            cashierUser = cashierUser,
            warehouseCode = "ALM-01",
            notes = notes
        )

        val docId = db.salesDao().insertSale(saleDoc)

        val saleItems = items.map {
            SalesItemEntity(
                documentId = docId,
                productId = it.productId,
                productCode = it.productCode,
                productName = it.productName,
                quantity = it.quantity,
                unitPriceUsd = it.unitPriceUsd,
                taxRatePercent = it.taxRatePercent,
                totalUsd = it.quantity * it.unitPriceUsd,
                warehouseCode = "ALM-01"
            )
        }
        db.salesDao().insertSaleItems(saleItems)

        // Deduct inventory & record Kardex
        for (it in items) {
            val current = db.productDao().getProductById(it.productId)
            val prevStock = current?.currentStock ?: 0.0
            val newStock = prevStock - it.quantity
            db.productDao().updateStock(it.productId, -it.quantity)

            db.inventoryMovementDao().insertMovement(
                InventoryMovementEntity(
                    timestamp = now,
                    movementType = "VENTA",
                    documentReference = docNumber,
                    productId = it.productId,
                    productCode = it.productCode,
                    productName = it.productName,
                    warehouseCode = "ALM-01",
                    quantityChange = -it.quantity,
                    previousStock = prevStock,
                    newStock = newStock,
                    costUsd = current?.costUsd ?: 0.0,
                    bcvRate = bcvRate,
                    user = cashierUser,
                    reason = "Venta $docNumber a ${client.name}"
                )
            )
        }

        // If unpaid balance, add to Accounts Receivable (CxC)
        if (balanceUsd > 0.01) {
            db.clientDao().updateBalance(client.id, balanceUsd)
            val updatedClient = db.clientDao().getClientById(client.id)
            db.cxcDao().insertMovement(
                CxcMovementEntity(
                    timestamp = now,
                    clientId = client.id,
                    clientName = client.name,
                    docReference = docNumber,
                    movementType = "CARGO_FACTURA",
                    amountUsd = balanceUsd,
                    bcvRate = bcvRate,
                    amountVes = balanceUsd * bcvRate,
                    paymentMethod = "CREDITO",
                    referenceNumber = docNumber,
                    balanceAfterUsd = updatedClient?.currentBalanceUsd ?: balanceUsd,
                    user = cashierUser,
                    notes = "Cargo por venta a crédito $docNumber"
                )
            )
        }

        // Register in active Cash Session if cash/POS paid
        val activeSession = db.cashDao().getActiveSessionDirect()
        if (activeSession != null) {
            for (p in payments) {
                if (p.method != "CREDITO") {
                    db.cashDao().insertMovement(
                        CashMovementEntity(
                            sessionId = activeSession.id,
                            timestamp = now,
                            type = "VENTA_DIRECTA",
                            category = "VENTAS",
                            description = "Venta $docNumber - ${client.name}",
                            amountUsd = p.amountUsd,
                            amountVes = p.amountVes,
                            currency = if (p.method == "EFECTIVO_USD" || p.method == "ZELLE") "USD" else "VES",
                            paymentMethod = p.method,
                            referenceNumber = p.reference,
                            user = cashierUser
                        )
                    )
                }
            }
        }

        // Create Automated Accounting Entry
        val entryNumber = "ASI-" + (1000..9999).random()
        val entryId = db.accountingDao().insertEntry(
            AccountingEntryEntity(
                entryNumber = entryNumber,
                date = now,
                concept = "Asiento por Venta $docNumber Cliente ${client.name}",
                documentReference = docNumber,
                totalDebitUsd = totalUsd,
                totalCreditUsd = totalUsd
            )
        )
        val lines = listOf(
            AccountingLineEntity(
                entryId = entryId,
                accountCode = if (balanceUsd > 0.01) "1.1.02.01" else "1.1.01.01",
                accountName = if (balanceUsd > 0.01) "Cuentas por Cobrar Clientes" else "Caja y Bancos",
                debitUsd = totalUsd,
                creditUsd = 0.0,
                debitVes = totalVes,
                creditVes = 0.0
            ),
            AccountingLineEntity(
                entryId = entryId,
                accountCode = "4.1.01.01",
                accountName = "Ventas de Mercancías",
                debitUsd = 0.0,
                creditUsd = subtotalUsd,
                debitVes = 0.0,
                creditVes = subtotalUsd * bcvRate
            ),
            AccountingLineEntity(
                entryId = entryId,
                accountCode = "2.1.03.01",
                accountName = "Débito Fiscal IVA 16%",
                debitUsd = 0.0,
                creditUsd = taxAmountUsd + igtfAmountUsd,
                debitVes = 0.0,
                creditVes = (taxAmountUsd + igtfAmountUsd) * bcvRate
            )
        )
        db.accountingDao().insertLines(lines)

        logAudit(cashierUser, "VENTAS", "EMITIR", "Emisión $docType: $docNumber por $${String.format(Locale.US, "%.2f", totalUsd)} (${String.format(Locale.US, "%.2f", totalVes)} Bs.)", docNumber)

        return docId
    }

    // Purchases Process
    val allPurchases: Flow<List<PurchaseDocumentEntity>> = db.purchaseDao().getAllPurchases()

    suspend fun registerPurchaseTransaction(
        provider: ProviderEntity,
        invoiceNumber: String,
        controlNumber: String = "",
        warehouseCode: String = "ALM-01",
        ivaWithholdingPercent: Double = 0.0,
        islrPercent: Double = 0.0,
        items: List<PurchaseCartItem>,
        bcvRate: Double,
        isCredit: Boolean,
        user: String,
        notes: String
    ): Long {
        val now = System.currentTimeMillis()
        val docNumber = getNextCorrelative("COMPRA")

        var subtotalUsd = 0.0
        var taxAmountUsd = 0.0

        for (it in items) {
            val lineTotal = it.quantity * it.unitCostUsd
            subtotalUsd += lineTotal
            taxAmountUsd += lineTotal * (it.taxRatePercent / 100.0)
        }

        val totalUsd = subtotalUsd + taxAmountUsd
        val ivaWithholdingUsd = if (ivaWithholdingPercent > 0) taxAmountUsd * (ivaWithholdingPercent / 100.0) else 0.0
        val islrUsd = if (islrPercent > 0) subtotalUsd * (islrPercent / 100.0) else 0.0
        val totalNetToPayUsd = (totalUsd - ivaWithholdingUsd - islrUsd).coerceAtLeast(0.0)

        val totalVes = totalUsd * bcvRate
        val balanceUsd = if (isCredit) totalNetToPayUsd else 0.0
        val paidAmountUsd = if (isCredit) 0.0 else totalNetToPayUsd

        val purchaseDoc = PurchaseDocumentEntity(
            docType = "FACTURA_COMPRA",
            docNumber = docNumber,
            providerInvoiceNumber = invoiceNumber,
            controlNumber = controlNumber.ifBlank { "00-${(100000..999999).random()}" },
            date = now,
            expirationDate = now + (30L * 86400000L),
            providerId = provider.id,
            providerName = provider.name,
            providerRif = provider.rif,
            warehouseCode = warehouseCode,
            subtotalUsd = subtotalUsd,
            baseImponibleUsd = subtotalUsd,
            taxAmountUsd = taxAmountUsd,
            ivaWithholdingPercent = ivaWithholdingPercent,
            ivaWithholdingUsd = ivaWithholdingUsd,
            islrPercent = islrPercent,
            islrUsd = islrUsd,
            totalUsd = totalUsd,
            totalNetToPayUsd = totalNetToPayUsd,
            bcvRate = bcvRate,
            totalVes = totalVes,
            paidAmountUsd = paidAmountUsd,
            balanceUsd = balanceUsd,
            status = if (isCredit) "PENDIENTE" else "PAGADA",
            cashierUser = user,
            notes = notes
        )

        val docId = db.purchaseDao().insertPurchase(purchaseDoc)

        val purchaseItems = items.map {
            PurchaseItemEntity(
                documentId = docId,
                productId = it.productId,
                productCode = it.productCode,
                productName = it.productName,
                unit = it.unit,
                quantity = it.quantity,
                unitCostUsd = it.unitCostUsd,
                discountPercent = it.discountPercent,
                taxRatePercent = it.taxRatePercent,
                totalUsd = it.quantity * it.unitCostUsd
            )
        }
        db.purchaseDao().insertPurchaseItems(purchaseItems)

        // Update product stock and costs
        for (it in items) {
            val current = db.productDao().getProductById(it.productId)
            val prevStock = current?.currentStock ?: 0.0
            val newStock = prevStock + it.quantity
            db.productDao().updateStock(it.productId, it.quantity)

            // Update cost & average cost
            if (current != null) {
                val prevCost = current.costUsd
                val avgCost = if (newStock > 0) ((prevStock * prevCost) + (it.quantity * it.unitCostUsd)) / newStock else it.unitCostUsd
                db.productDao().updateProduct(
                    current.copy(
                        costUsd = it.unitCostUsd,
                        lastCostUsd = prevCost,
                        averageCostUsd = avgCost,
                        currentStock = newStock
                    )
                )
            }

            db.inventoryMovementDao().insertMovement(
                InventoryMovementEntity(
                    timestamp = now,
                    movementType = "COMPRA",
                    documentReference = docNumber,
                    productId = it.productId,
                    productCode = it.productCode,
                    productName = it.productName,
                    warehouseCode = warehouseCode,
                    quantityChange = it.quantity,
                    previousStock = prevStock,
                    newStock = newStock,
                    costUsd = it.unitCostUsd,
                    bcvRate = bcvRate,
                    user = user,
                    reason = "Recepción Fact. Prov. $invoiceNumber - ${provider.name}"
                )
            )
        }

        // If on credit, register in CxP with Net to pay
        if (isCredit) {
            db.providerDao().updateBalance(provider.id, totalNetToPayUsd)
            val updatedProvider = db.providerDao().getProviderById(provider.id)
            db.cxpDao().insertMovement(
                CxpMovementEntity(
                    timestamp = now,
                    providerId = provider.id,
                    providerName = provider.name,
                    docReference = docNumber,
                    movementType = "CARGO_COMPRA",
                    amountUsd = totalNetToPayUsd,
                    bcvRate = bcvRate,
                    amountVes = totalNetToPayUsd * bcvRate,
                    paymentMethod = "CREDITO",
                    referenceNumber = invoiceNumber,
                    balanceAfterUsd = updatedProvider?.currentBalanceUsd ?: totalNetToPayUsd,
                    user = user,
                    notes = "Cargo Compra Fact. $invoiceNumber (Neto con Retenciones)"
                )
            )
        }

        logAudit(user, "COMPRAS", "REGISTRAR", "Compra $docNumber (Fact: $invoiceNumber) Total: $${String.format(Locale.US, "%.2f", totalUsd)} Neto: $${String.format(Locale.US, "%.2f", totalNetToPayUsd)}", docNumber)

        return docId
    }

    // Inventory Movements & Adjustments
    val inventoryMovements: Flow<List<InventoryMovementEntity>> = db.inventoryMovementDao().getAllMovements()

    suspend fun registerInventoryAdjustment(
        productId: Long,
        type: String, // AJUSTE_POS, AJUSTE_NEG
        quantity: Double,
        warehouseCode: String,
        reason: String,
        user: String,
        bcvRate: Double
    ) {
        val now = System.currentTimeMillis()
        val docNumber = getNextCorrelative("AJUSTE")
        val product = db.productDao().getProductById(productId) ?: return
        val change = if (type == "AJUSTE_POS") quantity else -quantity
        val prevStock = product.currentStock
        val newStock = (prevStock + change).coerceAtLeast(0.0)

        db.productDao().updateStock(productId, change)
        db.inventoryMovementDao().insertMovement(
            InventoryMovementEntity(
                timestamp = now,
                movementType = type,
                documentReference = docNumber,
                productId = productId,
                productCode = product.code,
                productName = product.name,
                warehouseCode = warehouseCode,
                quantityChange = change,
                previousStock = prevStock,
                newStock = newStock,
                costUsd = product.costUsd,
                bcvRate = bcvRate,
                user = user,
                reason = reason
            )
        )
        logAudit(user, "INVENTARIO", "AJUSTE", "Ajuste $type de ${product.name}: $change $warehouseCode ($reason)", docNumber)
    }

    // Accounts Receivable (CxC) & Payable (CxP)
    val cxcMovements: Flow<List<CxcMovementEntity>> = db.cxcDao().getAllCxcMovements()
    val cxpMovements: Flow<List<CxpMovementEntity>> = db.cxpDao().getAllCxpMovements()

    suspend fun registerClientPayment(
        client: ClientEntity,
        amountUsd: Double,
        bcvRate: Double,
        paymentMethod: String,
        referenceNumber: String,
        notes: String,
        user: String
    ) {
        val now = System.currentTimeMillis()
        val receiptNumber = getNextCorrelative("COBRO")
        val amountVes = amountUsd * bcvRate

        db.clientDao().updateBalance(client.id, -amountUsd)
        val updatedClient = db.clientDao().getClientById(client.id)

        db.cxcDao().insertMovement(
            CxcMovementEntity(
                timestamp = now,
                clientId = client.id,
                clientName = client.name,
                docReference = receiptNumber,
                movementType = "ABONO",
                amountUsd = amountUsd,
                bcvRate = bcvRate,
                amountVes = amountVes,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                balanceAfterUsd = updatedClient?.currentBalanceUsd ?: 0.0,
                user = user,
                notes = notes
            )
        )

        // Record in active Cash Session
        val activeSession = db.cashDao().getActiveSessionDirect()
        if (activeSession != null) {
            db.cashDao().insertMovement(
                CashMovementEntity(
                    sessionId = activeSession.id,
                    timestamp = now,
                    type = "COBRO_CXC",
                    category = "COBRANZA",
                    description = "Cobro $receiptNumber Cliente ${client.name}",
                    amountUsd = amountUsd,
                    amountVes = amountVes,
                    currency = if (paymentMethod.contains("USD") || paymentMethod.contains("ZELLE")) "USD" else "VES",
                    paymentMethod = paymentMethod,
                    referenceNumber = referenceNumber,
                    user = user
                )
            )
        }

        logAudit(user, "CXC", "ABONO", "Abono cliente ${client.name} por $${String.format(Locale.US, "%.2f", amountUsd)} ($paymentMethod)", receiptNumber)
    }

    suspend fun registerProviderPayment(
        provider: ProviderEntity,
        amountUsd: Double,
        bcvRate: Double,
        paymentMethod: String,
        referenceNumber: String,
        notes: String,
        user: String
    ) {
        val now = System.currentTimeMillis()
        val orderNumber = getNextCorrelative("PAGO")
        val amountVes = amountUsd * bcvRate

        db.providerDao().updateBalance(provider.id, -amountUsd)
        val updatedProvider = db.providerDao().getProviderById(provider.id)

        db.cxpDao().insertMovement(
            CxpMovementEntity(
                timestamp = now,
                providerId = provider.id,
                providerName = provider.name,
                docReference = orderNumber,
                movementType = "PAGO_ABONO",
                amountUsd = amountUsd,
                bcvRate = bcvRate,
                amountVes = amountVes,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                balanceAfterUsd = updatedProvider?.currentBalanceUsd ?: 0.0,
                user = user,
                notes = notes
            )
        )

        val activeSession = db.cashDao().getActiveSessionDirect()
        if (activeSession != null) {
            db.cashDao().insertMovement(
                CashMovementEntity(
                    sessionId = activeSession.id,
                    timestamp = now,
                    type = "PAGO_CXP",
                    category = "PROVEEDORES",
                    description = "Pago $orderNumber Proveedor ${provider.name}",
                    amountUsd = amountUsd,
                    amountVes = amountVes,
                    currency = if (paymentMethod.contains("USD") || paymentMethod.contains("ZELLE")) "USD" else "VES",
                    paymentMethod = paymentMethod,
                    referenceNumber = referenceNumber,
                    user = user
                )
            )
        }

        logAudit(user, "CXP", "PAGO", "Pago proveedor ${provider.name} por $${String.format(Locale.US, "%.2f", amountUsd)} ($paymentMethod)", orderNumber)
    }

    // Cash Management & Sessions
    val activeCashSession: Flow<CashSessionEntity?> = db.cashDao().getActiveSession()
    val recentCashMovements: Flow<List<CashMovementEntity>> = db.cashDao().getRecentCashMovements()
    val allCashSessions: Flow<List<CashSessionEntity>> = db.cashDao().getAllSessions()

    suspend fun openCashSession(registerName: String, openingUsd: Double, openingVes: Double, user: String) {
        val now = System.currentTimeMillis()
        val sessionId = db.cashDao().insertSession(
            CashSessionEntity(
                registerName = registerName,
                openedAt = now,
                openingUsd = openingUsd,
                openingVes = openingVes,
                expectedUsd = openingUsd,
                expectedVes = openingVes,
                status = "ABIERTA",
                openedByUser = user
            )
        )
        db.cashDao().insertMovement(
            CashMovementEntity(
                sessionId = sessionId,
                timestamp = now,
                type = "INGRESO",
                category = "FONDO_INICIAL",
                description = "Apertura de $registerName",
                amountUsd = openingUsd,
                amountVes = openingVes,
                currency = "MIXTO",
                paymentMethod = "EFECTIVO",
                referenceNumber = "APER-$sessionId",
                user = user
            )
        )
        logAudit(user, "CAJA", "APERTURA", "Apertura de caja: $registerName (USD: $$openingUsd, VES: $openingVes Bs.)", "APER-$sessionId")
    }

    suspend fun closeCashSession(session: CashSessionEntity, closingUsd: Double, closingVes: Double, user: String) {
        val now = System.currentTimeMillis()
        val diffUsd = closingUsd - session.expectedUsd
        val diffVes = closingVes - session.expectedVes

        val updated = session.copy(
            closedAt = now,
            closingUsd = closingUsd,
            closingVes = closingVes,
            differenceUsd = diffUsd,
            differenceVes = diffVes,
            status = "CERRADA",
            closedByUser = user
        )
        db.cashDao().updateSession(updated)
        logAudit(user, "CAJA", "CIERRE", "Cierre Z de ${session.registerName}. Diferencia USD: $$diffUsd, VES: $diffVes Bs.", "CIERRE-${session.id}")
    }

    suspend fun addCashMovement(
        type: String, // INGRESO, EGRESO, GASTO
        category: String,
        description: String,
        amountUsd: Double,
        amountVes: Double,
        paymentMethod: String,
        user: String
    ) {
        val session = db.cashDao().getActiveSessionDirect() ?: return
        val now = System.currentTimeMillis()
        db.cashDao().insertMovement(
            CashMovementEntity(
                sessionId = session.id,
                timestamp = now,
                type = type,
                category = category,
                description = description,
                amountUsd = amountUsd,
                amountVes = amountVes,
                currency = if (paymentMethod.contains("USD") || paymentMethod.contains("ZELLE")) "USD" else "VES",
                paymentMethod = paymentMethod,
                referenceNumber = "MOV-${System.currentTimeMillis() % 100000}",
                user = user
            )
        )
        logAudit(user, "CAJA", type, "$type ($category): $description ($amountUsd USD / $amountVes Bs.)", "")
    }

    // Bank Accounts
    val bankAccounts: Flow<List<BankAccountEntity>> = db.bankDao().getAllAccounts()
    suspend fun saveBankAccount(account: BankAccountEntity) {
        if (account.id == 0L) db.bankDao().insertAccount(account)
        else db.bankDao().updateAccount(account)
    }

    // Accounting Entries & Audit
    val accountingEntries: Flow<List<AccountingEntryEntity>> = db.accountingDao().getAllEntries()
    suspend fun getAccountingLines(entryId: Long) = db.accountingDao().getLinesForEntry(entryId)

    val auditLogs: Flow<List<AuditLogEntity>> = db.auditDao().getRecentLogs()

    private suspend fun logAudit(user: String, module: String, action: String, description: String, ref: String) {
        db.auditDao().insertLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                user = user,
                module = module,
                action = action,
                description = description,
                documentReference = ref
            )
        )
    }
}

// Helper Data Classes for POS & Purchase
data class CartItem(
    val productId: Long,
    val productCode: String,
    val productName: String,
    val unit: String = "UND",
    val quantity: Double,
    val unitPriceUsd: Double,
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double,
    val isExempt: Boolean = false
)

data class PurchaseCartItem(
    val productId: Long,
    val productCode: String,
    val productName: String,
    val unit: String = "UND",
    val quantity: Double,
    val unitCostUsd: Double,
    val discountPercent: Double = 0.0,
    val taxRatePercent: Double
)

data class PaymentEntry(
    val method: String, // EFECTIVO_USD, EFECTIVO_VES, PAGO_MOVIL, TRANSFERENCIA, ZELLE, CREDITO
    val amountUsd: Double,
    val amountVes: Double,
    val reference: String = ""
)
