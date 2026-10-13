package com.nutriagricola.app.ui.farmer.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.local.seedDatabase
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.data.repository.FarmRepository
import com.nutriagricola.app.ui.farmer.home.components.CropsSection
import com.nutriagricola.app.ui.farmer.home.components.EmptyFarmCard
import com.nutriagricola.app.ui.farmer.home.components.FarmCard
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomBar
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomItem
import com.nutriagricola.app.ui.farmer.home.components.HomeHeader
import com.nutriagricola.app.ui.farmer.home.components.SyncStatus

@Composable
fun FarmerHomeScreen(
    onNewRecordClick: (String, String) -> Unit
) {

    val context = LocalContext.current

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val farmRepository = remember { FarmRepository(database.farmDao()) }

    val cropRepository = remember { CropRepository(database.cropDao()) }

    val factory = remember {
        FarmerHomeViewModelFactory(farmRepository, cropRepository)
    }

    val viewModel: FarmerHomeViewModel = viewModel(
        factory = factory
    )

    val farms by viewModel.farms.collectAsState()
    val crops by viewModel.crops.collectAsState()

    LaunchedEffect(Unit) {
        seedDatabase(database)
        viewModel.loadFarms()
    }

    LaunchedEffect(farms) {
        farms.firstOrNull()?.let { farm ->
            viewModel.loadCrops(farm.id)
        }
    }

    Scaffold(
        bottomBar = {
            FarmerBottomBar(
                selectedItem = FarmerBottomItem.HOME,
                onHomeClick = { },
                onHistoryClick = { },
                onProgramClick = { },
                onFarmsClick = { }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {

                HomeHeader(
                    modifier = Modifier.height(150.dp)
                )

                if (farms.isNotEmpty()) {
                    FarmCard(
                        farm = farms.first(),
                        cropCount = crops.size,
                        onClick = { },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                } else {
                    EmptyFarmCard(
                        onClick = { },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            SyncStatus(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            CropsSection(
                crops = crops,
                onAddClick = {
                    // Después abriremos agregar cultivo
                },
                onCropClick = { crop ->
                    // Después abriremos el detalle de este cultivo
                }
            )

            Button(
                onClick = {
                    farms.firstOrNull()?.let { farm ->
                        onNewRecordClick(farm.id, farm.name)
                    }
                },
                enabled = farms.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Nuevo registro",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}