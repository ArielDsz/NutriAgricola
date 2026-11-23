package com.nutriagricola.app.ui.advisor.components

import androidx.compose.runtime.Composable
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomBar
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomItem

enum class AdvisorBottomItem { HOME, ALL_RECORDS, PROGRAM, FARMS }

@Composable
fun AdvisorBottomBar(
    selectedItem: AdvisorBottomItem,
    onHomeClick: () -> Unit,
    onAllRecordsClick: () -> Unit,
    onProgramClick: () -> Unit,
    onFarmsClick: () -> Unit = {}
) {
    FarmerBottomBar(
        selectedItem = when (selectedItem) {
            AdvisorBottomItem.HOME -> FarmerBottomItem.HOME
            AdvisorBottomItem.ALL_RECORDS -> FarmerBottomItem.HISTORY
            AdvisorBottomItem.PROGRAM -> FarmerBottomItem.PROGRAM
            AdvisorBottomItem.FARMS -> FarmerBottomItem.FARMS
        },
        onHomeClick = onHomeClick, onHistoryClick = onAllRecordsClick,
        onProgramClick = onProgramClick, onFarmsClick = onFarmsClick,
        historyLabel = "Registros", programLabel = "Programas"
    )
}
