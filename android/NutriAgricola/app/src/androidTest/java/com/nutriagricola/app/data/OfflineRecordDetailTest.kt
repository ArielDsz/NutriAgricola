package com.nutriagricola.app.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.mapper.toEntity
import com.nutriagricola.app.data.remote.ApiClient
import com.nutriagricola.app.data.remote.dto.*
import com.nutriagricola.app.data.repository.*
import com.nutriagricola.app.domain.model.farm.Farm
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.domain.model.user.UserRole
import com.nutriagricola.app.ui.farmer.FarmerContextUiState
import com.nutriagricola.app.ui.farmer.record.detail.RecordDetailScreen
import com.nutriagricola.app.ui.farmer.record.detail.RecordDetailViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class OfflineRecordDetailTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun cachedMeasurementsAndAdvisorCommentsRemainVisibleWhenFarmRefreshFails(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(context.cacheDir, "offline-ui-${UUID.randomUUID()}.preferences_pb")
        val store = PreferenceDataStoreFactory.create(scope = scope) { file }
        try {
            val user = User("farmer", "Ana", "Prueba", "farmer@example.test", UserRole.FARMER, true)
            val farm = Farm(id = "farm", name = "Predio de prueba", location = null, active = true)
            db.userDao().insert(user.toEntity())
            db.farmDao().insert(farm.toEntity())
            db.cropDao().insert(com.nutriagricola.app.data.local.entity.CropEntity("crop", "farm", "Palto", null, null, true))
            val records = RecordRepository(db)
            records.cacheRemoteRecord(RecordDetailDto("remote", "crop", "farmer", "2026-11-20", "SOIL", "PENDING",
                soilData = SoilDataDto(ph = 6.5), reviews = listOf(ReviewResponseDto("review", "advisor", "Asesor Prueba",
                    "2026-11-22T12:00:00", "OBSERVED", "Comentario del asesor descargado"))))
            val api = ApiClient()
            val sessions = SessionRepository(store, UserRepository(db.userDao()), api.authApi)
            val vm = RecordDetailViewModel("remote", records, RecordReviewRepository(db),
                CropRepository(db.cropDao(), api.cropApi, sessions), { user }, flowOf(farm))
            compose.setContent {
                MaterialTheme {
                    RecordDetailScreen(vm, FarmerContextUiState(farms = listOf(farm), selectedFarm = farm,
                        isLoading = false, error = "Sin conexión al servicio"), onBackClick = {})
                }
            }
            compose.waitUntil(5_000) { !vm.uiState.value.isLoading }
            compose.onNodeWithText("Datos del análisis").assertIsDisplayed()
            compose.onNodeWithText("Comentario del asesor descargado").performScrollTo().assertIsDisplayed()
        } finally {
            scope.cancel()
            scope.coroutineContext[Job]?.join()
            db.close()
            file.delete()
        }
    }
}
