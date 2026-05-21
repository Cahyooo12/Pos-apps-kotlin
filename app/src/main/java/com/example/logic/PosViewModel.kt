package com.example.logic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.models.CartItem
import com.example.data.models.Customer
import com.example.data.models.Order
import com.example.data.models.OrderItem
import com.example.data.models.Product
import com.example.data.repositories.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PosRepository

    // Central Flows from DB
    val products: StateFlow<List<Product>>
    val customers: StateFlow<List<Customer>>
    val orders: StateFlow<List<Order>>
    val orderItems: StateFlow<List<OrderItem>>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = PosRepository(
            database.productDao(),
            database.customerDao(),
            database.orderDao()
        )

        // Bind streams
        products = repository.allProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        customers = repository.allCustomers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        orders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        orderItems = repository.allOrderItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // --- Onboarding & Auth State ---
    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted = _onboardingCompleted.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _activeCashier = MutableStateFlow("Admin Utama")
    val activeCashier = _activeCashier.asStateFlow()

    fun completeOnboarding() {
        _onboardingCompleted.value = true
    }

    fun login(pin: String): Boolean {
        return if (pin == "1234" || pin == "0000") {
            _isLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    // --- Business Settings Subsystem ---
    private val _businessName = MutableStateFlow("Toko Berkah Utama")
    val businessName = _businessName.asStateFlow()

    private val _businessAddress = MutableStateFlow("Jl. Veteran Jaya No. 99, Bandung")
    val businessAddress = _businessAddress.asStateFlow()

    private val _businessPhone = MutableStateFlow("081234567890")
    val businessPhone = _businessPhone.asStateFlow()

    private val _isBluetoothPrinterConnected = MutableStateFlow(false)
    val isBluetoothPrinterConnected = _isBluetoothPrinterConnected.asStateFlow()

    fun updateSettings(name: String, address: String, phone: String) {
        _businessName.value = name
        _businessAddress.value = address
        _businessPhone.value = phone
    }

    fun toggleBluetoothPrinter() {
        _isBluetoothPrinterConnected.value = !_isBluetoothPrinterConnected.value
    }

    // --- Inventory Stock Administration ---
    fun saveProduct(
        id: Int = 0,
        sku: String,
        name: String,
        category: String,
        buyPrice: Double,
        sellPrice: Double,
        stockQuantity: Int,
        minStock: Int,
        unit: String,
        description: String
    ) {
        viewModelScope.launch {
            val product = Product(
                id = id,
                sku = sku.ifBlank { "SKU-" + System.currentTimeMillis().toString().takeLast(6) },
                name = name,
                category = category.ifBlank { "Umum" },
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                stockQuantity = stockQuantity,
                minStockAlert = minStock,
                unit = unit.ifBlank { "pcs" },
                description = description
            )
            if (id == 0) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProductById(id)
        }
    }

    // --- Customer Subsystem ---
    fun saveCustomer(
        id: Int = 0,
        name: String,
        phone: String,
        email: String,
        address: String
    ) {
        viewModelScope.launch {
            val customer = Customer(
                id = id,
                name = name,
                phone = phone,
                email = email,
                address = address
            )
            if (id == 0) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
        }
    }

    fun deleteCustomer(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomerById(id)
        }
    }

    // --- Cashier Cart Subsystem (Stateful Cart) ---
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer = _selectedCustomer.asStateFlow()

    private val _discountPercent = MutableStateFlow(0.0)
    val discountPercent = _discountPercent.asStateFlow()

    private val _taxPercent = MutableStateFlow(11.0) // 11% PPN standard Indonesia
    val taxPercent = _taxPercent.asStateFlow()

    fun addToCart(product: Product, quantity: Int = 1) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }

        if (index != -1) {
            val existing = currentList[index]
            val proposedQty = existing.quantity + quantity
            // Clamp stock constraint
            val allowedQty = if (proposedQty > product.stockQuantity) product.stockQuantity else proposedQty
            currentList[index] = existing.copy(quantity = allowedQty)
        } else {
            val allowedQty = if (quantity > product.stockQuantity) product.stockQuantity else quantity
            if (allowedQty > 0) {
                currentList.add(CartItem(product = product, quantity = allowedQty))
            }
        }
        _cart.value = currentList
    }

    fun updateCartQty(product: Product, quantity: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            if (quantity <= 0) {
                currentList.removeAt(index)
            } else {
                val clampedQty = if (quantity > product.stockQuantity) product.stockQuantity else quantity
                currentList[index] = currentList[index].copy(quantity = clampedQty)
            }
            _cart.value = currentList
        }
    }

    fun removeFromCart(product: Product) {
        _cart.value = _cart.value.filter { it.product.id != product.id }
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun setDiscount(percent: Double) {
        _discountPercent.value = percent.coerceIn(0.0, 100.0)
    }

    fun setTax(percent: Double) {
        _taxPercent.value = percent.coerceIn(0.0, 100.0)
    }

    fun clearCart() {
        _cart.value = emptyList()
        _selectedCustomer.value = null
        _discountPercent.value = 0.0
    }

    // Computed Cart Values
    val cartSubtotal: StateFlow<Double> = _cart.combine(_cart) { _, _ ->
        _cart.value.sumOf { it.product.sellPrice * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartDiscountAmount: StateFlow<Double> = combine(cartSubtotal, _discountPercent) { subtotal, pct ->
        subtotal * (pct / 100.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTaxAmount: StateFlow<Double> = combine(cartSubtotal, cartDiscountAmount, _taxPercent) { subtotal, discount, taxPct ->
        (subtotal - discount) * (taxPct / 100.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTotal: StateFlow<Double> = combine(cartSubtotal, cartDiscountAmount, cartTaxAmount) { subtotal, discount, tax ->
        subtotal - discount + tax
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Last completed receipt (for screen overlay invoice printing popup)
    private val _lastCompletedOrder = MutableStateFlow<Order?>(null)
    val lastCompletedOrder = _lastCompletedOrder.asStateFlow()

    private val _lastCompletedItems = MutableStateFlow<List<OrderItem>>(emptyList())
    val lastCompletedItems = _lastCompletedItems.asStateFlow()

    fun dismissReceiptPopup() {
        _lastCompletedOrder.value = null
        _lastCompletedItems.value = emptyList()
    }

    // Checkout Logic
    fun processCheckout(
        paymentMethod: String,
        cashPaid: Double,
        onComplete: (Boolean, String) -> Unit
    ) {
        val rawItems = _cart.value
        if (rawItems.isEmpty()) {
            onComplete(false, "Keranjang belanja kosong.")
            return
        }

        val totalAmount = cartTotal.value
        val discountAmount = cartDiscountAmount.value
        val taxAmount = cartTaxAmount.value

        if (paymentMethod == "Tunai" && cashPaid < totalAmount) {
            onComplete(false, "Pembayaran tunai kurang dari total belanja.")
            return
        }

        val invoiceNumber = "INV-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())}-${(100..999).random()}"

        // Build Order
        val newOrder = Order(
            orderNumber = invoiceNumber,
            timestamp = System.currentTimeMillis(),
            totalAmount = totalAmount,
            discount = discountAmount,
            tax = taxAmount,
            paymentMethod = paymentMethod,
            cashPaid = if (paymentMethod == "Tunai") cashPaid else totalAmount,
            cashReturn = if (paymentMethod == "Tunai") (cashPaid - totalAmount).coerceAtLeast(0.0) else 0.0,
            customerName = _selectedCustomer.value?.name ?: "Umum"
        )

        viewModelScope.launch {
            // Deduct stock, insert items and record order to DB
            val success = repository.checkout(newOrder, rawItems)
            if (success) {
                // Pre-fetch items from database for showing in receipt dialog
                // (Using just the flow of items from the cart matching our generated number for reliable display)
                val dummyItems = rawItems.map {
                    OrderItem(
                        orderId = 0,
                        productId = it.product.id,
                        productName = it.product.name,
                        quantity = it.quantity,
                        sellPrice = it.product.sellPrice,
                        buyPrice = it.product.buyPrice
                    )
                }

                _lastCompletedOrder.value = newOrder
                _lastCompletedItems.value = dummyItems

                clearCart()
                onComplete(true, "Transaksi Berhasil!")
            } else {
                onComplete(false, "Gagal memproses transaksi di database.")
            }
        }
    }

    // --- Report / Dashboard Analytics Engine ---
    val lowStockCount = products.combine(products) { _, _ ->
        products.value.count { it.stockQuantity <= it.minStockAlert }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val outOfStockCount = products.combine(products) { _, _ ->
        products.value.count { it.stockQuantity == 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalRevenueAmount = orders.combine(orders) { _, _ ->
        orders.value.sumOf { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalProfitAmount = orderItems.combine(orderItems) { _, _ ->
        orderItems.value.sumOf { (it.sellPrice - it.buyPrice) * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSalesCount = orders.combine(orders) { _, _ ->
        orders.value.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
