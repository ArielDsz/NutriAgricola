package com.nutriagricola.app.ui.programs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomBar
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerProgramsScreen(viewModel: ProgramsViewModel, onBack: () -> Unit,
                          onHome: () -> Unit, onHistory: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val fullHeader = state.mode == ProgramMode.LIST || state.mode == ProgramMode.DETAIL
    Scaffold(topBar = {
        if (!fullHeader) TopAppBar(title = { Text("Programas") }, navigationIcon = {
            IconButton(onClick = { if (!viewModel.back()) onBack() }) {
                Icon(painterResource(R.drawable.ic_chevron_right), "Volver", Modifier.size(24.dp).rotate(180f))
            }
        })
    }, bottomBar = {
        if (state.mode == ProgramMode.LIST) FarmerBottomBar(FarmerBottomItem.PROGRAM,
            onHomeClick = onHome, onHistoryClick = onHistory, onProgramClick = {}, onFarmsClick = {})
    }) { padding ->
        ProgramsContent(viewModel, if (fullHeader)
            Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())
            else Modifier.fillMaxSize().padding(padding), onBack = { if (!viewModel.back()) onBack() })
    }
}
