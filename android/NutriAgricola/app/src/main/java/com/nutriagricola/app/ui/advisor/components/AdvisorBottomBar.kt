package com.nutriagricola.app.ui.advisor.components

import androidx.compose.runtime.Composable
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomBar
import com.nutriagricola.app.ui.farmer.home.components.FarmerBottomItem

enum class AdvisorBottomItem { HOME, ALL_RECORDS, PROGRAM, ACCOUNT }

@Composable
fun AdvisorBottomBar(
    selectedItem: AdvisorBottomItem,
    onHomeClick: () -> Unit,
    onAllRecordsClick: () -> Unit,
    onProgramClick: () -> Unit,
    onAccountClick: () -> Unit = {}
) {
    FarmerBottomBar(
        selectedItem = when (selectedItem) {
            AdvisorBottomItem.HOME -> FarmerBottomItem.HOME
            AdvisorBottomItem.ALL_RECORDS -> FarmerBottomItem.HISTORY
            AdvisorBottomItem.PROGRAM -> FarmerBottomItem.PROGRAM
            AdvisorBottomItem.ACCOUNT -> FarmerBottomItem.ACCOUNT
        },
        onHomeClick = onHomeClick, onHistoryClick = onAllRecordsClick,
        onProgramClick = onProgramClick, onAccountClick = onAccountClick,
        historyLabel = "Registros", programLabel = "Programas"
    )
}
