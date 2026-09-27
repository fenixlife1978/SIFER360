package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies LIMIT 1")
    fun getCompany(): Flow<CompanyEntity?>

    @Query("SELECT * FROM companies LIMIT 1")
    suspend fun getCompanyDirect(): CompanyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(company: CompanyEntity)
}

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates ORDER BY effectiveDate DESC LIMIT 1")
    fun getLatestRate(): Flow<ExchangeRateEntity?>

    @Query("SELECT * FROM exchange_rates ORDER BY effectiveDate DESC LIMIT 1")
    suspend fun getLatestRateDirect(): ExchangeRateEntity?

    @Query("SELECT * FROM exchange_rates ORDER BY effectiveDate DESC")
    fun getAllRates(): Flow<List<ExchangeRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: ExchangeRateEntity): Long
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY username ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses WHERE isActive = 1 ORDER BY code ASC")
    fun getAllWarehouses(): Flow<List<WarehouseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: WarehouseEntity): Long

    @Update
    suspend fun updateWarehouse(warehouse: WarehouseEntity)

    @Query("DELETE FROM warehouses WHERE id = :id")
    suspend fun deleteWarehouse(id: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCountDirect(): Int

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE code = :code OR barcode = :code LIMIT 1")
    suspend fun findByCodeOrBarcode(code: String): ProductEntity?

    @Query("SELECT * FROM products WHERE currentStock <= minStock AND isActive = 1")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET currentStock = currentStock + :delta WHERE id = :id")
    suspend fun updateStock(id: Long, delta: Double)

    @Query("UPDATE products SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteProduct(id: Long)
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients WHERE isActive = 1 ORDER BY name ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: Long): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("UPDATE clients SET currentBalanceUsd = currentBalanceUsd + :delta WHERE id = :id")
    suspend fun updateBalance(id: Long, delta: Double)

    @Query("UPDATE clients SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteClient(id: Long)
}

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: Long): ProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity): Long

    @Update
    suspend fun updateProvider(provider: ProviderEntity)

    @Query("UPDATE providers SET currentBalanceUsd = currentBalanceUsd + :delta WHERE id = :id")
    suspend fun updateBalance(id: Long, delta: Double)

    @Query("UPDATE providers SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteProvider(id: Long)
}

@Dao
interface SalesDao {
    @Query("SELECT * FROM sales_documents ORDER BY date DESC")
    fun getAllSales(): Flow<List<SalesDocumentEntity>>

    @Query("SELECT * FROM sales_documents WHERE docType = 'FACTURA' ORDER BY date DESC")
    fun getInvoices(): Flow<List<SalesDocumentEntity>>

