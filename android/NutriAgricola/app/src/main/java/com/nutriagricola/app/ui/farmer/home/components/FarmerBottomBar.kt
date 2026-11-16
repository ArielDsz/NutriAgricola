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
    FARMS
}

@Composable
fun FarmerBottomBar(
    selectedItem: FarmerBottomItem,
    onHomeClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProgramClick: () -> Unit,
    onFarmsClick: () -> Unit
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
                    Text("Historial", style = MaterialTheme.typography.bodySmall)
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
                    Text("Programa", style = MaterialTheme.typography.bodySmall)
                }
            )

            NavigationBarItem(
                selected = selectedItem == FarmerBottomItem.FARMS,
                onClick = onFarmsClick,
                colors = itemColors,
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_farms),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text("Predios", style = MaterialTheme.typography.bodySmall)
                }
            )
        }
    }
}
