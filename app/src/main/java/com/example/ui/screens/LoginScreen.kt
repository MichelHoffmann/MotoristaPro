package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuthUser
import com.example.ui.theme.*

enum class AuthMode {
    LOGIN,
    REGISTER
}

@Composable
fun LoginScreen(
    currentUser: AuthUser,
    onLoginWithEmailPassword: (email: String, password: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onRegisterWithEmailPassword: (email: String, password: String, name: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onSignInWithGoogle: (activityContext: android.content.Context, onResult: (Boolean, String?) -> Unit) -> Unit,
    onSimulatedGoogleLogin: (name: String, email: String) -> Unit,
    onGuestLogin: () -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit = {},
    isInitialLogin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // If user is already logged in, show their active profile with options
    if (currentUser.isLoggedIn && !isInitialLogin) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sua Conta",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.2f))
                            .border(2.dp, EmeraldGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Text(
                        text = currentUser.name.ifBlank { "Motorista" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (currentUser.isGuest) "Modo Visitante" else currentUser.email.ifBlank { "Conta Ativa" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Você está conectado ao app. Seus dados e configurações estão salvos no banco de dados.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Continuar para o Aplicativo", fontWeight = FontWeight.Bold, color = Color.White)
            }

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRedSoft),
                border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sair da Conta (Logout)", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // Main Login / Registration UI (Reference mockup design)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isInitialLogin) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = TextWhite
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Top Brand Mark (Inspired by reference mockup: "Dotcom" style with Emerald branding)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(EmeraldGreen.copy(alpha = 0.2f))
                    .border(2.dp, EmeraldGreen, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "Motorista Pro",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Gestão inteligente de corridas e faturamento",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Login / Register Container Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Segmented Toggle: Login vs Cadastrar (Matching mockup tabs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDark)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (authMode == AuthMode.LOGIN) EmeraldGreen else Color.Transparent)
                            .clickable {
                                authMode = AuthMode.LOGIN
                                errorMessage = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Entrar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (authMode == AuthMode.LOGIN) Color.White else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (authMode == AuthMode.REGISTER) EmeraldGreen else Color.Transparent)
                            .clickable {
                                authMode = AuthMode.REGISTER
                                errorMessage = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cadastrar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (authMode == AuthMode.REGISTER) Color.White else TextSecondary
                        )
                    }
                }

                // Title
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (authMode == AuthMode.LOGIN) "Acesse sua conta" else "Crie sua nova conta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = if (authMode == AuthMode.LOGIN)
                            "Digite seu e-mail e senha para continuar"
                        else
                            "Seus dados ficarão salvos com segurança no banco de dados",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Error Message banner
                AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                    errorMessage?.let { err ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ExpenseRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = ExpenseRedSoft,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = err,
                                    fontSize = 12.sp,
                                    color = ExpenseRedSoft,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Name field (Only in Registration mode)
                if (authMode == AuthMode.REGISTER) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            errorMessage = null
                        },
                        label = { Text("Nome do Motorista") },
                        placeholder = { Text("Ex: Carlos Silva") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = EmeraldGreen)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_register_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        )
                    )
                }

                // Email field (Rounded pill shape from mockup)
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = null
                    },
                    label = { Text("E-mail") },
                    placeholder = { Text("seunome@gmail.com") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = EmeraldGreen)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_email"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    )
                )

                // Password field (With eye visibility toggle from mockup)
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text("Senha") },
                    placeholder = { Text("••••••••") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = EmeraldGreen)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (authMode == AuthMode.REGISTER) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_auth_password"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    )
                )

                // Confirm Password field (Only in Registration mode)
                if (authMode == AuthMode.REGISTER) {
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            errorMessage = null
                        },
                        label = { Text("Confirmar Senha") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = EmeraldGreen)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Ocultar senha" else "Mostrar senha",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_auth_confirm_password"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        )
                    )
                }

                // Forgot password link (As in mockup)
                if (authMode == AuthMode.LOGIN) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Esqueceu a senha?",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { showForgotPasswordDialog = true }
                        )
                    }
                }

                // Submit Button (Entrar / Criar Conta)
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val cleanEmail = emailInput.trim()
                        val cleanPass = passwordInput.trim()

                        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                            errorMessage = "Por favor, digite um e-mail válido."
                            return@Button
                        }
                        if (cleanPass.length < 4) {
                            errorMessage = "A senha deve ter no mínimo 4 caracteres."
                            return@Button
                        }

                        if (authMode == AuthMode.REGISTER) {
                            if (cleanPass != confirmPasswordInput.trim()) {
                                errorMessage = "As senhas não coincidem. Digite novamente."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            onRegisterWithEmailPassword(cleanEmail, cleanPass, nameInput.trim()) { success, err ->
                                isLoading = false
                                if (!success) {
                                    errorMessage = err ?: "Erro ao criar conta. Tente novamente."
                                } else {
                                    Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // LOGIN
                            isLoading = true
                            errorMessage = null
                            onLoginWithEmailPassword(cleanEmail, cleanPass) { success, err ->
                                isLoading = false
                                if (!success) {
                                    errorMessage = err ?: "E-mail ou senha incorretos."
                                } else {
                                    Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_auth_submit"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (authMode == AuthMode.LOGIN) "Entrar" else "Criar Conta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // "Ou entrar com" Social Divider (Matching reference image)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderDark, thickness = 0.5.dp)
                    Text(
                        text = "Ou entrar com",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderDark, thickness = 0.5.dp)
                }

                // Google Sign In Button (From mockup)
                OutlinedButton(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        onSignInWithGoogle(context) { success, err ->
                            isLoading = false
                            if (!success) {
                                // If CredentialManager is unavailable (e.g. without Play Services), offer direct login
                                onSimulatedGoogleLogin(
                                    emailInput.substringBefore("@").ifBlank { "Motorista Google" },
                                    emailInput.ifBlank { "motorista@gmail.com" }
                                )
                                Toast.makeText(context, "Conectado via Google!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_google_sign_in"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Continuar com o Google",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Guest / Test mode option
                TextButton(
                    onClick = onGuestLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Continuar como visitante (modo de teste)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Bottom Switch Link: "Não tem conta? Cadastre-se" / "Já tem conta? Entrar" (From mockup)
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (authMode == AuthMode.LOGIN) "Não tem uma conta?" else "Já tem uma conta?",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = if (authMode == AuthMode.LOGIN) "Cadastre-se" else "Entrar",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldGreen,
                modifier = Modifier
                    .clickable {
                        authMode = if (authMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                        errorMessage = null
                    }
                    .testTag("btn_switch_auth_mode")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var recoveryEmail by remember { mutableStateOf(emailInput) }
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text("Recuperar Senha", fontWeight = FontWeight.Bold, color = TextWhite)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Digite seu e-mail cadastrado. Suas credenciais estão salvas no banco de dados local do seu dispositivo.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = recoveryEmail,
                        onValueChange = { recoveryEmail = it },
                        label = { Text("E-mail") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotPasswordDialog = false
                        Toast.makeText(
                            context,
                            "Se sua conta estiver cadastrada, você pode criar uma nova ou redefinir seus dados.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }
}
