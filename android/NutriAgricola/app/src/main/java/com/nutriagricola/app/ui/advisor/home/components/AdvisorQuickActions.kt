package com.nutriagricola.app.ui.advisor.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R

@Composable
fun AdvisorQuickActions(pendingCount: Int, onPendingClick: () -> Unit,
                        onProgramClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onPendingClick, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(painterResource(R.drawable.ic_history), null, Modifier.size(22.dp))
                Text("Atender solicitudes", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                if (pendingCount > 0) Badge(containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary) {
                    Text(pendingCount.toString(), style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
        }
        OutlinedButton(onClick = onProgramClick, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
            Icon(painterResource(R.drawable.ic_program), null, Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text("Programas", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
