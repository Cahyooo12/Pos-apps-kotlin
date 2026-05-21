package com.example.data.repositories

import com.example.data.database.CustomerDao
import com.example.data.database.OrderDao
import com.example.data.database.ProductDao
import com.example.data.models.CartItem
import com.example.data.models.Customer
import com.example.data.models.Order
import com.example.data.models.OrderItem
import com.example.data.models.Product
import kotlinx.coroutines.flow.Flow

class PosRepository(
    private val productDao: ProductDao,
    private val customerDao: CustomerDao,
    private val orderDao: OrderDao
) {
    // Flow getters
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allOrderItems: Flow<List<OrderItem>> = orderDao.getAllOrderItems()

    // Product CRUDs
    suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProductById(id: Int) {
        productDao.deleteProductById(id)
    }

    suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)
    }

    suspend fun getProductBySku(sku: String): Product? {
        return productDao.getProductBySku(sku)
    }

    suspend fun updateStock(id: Int, newStock: Int) {
        productDao.updateStock(id, newStock)
    }

    // Customer CRUDs
    suspend fun insertCustomer(customer: Customer): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomerById(id: Int) {
        customerDao.deleteCustomerById(id)
    }

    // Transactions API & Stock Deduction
    suspend fun checkout(order: Order, cartItems: List<CartItem>): Boolean {
        return try {
            // 1. Insert master order row
            val orderId = orderDao.insertOrder(order).toInt()

            // 2. Map and insert line items, and deduct actual stock
            val itemsToSave = mutableListOf<OrderItem>()
            for (cart in cartItems) {
                val dbProduct = productDao.getProductById(cart.product.id)
                if (dbProduct != null) {
                    // Update the remaining stock (cannot be lower than 0)
                    val newStock = (dbProduct.stockQuantity - cart.quantity).coerceAtLeast(0)
                    productDao.updateStock(dbProduct.id, newStock)

                    itemsToSave.add(
                        OrderItem(
                            orderId = orderId,
                            productId = dbProduct.id,
                            productName = dbProduct.name,
                            quantity = cart.quantity,
                            sellPrice = cart.product.sellPrice,
                            buyPrice = dbProduct.buyPrice
                        )
                    )
                }
            }

            if (itemsToSave.isNotEmpty()) {
                orderDao.insertOrderItems(itemsToSave)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Fetch order details
    suspend fun getOrderItemsByOrderId(orderId: Int): List<OrderItem> {
        return orderDao.getOrderItemsByOrderId(orderId)
    }
}
