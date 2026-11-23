package com.nutriagricola.app.ui.advisor.programs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.nutriagricola.app.ui.programs.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.ui.components.HeaderActions
import com.nutriagricola.app.ui.advisor.components.AdvisorBottomBar
import com.nutriagricola.app.ui.advisor.components.AdvisorBottomItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisorProgramsScreen(
    viewModel: ProgramsViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPendingClick: () -> Unit,
    onAllRecordsClick: () -> Unit,
    headerInitials: String = "", onLogoutClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val fullHeader = state.mode == ProgramMode.LIST || state.mode == ProgramMode.DETAIL
    Scaffold(
        topBar = {
            if (!fullHeader) TopAppBar(
                title = { Text("Programas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { if (!viewModel.back()) onBackClick() }) {
                        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "Volver",
                            modifier = Modifier.size(24.dp).rotate(180f))
                    }
                }
            )
        },
        bottomBar = {
            if (state.mode == ProgramMode.LIST) AdvisorBottomBar(
                selectedItem = AdvisorBottomItem.PROGRAM,
                onHomeClick = onHomeClick,
                onAllRecordsClick = onAllRecordsClick,
                onProgramClick = {}
            )
        }
    ) { padding ->
        ProgramsContent(viewModel, if (fullHeader)
            Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())
            else Modifier.fillMaxSize().padding(padding), onBack = { if (!viewModel.back()) onBackClick() },
            headerActions = { HeaderActions(headerInitials, onLogoutClick) })
    }
}
