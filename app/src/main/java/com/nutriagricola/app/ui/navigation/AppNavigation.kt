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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nutriagricola.app.ui.advisor.home.AdvisorHomeScreen
import com.nutriagricola.app.ui.farmer.FarmerContextViewModel
import com.nutriagricola.app.ui.farmer.FarmerContextViewModelFactory
import com.nutriagricola.app.ui.farmer.history.FarmerHistoryScreen
import com.nutriagricola.app.ui.farmer.history.FarmerHistoryViewModel
import com.nutriagricola.app.ui.farmer.history.FarmerHistoryViewModelFactory
import com.nutriagricola.app.ui.farmer.home.FarmerHomeViewModel
import com.nutriagricola.app.ui.farmer.home.FarmerHomeViewModelFactory
import com.nutriagricola.app.ui.farmer.home.FarmerHomeScreen
import com.nutriagricola.app.ui.farmer.record.NewRecordScreen
import com.nutriagricola.app.ui.farmer.record.soil.SoilRecordScreen
import com.nutriagricola.app.ui.farmer.record.soil.SoilRecordViewModel
import com.nutriagricola.app.ui.farmer.record.soil.SoilRecordViewModelFactory
import com.nutriagricola.app.ui.farmer.record.water.WaterRecordScreen
import com.nutriagricola.app.ui.farmer.record.water.WaterRecordViewModel
import com.nutriagricola.app.ui.farmer.record.water.WaterRecordViewModelFactory
import com.nutriagricola.app.ui.farmer.record.foliar.FoliarRecordScreen
import com.nutriagricola.app.ui.farmer.record.foliar.FoliarRecordViewModel
import com.nutriagricola.app.ui.farmer.record.foliar.FoliarRecordViewModelFactory
import com.nutriagricola.app.ui.login.LoginScreen
import com.nutriagricola.app.ui.login.LoginViewModel
import com.nutriagricola.app.ui.login.LoginViewModelFactory
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.seedDatabase
import com.nutriagricola.app.data.repository.AuthRepository
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.data.repository.RecordRepository
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
    val cropRepository = remember(database) { CropRepository(database.cropDao()) }
    val recordRepository = remember(database) { RecordRepository(database) }
    val farmerContextFactory = remember(database) {
        FarmerContextViewModelFactory(FarmRepository(database.farmDao()))
    }
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
            val onRecordSaved: () -> Unit = {
                navController.popBackStack("farmer_home", inclusive = false)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Registro guardado")
                }
            }
            val startDestination = when (currentUser?.role) {
                UserRole.FARMER -> "farmer"
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
                    navigation(startDestination = "farmer_home", route = "farmer") {
                        composable("farmer_home") { backStackEntry ->
                            val farmerContext = farmerContextViewModel(
                                navController, backStackEntry, farmerContextFactory
                            )
                            val contextState by farmerContext.uiState.collectAsState()
                            val homeFactory = remember(currentUser.id, farmerContext, cropRepository, recordRepository) {
                                FarmerHomeViewModelFactory(
                                    userId = currentUser.id,
                                    selectedFarm = farmerContext.selectedFarm,
                                    cropRepository = cropRepository,
                                    recordRepository = recordRepository
                                )
                            }
                            val homeViewModel: FarmerHomeViewModel = viewModel(factory = homeFactory)

                            FarmerHomeScreen(
                                user = currentUser,
                                viewModel = homeViewModel,
                                contextState = contextState,
                                onLogoutClick = { sessionViewModel.logout() },
                                onHistoryClick = {
                                    navController.navigate("farmer_history") {
                                        launchSingleTop = true
                                    }
                                },
                                onNewRecordClick = { farmId, farmName ->
                                    navController.navigate(
                                        "new_record/$farmId/${Uri.encode(farmName)}"
                                    )
                                }
                            )
                        }

                        composable("farmer_history") { backStackEntry ->
                            val farmerContext = farmerContextViewModel(
                                navController, backStackEntry, farmerContextFactory
                            )
                            val contextState by farmerContext.uiState.collectAsState()
                            val historyFactory = remember(currentUser.id, farmerContext, cropRepository, recordRepository) {
                                FarmerHistoryViewModelFactory(
                                    userId = currentUser.id,
                                    selectedFarm = farmerContext.selectedFarm,
                                    cropRepository = cropRepository,
                                    recordRepository = recordRepository
                                )
                            }
                            val historyViewModel: FarmerHistoryViewModel = viewModel(factory = historyFactory)

                            FarmerHistoryScreen(
                                viewModel = historyViewModel,
                                contextState = contextState,
                                onHomeClick = {
                                    navController.popBackStack("farmer_home", inclusive = false)
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
                                    val route = when (type) {
                                        RecordType.SOIL -> "soil_record"
                                        RecordType.WATER -> "water_record"
                                        RecordType.FOLIAR -> "foliar_record"
                                    }
                                    navController.navigate("$route/${Uri.encode(cropId)}")
                                }
                            )
                        }

                        composable("soil_record/{cropId}") { backStackEntry ->
                            val cropId = backStackEntry.arguments?.getString("cropId")
                                ?: return@composable
                            val soilFactory = remember(cropId, cropRepository, recordRepository, sessionViewModel) {
                                SoilRecordViewModelFactory(
                                    cropId = cropId,
                                    cropRepository = cropRepository,
                                    recordRepository = recordRepository,
                                    getCurrentUser = { sessionViewModel.uiState.value.currentUser }
                                )
                            }
                            val soilViewModel: SoilRecordViewModel = viewModel(factory = soilFactory)

                            SoilRecordScreen(
                                viewModel = soilViewModel,
                                onBackClick = { navController.popBackStack() },
                                onSaved = onRecordSaved
                            )
                        }

                        composable("water_record/{cropId}") { backStackEntry ->
                            val cropId = backStackEntry.arguments?.getString("cropId")
                                ?: return@composable
                            val waterFactory = remember(cropId, cropRepository, recordRepository, sessionViewModel) {
                                WaterRecordViewModelFactory(
                                    cropId = cropId,
                                    cropRepository = cropRepository,
                                    recordRepository = recordRepository,
                                    getCurrentUser = { sessionViewModel.uiState.value.currentUser }
                                )
                            }
                            val waterViewModel: WaterRecordViewModel = viewModel(factory = waterFactory)

                            WaterRecordScreen(
                                viewModel = waterViewModel,
                                onBackClick = { navController.popBackStack() },
                                onSaved = onRecordSaved
                            )
                        }

                        composable("foliar_record/{cropId}") { backStackEntry ->
                            val cropId = backStackEntry.arguments?.getString("cropId")
                                ?: return@composable
                            val foliarFactory = remember(cropId, cropRepository, recordRepository, sessionViewModel) {
                                FoliarRecordViewModelFactory(
                                    cropId = cropId,
                                    cropRepository = cropRepository,
                                    recordRepository = recordRepository,
                                    getCurrentUser = { sessionViewModel.uiState.value.currentUser }
                                )
                            }
                            val foliarViewModel: FoliarRecordViewModel = viewModel(factory = foliarFactory)

                            FoliarRecordScreen(
                                viewModel = foliarViewModel,
                                onBackClick = { navController.popBackStack() },
                                onSaved = onRecordSaved
                            )
                        }
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

@Composable
private fun farmerContextViewModel(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
    factory: FarmerContextViewModelFactory
): FarmerContextViewModel {
    val farmerEntry = remember(navController, backStackEntry) {
        navController.getBackStackEntry("farmer")
    }
    return viewModel(viewModelStoreOwner = farmerEntry, factory = factory)
}
