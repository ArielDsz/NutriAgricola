package com.nutriagricola.app.ui.advisor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.nutriagricola.app.ui.farmer.home.components.HomeHeader
import com.nutriagricola.app.ui.farmer.home.components.SyncStatus
import com.nutriagricola.app.ui.farmer.record.components.RecordHistoryCard
import com.nutriagricola.app.ui.theme.ForestGreen

@Composable
fun AdvisorHomeScreen(
    user: User,
    viewModel: AdvisorHomeViewModel,
    onLogoutClick: () -> Unit,
    onPendingClick: () -> Unit,
    onAllRecordsClick: () -> Unit,
    onRecordClick: (String) -> Unit
) {
    val initials = remember(user.firstName, user.lastName) {
        listOf(user.firstName, user.lastName)
            .mapNotNull { it.trim().firstOrNull()?.toString() }
            .joinToString("")
            .uppercase()
    }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        bottomBar = {
            AdvisorBottomBar(
                selectedItem = AdvisorBottomItem.HOME,
                onHomeClick = {},
                onPendingClick = onPendingClick,
                onAllRecordsClick = onAllRecordsClick,
                onProgramClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // Header with overlapping Context Card
            Box(modifier = Modifier.fillMaxWidth()) {
                HomeHeader(
                    initials = initials,
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(164.dp)
                )

                // Advisor Profile / Summary Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 116.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${user.firstName} ${user.lastName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Asesor Agronómico",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${state.pendingCount} análisis pendientes de revisión",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Connection Status
            SyncStatus(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Main Actions Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Gestión de Asesoría",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Action 1: Pending Reviews
                AdvisorActionCard(
                    title = "Revisar Análisis Pendientes",
                    subtitle = "${state.pendingCount} muestras requieren tu dictamen",
                    iconRes = R.drawable.ic_history,
                    badgeText = if (state.pendingCount > 0) state.pendingCount.toString() else null,
                    onClick = onPendingClick
                )

                // Action 2: All Records & Farms
                AdvisorActionCard(
                    title = "Predios y Análisis Totales",
                    subtitle = "${state.totalFarmsCount} predios • ${state.pendingCount + state.reviewedCount} registros en total",
                    iconRes = R.drawable.ic_farms,
                    badgeText = null,
                    onClick = onAllRecordsClick
                )

                // Action 3: Nutritional Programs
                AdvisorActionCard(
                    title = "Programas Nutricionales",
                    subtitle = "Planes de fertilización y fertirriego",
                    iconRes = R.drawable.ic_program,
                    badgeText = null,
                    isUpcoming = true,
                    onClick = {}
                )
            }

            // Recent Pending List
            if (state.recentPendingRecords.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pendientes recientes",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onPendingClick) {
                            Text("Ver todos", style = MaterialTheme.typography.bodySmall)
                            Icon(
                                painter = painterResource(R.drawable.ic_chevron_right),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    state.recentPendingRecords.take(3).forEach { item ->
                        RecordHistoryCard(
                            record = item.record,
                            cropName = "${item.farmName} • ${item.cropName}",
                            onClick = { onRecordClick(item.record.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvisorActionCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    badgeText: String?,
    isUpcoming: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = !isUpcoming,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ForestGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = ForestGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isUpcoming) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Próximamente",
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (badgeText != null) {
                Badge(
                    containerColor = ForestGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else if (!isUpcoming) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
