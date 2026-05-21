package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Customer
import com.example.logic.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    viewModel: PosViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    // Dialog form controls
    var showCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomerForEdit by remember { mutableStateOf<Customer?>(null) }

    var formName by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    var formEmail by remember { mutableStateOf("") }
    var formAddress by remember { mutableStateOf("") }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    fun openAddCustomer() {
        selectedCustomerForEdit = null
        formName = ""
        formPhone = ""
        formEmail = ""
        formAddress = ""
        showCustomerDialog = true
    }

    fun openEditCustomer(customer: Customer) {
        selectedCustomerForEdit = customer
        formName = customer.name
        formPhone = customer.phone
        formEmail = customer.email
        formAddress = customer.address
        showCustomerDialog = true
    }

    fun saveCustomer() {
        if (formName.isBlank()) {
            Toast.makeText(context, "Nama pelanggan wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveCustomer(
            id = selectedCustomerForEdit?.id ?: 0,
            name = formName,
            phone = formPhone,
            email = formEmail,
            address = formAddress
        )

        Toast.makeText(context, "Data pelanggan disimpan!", Toast.LENGTH_SHORT).show()
        showCustomerDialog = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database Pelanggan", fontWeight = FontWeight.Bold) },
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
                onClick = { openAddCustomer() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Pelanggan")
            }
        },
        modifier = Modifier.testTag("customer_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Row
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari pelanggan berdasarkan nama/telepon...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("customer_search_input")
            )

            // Customers list
            if (filteredCustomers.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Data Pelanggan Kosong",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tautkan nota belanja pelanggan untuk rekapan penjualan yg optimal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers) { cust ->
                        CustomerListItem(
                            customer = cust,
                            onEdit = { openEditCustomer(cust) },
                            onDelete = {
                                viewModel.deleteCustomer(cust.id)
                                Toast.makeText(context, "Pelanggan ${cust.name} dihapus", Toast.LENGTH_SHORT).show()
                            },
                            onWhatsAppMessage = {
                                Toast.makeText(context, "Mengirim pesan WA promosi ke ${cust.name} (${cust.phone})...", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            }
        }

        // Add / Edit Dialog
        if (showCustomerDialog) {
            AlertDialog(
                onDismissRequest = { showCustomerDialog = false },
                title = {
                    Text(
                        text = if (selectedCustomerForEdit == null) "Tulis Pelanggan Baru" else "Edit Profil Pelanggan",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = formName,
                            onValueChange = { formName = it },
                            label = { Text("Nama Lengkap") },
                            placeholder = { Text("Contoh: H. Akbar") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("customer_form_name")
                        )
                        OutlinedTextField(
                            value = formPhone,
                            onValueChange = { formPhone = it },
                            label = { Text("Nomor HP (WhatsApp)") },
                            placeholder = { Text("Contoh: 0812xxxxxxxx") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("customer_form_phone")
                        )
                        OutlinedTextField(
                            value = formEmail,
                            onValueChange = { formEmail = it },
                            label = { Text("Email (Opsional)") },
                            placeholder = { Text("Contoh: akbar@email.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = formAddress,
                            onValueChange = { formAddress = it },
                            label = { Text("Alamat Tempat Tinggal") },
                            placeholder = { Text("Tulis RT/RW, Kota tinggal...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { saveCustomer() },
                        modifier = Modifier.testTag("save_customer_button")
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomerDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun CustomerListItem(
    customer: Customer,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onWhatsAppMessage: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = customer.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "WA: ${customer.phone}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (customer.address.isNotBlank()) {
                        Text(
                            text = customer.address,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }

            // Subsystem actions row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onWhatsAppMessage) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF25D366), // WhatsApp Green
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
