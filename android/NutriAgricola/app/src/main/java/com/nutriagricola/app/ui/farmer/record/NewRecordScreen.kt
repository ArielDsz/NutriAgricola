package com.nutriagricola.app.ui.farmer.record

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.nutriagricola.app.R
import com.nutriagricola.app.data.local.database.AppDatabase
import com.nutriagricola.app.data.repository.CropRepository
import com.nutriagricola.app.domain.model.farm.Crop
import com.nutriagricola.app.domain.model.record.RecordType
import com.nutriagricola.app.ui.farmer.record.components.CropSelectorBottomSheet
import com.nutriagricola.app.ui.farmer.record.components.RecordTypeCard

@Composable
fun NewRecordScreen(
    farmId: String,
    farmName: String,
    onBackClick: () -> Unit,
    onContinueClick: (String, RecordType) -> Unit,
    farmImagePath: String? = null
) {

    val context = LocalContext.current

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val cropRepository = remember {
        CropRepository(database.cropDao())
    }

    val factory = remember {
        NewRecordViewModelFactory(cropRepository)
    }

    val viewModel: NewRecordViewModel = viewModel(
        factory = factory
    )

    val crops by viewModel.crops.collectAsState()
    val selectedCrop by viewModel.selectedCrop.collectAsState()

    var showCropSelector by remember(farmId) {
        mutableStateOf(false)
    }

    val selectedType by viewModel.selectedType.collectAsState()

    LaunchedEffect(farmId) {
        viewModel.loadCrops(farmId)
    }

    if (showCropSelector) {
        CropSelectorBottomSheet(
            crops = crops,
            selectedCropId = selectedCrop?.id,
            onDismiss = { showCropSelector = false },
            onCropSelected = { crop ->
                viewModel.selectCrop(crop)
                showCropSelector = false
            }
        )
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                NewRecordHeader(onBackClick)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 120.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RecordSelectionImage(farmImagePath, "Imagen del predio $farmName", Modifier.size(56.dp))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Predio actual", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(farmName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cultivo", style = MaterialTheme.typography.bodyLarge)
                    CropSelectionCard(selectedCrop, onClick = { showCropSelector = true })
                }

                AnimatedVisibility(
                    visible = selectedCrop != null,
                    enter = expandVertically(animationSpec = tween(280), expandFrom = Alignment.Top) +
                        fadeIn(animationSpec = tween(200)),
                    exit = shrinkVertically(animationSpec = tween(200), shrinkTowards = Alignment.Top) +
                        fadeOut(animationSpec = tween(120))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Tipo de registro", style = MaterialTheme.typography.bodyLarge)
                        RecordTypeCard(
                            title = "Análisis de suelo",
                            description = "pH, CE, nutrientes y materia orgánica",
                            imageRes = R.drawable.analysis_soil,
                            selected = selectedType == RecordType.SOIL,
                            onClick = { viewModel.selectType(RecordType.SOIL) }
                        )
                        RecordTypeCard(
                            title = "Análisis de agua",
                            description = "pH, CE, nutrientes y fuente de agua",
                            imageRes = R.drawable.analysis_water,
                            selected = selectedType == RecordType.WATER,
                            onClick = { viewModel.selectType(RecordType.WATER) }
                        )
                        RecordTypeCard(
                            title = "Análisis foliar",
                            description = "Nutrientes, síntomas y fotografías",
                            imageRes = R.drawable.analysis_foliar,
                            selected = selectedType == RecordType.FOLIAR,
                            onClick = { viewModel.selectType(RecordType.FOLIAR) }
                        )
                    }
                }

                val canContinue = selectedCrop != null && selectedType != null
                Button(
                    onClick = {
                        selectedCrop?.let { crop ->
                            selectedType?.let { type -> onContinueClick(crop.id, type) }
                        }
                    },
                    enabled = canContinue,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.verticalGradient(
                                if (canContinue) listOf(
                                    lerp(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, 0.3f),
                                    MaterialTheme.colorScheme.primary
                                ) else listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                            ))
                            .heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Continuar", style = MaterialTheme.typography.bodyLarge)
                        Icon(painterResource(R.drawable.ic_chevron_right), null,
                            modifier = Modifier.padding(start = 10.dp).size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewRecordHeader(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(160.dp)
        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))) {
        Image(
            painterResource(R.drawable.header_background), contentDescription = null,
            contentScale = ContentScale.Crop, alignment = Alignment.TopCenter,
            modifier = Modifier.matchParentSize()
        )
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.background.copy(alpha = 0.94f)) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                    Icon(painterResource(R.drawable.ic_chevron_right), "Volver",
                        modifier = Modifier.size(28.dp).rotate(180f), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Nuevo registro", style = MaterialTheme.typography.titleLarge)
                Text("Selecciona el cultivo y tipo de registro", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CropSelectionCard(crop: Crop?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).heightIn(min = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (crop != null) {
                RecordSelectionImage(crop.imagePath, "Imagen de ${crop.name}", Modifier.size(64.dp))
            } else {
                Image(painterResource(R.drawable.analysis_foliar), null, modifier = Modifier.size(48.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(crop?.name ?: "Seleccionar cultivo", style = MaterialTheme.typography.bodyLarge)
                crop?.variety?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(painterResource(R.drawable.ic_expand_more), "Seleccionar cultivo",
                modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun RecordSelectionImage(imagePath: String?, description: String, modifier: Modifier) {
    val fallback = painterResource(R.drawable.default_crop)
    AsyncImage(
        model = imagePath?.takeIf { it.isNotBlank() },
        contentDescription = description,
        placeholder = fallback, fallback = fallback, error = fallback,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(10.dp))
    )
}
