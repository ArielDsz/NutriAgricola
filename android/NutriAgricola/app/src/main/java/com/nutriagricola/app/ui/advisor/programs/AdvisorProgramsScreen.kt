package com.nutriagricola.app.ui.advisor.programs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Icon(painterResource(R.drawable.ic_program), contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            Text("Módulo en preparación", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Aquí podrás consultar y organizar los programas nutricionales de tus cultivos.",
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(onClick = onHomeClick) { Text("Volver al inicio") }
        }
    }
}
