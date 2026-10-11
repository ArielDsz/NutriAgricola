package com.nutriagricola.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nutriagricola.app.ui.farmer.home.FarmerHomeScreen
import com.nutriagricola.app.ui.login.LoginScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ){
        composable("login") {
            LoginScreen(
                onLoginClick = {
                    navController.navigate("farmer_home")
                }
            )
        }

        composable("farmer_home") {
            FarmerHomeScreen()
        }
    }
}