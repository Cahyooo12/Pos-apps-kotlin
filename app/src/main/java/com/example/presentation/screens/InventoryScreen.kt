package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.utils.Helper
import com.example.data.models.Product
import com.example.logic.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: PosViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    // Dialog state for Add / Edit
    var showProductDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }

    // Local form fields
    var formSku by remember { mutableStateOf("") }
    var formName by remember { mutableStateOf("") }
    var formCategory by remember { mutableStateOf("") }
    var formBuyPrice by remember { mutableStateOf("") }
    var formSellPrice by remember { mutableStateOf("") }
    var formStockQuantity by remember { mutableStateOf("") }
    var formMinStockAlert by remember { mutableStateOf("") }
    var formUnit by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }

    // Category suggestions
    val categories = listOf("Semua", "Makanan", "Minuman", "Camilan", "Lainnya")

    fun openAddDialog() {
        selectedProductForEdit = null
        formSku = ""
        formName = ""
        formCategory = "Makanan"
        formBuyPrice = ""
        formSellPrice = ""
        formStockQuantity = ""
        formMinStockAlert = "5"
        formUnit = "pcs"
        formDescription = ""
        showProductDialog = true
    }

    fun openEditDialog(product: Product) {
        selectedProductForEdit = product
        formSku = product.sku
        formName = product.name
        formCategory = product.category
        formBuyPrice = product.buyPrice.toLong().toString()
        formSellPrice = product.sellPrice.toLong().toString()
        formStockQuantity = product.stockQuantity.toString()
        formMinStockAlert = product.minStockAlert.toString()
        formUnit = product.unit
        formDescription = product.description
        showProductDialog = true
    }

    fun handleSaveProduct() {
        if (formName.isBlank()) {
            Toast.makeText(context, "Nama barang tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        val buy = formBuyPrice.toDoubleOrNull() ?: 0.0
        val sell = formSellPrice.toDoubleOrNull() ?: 0.0
        val stock = formStockQuantity.toIntOrNull() ?: 0
        val minAlert = formMinStockAlert.toIntOrNull() ?: 5

        if (sell <= 0) {
            Toast.makeText(context, "Harga jual harus lebih besar dari 0", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveProduct(
            id = selectedProductForEdit?.id ?: 0,
            sku = formSku,
            name = formName,
            category = formCategory,
            buyPrice = buy,
            sellPrice = sell,
            stockQuantity = stock,
            minStock = minAlert,
            unit = formUnit,
            description = formDescription
        )

        Toast.makeText(
            context,
            if (selectedProductForEdit == null) "Barang ditambahkan!" else "Barang diupdate!",
            Toast.LENGTH_SHORT
        ).show()

        showProductDialog = false
    }

    // Filtered lists
    val filteredProducts = products.filter {
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "Semua" || it.category == selectedCategoryFilter
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventaris Barang", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Barang")
            }
        },
        modifier = Modifier.testTag("inventory_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari barang atau scan SKU...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            // Simulator scan barcode
                            val randomSku = "SKU-KOPI-01"
                            searchQuery = randomSku
                            Toast.makeText(context, "Membaca Barcode: $randomSku", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(imageVector = Icons.Default.QrCode, contentDescription = "Scan SKU Simulator")
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("inventory_search_input")
                )
            }

            // Categories horizontal slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                for (cat in categories) {
                    val isSelected = cat == selectedCategoryFilter
                    InputChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(text = cat) },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Products list
            if (filteredProducts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tidak Ada Barang Dagangan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Silakan tambah barang atau ubah kriteria pencarian.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductInventoryItem(
                            product = product,
                            onEdit = { openEditDialog(product) },
                            onDelete = {
                                viewModel.deleteProduct(product.id)
                                Toast.makeText(context, "${product.name} dihapus", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Add & Edit Dialog
        if (showProductDialog) {
            AlertDialog(
                onDismissRequest = { showProductDialog = false },
                title = {
                    Text(
                        text = if (selectedProductForEdit == null) "Tambah Barang Baru" else "Edit Detail Barang",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = formSku,
                                onValueChange = { formSku = it },
                                label = { Text("Bar-code SKU (Opsional)") },
                                placeholder = { Text("Contoh: SKU-KOPI-02") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = formName,
                                onValueChange = { formName = it },
                                label = { Text("Nama Barang Dagangan") },
                                placeholder = { Text("Contoh: Roti Srikaya") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("product_form_name")
                            )
                        }
                        item {
                            // Simple Category Form Input
                            OutlinedTextField(
                                value = formCategory,
                                onValueChange = { formCategory = it },
                                label = { Text("Kategori") },
                                placeholder = { Text("Contoh: Makanan, Minuman, Lainnya") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = formBuyPrice,
                                    onValueChange = { formBuyPrice = it },
                                    label = { Text("Harga Beli (Rp)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("product_form_buy")
                                )
                                OutlinedTextField(
                                    value = formSellPrice,
                                    onValueChange = { formSellPrice = it },
                                    label = { Text("Harga Jual (Rp)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("product_form_sell")
                                )
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = formStockQuantity,
                                    onValueChange = { formStockQuantity = it },
                                    label = { Text("Jumlah Stok") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("product_form_stock")
                                )
                                OutlinedTextField(
                                    value = formMinStockAlert,
                                    onValueChange = { formMinStockAlert = it },
                                    label = { Text("Min Stok Alarm") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = formUnit,
                                onValueChange = { formUnit = it },
                                label = { Text("Satuan Unit") },
                                placeholder = { Text("Contoh: pcs, cup, porsi, box") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = formDescription,
                                onValueChange = { formDescription = it },
                                label = { Text("Keterangan Tambahan") },
                                placeholder = { Text("Deskripsi singkat barang") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { handleSaveProduct() },
                        modifier = Modifier.testTag("save_product_button")
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProductDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun ProductInventoryItem(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = product.stockQuantity <= product.minStockAlert
    val isOutOfStock = product.stockQuantity == 0

    val badgeBg = if (isOutOfStock || isLowStock) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        com.example.ui.theme.InfoContainerGeom
    }

    val badgeText = if (isOutOfStock || isLowStock) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        com.example.ui.theme.OnInfoContainerGeom
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = product.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "SKU: ${product.sku} • Kat: ${product.category}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Edit & Delete row
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock warning badge or indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(badgeBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                isOutOfStock -> "Stok Habis"
                                isLowStock -> "Stok Tipis"
                                else -> "Stok Aman"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${product.stockQuantity} ${product.unit}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Buy price and sell price
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Jual: ${Helper.formatRupiah(product.sellPrice)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Beli: ${Helper.formatRupiah(product.buyPrice)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
