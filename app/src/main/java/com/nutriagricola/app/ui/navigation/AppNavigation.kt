package com.nutriagricola.app.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nutriagricola.app.ui.advisor.home.AdvisorHomeScreen
import com.nutriagricola.app.ui.farmer.home.FarmerHomeScreen
import com.nutriagricola.app.ui.farmer.record.NewRecordScreen
import com.nutriagricola.app.ui.farmer.record.soil.SoilRecordScreen
import com.nutriagricola.app.ui.farmer.record.soil.SoilRecordViewModel
import com.nutriagricola.app.ui.farmer.record.soil.SoilRecordViewModelFactory
import com.nutriagricola.app.ui.login.LoginScreen
import com.nutriagricola.app.ui.login.LoginViewModel
import com.nutriagricola.app.ui.login.LoginViewModelFactory
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.seedDatabase
import com.nutriagricola.app.data.repository.AuthRepository
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.SessionRepository
import com.nutriagricola.app.data.repository.UserRepository
import com.nutriagricola.app.domain.model.user.UserRole
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.ui.session.SessionViewModel
import com.nutriagricola.app.ui.session.SessionViewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AppNavigation() {

    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val sessionFactory = remember(database) {
        SessionViewModelFactory(
            SessionRepository(context, UserRepository(database.userDao()))
        )
    }
    // This ViewModel belongs to the Activity, outside individual navigation destinations.
    val sessionViewModel: SessionViewModel = viewModel(factory = sessionFactory)
    val sessionState by sessionViewModel.uiState.collectAsState()
    var isDatabaseReady by remember { mutableStateOf(false) }
    var initializationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(database) {
        try {
            withContext(Dispatchers.IO) {
                seedDatabase(database)
            }
            isDatabaseReady = true
            sessionViewModel.restoreSession()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            initializationError = "No se pudieron preparar los datos locales. Reinicia la aplicación."
        }
    }

    if (!isDatabaseReady || !sessionState.isRestored) {
        val error = initializationError ?: sessionState.error
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (error == null) {
                CircularProgressIndicator()
                Text("Preparando datos locales y sesión…")
            } else {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        return
    }

    val loginFactory = remember(database) {
        LoginViewModelFactory(
            AuthRepository(
                userRepository = UserRepository(database.userDao()),
                userCredentialDao = database.userCredentialDao()
            )
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(sessionState.error) {
        sessionState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            sessionViewModel.clearError()
        }
    }

    val currentUser = sessionState.currentUser
    Box(modifier = Modifier.fillMaxSize()) {
        // A different session gets a fresh controller, discarding the previous back stack.
        key(currentUser?.id) {
            val navController = rememberNavController()
            val startDestination = when (currentUser?.role) {
                UserRole.FARMER -> "farmer_home"
                UserRole.ADVISOR -> "advisor_home"
                else -> "login"
            }

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                if (currentUser == null) {
                    composable("login") {
                        val loginViewModel: LoginViewModel = viewModel(factory = loginFactory)
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = { user -> sessionViewModel.createSession(user) }
                        )
                    }
                } else if (currentUser.role == UserRole.ADVISOR) {
                    composable("advisor_home") {
                        AdvisorHomeScreen()
                    }
                } else {
                    composable("farmer_home") {
                        FarmerHomeScreen(
                            user = currentUser,
                            onLogoutClick = { sessionViewModel.logout() },
                            onNewRecordClick = { farmId, farmName ->
                                navController.navigate(
                                    "new_record/$farmId/${Uri.encode(farmName)}"
                                )
                            }
                        )
                    }

                    composable("new_record/{farmId}/{farmName}") { backStackEntry ->
                        val farmId = backStackEntry.arguments?.getString("farmId")
                            ?: return@composable
                        val farmName = backStackEntry.arguments?.getString("farmName") ?: ""

                        NewRecordScreen(
                            farmId = farmId,
                            farmName = farmName,
                            onBackClick = { navController.popBackStack() },
                            onContinueClick = { cropId, type ->
                                if (type == RecordType.SOIL) {
                                    navController.navigate("soil_record/${Uri.encode(cropId)}")
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            "El formulario seleccionado aún no está disponible"
                                        )
                                    }
                                }
                            }
                        )
                    }

                    composable("soil_record/{cropId}") { backStackEntry ->
                        val cropId = backStackEntry.arguments?.getString("cropId")
                            ?: return@composable
                        val soilFactory = remember(cropId, database) {
                            SoilRecordViewModelFactory(
                                cropId = cropId,
                                cropRepository = CropRepository(database.cropDao())
                            )
                        }
                        val soilViewModel: SoilRecordViewModel = viewModel(factory = soilFactory)

                        SoilRecordScreen(
                            viewModel = soilViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
                .navigationBarsPadding().padding(16.dp)
        )
    }

    if (sessionState.isBusy) {
        Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Actualizando sesión…")
                }
            }
        }
    }
}
