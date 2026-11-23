package com.nutriagricola.app.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.domain.model.user.UserRole
import com.nutriagricola.app.ui.components.HeaderActions
import com.nutriagricola.app.ui.components.SectionHeader
import com.nutriagricola.app.ui.components.userInitials

@Composable
fun AccountScreen(user: User, onLogoutClick: () -> Unit, bottomBar: @Composable () -> Unit) {
    Scaffold(bottomBar = bottomBar) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SectionHeader("Cuenta") { HeaderActions(userInitials(user), onLogoutClick) } }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(80.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(userInitials(user), style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text("${user.firstName} ${user.lastName}".trim(),
                            style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(if (user.role == UserRole.ADVISOR) "Asesor" else "Agricultor",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Información personal", style = MaterialTheme.typography.titleMedium)
                        AccountField("Nombre", user.firstName)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        AccountField("Apellido", user.lastName)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_email), null, Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary)
                            AccountField("Correo electrónico", user.email, Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                OutlinedButton(onClick = onLogoutClick, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 52.dp)) {
                    Text("Cerrar sesión", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun AccountField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
