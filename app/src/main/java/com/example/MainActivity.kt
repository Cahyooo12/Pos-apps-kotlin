package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logic.PosViewModel
import com.example.presentation.screens.CashierScreen
import com.example.presentation.screens.CustomerScreen
import com.example.presentation.screens.DashboardScreen
import com.example.presentation.screens.InventoryScreen
import com.example.presentation.screens.LoginScreen
import com.example.presentation.screens.OnboardingScreen
import com.example.presentation.screens.ReportScreen
import com.example.presentation.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: PosViewModel = viewModel()
                
                // Reactive state triggers
                val isOnboardingDone by viewModel.onboardingCompleted.collectAsState()
                val isUserLoggedIn by viewModel.isLoggedIn.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        !isOnboardingDone -> {
                            OnboardingScreen(
                                onFinished = { viewModel.completeOnboarding() }
                            )
                        }
                        !isUserLoggedIn -> {
                            LoginScreen(
                                viewModel = viewModel
                            )
                        }
                        else -> {
                            MainNavigationFlow(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigationFlow(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        modifier = modifier
    ) {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToCashier = { navController.navigate("cashier") },
                onNavigateToInventory = { navController.navigate("inventory") },
                onNavigateToReports = { navController.navigate("reports") },
                onNavigateToCustomers = { navController.navigate("customers") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("cashier") {
            CashierScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("inventory") {
            InventoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("reports") {
            ReportScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("customers") {
            CustomerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
