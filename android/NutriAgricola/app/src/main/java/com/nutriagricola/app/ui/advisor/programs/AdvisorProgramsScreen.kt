package com.nutriagricola.app.ui.advisor.programs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.ui.advisor.components.AdvisorBottomBar
import com.nutriagricola.app.ui.advisor.components.AdvisorBottomItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisorProgramsScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onPendingClick: () -> Unit,
    onAllRecordsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Programas nutricionales", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "Volver",
                            modifier = Modifier.size(24.dp).rotate(180f))
                    }
                }
            )
        },
        bottomBar = {
            AdvisorBottomBar(
                selectedItem = AdvisorBottomItem.PROGRAM,
                onHomeClick = onHomeClick,
                onPendingClick = onPendingClick,
                onAllRecordsClick = onAllRecordsClick,
                onProgramClick = {}
            )
        }
    ) { padding ->
        ProgramCalculatorForm(Modifier.fillMaxSize().padding(padding))
    }
}
