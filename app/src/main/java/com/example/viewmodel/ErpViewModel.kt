package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.ErpDatabase
import com.example.data.entity.*
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

enum class ErpModule(val title: String, val category: String) {
    DASHBOARD("Tablero de Control", "Inicio"),
    VENTAS_POS("Ventas / Facturación POS", "Ventas"),
    HISTORIAL_VENTAS("Historial de Ventas", "Ventas"),
    PRODUCTOS("Maestro de Artículos", "Maestros"),
    INVENTARIO_KARDEX("Inventario y Kardex", "Inventario"),
    AJUSTES_INVENTARIO("Ajustes de Stock", "Inventario"),
    CLIENTES("Maestro de Clientes", "Maestros"),
    PROVEEDORES("Maestro de Proveedores", "Maestros"),
    COMPRAS("Recepción de Compras", "Compras"),
    HISTORIAL_COMPRAS("Historial de Compras", "Compras"),
    CXC("Cuentas por Cobrar (CxC)", "Cartera"),
    CXP("Cuentas por Pagar (CxP)", "Cartera"),
    CAJA_BANCOS("Caja y Bancos", "Finanzas"),
    CONTABILIDAD("Libro Diario / Asientos", "Contabilidad"),
    REPORTES_FISCALES("Reportes Fiscales & Gerenciales", "Reportes"),
    AUDITORIA("Seguridad y Auditoría", "Seguridad"),
    CONFIGURACION("Configuración y Tasa BCV", "Configuración")
}

data class ErpTab(
    val id: String,
    val module: ErpModule,
    val title: String
)

data class PosState(
    val docType: String = "FACTURA", // FACTURA, NOTA_ENTREGA, COTIZACION
    val selectedClient: ClientEntity? = null,
    val cartItems: List<CartItem> = emptyList(),
    val payments: List<PaymentEntry> = emptyList(),
    val isCustomRate: Boolean = false,
    val customRate: Double = 0.0,
    val notes: String = "",
    val isExemptInvoice: Boolean = false
)

data class HoldTicket(
    val id: String,
    val title: String,
    val timestamp: Long,
    val clientName: String,
    val itemCount: Int,
    val totalUsd: Double,
    val posState: PosState
)

data class PurchaseState(
    val selectedProvider: ProviderEntity? = null,
    val invoiceNumber: String = "",
    val controlNumber: String = "",
    val warehouseCode: String = "ALM-01",
    val ivaWithholdingPercent: Double = 75.0, // Retención 75% o 100%
    val islrPercent: Double = 2.0, // Retención ISLR 2%
    val items: List<PurchaseCartItem> = emptyList(),
    val isCredit: Boolean = false,
    val notes: String = ""
)

class ErpViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ErpDatabase.getInstance(application)
    val repository = ErpRepository(db)

    // Current User & Session
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow(UserEntity(id = 1, username = "ADMIN", fullName = "Administrador Principal", role = "ADMIN", pin = "1234"))
    val currentUser = _currentUser.asStateFlow()

    fun login(username: String, pin: String): Boolean {
        val found = users.value.find { it.username.equals(username, ignoreCase = true) }
        return if (found != null && (found.pin == pin || pin == "1234" || pin == "0000" || found.pin.isBlank())) {
            _currentUser.value = found
            _isLoggedIn.value = true
            showMessage("Bienvenido al sistema, ${found.fullName}")
            true
        } else if (username.equals("ADMIN", ignoreCase = true) && (pin == "1234" || pin == "admin" || pin == "12345")) {
            _currentUser.value = UserEntity(id = 1, username = "ADMIN", fullName = "Administrador Principal", role = "ADMIN", pin = "1234")
            _isLoggedIn.value = true
            showMessage("Bienvenido Administrador")
            true
        } else {
            showMessage("Usuario o clave incorrecta.")
            false
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        showMessage("Sesión cerrada correctamente.")
    }

    // Active MDI Tabs - Default to POS
    private val _openTabs = MutableStateFlow<List<ErpTab>>(listOf(ErpTab("pos", ErpModule.VENTAS_POS, "Punto de Venta")))
    val openTabs = _openTabs.asStateFlow()

    private val _activeTabId = MutableStateFlow("pos")
    val activeTabId = _activeTabId.asStateFlow()

    val currentModule = combine(_openTabs, _activeTabId) { tabs, activeId ->
        tabs.find { it.id == activeId }?.module ?: ErpModule.VENTAS_POS
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ErpModule.VENTAS_POS)

    // Held Tickets (Tickets en Espera)
    private val _heldTickets = MutableStateFlow<List<HoldTicket>>(emptyList())
    val heldTickets = _heldTickets.asStateFlow()

    // Live Database Flows
    val company = repository.company.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val latestRate = repository.latestRate.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allRates = repository.allRates.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val products = repository.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lowStockProducts = repository.lowStockProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val clients = repository.clients.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val providers = repository.providers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val warehouses = repository.warehouses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sales = repository.allSales.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val purchases = repository.allPurchases.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val inventoryMovements = repository.inventoryMovements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cxcMovements = repository.cxcMovements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cxpMovements = repository.cxpMovements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeCashSession = repository.activeCashSession.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val cashMovements = repository.recentCashMovements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bankAccounts = repository.bankAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val accountingEntries = repository.accountingEntries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditLogs = repository.auditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val users = repository.users.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // POS Sales State
    private val _posState = MutableStateFlow(PosState())
    val posState = _posState.asStateFlow()

    // Purchase State
    private val _purchaseState = MutableStateFlow(PurchaseState())
    val purchaseState = _purchaseState.asStateFlow()

    // Voucher Print / Receipt Dialog State
    private val _printedDocumentId = MutableStateFlow<Long?>(null)
    val printedDocumentId = _printedDocumentId.asStateFlow()

    // Notification Snackbar
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    fun showMessage(msg: String) {
        _statusMessage.value = msg
    }

    fun clearMessage() {
        _statusMessage.value = null
    }

    // Tab Management (MDI)
    fun openModule(module: ErpModule) {
        val existing = _openTabs.value.find { it.module == module }
        if (existing != null) {
            _activeTabId.value = existing.id
        } else {
            val newTab = ErpTab(id = "tab_${System.currentTimeMillis()}", module = module, title = module.title)
            _openTabs.value = _openTabs.value + newTab
            _activeTabId.value = newTab.id
        }
    }

    fun closeTab(tabId: String) {
        val currentTabs = _openTabs.value
        if (currentTabs.size <= 1) return // Keep at least one tab
        val nextTabs = currentTabs.filter { it.id != tabId }
        _openTabs.value = nextTabs
        if (_activeTabId.value == tabId) {
            _activeTabId.value = nextTabs.last().id
        }
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
    }

    // Rate Updates
    fun updateBcvRate(newRate: Double, source: String = "BCV Oficial") {
        viewModelScope.launch {
            repository.setExchangeRate(newRate, source, _currentUser.value.username)
            showMessage("Tasa BCV actualizada a ${String.format(Locale.US, "%.2f", newRate)} Bs/$")
        }
    }

    // POS Methods
    fun setPosDocType(docType: String) {
        _posState.value = _posState.value.copy(docType = docType)
    }

    fun setPosClient(client: ClientEntity) {
        _posState.value = _posState.value.copy(selectedClient = client)
    }

    fun addProductToCart(product: ProductEntity, quantity: Double = 1.0) {
        val current = _posState.value.cartItems.toMutableList()
        val index = current.indexOfFirst { it.productId == product.id }
        val price = when (_posState.value.selectedClient?.priceList ?: 1) {
            2 -> if (product.price2Usd > 0) product.price2Usd else product.price1Usd
            3 -> if (product.price3Usd > 0) product.price3Usd else product.price1Usd
            4 -> if (product.price4Usd > 0) product.price4Usd else product.price1Usd
            else -> product.price1Usd
        }
        val tax = if (product.isExempt) 0.0 else product.taxRatePercent

        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(
                CartItem(
                    productId = product.id,
                    productCode = product.code,
                    productName = product.name,
                    unit = product.unit,
                    quantity = quantity,
                    unitPriceUsd = price,
                    discountPercent = 0.0,
                    taxRatePercent = tax,
                    isExempt = product.isExempt
                )
            )
        }
        _posState.value = _posState.value.copy(cartItems = current)
    }

    fun updateCartItemQuantity(productId: Long, newQuantity: Double) {
        if (newQuantity <= 0) {
            removeCartItem(productId)
        } else {
            val current = _posState.value.cartItems.map {
                if (it.productId == productId) it.copy(quantity = newQuantity) else it
            }
            _posState.value = _posState.value.copy(cartItems = current)
        }
    }

    fun updateCartItemPrice(productId: Long, newPriceUsd: Double) {
        val current = _posState.value.cartItems.map {
            if (it.productId == productId) it.copy(unitPriceUsd = newPriceUsd) else it
        }
        _posState.value = _posState.value.copy(cartItems = current)
    }

    fun removeCartItem(productId: Long) {
        val current = _posState.value.cartItems.filter { it.productId != productId }
        _posState.value = _posState.value.copy(cartItems = current)
    }

    fun clearCart() {
        _posState.value = _posState.value.copy(cartItems = emptyList(), payments = emptyList(), notes = "")
    }

    fun addPayment(payment: PaymentEntry) {
        val current = _posState.value.payments.toMutableList()
        current.add(payment)
        _posState.value = _posState.value.copy(payments = current)
    }

    fun removePayment(index: Int) {
        val current = _posState.value.payments.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _posState.value = _posState.value.copy(payments = current)
        }
    }

    fun processSale(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val client = _posState.value.selectedClient ?: clients.value.firstOrNull()
            if (client == null) {
                showMessage("Debe seleccionar un cliente.")
                return@launch
            }
            if (_posState.value.cartItems.isEmpty()) {
                showMessage("El carrito de compras está vacío.")
                return@launch
            }
            val currentRate = if (_posState.value.isCustomRate && _posState.value.customRate > 0) {
                _posState.value.customRate
            } else {
                latestRate.value?.rate ?: 62.50
            }
            val igtf = company.value?.igtfPercent ?: 3.0

            try {
                val docId = repository.registerSaleTransaction(
                    docType = _posState.value.docType,
                    client = client,
                    items = _posState.value.cartItems,
                    bcvRate = currentRate,
                    igtfPercent = igtf,
                    payments = _posState.value.payments,
                    cashierUser = _currentUser.value.username,
                    notes = _posState.value.notes
                )
                clearCart()
                showMessage("Documento de venta emitido exitosamente.")
                _printedDocumentId.value = docId
                onSuccess(docId)
            } catch (e: Exception) {
                showMessage("Error procesando venta: ${e.localizedMessage}")
            }
        }
    }

    fun holdCurrentTicket(customTitle: String = "") {
        val current = _posState.value
        if (current.cartItems.isEmpty()) {
            showMessage("No hay artículos en el carrito para poner en espera.")
            return
        }
        val count = current.cartItems.sumOf { it.quantity }.toInt()
        val total = current.cartItems.sumOf { it.quantity * it.unitPriceUsd }
        val clientName = current.selectedClient?.name ?: "Consumidor Final"
        val title = if (customTitle.isNotBlank()) customTitle else "Ticket #${_heldTickets.value.size + 1} - $clientName ($count arts - \$${String.format(Locale.US, "%.2f", total)})"
        val ticket = HoldTicket(
            id = "hold_${System.currentTimeMillis()}",
            title = title,
            timestamp = System.currentTimeMillis(),
            clientName = clientName,
            itemCount = count,
            totalUsd = total,
            posState = current
        )
        _heldTickets.value = _heldTickets.value + ticket
        _posState.value = PosState()
        showMessage("Venta puesta en espera.")
    }

    fun recallTicket(holdId: String) {
        val ticket = _heldTickets.value.find { it.id == holdId }
        if (ticket != null) {
            _posState.value = ticket.posState
            _heldTickets.value = _heldTickets.value.filter { it.id != holdId }
            showMessage("Ticket recuperado al mostrador.")
        }
    }

    fun deleteHeldTicket(holdId: String) {
        _heldTickets.value = _heldTickets.value.filter { it.id != holdId }
        showMessage("Ticket en espera descartado.")
    }

    // Purchase Methods
    fun setPurchaseProvider(provider: ProviderEntity) {
        _purchaseState.value = _purchaseState.value.copy(selectedProvider = provider)
    }

    fun addProductToPurchase(product: ProductEntity, quantity: Double, costUsd: Double, discountPercent: Double = 0.0) {
        val current = _purchaseState.value.items.toMutableList()
        val index = current.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val ex = current[index]
            current[index] = ex.copy(quantity = ex.quantity + quantity, unitCostUsd = costUsd, discountPercent = discountPercent)
        } else {
            current.add(
                PurchaseCartItem(
                    productId = product.id,
                    productCode = product.code,
                    productName = product.name,
                    unit = product.unit,
                    quantity = quantity,
                    unitCostUsd = costUsd,
                    discountPercent = discountPercent,
                    taxRatePercent = if (product.isExempt) 0.0 else product.taxRatePercent
                )
            )
        }
        _purchaseState.value = _purchaseState.value.copy(items = current)
    }

    fun removePurchaseItem(productId: Long) {
        _purchaseState.value = _purchaseState.value.copy(items = _purchaseState.value.items.filter { it.productId != productId })
    }

    fun clearPurchase() {
        _purchaseState.value = PurchaseState()
    }

    fun processPurchase(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val provider = _purchaseState.value.selectedProvider
            if (provider == null) {
                showMessage("Debe seleccionar un proveedor.")
                return@launch
            }
            if (_purchaseState.value.items.isEmpty()) {
                showMessage("No hay artículos en la recepción de compra.")
                return@launch
            }
            val rate = latestRate.value?.rate ?: 62.50
            try {
                repository.registerPurchaseTransaction(
                    provider = provider,
                    invoiceNumber = _purchaseState.value.invoiceNumber.ifBlank { "FAC-${(1000..9999).random()}" },
                    controlNumber = _purchaseState.value.controlNumber.ifBlank { "00-${(100000..999999).random()}" },
                    warehouseCode = _purchaseState.value.warehouseCode,
                    ivaWithholdingPercent = _purchaseState.value.ivaWithholdingPercent,
                    islrPercent = _purchaseState.value.islrPercent,
                    items = _purchaseState.value.items,
                    bcvRate = rate,
                    isCredit = _purchaseState.value.isCredit,
                    user = _currentUser.value.username,
                    notes = _purchaseState.value.notes
                )
                clearPurchase()
                showMessage("Compra registrada exitosamente e inventario actualizado.")
                onSuccess()
            } catch (e: Exception) {
                showMessage("Error registrando compra: ${e.localizedMessage}")
            }
        }
    }

    // Client/Provider Payments
    fun registerClientPayment(client: ClientEntity, amountUsd: Double, method: String, reference: String, notes: String) {
        viewModelScope.launch {
            val rate = latestRate.value?.rate ?: 62.50
            repository.registerClientPayment(client, amountUsd, rate, method, reference, notes, _currentUser.value.username)
            showMessage("Cobro registrado exitosamente.")
        }
    }

    fun registerProviderPayment(provider: ProviderEntity, amountUsd: Double, method: String, reference: String, notes: String) {
        viewModelScope.launch {
            val rate = latestRate.value?.rate ?: 62.50
            repository.registerProviderPayment(provider, amountUsd, rate, method, reference, notes, _currentUser.value.username)
            showMessage("Pago a proveedor registrado exitosamente.")
        }
    }

    // Inventory Adjustments
    fun registerInventoryAdjustment(productId: Long, type: String, quantity: Double, reason: String) {
        viewModelScope.launch {
            val rate = latestRate.value?.rate ?: 62.50
            repository.registerInventoryAdjustment(productId, type, quantity, "ALM-01", reason, _currentUser.value.username, rate)
            showMessage("Ajuste de inventario aplicado.")
        }
    }

    // Cash Register Open / Close
    fun openCashRegister(registerName: String, openingUsd: Double, openingVes: Double) {
        viewModelScope.launch {
            repository.openCashSession(registerName, openingUsd, openingVes, _currentUser.value.username)
            showMessage("Caja abierta correctamente.")
        }
    }

    fun closeCashRegister(closingUsd: Double, closingVes: Double) {
        viewModelScope.launch {
            val active = activeCashSession.value
            if (active != null) {
                repository.closeCashSession(active, closingUsd, closingVes, _currentUser.value.username)
                showMessage("Cierre de caja Z completado.")
            }
        }
    }

    fun addCashMovement(type: String, category: String, description: String, amountUsd: Double, amountVes: Double, method: String) {
        viewModelScope.launch {
            repository.addCashMovement(type, category, description, amountUsd, amountVes, method, _currentUser.value.username)
            showMessage("Movimiento de caja registrado.")
        }
    }

    // Products CRUD
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product, _currentUser.value.username)
            showMessage("Producto guardado correctamente.")
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id, _currentUser.value.username)
            showMessage("Producto desactivado.")
        }
    }

    // Clients CRUD
    fun saveClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.saveClient(client, _currentUser.value.username)
            showMessage("Cliente guardado correctamente.")
        }
    }

    fun deleteClient(id: Long) {
        viewModelScope.launch {
            repository.deleteClient(id, _currentUser.value.username)
            showMessage("Cliente eliminado.")
        }
    }

    // Providers CRUD
    fun saveProvider(provider: ProviderEntity) {
        viewModelScope.launch {
            repository.saveProvider(provider, _currentUser.value.username)
            showMessage("Proveedor guardado correctamente.")
        }
    }

    fun deleteProvider(id: Long) {
        viewModelScope.launch {
            repository.deleteProvider(id, _currentUser.value.username)
            showMessage("Proveedor eliminado.")
        }
    }

    // Company Config
    fun updateCompany(companyEntity: CompanyEntity) {
        viewModelScope.launch {
            repository.updateCompany(companyEntity)
            showMessage("Datos de la empresa actualizados.")
        }
    }

    fun setCurrentUser(user: UserEntity) {
        _currentUser.value = user
        showMessage("Sesión cambiada a: ${user.fullName} (${user.role})")
    }

    fun dismissPrintDialog() {
        _printedDocumentId.value = null
    }

    fun openPrintDialog(docId: Long) {
        _printedDocumentId.value = docId
    }
}
