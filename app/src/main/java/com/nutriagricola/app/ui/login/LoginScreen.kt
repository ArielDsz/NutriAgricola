package com.nutriagricola.app.ui.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nutriagricola.app.R
import com.nutriagricola.app.domain.model.user.User
import com.nutriagricola.app.ui.components.dismissKeyboardOnBackgroundTap
import com.nutriagricola.app.ui.theme.NutriAgricolaTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (User) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.authenticatedUser) {
        uiState.authenticatedUser?.let { user ->
            password = ""
            viewModel.onNavigationHandled()
            onLoginSuccess(user)
        }
    }

    LoginContent(
        email = email,
        password = password,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        isLoading = uiState.isLoading,
        error = uiState.error,
        onLogin = { viewModel.login(email, password) }
    )
}

@Composable
private fun LoginContent(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onLogin: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val passwordFocus = remember { FocusRequester() }
    var passwordVisible by remember { mutableStateOf(false) }
    val submit: () -> Unit = {
        if (!isLoading) {
            focusManager.clearFocus()
            onLogin()
        }
    }
    val headerHeight = (LocalConfiguration.current.screenHeightDp.dp * 0.36f)
        .coerceIn(240.dp, 340.dp)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(colors.background)
            .dismissKeyboardOnBackgroundTap().imePadding()
    ) {
        val panelMinHeight = (maxHeight - headerHeight + 24.dp).coerceAtLeast(0.dp)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(headerHeight)) {
                Image(
                    painter = painterResource(R.drawable.login_landscape),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxSize()
                )
                Row(
                    modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 480.dp)
                        .fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        "NutriAgrícola",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = colors.background,
                contentColor = colors.onSurface
            ) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = panelMinHeight)) {
                    Box(
                        modifier = Modifier.matchParentSize()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.login_bottom_decoration),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.BottomCenter,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth().height(40.dp).background(
                                Brush.verticalGradient(listOf(colors.background, Color.Transparent))
                            )
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 480.dp)
                            .fillMaxWidth().navigationBarsPadding()
                            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Iniciar sesión",
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Ingresa tus datos para continuar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(28.dp))

                        LoginField(
                            value = email,
                            onValueChange = onEmailChange,
                            label = "Correo electrónico",
                            icon = R.drawable.ic_email,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() })
                        )
                        Spacer(Modifier.height(14.dp))
                        LoginField(
                            value = password,
                            onValueChange = onPasswordChange,
                            label = "Contraseña",
                            icon = R.drawable.ic_lock,
                            enabled = !isLoading,
                            modifier = Modifier.focusRequester(passwordFocus),
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    enabled = !isLoading
                                ) {
                                    Icon(
                                        painterResource(if (passwordVisible) R.drawable.ic_visibility
                                            else R.drawable.ic_visibility_off),
                                        contentDescription = if (passwordVisible) "Ocultar contraseña"
                                            else "Mostrar contraseña",
                                        tint = colors.onSurfaceVariant
                                    )
                                }
                            }
                        )
                        error?.let {
                            Text(
                                it,
                                color = colors.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = submit,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = colors.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    painterResource(R.drawable.ic_login), contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Iniciar sesión", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    @DrawableRes icon: Int,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth().heightIn(min = 80.dp),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = MaterialTheme.colorScheme.surface
        ),
        leadingIcon = {
            Surface(
                modifier = Modifier.padding(start = 12.dp, end = 8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        },
        trailingIcon = trailingIcon
    )
}

@Preview(name = "Login", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun LoginPreview() {
    NutriAgricolaTheme {
        LoginContent(
            email = "", password = "", onEmailChange = {}, onPasswordChange = {},
            isLoading = false, error = null, onLogin = {}
        )
    }
}
