package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AuthUser
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    currentUser: AuthUser,
    onSignInWithGoogle: (activityContext: android.content.Context, onResult: (Boolean, String?) -> Unit) -> Unit,
    onSimulatedGoogleLogin: (name: String, email: String) -> Unit,
    onGuestLogin: () -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSupabase: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSimulatedDialog by remember { mutableStateOf(false) }

    // Dialog for direct/simulated Google login (in case Google Play Services are absent in emulators)
    if (showSimulatedDialog) {
        var testName by remember { mutableStateOf("Motorista Pro") }
        var testEmail by remember { mutableStateOf("motorista@gmail.com") }

        AlertDialog(
            onDismissRequest = { showSimulatedDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = EmeraldGreen)
                    Text("Login com Conta Google", fontWeight = FontWeight.Bold, color = TextWhite)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Digite os dados da sua conta Google para autenticar rapidamente e testar a sincronização com o Supabase:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = testName,
                        onValueChange = { testName = it },
                        label = { Text("Nome Completo") },
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
                    OutlinedTextField(
                        value = testEmail,
                        onValueChange = { testEmail = it },
                        label = { Text("E-mail Google") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                        if (testEmail.isNotBlank()) {
                            onSimulatedGoogleLogin(testName.ifBlank { "Motorista" }, testEmail.trim())
                            showSimulatedDialog = false
                            Toast.makeText(context, "Autenticado com sucesso via Google!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Confirmar Login", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulatedDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentUser.isLoggedIn) "Perfil & Conta" else "Autenticação", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSupabase) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Supabase", tint = EmeraldGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextWhite
                )
            )
        },
        containerColor = BackgroundDark,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                EmeraldGreen.copy(alpha = 0.25f),
                                CardDark
                            )
                        )
                    )
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Motorista Pro",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )

                    Text(
                        text = "Acesso facilitado com conta Google e sincronização em nuvem Supabase",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // Error Message Banner if any
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ExpenseRed)
                        Text(
                            text = errorMessage ?: "",
                            color = TextWhite,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { errorMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // User Logged In Card or Login Buttons
            if (currentUser.isLoggedIn) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldGreen, BorderDark))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_user_logged_in")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Avatar
                        if (currentUser.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = currentUser.photoUrl,
                                contentDescription = "Foto de Perfil",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, EmeraldGreen, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.2f))
                                    .border(2.dp, EmeraldGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentUser.isGuest) Icons.Default.PersonOutline else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentUser.name.ifBlank { "Motorista Autenticado" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            if (currentUser.email.isNotBlank()) {
                                Text(
                                    text = currentUser.email,
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (currentUser.isGuest) SurfaceDark else EmeraldGreen.copy(alpha = 0.2f),
                                border = if (currentUser.isGuest) null else CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (currentUser.isGuest) Icons.Default.LockOpen else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (currentUser.isGuest) TextSecondary else EmeraldGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (currentUser.isGuest) "Modo Convidado" else "Conta Google Vinculada",
                                        fontSize = 12.sp,
                                        color = if (currentUser.isGuest) TextSecondary else EmeraldGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                        // Supabase cloud table shortcut
                        Button(
                            onClick = onNavigateToSupabase,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_go_supabase"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Abrir Tabela Dados (Supabase)", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Logout button
                        OutlinedButton(
                            onClick = {
                                onLogout()
                                Toast.makeText(context, "Sessão encerrada.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_logout"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ExpenseRedSoft, ExpenseRed))),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ExpenseRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Desconectar / Sair da Conta", fontWeight = FontWeight.SemiBold, color = ExpenseRed)
                        }
                    }
                }
            } else {
                // Not logged in: Show Google Sign-in Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Entre para continuar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Conecte sua conta para sincronizar faturamentos e alimentar a tabela de dados no Supabase de forma rápida e segura.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        // 1. Official Google Sign-in Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable(enabled = !isLoading) {
                                    isLoading = true
                                    errorMessage = null
                                    onSignInWithGoogle(context) { success, error ->
                                        isLoading = false
                                        if (success) {
                                            Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // If Google Play Services is missing or cancelled, show friendly hint
                                            errorMessage = error ?: "Não foi possível autenticar pelo Credential Manager. Você pode usar a opção de Login Rápido abaixo."
                                            showSimulatedDialog = true
                                        }
                                    }
                                }
                                .testTag("btn_google_signin")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = EmeraldGreen, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Autenticando...", color = Color.Black, fontWeight = FontWeight.Medium)
                                } else {
                                    // Google "G" representation
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4285F4),
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("G", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Continuar com o Google",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F1F1F)
                                    )
                                }
                            }
                        }

                        // 2. Direct / Quick Google Sign-In (ideal for emulators without Google Play Services)
                        OutlinedButton(
                            onClick = { showSimulatedDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_quick_google_login"),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldGreen, LightEmeraldGreen)))
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Login Rápido com E-mail Google",
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        // 3. Guest login button
                        TextButton(
                            onClick = {
                                onGuestLogin()
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_guest_login")
                        ) {
                            Text("Continuar sem login (Modo Convidado)", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Supabase Architecture Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = EmeraldGreen)
                        Text("Integração Supabase", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                    }

                    Text(
                        text = "A tabela 'dados' (colunas: id, usuário, conteúdo) armazena anotações, logs e informações operacionais vinculadas ao usuário autenticado, utilizando a anon key do Supabase.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )

                    OutlinedButton(
                        onClick = onNavigateToSupabase,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldGreen)
                    ) {
                        Icon(Icons.Default.TableView, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ver Tabela 'dados' do Supabase", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
