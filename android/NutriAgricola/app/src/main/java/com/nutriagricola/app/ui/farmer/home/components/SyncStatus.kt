package com.nutriagricola.app.ui.farmer.home.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutriagricola.app.R
import com.nutriagricola.app.ui.theme.OfflineBackground
import com.nutriagricola.app.ui.theme.OfflineYellow
import com.nutriagricola.app.ui.theme.OnlineBackground
import com.nutriagricola.app.ui.theme.OnlineGreen


@Composable
fun SyncStatus(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    var isOnline by remember { mutableStateOf(false) }

    fun updateConnection() {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }

    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onLost(network: Network) {
                isOnline = false
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                isOnline = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
        }

        updateConnection()
        connectivityManager.registerDefaultNetworkCallback(callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    val statusColor = if (isOnline) OnlineGreen else OfflineYellow
    val backgroundColor = if (isOnline) OnlineBackground else OfflineBackground

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = CircleShape,
                color = backgroundColor,
                modifier = Modifier.size(26.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isOnline) R.drawable.ic_wifi else R.drawable.ic_wifi_off),
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = if (isOnline) "En línea" else "Sin conexión",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 22.sp),
                fontWeight = FontWeight.Medium,
                color = statusColor,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(32.dp),
                color = statusColor.copy(alpha = 0.25f)
            )
            Surface(
                onClick = { updateConnection() },
                shape = RoundedCornerShape(10.dp),
                color = statusColor.copy(alpha = 0.08f),
                contentColor = statusColor
            ) {
                Column(
                    modifier = Modifier.heightIn(min = 48.dp)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_sync), contentDescription = null,
                        modifier = Modifier.size(20.dp))
                    Text("Actualizar", style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
