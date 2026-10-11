package com.nutriagricola.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit
){
    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ){
                    focusManager.clearFocus()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Logo NutriAgrícola",
                    modifier = Modifier.size(88.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "NutriAgrícola",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Iniciar sesión",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = onLoginClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Iniciar sesión",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

}