    @Query("SELECT * FROM sales_documents WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: Long): SalesDocumentEntity?

    @Query("SELECT * FROM sales_items WHERE documentId = :documentId")
    fun getItemsForDocument(documentId: Long): Flow<List<SalesItemEntity>>

    @Query("SELECT * FROM sales_items WHERE documentId = :documentId")
    suspend fun getItemsForDocumentDirect(documentId: Long): List<SalesItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(doc: SalesDocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SalesItemEntity>)

    @Query("UPDATE sales_documents SET paymentStatus = :status, balanceUsd = :balance WHERE id = :id")
    suspend fun updatePaymentStatus(id: Long, status: String, balance: Double)

    @Query("UPDATE sales_documents SET paymentStatus = 'ANULADA' WHERE id = :id")
    suspend fun cancelSale(id: Long)
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchase_documents ORDER BY date DESC")
    fun getAllPurchases(): Flow<List<PurchaseDocumentEntity>>

    @Query("SELECT * FROM purchase_documents WHERE id = :id LIMIT 1")
    suspend fun getPurchaseById(id: Long): PurchaseDocumentEntity?

    @Query("SELECT * FROM purchase_items WHERE documentId = :documentId")
    fun getItemsForPurchase(documentId: Long): Flow<List<PurchaseItemEntity>>

    @Query("SELECT * FROM purchase_items WHERE documentId = :documentId")
    suspend fun getItemsForPurchaseDirect(documentId: Long): List<PurchaseItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(doc: PurchaseDocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)

    @Query("UPDATE purchase_documents SET status = :status, balanceUsd = :balance WHERE id = :id")
    suspend fun updatePurchaseStatus(id: Long, status: String, balance: Double)
}

@Dao
interface InventoryMovementDao {
    @Query("SELECT * FROM inventory_movements ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<InventoryMovementEntity>>

    @Query("SELECT * FROM inventory_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<InventoryMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: InventoryMovementEntity): Long
}

@Dao
interface CxcDao {
    @Query("SELECT * FROM cxc_movements ORDER BY timestamp DESC")
    fun getAllCxcMovements(): Flow<List<CxcMovementEntity>>

    @Query("SELECT * FROM cxc_movements WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getMovementsForClient(clientId: Long): Flow<List<CxcMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: CxcMovementEntity): Long
}

@Dao
interface CxpDao {
    @Query("SELECT * FROM cxp_movements ORDER BY timestamp DESC")
    fun getAllCxpMovements(): Flow<List<CxpMovementEntity>>

    @Query("SELECT * FROM cxp_movements WHERE providerId = :providerId ORDER BY timestamp DESC")
    fun getMovementsForProvider(providerId: Long): Flow<List<CxpMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: CxpMovementEntity): Long
}

@Dao
interface CashDao {
    @Query("SELECT * FROM cash_sessions WHERE status = 'ABIERTA' ORDER BY openedAt DESC LIMIT 1")
    fun getActiveSession(): Flow<CashSessionEntity?>

    @Query("SELECT * FROM cash_sessions WHERE status = 'ABIERTA' ORDER BY openedAt DESC LIMIT 1")
    suspend fun getActiveSessionDirect(): CashSessionEntity?

    @Query("SELECT * FROM cash_sessions ORDER BY openedAt DESC")
    fun getAllSessions(): Flow<List<CashSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CashSessionEntity): Long

    @Update
    suspend fun updateSession(session: CashSessionEntity)

    @Query("SELECT * FROM cash_movements WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getMovementsForSession(sessionId: Long): Flow<List<CashMovementEntity>>

    @Query("SELECT * FROM cash_movements ORDER BY timestamp DESC LIMIT 200")
    fun getRecentCashMovements(): Flow<List<CashMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: CashMovementEntity): Long
}

@Dao
interface BankDao {
    @Query("SELECT * FROM bank_accounts WHERE isActive = 1")
    fun getAllAccounts(): Flow<List<BankAccountEntity>>

    @Query("SELECT * FROM bank_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): BankAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: BankAccountEntity): Long

    @Update
    suspend fun updateAccount(account: BankAccountEntity)

    @Query("UPDATE bank_accounts SET balance = balance + :delta WHERE id = :id")
    suspend fun updateBalance(id: Long, delta: Double)

    @Query("SELECT * FROM bank_movements WHERE bankAccountId = :accountId ORDER BY timestamp DESC")
    fun getMovementsForAccount(accountId: Long): Flow<List<BankMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: BankMovementEntity): Long
}

@Dao
interface AccountingDao {
    @Query("SELECT * FROM accounting_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<AccountingEntryEntity>>

    @Query("SELECT * FROM accounting_lines WHERE entryId = :entryId")
    suspend fun getLinesForEntry(entryId: Long): List<AccountingLineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: AccountingEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<AccountingLineEntity>)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 300")
    fun getRecentLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long
}

@Dao
interface CorrelativeDao {
    @Query("SELECT * FROM correlatives WHERE docType = :docType LIMIT 1")
    suspend fun getCorrelative(docType: String): CorrelativeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrelative(correlative: CorrelativeEntity)

    @Query("UPDATE correlatives SET nextNumber = nextNumber + 1 WHERE docType = :docType")
    suspend fun incrementCorrelative(docType: String)
}
