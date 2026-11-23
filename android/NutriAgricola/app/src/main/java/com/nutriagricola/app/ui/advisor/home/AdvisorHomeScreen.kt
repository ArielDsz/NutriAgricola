package com.nutriagricola.app.ui.advisor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.ui.advisor.components.AdvisorBottomBar
import com.nutriagricola.app.ui.advisor.components.AdvisorBottomItem
import com.nutriagricola.app.ui.farmer.home.components.HomeHeader
import com.nutriagricola.app.ui.farmer.home.components.SyncStatus

@Composable
fun AdvisorHomeScreen(
    user: User,
    onLogoutClick: () -> Unit,
    onPendingClick: () -> Unit,
    onAllRecordsClick: () -> Unit,
    onProgramClick: () -> Unit
) {
    val initials = remember(user.firstName, user.lastName) {
        listOf(user.firstName, user.lastName)
            .mapNotNull { it.trim().firstOrNull()?.toString() }
            .joinToString("")
            .uppercase()
    }

    Scaffold(
        bottomBar = {
            AdvisorBottomBar(
                selectedItem = AdvisorBottomItem.HOME,
                onHomeClick = {},
                onAllRecordsClick = onAllRecordsClick,
                onProgramClick = onProgramClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                HomeHeader(
                    initials = initials,
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth().height(164.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 116.dp, start = 16.dp, end = 16.dp),
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
                            modifier = Modifier.size(52.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_check), contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.firstName + " " + user.lastName,
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Asesor agronómico", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            SyncStatus(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Stack the cards on narrow screens or when larger text needs more space.
                if (maxWidth < 320.dp || LocalDensity.current.fontScale > 1.3f) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdvisorModuleCard("Registros",
                            R.drawable.ic_history, onAllRecordsClick, Modifier.fillMaxWidth())
                        AdvisorModuleCard("Programas",
                            R.drawable.ic_program, onProgramClick, Modifier.fillMaxWidth())
                    }
                } else {
                    Row(modifier = Modifier.height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdvisorModuleCard("Registros",
                            R.drawable.ic_history, onAllRecordsClick, Modifier.weight(1f).fillMaxHeight())
                        AdvisorModuleCard("Programas",
                            R.drawable.ic_program, onProgramClick, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvisorModuleCard(
    title: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 168.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 168.dp).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(iconRes), contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
