package com.nutriagricola.app.ui.farmer.home.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.ui.theme.OfflineBackground


@Composable
fun SyncStatus(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    var isOnline by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {

        fun updateConnection() {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            isOnline =
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        }

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                updateConnection()
            }

            override fun onLost(network: Network) {
                updateConnection()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateConnection()
            }
        }

        updateConnection()
        connectivityManager.registerDefaultNetworkCallback(callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (isOnline) MaterialTheme.colorScheme.primaryContainer else OfflineBackground,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(28.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isOnline) R.drawable.ic_wifi else R.drawable.ic_wifi_off),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = if (isOnline) "En línea" else "Sin conexión",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
