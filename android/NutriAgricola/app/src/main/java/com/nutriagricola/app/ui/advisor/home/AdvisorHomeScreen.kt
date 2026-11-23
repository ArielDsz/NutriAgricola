package com.nutriagricola.app.ui.advisor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.ui.advisor.components.AdvisorBottomBar
import com.nutriagricola.app.ui.advisor.components.AdvisorBottomItem
import com.nutriagricola.app.ui.advisor.home.components.AdvisorKpiSection
import com.nutriagricola.app.ui.advisor.home.components.AdvisorQuickActions
import com.nutriagricola.app.ui.farmer.home.components.HomeHeader
import com.nutriagricola.app.ui.farmer.record.components.RecordHistoryCard

@Composable
fun AdvisorHomeScreen(
    onAccountClick: () -> Unit,
    user: User,
    viewModel: AdvisorHomeViewModel,
    onLogoutClick: () -> Unit,
    onPendingClick: () -> Unit,
    onAllRecordsClick: () -> Unit,
    onProgramClick: () -> Unit,
    onRecordClick: (String) -> Unit = {},
    refreshVersion: Int = 0
) {
    val initials = remember(user.firstName, user.lastName) {
        listOf(user.firstName, user.lastName)
            .mapNotNull { it.trim().firstOrNull()?.toString() }
            .joinToString("")
            .uppercase()
    }

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(refreshVersion) {
        viewModel.loadData()
    }

    Scaffold(
        bottomBar = {
            AdvisorBottomBar(
                selectedItem = AdvisorBottomItem.HOME,
                onHomeClick = {},
                onAllRecordsClick = onAllRecordsClick,
                onProgramClick = onProgramClick,
                onAccountClick = onAccountClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                HomeHeader(
                    initials = initials,
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(164.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 116.dp, start = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${user.firstName} ${user.lastName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Asesor agronómico",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }



            AdvisorKpiSection(
                pendingCount = state.pendingCount,
                totalRecordsCount = state.totalRecordsCount,
                totalFarmsCount = state.totalFarmsCount,
                onPendingClick = onPendingClick,
                onAllRecordsClick = onAllRecordsClick,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AdvisorQuickActions(
                pendingCount = state.pendingCount,
                onPendingClick = onPendingClick,
                onProgramClick = onProgramClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Solicitudes pendientes",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.pendingCount > 0) {
                        TextButton(onClick = onPendingClick) {
                            Text("Ver todas (${state.pendingCount})", style = MaterialTheme.typography.bodySmall)
                            Icon(
                                painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.recentPendingRecords.isEmpty() -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "✅ No tienes solicitudes pendientes por revisar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    else -> {
                        state.recentPendingRecords.forEach { item ->
                            RecordHistoryCard(
                                record = item.record,
                                cropName = "${item.cropName} • ${item.farmName}",
                                onClick = { onRecordClick(item.record.id) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                                showCropThumbnail = true,
                                  cropImagePath = item.cropImagePath
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
