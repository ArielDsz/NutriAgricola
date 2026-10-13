package com.nutriagricola.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nutriagricola.app.ui.advisor.home.AdvisorHomeScreen
import com.nutriagricola.app.ui.farmer.home.FarmerHomeScreen
import com.nutriagricola.app.ui.farmer.record.NewRecordScreen
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
                onFarmerLogin = {
                    navController.navigate("farmer_home")
                },
                onAdvisorLogin = {
                    navController.navigate("advisor_home")
                }
            )
        }

        composable("farmer_home") {
            FarmerHomeScreen(
                onNewRecordClick = { farmId, farmName ->
                    navController.navigate(
                        "new_record/$farmId/${Uri.encode(farmName)}"
                    )
                }
            )
        }

        composable("advisor_home") {
            AdvisorHomeScreen()
        }

        composable(
            route = "new_record/{farmId}/{farmName}"
        ) { backStackEntry ->

            val farmId =
                backStackEntry.arguments?.getString("farmId") ?: return@composable

            val farmName =
                backStackEntry.arguments?.getString("farmName") ?: ""

            NewRecordScreen(
                farmId = farmId,
                farmName = farmName,
                onBackClick = {
                    navController.popBackStack()
                },
                onContinueClick = {
                    // después navegaremos al formulario correspondiente
                }
            )
        }
    }
}