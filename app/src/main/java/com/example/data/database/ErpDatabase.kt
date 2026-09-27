package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CompanyEntity::class,
        ExchangeRateEntity::class,
        UserEntity::class,
        WarehouseEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        ClientEntity::class,
        ProviderEntity::class,
        SalesDocumentEntity::class,
        SalesItemEntity::class,
        PurchaseDocumentEntity::class,
        PurchaseItemEntity::class,
        InventoryMovementEntity::class,
        CxcMovementEntity::class,
        CxpMovementEntity::class,
        CashSessionEntity::class,
        CashMovementEntity::class,
        BankAccountEntity::class,
        BankMovementEntity::class,
        AccountingEntryEntity::class,
        AccountingLineEntity::class,
        AuditLogEntity::class,
        CorrelativeEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ErpDatabase : RoomDatabase() {

    abstract fun companyDao(): CompanyDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun userDao(): UserDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun providerDao(): ProviderDao
    abstract fun salesDao(): SalesDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun inventoryMovementDao(): InventoryMovementDao
    abstract fun cxcDao(): CxcDao
    abstract fun cxpDao(): CxpDao
    abstract fun cashDao(): CashDao
    abstract fun bankDao(): BankDao
    abstract fun accountingDao(): AccountingDao
    abstract fun auditDao(): AuditDao
    abstract fun correlativeDao(): CorrelativeDao

    companion object {
        @Volatile
        private var INSTANCE: ErpDatabase? = null

        fun getInstance(context: Context): ErpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ErpDatabase::class.java,
                    "a2_erp_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        seedInitialData(database)
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        seedInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        try {
                            if (database.companyDao().getCompanyDirect() == null || database.productDao().getProductCountDirect() == 0) {
                                seedInitialData(database)
                            }
                        } catch (e: Exception) {
                            // Ignore if during early initialization
                        }
                    }
                }
            }
        }

        suspend fun seedInitialData(database: ErpDatabase) {
            val now = System.currentTimeMillis()

            // 1. Company
            database.companyDao().insertOrUpdate(
                CompanyEntity(
                    rif = "J-40123456-7",
                    businessName = "DISTRIBUIDORA & COMERCIALIZADORA LOS ANDES, C.A.",
                    tradeName = "LOS ANDES C.A. - SEDE PRINCIPAL",
                    address = "Av. Francisco de Miranda, Edif. Centro Empresarial, Piso 4, Ofic. 4-A, Chacao, Caracas",
                    phone = "0212-5551234 / 0414-9876543",
                    email = "administracion@losandeserp.com.ve",
                    defaultTaxPercent = 16.0,
                    igtfPercent = 3.0,
                    fiscalYear = "2026"
                )
            )

            // 2. Exchange Rate BCV
            database.exchangeRateDao().insertRate(
                ExchangeRateEntity(
                    rate = 62.50,
                    effectiveDate = now,
                    source = "BCV Oficial",
                    isCurrent = true,
                    registeredBy = "SISTEMA"
                )
            )

            // 3. Users
            database.userDao().insertUser(
                UserEntity(
                    username = "ADMIN",
                    fullName = "Administrador Principal",
                    role = "ADMIN",
                    pin = "1234",
                    isActive = true
                )
            )
            database.userDao().insertUser(
                UserEntity(
                    username = "CAJERO1",
                    fullName = "Cajero Turno Mañana",
                    role = "CAJERO",
                    pin = "0000",
                    isActive = true
                )
            )
            database.userDao().insertUser(
                UserEntity(
                    username = "SUPERVISOR",
                    fullName = "Gerente de Operaciones",
                    role = "SUPERVISOR",
                    pin = "7777",
                    isActive = true
                )
            )

            // 4. Warehouses
            database.warehouseDao().insertWarehouse(
                WarehouseEntity(
                    code = "ALM-01",
                    name = "Almacén Principal Caracas",
                    location = "Chacao, Caracas",
                    isDefault = true,
                    isActive = true
                )
            )
            database.warehouseDao().insertWarehouse(
                WarehouseEntity(
                    code = "DEP-02",
                    name = "Depósito Secundario Maracay",
                    location = "Zona Industrial San Vicente, Maracay",
                    isDefault = false,
                    isActive = true
                )
            )

            // 5. Categories
            database.categoryDao().insertCategory(CategoryEntity(code = "CAT-ALIM", name = "Alimentos y Víveres", description = "Productos de primera necesidad"))
            database.categoryDao().insertCategory(CategoryEntity(code = "CAT-BEB", name = "Bebidas y Refrescos", description = "Jugos, refrescos y agua mineral"))
            database.categoryDao().insertCategory(CategoryEntity(code = "CAT-LIMP", name = "Limpieza y Hogar", description = "Detergentes, jabones y desinfectantes"))
            database.categoryDao().insertCategory(CategoryEntity(code = "CAT-FERR", name = "Ferretería e Insumos", description = "Herramientas y consumibles"))
            database.categoryDao().insertCategory(CategoryEntity(code = "CAT-SERV", name = "Servicios y Asistencia", description = "Servicios administrativos y técnicos"))

            // 6. Products with full a2 Softway fields
            val p1 = ProductEntity(
                code = "ART-001",
                alternateCode = "AL-PAN-01",
                barcode = "7591001000101",
                name = "Harina PAN Blanca 1 Kg",
                shortName = "HARINA PAN BLANCA 1KG",
                category = "Alimentos y Víveres",
                brand = "POLAR",
                model = "TRADICIONAL",
                location = "A-01",
                unit = "UND",
                secondaryUnit = "BTO",
                conversionFactor = 20.0,
                costUsd = 0.95,
                lastCostUsd = 0.92,
                averageCostUsd = 0.94,
                margin1Percent = 42.0,
                price1Usd = 1.35,
                margin2Percent = 31.0,
                price2Usd = 1.25,
                margin3Percent = 21.0,
                price3Usd = 1.15,
                margin4Percent = 15.0,
                price4Usd = 1.10,
                taxRatePercent = 0.0,
                isExempt = true,
                minStock = 20.0,
                maxStock = 500.0,
                currentStock = 145.0,
                stockWarehouse1 = 120.0,
                stockWarehouse2 = 25.0,
                isActive = true
            )
            val p2 = ProductEntity(
                code = "ART-002",
                alternateCode = "AL-PRI-02",
                barcode = "7591001000202",
                name = "Arroz Primor Tradicional 1 Kg",
                shortName = "ARROZ PRIMOR 1KG",
                category = "Alimentos y Víveres",
                brand = "PRIMOR",
                model = "GRANO LARGO",
                location = "A-02",
                unit = "UND",
                secondaryUnit = "BTO",
                conversionFactor = 24.0,
                costUsd = 1.10,
                lastCostUsd = 1.05,
                averageCostUsd = 1.08,
                margin1Percent = 40.0,
                price1Usd = 1.55,
                margin2Percent = 31.0,
                price2Usd = 1.45,
                margin3Percent = 22.0,
                price3Usd = 1.35,
                margin4Percent = 18.0,
                price4Usd = 1.30,
                taxRatePercent = 0.0,
                isExempt = true,
                minStock = 15.0,
                maxStock = 300.0,
                currentStock = 88.0,
                stockWarehouse1 = 70.0,
                stockWarehouse2 = 18.0,
                isActive = true
            )
            val p3 = ProductEntity(
                code = "ART-003",
                alternateCode = "AL-MAZ-03",
                barcode = "7591001000303",
                name = "Aceite de Maíz Mazeite 1 Litro",
                shortName = "ACEITE MAZEITE 1L",
                category = "Alimentos y Víveres",
                brand = "MAZEITE",
                model = "COMESTIBLE",
                location = "A-03",
                unit = "UND",
                secondaryUnit = "CJA",
                conversionFactor = 12.0,
                costUsd = 2.80,
                lastCostUsd = 2.75,
                averageCostUsd = 2.78,
                margin1Percent = 34.0,
                price1Usd = 3.75,
                margin2Percent = 25.0,
                price2Usd = 3.50,
                margin3Percent = 18.0,
                price3Usd = 3.30,
                margin4Percent = 14.0,
                price4Usd = 3.20,
                taxRatePercent = 16.0,
                isExempt = false,
                minStock = 10.0,
                maxStock = 200.0,
                currentStock = 54.0,
                stockWarehouse1 = 44.0,
                stockWarehouse2 = 10.0,
                isActive = true
            )
            val p4 = ProductEntity(
                code = "ART-004",
                alternateCode = "AL-FAM-04",
                barcode = "7591001000404",
                name = "Café Fama de América 500g",
                shortName = "CAFE FAMA AMERICA 500G",
                category = "Alimentos y Víveres",
                brand = "FAMA DE AMÉRICA",
                model = "MOLIDO GOURMET",
                location = "A-04",
                unit = "UND",
                secondaryUnit = "BTO",
                conversionFactor = 24.0,
                costUsd = 3.20,
                lastCostUsd = 3.10,
                averageCostUsd = 3.15,
                margin1Percent = 40.0,
                price1Usd = 4.50,
                margin2Percent = 31.0,
                price2Usd = 4.20,
                margin3Percent = 22.0,
                price3Usd = 3.90,
                margin4Percent = 15.0,
                price4Usd = 3.70,
                taxRatePercent = 16.0,
                isExempt = false,
                minStock = 12.0,
                maxStock = 150.0,
                currentStock = 32.0,
                stockWarehouse1 = 25.0,
                stockWarehouse2 = 7.0,
                isActive = true
            )
            val p5 = ProductEntity(
                code = "ART-005",
                alternateCode = "BEB-COCA-05",
                barcode = "7591001000505",
                name = "Refresco Coca-Cola 2 Litros",
                shortName = "COCA COLA 2L",
                category = "Bebidas y Refrescos",
                brand = "COCA-COLA",
                model = "BOTELLA RETORNABLE",
                location = "B-01",
                unit = "UND",
                secondaryUnit = "CJA",
                conversionFactor = 8.0,
                costUsd = 1.80,
                lastCostUsd = 1.70,
                averageCostUsd = 1.75,
                margin1Percent = 38.0,
                price1Usd = 2.50,
                margin2Percent = 28.0,
                price2Usd = 2.30,
                margin3Percent = 17.0,
                price3Usd = 2.10,
                margin4Percent = 12.0,
                price4Usd = 2.00,
                taxRatePercent = 16.0,
                isExempt = false,
                minStock = 24.0,
                maxStock = 400.0,
                currentStock = 6.0, // Low stock alert
                stockWarehouse1 = 4.0,
                stockWarehouse2 = 2.0,
                isActive = true
            )
            val p6 = ProductEntity(
                code = "ART-006",
                alternateCode = "LIMP-LLAV-06",
                barcode = "7591001000606",
                name = "Detergente en Polvo Las Llaves 1 Kg",
                shortName = "DETERGENTE LLAVES 1KG",
                category = "Limpieza y Hogar",
                brand = "LAS LLAVES",
                model = "MULTIUSO POLVO",
                location = "C-02",
                unit = "UND",
                secondaryUnit = "CJA",
                conversionFactor = 12.0,
                costUsd = 2.10,
                lastCostUsd = 2.00,
                averageCostUsd = 2.05,
                margin1Percent = 40.0,
                price1Usd = 2.95,
                margin2Percent = 31.0,
                price2Usd = 2.75,
                margin3Percent = 19.0,
                price3Usd = 2.50,
                margin4Percent = 14.0,
                price4Usd = 2.40,
                taxRatePercent = 16.0,
                isExempt = false,
                minStock = 10.0,
                maxStock = 200.0,
                currentStock = 42.0,
                stockWarehouse1 = 30.0,
                stockWarehouse2 = 12.0,
                isActive = true
            )
            val p7 = ProductEntity(
                code = "ART-007",
                alternateCode = "FERR-BOMB-07",
                barcode = "7591001000707",
                name = "Bombillo LED 12W Luz Blanca E27",
                shortName = "BOMBILLO LED 12W",
                category = "Ferretería e Insumos",
                brand = "SYLVANIA",
                model = "E27 LUZ BLANCA",
                location = "D-01",
                unit = "UND",
                secondaryUnit = "CJA",
                conversionFactor = 50.0,
                costUsd = 0.90,
                lastCostUsd = 0.85,
                averageCostUsd = 0.88,
                margin1Percent = 77.0,
                price1Usd = 1.60,
                margin2Percent = 55.0,
                price2Usd = 1.40,
                margin3Percent = 33.0,
                price3Usd = 1.20,
                margin4Percent = 22.0,
                price4Usd = 1.10,
                taxRatePercent = 16.0,
                isExempt = false,
                minStock = 15.0,
                maxStock = 300.0,
                currentStock = 75.0,
                stockWarehouse1 = 50.0,
                stockWarehouse2 = 25.0,
                isActive = true
            )

            database.productDao().insertProduct(p1)
            database.productDao().insertProduct(p2)
            database.productDao().insertProduct(p3)
            database.productDao().insertProduct(p4)
            database.productDao().insertProduct(p5)
            database.productDao().insertProduct(p6)
            database.productDao().insertProduct(p7)

            // 7. Clients
            database.clientDao().insertClient(
                ClientEntity(
                    rif = "V-14238492-1",
                    name = "Carlos Eduardo Mendoza",
                    phone = "0414-1122334",
                    email = "carlos.mendoza@gmail.com",
                    address = "Av. Principal Los Ruices, Edif. Alba, Apt 3-B, Caracas",
                    priceList = 1,
                    creditLimitUsd = 300.0,
                    creditDays = 15,
                    currentBalanceUsd = 45.00
                )
            )
            database.clientDao().insertClient(
                ClientEntity(
                    rif = "J-30491823-4",
                    name = "Inversiones y Suministros El Ávila C.A.",
                    phone = "0212-9988776",
                    email = "compras@elavila.com.ve",
                    address = "Urb. Boleíta Norte, Calle Tiuna, Galpón 12, Caracas",
                    priceList = 2,
                    creditLimitUsd = 1500.0,
                    creditDays = 30,
                    currentBalanceUsd = 280.00
                )
            )
            database.clientDao().insertClient(
                ClientEntity(
                    rif = "V-18765432-0",
                    name = "María Gabriela Rivas",
                    phone = "0424-5544332",
                    email = "mgrivas@hotmail.com",
                    address = "Residencias Parque Cristal, Los Palos Grandes",
                    priceList = 1,
                    creditLimitUsd = 200.0,
                    creditDays = 7,
                    currentBalanceUsd = 0.00
                )
            )
            database.clientDao().insertClient(
                ClientEntity(
                    rif = "J-50123984-9",
                    name = "Supermercado Gran Plaza C.A.",
                    phone = "0241-8765432",
                    email = "administracion@granplaza.ve",
                    address = "Av. Bolívar Norte, Valencia, Edo. Carabobo",
                    priceList = 3,
                    creditLimitUsd = 5000.0,
                    creditDays = 45,
                    currentBalanceUsd = 620.50
                )
            )

            // 8. Providers
            database.providerDao().insertProvider(
                ProviderEntity(
                    rif = "J-00123984-2",
                    name = "Empresas Polar Alimentos C.A.",
                    phone = "0212-2023111",
                    email = "ventas.corporativas@empresas-polar.com",
                    address = "4ta Transversal Los Cortijos de Lourdes, Caracas",
                    contactPerson = "Lcda. Andrea Colmenares",
                    currentBalanceUsd = 450.00
                )
            )
            database.providerDao().insertProvider(
                ProviderEntity(
                    rif = "J-30918274-1",
                    name = "Nestlé Venezuela S.A.",
                    phone = "0212-9016111",
                    email = "pedidos@ve.nestle.com",
                    address = "Edif. Nestlé, Av. Eugenio Mendoza, La Castellana",
                    contactPerson = "Ing. Manuel Briceño",
                    currentBalanceUsd = 0.00
                )
            )
            database.providerDao().insertProvider(
                ProviderEntity(
                    rif = "J-40982314-5",
                    name = "Distribuidora Industrial del Centro C.A.",
                    phone = "0243-2345678",
                    email = "ventas@discentro.com.ve",
                    address = "Zona Industrial II, Maracay, Edo. Aragua",
                    contactPerson = "Roberto Gómez",
                    currentBalanceUsd = 320.00
                )
            )

            // 9. Bank Accounts
            database.bankDao().insertAccount(
                BankAccountEntity(
                    bankName = "Banesco Banco Universal",
                    accountNumber = "0134-0012-34-1234567890",
                    accountType = "CORRIENTE",
                    currency = "VES",
                    balance = 45800.00,
                    holderName = "Distribuidora Los Andes C.A."
                )
            )
            database.bankDao().insertAccount(
                BankAccountEntity(
                    bankName = "Banco Mercantil (Pago Móvil)",
                    accountNumber = "0105-0045-89-0987654321",
                    accountType = "CORRIENTE",
                    currency = "VES",
                    balance = 28450.00,
                    holderName = "Distribuidora Los Andes C.A."
                )
            )
            database.bankDao().insertAccount(
                BankAccountEntity(
                    bankName = "Banco de Venezuela",
                    accountNumber = "0102-0111-22-1122334455",
                    accountType = "CORRIENTE",
                    currency = "VES",
                    balance = 19200.00,
                    holderName = "Distribuidora Los Andes C.A."
                )
            )
            database.bankDao().insertAccount(
                BankAccountEntity(
                    bankName = "Zelle Business (USD)",
                    accountNumber = "pagos@losandeserp.com.ve",
                    accountType = "DIGITAL",
                    currency = "USD",
                    balance = 1250.00,
                    holderName = "Los Andes LLC"
                )
            )

            // 10. Cash Session (Active)
            val sessionId = database.cashDao().insertSession(
                CashSessionEntity(
                    registerName = "CAJA 01 - MOSTRADOR",
                    openedAt = now - 18000000,
                    openingUsd = 50.00,
                    openingVes = 1500.00,
                    expectedUsd = 185.00,
                    expectedVes = 9550.00,
                    status = "ABIERTA",
                    openedByUser = "ADMIN"
                )
            )

            database.cashDao().insertMovement(
                CashMovementEntity(
                    sessionId = sessionId,
                    timestamp = now - 17000000,
                    type = "INGRESO",
                    category = "FONDO_INICIAL",
                    description = "Fondo de apertura de caja turno mañana",
                    amountUsd = 50.00,
                    amountVes = 1500.00,
                    currency = "MIXTO",
                    paymentMethod = "EFECTIVO",
                    referenceNumber = "APER-01",
                    user = "ADMIN"
                )
            )

            // 11. Initial Correlatives
            database.correlativeDao().insertCorrelative(CorrelativeEntity("FACTURA", "FAC-", 101))
            database.correlativeDao().insertCorrelative(CorrelativeEntity("NOTA_ENTREGA", "NE-", 51))
            database.correlativeDao().insertCorrelative(CorrelativeEntity("COTIZACION", "COT-", 35))
            database.correlativeDao().insertCorrelative(CorrelativeEntity("NOTA_CREDITO", "NC-", 12))
            database.correlativeDao().insertCorrelative(CorrelativeEntity("COMPRA", "COM-", 84))
            database.correlativeDao().insertCorrelative(CorrelativeEntity("COBRO", "REC-", 205))
            database.correlativeDao().insertCorrelative(CorrelativeEntity("PAGO", "ORD-", 92))
            database.correlativeDao().insertCorrelative(CorrelativeEntity("AJUSTE", "AJU-", 18))

            // 12. Sample Sales & CxC history
            val saleId = database.salesDao().insertSale(
                SalesDocumentEntity(
                    docType = "FACTURA",
                    docNumber = "FAC-000100",
                    controlNumber = "00-000452",
                    date = now - 86400000,
                    clientId = 1,
                    clientName = "Carlos Eduardo Mendoza",
                    clientRif = "V-14238492-1",
                    clientAddress = "Los Ruices, Caracas",
                    subtotalUsd = 38.75,
                    exemptAmountUsd = 13.50,
                    baseImponibleUsd = 25.25,
                    taxAmountUsd = 4.04,
                    igtfAmountUsd = 0.0,
                    totalUsd = 42.79,
                    bcvRate = 62.50,
                    totalVes = 2674.38,
                    paidAmountUsd = 42.79,
                    balanceUsd = 0.0,
                    paymentStatus = "PAGADA",
                    paymentMethodsSummary = "Efectivo USD $20.00 | Pago Móvil 1,424.38 Bs.",
                    cashierUser = "ADMIN",
                    warehouseCode = "ALM-01",
                    notes = "Venta cancelada al contado"
                )
            )

            database.salesDao().insertSaleItems(
                listOf(
                    SalesItemEntity(
                        documentId = saleId,
                        productId = 1,
                        productCode = "ART-001",
                        productName = "Harina PAN Blanca 1 Kg",
                        quantity = 10.0,
                        unitPriceUsd = 1.35,
                        taxRatePercent = 0.0,
                        totalUsd = 13.50
                    ),
                    SalesItemEntity(
                        documentId = saleId,
                        productId = 3,
                        productCode = "ART-003",
                        productName = "Aceite de Maíz Mazeite 1 Litro",
                        quantity = 4.0,
                        unitPriceUsd = 3.75,
                        taxRatePercent = 16.0,
                        totalUsd = 15.00
                    ),
                    SalesItemEntity(
                        documentId = saleId,
                        productId = 4,
                        productCode = "ART-004",
                        productName = "Café Fama de América 500g",
                        quantity = 2.0,
                        unitPriceUsd = 4.50,
                        taxRatePercent = 16.0,
                        totalUsd = 9.00
                    )
                )
            )

            // Audit initial creation
            database.auditDao().insertLog(
                AuditLogEntity(
                    timestamp = now,
                    user = "ADMIN",
                    module = "SISTEMA",
                    action = "INICIALIZACION",
                    description = "Base de datos a2 ERP Administrativo inicializada con éxito",
                    documentReference = "INIT-01"
                )
            )
        }
    }
}
