package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.utils.Helper
import com.example.data.models.CartItem
import com.example.data.models.Customer
import com.example.data.models.Product
import com.example.logic.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    viewModel: PosViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Observe central states
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val discountPercent by viewModel.discountPercent.collectAsState()
    val taxPercent by viewModel.taxPercent.collectAsState()

    // Observe computed totals
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val discountAmt by viewModel.cartDiscountAmount.collectAsState()
    val taxAmt by viewModel.cartTaxAmount.collectAsState()
    val finalTotal by viewModel.cartTotal.collectAsState()

    val lastCompletedOrder by viewModel.lastCompletedOrder.collectAsState()
    val lastCompletedItems by viewModel.lastCompletedItems.collectAsState()

    // Local UI control states
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }
    var isCartDrawerOpen by remember { mutableStateOf(false) }

    // Checkout popups
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("Tunai") }
    var cashPaidString by remember { mutableStateOf("") }
    val cashPaid = cashPaidString.toDoubleOrNull() ?: 0.0
    val cashReturn = (cashPaid - finalTotal).coerceAtLeast(0.0)

    // Customer picker state
    var showCustomerPickerDialog by remember { mutableStateOf(false) }

    // Receipt printing state wrapper
    val isPrinterConnected by viewModel.isBluetoothPrinterConnected.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val businessAddress by viewModel.businessAddress.collectAsState()
    val businessPhone by viewModel.businessPhone.collectAsState()

    val categories = listOf("Semua", "Makanan", "Minuman", "Camilan", "Lainnya")

    val filteredProducts = products.filter {
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "Semua" || it.category == selectedCategoryFilter
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesin Kasir", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // Shopping cart button displaying counts
                    IconButton(
                        onClick = { isCartDrawerOpen = !isCartDrawerOpen },
                        modifier = Modifier.testTag("cashier_cart_trigger_button")
                    ) {
                        Box {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Keranjang")
                            if (cart.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cart.sumOf { it.quantity }.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = Modifier.testTag("cashier_root")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main browse catalog content
            Column(modifier = Modifier.fillMaxSize()) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari barang atau scan SKU...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("cashier_search_input")
                )

                // Categories chips horizontally
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
                        modifier = Modifier.size(18.dp)
                    )
                    for (cat in categories) {
                        val isSelected = cat == selectedCategoryFilter
                        InputChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat) },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Catalog items in vertical list (compact display)
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Barang tidak ditemukan.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredProducts) { item ->
                            ProductCashierCard(
                                product = item,
                                isInCart = cart.any { it.product.id == item.id },
                                cartQty = cart.find { it.product.id == item.id }?.quantity ?: 0,
                                onAddClick = { viewModel.addToCart(item) },
                                onRemoveClick = { viewModel.updateCartQty(item, (cart.find { it.product.id == item.id }?.quantity ?: 0) - 1) }
                            )
                        }
                    }
                }

                // Quick Floating cart summary bar at the bottom if cart drawer is closed
                if (!isCartDrawerOpen && cart.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCartDrawerOpen = true }
                            .testTag("cashier_bottom_bar_trigger")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${cart.sumOf { it.quantity }} item terpilih",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Pelanggan: ${selectedCustomer?.name ?: "Umum"}",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Helper.formatRupiah(finalTotal),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bayar >",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Animated slide-up visual overlay state containing the interactive checkout drawer
            AnimatedVisibility(
                visible = isCartDrawerOpen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Title row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Daftar Belanja",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { isCartDrawerOpen = false }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Tutup")
                            }
                        }

                        // Registered Customer linkage selector
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCustomerPickerDialog = true }
                                .padding(vertical = 8.dp)
                                .testTag("choose_customer_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Tautkan Pelanggan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = selectedCustomer?.name ?: "Buku pelanggan Umum (Default)",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (selectedCustomer != null) {
                                    IconButton(onClick = { viewModel.selectCustomer(null) }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Text("Pilih >", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Listed cart items
                        if (cart.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Belum ada belanjaan terpilih.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(cart) { cartItem ->
                                    CartListItemRow(
                                        cartItem = cartItem,
                                        onAdd = { viewModel.addToCart(cartItem.product) },
                                        onSubtract = { viewModel.updateCartQty(cartItem.product, cartItem.quantity -1) },
                                        onRemove = { viewModel.removeFromCart(cartItem.product) }
                                    )
                                }
                            }
                        }

                        // Discount selectors Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Diskon Promo:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val discountRates = listOf(0.0, 5.0, 10.0, 15.0, 20.0)
                                for (rate in discountRates) {
                                    val isPicked = discountPercent == rate
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isPicked) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { viewModel.setDiscount(rate) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${rate.toInt()}%",
                                            color = if (isPicked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Totals calculations summary block
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(Helper.formatRupiah(subtotal), fontSize = 13.sp)
                                }
                                if (discountAmt > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Potongan Diskon (${discountPercent.toInt()}%)", fontSize = 13.sp, color = Color(0xFFD50000))
                                        Text("- ${Helper.formatRupiah(discountAmt)}", fontSize = 13.sp, color = Color(0xFFD50000))
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pajak PPN (${taxPercent.toInt()}%)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(Helper.formatRupiah(taxAmt), fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("TOTAL BAYAR", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    Text(
                                        text = Helper.formatRupiah(finalTotal),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Process checkout action buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.clearCart() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .weight(0.3f)
                                    .height(52.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Cart")
                            }

                            Button(
                                onClick = {
                                    if (cart.isEmpty()) {
                                        Toast.makeText(context, "Keranjang kosong", Toast.LENGTH_SHORT).show()
                                    } else {
                                        cashPaidString = finalTotal.toLong().toString() // prefill with exact amount
                                        showCheckoutDialog = true
                                    }
                                },
                                enabled = cart.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.7f)
                                    .height(52.dp)
                                    .testTag("checkout_commit_button")
                            ) {
                                Text("Metode Bayar & Konfirmasi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }

        // Customer Picker Modal
        if (showCustomerPickerDialog) {
            AlertDialog(
                onDismissRequest = { showCustomerPickerDialog = false },
                title = { Text("Pilih Data Pelanggan", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Hubungkan nota belanja ini ke pelanggan tercatat di bawah:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (customers.isEmpty()) {
                            Text("Buku pelanggan kosong. Silakan tambah data pelanggan baru dahulu.", color = MaterialTheme.colorScheme.error)
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(customers) { cust ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectCustomer(cust)
                                                showCustomerPickerDialog = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(cust.name, fontWeight = FontWeight.Bold)
                                                Text(cust.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text("Pilih", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showCustomerPickerDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        // Checkout & Payment dialog
        if (showCheckoutDialog) {
            AlertDialog(
                onDismissRequest = { showCheckoutDialog = false },
                title = { Text("Penyelesaian Pembayaran", fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("Pilih Metode Bayar:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val methods = listOf("Tunai", "QRIS", "Transfer", "Debit")
                                for (met in methods) {
                                    val isSelected = selectedPaymentMethod == met
                                    InputChip(
                                        selected = isSelected,
                                        onClick = { selectedPaymentMethod = met },
                                        label = { Text(met) },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = "TOTAL BELANJA: ${Helper.formatRupiah(finalTotal)}",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            )
                        }

                        if (selectedPaymentMethod == "Tunai") {
                            item {
                                OutlinedTextField(
                                    value = cashPaidString,
                                    onValueChange = { cashPaidString = it },
                                    label = { Text("Jumlah Uang Tunai Diterima (Rp)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("cash_paid_input")
                                )
                            }

                            // Quick denom buttons
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val denoms = listOf(
                                        finalTotal.toLong(),
                                        10000,
                                        20000,
                                        50000,
                                        100000
                                    ).distinct()

                                    for (d in denoms) {
                                        val displayStr = if (d == finalTotal.toLong()) "Uang Pas" else Helper.formatRupiah(d.toDouble()).replace("Rp ", "")
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { cashPaidString = d.toString() }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(displayStr, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Kembalian:", fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (cashPaid >= finalTotal) Helper.formatRupiah(cashReturn) else "Uang kurang!",
                                        fontWeight = FontWeight.Bold,
                                        color = if (cashPaid >= finalTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        } else {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$selectedPaymentMethod Terpilih",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Uang pas otomatis tercatat secara real-time.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.processCheckout(
                                paymentMethod = selectedPaymentMethod,
                                cashPaid = if (selectedPaymentMethod == "Tunai") cashPaid else finalTotal,
                                onComplete = { success, msg ->
                                    if (success) {
                                        showCheckoutDialog = false
                                        isCartDrawerOpen = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "$msg", Toast.LENGTH_LONG).show()
                                    }
                                }
                            )
                        },
                        modifier = Modifier.testTag("submit_checkout_button")
                    ) {
                        Text("Proses Nota")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCheckoutDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        // Invoice Receipt PopUp Simulator (Shows immediately after checkout completes!)
        if (lastCompletedOrder != null) {
            val order = lastCompletedOrder!!
            val items = lastCompletedItems

            AlertDialog(
                onDismissRequest = { viewModel.dismissReceiptPopup() },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("STRUK PEMBELIAN", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                    }
                },
                text = {
                    // Thermal invoice ticket styling
                    Surface(
                        color = Color(0xFFFBFBFB),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(businessName.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                            Text(businessAddress, fontSize = 10.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                            Text("HP: $businessPhone", fontSize = 10.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                            Text("--------------------------------", fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("No: ${order.orderNumber.takeLast(10)}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("Kasir: Admin", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val dateFormatted = Helper.formatDate(order.timestamp).replace(", ", " ")
                                Text("Tgl: $dateFormatted", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("Cust: ${order.customerName}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Text("--------------------------------", fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                            // Item rows
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (item in items) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(item.productName, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${item.quantity} x ${Helper.formatRupiah(item.sellPrice).replace("Rp ", "")}",
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = Helper.formatRupiah(item.sellPrice * item.quantity).replace("Rp ", ""),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }

                            Text("--------------------------------", fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                            // Totals outline
                            ReceiptPriceRow(label = "SUBTOTAL :", value = Helper.formatRupiah(items.sumOf { it.sellPrice * it.quantity }))
                            if (order.discount > 0) {
                                ReceiptPriceRow(label = "DISKON   :", value = "- " + Helper.formatRupiah(order.discount), isDiscount = true)
                            }
                            ReceiptPriceRow(label = "PPN ($taxPercent%):", value = Helper.formatRupiah(order.tax))
                            ReceiptPriceRow(label = "TOTAL    :", value = Helper.formatRupiah(order.totalAmount), isBold = true)
                            Text("- - - - - - - - - - - - - - - -", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            ReceiptPriceRow(label = order.paymentMethod.uppercase() + "     :", value = Helper.formatRupiah(order.cashPaid))
                            if (order.paymentMethod == "Tunai") {
                                ReceiptPriceRow(label = "KEMBALI  :", value = Helper.formatRupiah(order.cashReturn))
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("TERIMA KASIH", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                            Text("Atas kunjungan Anda", fontSize = 10.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.Center)
                            Text("Powered by Toko POS", fontSize = 8.sp, color = Color.Gray, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Mencetak Struk Bluetooth...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bluetooth Print", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Membagikan Struk ke WhatsApp...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Color Green
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bagikan WA", fontSize = 11.sp)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissReceiptPopup() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Selesai & Sembunyikan", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            )
        }
    }
}

@Composable
fun ProductCashierCard(
    product: Product,
    isInCart: Boolean,
    cartQty: Int,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val isOutOfStock = product.stockQuantity == 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInCart) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = product.category,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = Helper.formatRupiah(product.sellPrice),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Stock and Add controller
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stok: ${product.stockQuantity}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (product.stockQuantity <= product.minStockAlert) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Habis", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isInCart) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onRemoveClick,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Text(
                            text = cartQty.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        // Clamp add-to-cart by stock
                        IconButton(
                            onClick = onAddClick,
                            enabled = cartQty < product.stockQuantity,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (cartQty < product.stockQuantity) MaterialTheme.colorScheme.primary
                                    else Color.Gray.copy(alpha = 0.5f)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onAddClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Pilih",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartListItemRow(
    cartItem: CartItem,
    onAdd: () -> Unit,
    onSubtract: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.5f)) {
                Text(
                    text = cartItem.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Helper.formatRupiah(cartItem.product.sellPrice),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(0.5f)
            ) {
                IconButton(onClick = onSubtract) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Subtract", tint = MaterialTheme.colorScheme.primary)
                }

                Text(
                    text = cartItem.quantity.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(32.dp)
                )

                IconButton(
                    onClick = onAdd,
                    enabled = cartItem.quantity < cartItem.product.stockQuantity
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(onClick = onRemove) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ReceiptPriceRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isDiscount: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isDiscount) Color(0xFFD50000) else Color.Black
        )
        Text(
            text = value,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isDiscount) Color(0xFFD50000) else Color.Black
        )
    }
}
