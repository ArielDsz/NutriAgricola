package com.nutriagricola.app.ui.farmer.home.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R

enum class FarmerBottomItem {
    HOME,
    HISTORY,
    PROGRAM,
    ACCOUNT
}

@Composable
fun FarmerBottomBar(
    selectedItem: FarmerBottomItem,
    onHomeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProgramClick: () -> Unit,
    onAccountClick: () -> Unit,
    historyLabel: String = "Historial",
    programLabel: String = "Programa"
) {
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Surface(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), shadowElevation = 4.dp) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            NavigationBarItem(
                selected = selectedItem == FarmerBottomItem.HOME,
                onClick = onHomeClick,
                colors = itemColors,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_home),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text("Inicio", style = MaterialTheme.typography.bodySmall)
                }
            )

            NavigationBarItem(
                selected = selectedItem == FarmerBottomItem.HISTORY,
                onClick = onHistoryClick,
                colors = itemColors,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_history),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(historyLabel, style = MaterialTheme.typography.bodySmall)
                }
            )

            NavigationBarItem(
                selected = selectedItem == FarmerBottomItem.PROGRAM,
                onClick = onProgramClick,
                colors = itemColors,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_program),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(programLabel, style = MaterialTheme.typography.bodySmall)
                }
            )

            NavigationBarItem(
                selected = selectedItem == FarmerBottomItem.ACCOUNT,
                onClick = onAccountClick,
                colors = itemColors,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_account),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text("Cuenta", style = MaterialTheme.typography.bodySmall)
                }
            )
        }
    }
}
