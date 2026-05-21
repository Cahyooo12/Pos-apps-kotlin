package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.models.Customer
import com.example.data.models.Order
import com.example.data.models.OrderItem
import com.example.data.models.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Product::class, Customer::class, Order::class, OrderItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_inventory_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            val productDao = db.productDao()
            val customerDao = db.customerDao()

            // Pre-populate Products (Merchandise Inventory)
            val products = listOf(
                Product(sku = "SKU-KOPI-01", name = "Kopi Susu Gula Aren", category = "Minuman", buyPrice = 8000.0, sellPrice = 18000.0, stockQuantity = 50, minStockAlert = 10, unit = "Cup", description = "Es kopi susu dengan gula aren murni"),
                Product(sku = "SKU-ES-TEH", name = "Es Teh Manis Jumbo", category = "Minuman", buyPrice = 1500.0, sellPrice = 5000.0, stockQuantity = 100, minStockAlert = 15, unit = "Cup", description = "Teh melati manis segar dengan es batu melimpah"),
                Product(sku = "SKU-MIE-02", name = "Indomie Goreng Double", category = "Makanan", buyPrice = 4000.0, sellPrice = 12000.0, stockQuantity = 40, minStockAlert = 8, unit = "Porsi", description = "Indomie goreng dobel lengkap dengan telur setengah matang"),
                Product(sku = "SKU-ROTI-03", name = "Roti Bakar Cokelat Keju", category = "Makanan", buyPrice = 6000.0, sellPrice = 15000.0, stockQuantity = 25, minStockAlert = 5, unit = "Porsi", description = "Toast manis dengan parutan keju premium dan meses cokelat"),
                Product(sku = "SKU-SNACK-04", name = "Keripik Singkong Pedas", category = "Camilan", buyPrice = 3000.0, sellPrice = 8000.0, stockQuantity = 3, minStockAlert = 5, unit = "Bungkus", description = "Keripik singkong kering pedas level 5 (Stok Hampir Habis!)")
            )
            for (product in products) {
                productDao.insertProduct(product)
            }

            // Pre-populate Customers
            val customers = listOf(
                Customer(name = "Andi Wijaya", phone = "081234567890", email = "andi@gmail.com", address = "Jl. Sudirman No. 45, Jakarta"),
                Customer(name = "Siti Rahma", phone = "085712345678", email = "siti@yahoo.com", address = "Perum Anggrek Blok C/12, Depok"),
                Customer(name = "Budi Santoso", phone = "089987654321", email = "budi@outlook.com", address = "Jl. Merdeka No. 101, Bogor")
            )
            for (customer in customers) {
                customerDao.insertCustomer(customer)
            }
        }
    }
}
