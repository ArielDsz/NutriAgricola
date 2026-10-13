package com.nutriagricola.app.ui.farmer.home.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
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
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = selectedItem == FarmerBottomItem.HOME,
            onClick = onHomeClick,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary
            ),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_home),
                    contentDescription = "Inicio"
                )
            },
            label = {
                Text("Inicio")
            }
        )

        NavigationBarItem(
            selected = selectedItem == FarmerBottomItem.HISTORY,
            onClick = onHistoryClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_history),
                    contentDescription = "Historial"
                )
            },
            label = {
                Text("Historial")
            }
        )

        NavigationBarItem(
            selected = selectedItem == FarmerBottomItem.PROGRAM,
            onClick = onProgramClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_program),
                    contentDescription = "Programa"
                )
            },
            label = {
                Text("Programa")
            }
        )

        NavigationBarItem(
            selected = selectedItem == FarmerBottomItem.FARMS,
            onClick = onFarmsClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_farms),
                    contentDescription = "Predios"
                )
            },
            label = {
                Text("Predios")
            }
        )
    }
}