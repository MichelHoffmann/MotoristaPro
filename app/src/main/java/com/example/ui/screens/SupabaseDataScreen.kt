package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SupabaseClient
import com.example.model.AuthUser
import com.example.model.Dado
import com.example.ui.theme.*
import com.example.ui.viewmodel.SupabaseSyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseDataScreen(
    dadosList: List<Dado>,
    authUser: AuthUser,
    syncState: SupabaseSyncState,
    supabaseClient: SupabaseClient,
    onSyncNow: () -> Unit,
    onInsertDado: (usuario: String, conteudo: String, onComplete: ((Boolean, String?) -> Unit)?) -> Unit,
    onDeleteDado: (Dado) -> Unit,
    onTestConnection: (onResult: (Boolean, String) -> Unit) -> Unit,
    onSaveConfig: (url: String, key: String) -> Unit,
    onClearConfig: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showSqlDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Dado?>(null) }

    // Dialog: Insert into table dados (colunas: id, usuario, conteudo)
    if (showAddDialog) {
        var usuarioInput by remember {
            mutableStateOf(
                if (authUser.isLoggedIn) authUser.name.ifBlank { authUser.email } else "Motorista"
            )
        }
        var conteudoInput by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = EmeraldGreen)
                    Text("Novo Registro na Tabela 'dados'", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Adiciona uma nova linha com as colunas solicitadas: usuário e conteúdo.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = usuarioInput,
                        onValueChange = { usuarioInput = it },
                        label = { Text("Coluna: usuário") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_dado_usuario")
                    )

                    OutlinedTextField(
                        value = conteudoInput,
                        onValueChange = { conteudoInput = it },
                        label = { Text("Coluna: conteúdo") },
                        placeholder = { Text("Ex: Fechamento de corridas da semana, notas de viagem, observação...") },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_dado_conteudo")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (conteudoInput.isNotBlank()) {
                            isSubmitting = true
                            onInsertDado(usuarioInput.trim(), conteudoInput.trim()) { success, err ->
                                isSubmitting = false
                                showAddDialog = false
                                if (success) {
                                    Toast.makeText(context, "Registro inserido com sucesso!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Salvo localmente ($err)", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Preencha o campo conteúdo.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.testTag("btn_confirm_add_dado")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Inserir no Supabase", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }

    // Dialog: Delete confirmation
    itemToDelete?.let { dado ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Excluir registro?", color = TextWhite) },
            text = { Text("Deseja apagar a linha ID #${dado.id} (${dado.usuario}) do Supabase e local?", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDado(dado)
                        itemToDelete = null
                        Toast.makeText(context, "Registro excluído.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Excluir", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar", color = TextWhite)
                }
            },
            containerColor = CardDark
        )
    }

    // Dialog: Supabase Config (URL & Anon Key)
    if (showConfigDialog) {
        var currentUrl by remember { mutableStateOf(supabaseClient.getUrl()) }
        var currentKey by remember { mutableStateOf(supabaseClient.getAnonKey()) }

        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = EmeraldGreen)
                    Text("Configurar Supabase", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Insira a URL do seu projeto Supabase e a Chave Pública Anon (anon key):",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = currentUrl,
                        onValueChange = { currentUrl = it },
                        label = { Text("Supabase Project URL") },
                        placeholder = { Text("https://xyzcompany.supabase.co") },
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
                        value = currentKey,
                        onValueChange = { currentKey = it },
                        label = { Text("Supabase Anon Key") },
                        placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6...") },
                        minLines = 2,
                        maxLines = 4,
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

                    TextButton(
                        onClick = {
                            onClearConfig()
                            currentUrl = supabaseClient.getUrl()
                            currentKey = supabaseClient.getAnonKey()
                            Toast.makeText(context, "Configuração restaurada aos valores padrão", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Restaurar Padrões", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveConfig(currentUrl.trim(), currentKey.trim())
                        showConfigDialog = false
                        Toast.makeText(context, "Configurações do Supabase salvas!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Salvar e Sincronizar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }

    // Dialog: SQL Schema for Supabase
    if (showSqlDialog) {
        val sqlScript = """
CREATE TABLE IF NOT EXISTS public.dados (
    id BIGSERIAL PRIMARY KEY,
    usuario TEXT NOT NULL,
    conteudo TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Ativar RLS e permitir leitura/escrita com Anon Key:
ALTER TABLE public.dados ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Permitir leitura publica" 
ON public.dados FOR SELECT 
USING (true);

CREATE POLICY "Permitir insercao publica" 
ON public.dados FOR INSERT 
WITH CHECK (true);

CREATE POLICY "Permitir exclusao publica" 
ON public.dados FOR DELETE 
USING (true);
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { showSqlDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = EmeraldGreen)
                    Text("Script SQL da Tabela 'dados'", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Execute este script no SQL Editor do seu Supabase Dashboard para criar a tabela com as colunas (id, usuário, conteúdo):",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Surface(
                        color = SurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = sqlScript,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Supabase SQL", sqlScript)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "SQL copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                        showSqlDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar SQL", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSqlDialog = false }) {
                    Text("Fechar", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Supabase • Tabela dados", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Colunas: id, usuário, conteúdo (Anon Key)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { showConfigDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar Supabase", tint = TextSecondary)
                    }
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = if (authUser.isLoggedIn) Icons.Default.AccountCircle else Icons.Outlined.AccountCircle,
                            contentDescription = "Conta / Login",
                            tint = if (authUser.isLoggedIn) EmeraldGreen else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = EmeraldGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_dado")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Registro")
            }
        },
        containerColor = BackgroundDark,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Status Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text("Status Supabase", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                                    val isConf = supabaseClient.isConfigured()
                                    Text(
                                        text = if (isConf) "Conexão Ativa / Configurada" else "Chave Anon Padrão",
                                        fontSize = 11.sp,
                                        color = if (isConf) EmeraldGreen else GoldWarning
                                    )
                                }
                            }

                            // User Badge
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceDark,
                                modifier = Modifier.clickable { onNavigateToLogin() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (authUser.isLoggedIn) Icons.Default.CheckCircle else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (authUser.isLoggedIn) EmeraldGreen else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (authUser.isLoggedIn) authUser.name.take(12) else "Fazer Login",
                                        fontSize = 11.sp,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Project URL & Anon Key Info
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val url = supabaseClient.getUrl()
                                Text(
                                    text = "URL: ${if (url.length > 35) url.take(35) + "..." else url.ifBlank { "Não configurada" }}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Auth: Anon Public Key (Bearer Header)",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Sync State message if available
                        if (syncState.message != null) {
                            Text(
                                text = syncState.message,
                                fontSize = 12.sp,
                                color = if (syncState.isError) ExpenseRed else EmeraldGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Action Buttons: Sincronizar & Testar & SQL
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onSyncNow,
                                enabled = !syncState.isLoading,
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (syncState.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sincronizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    onTestConnection { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGreen)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Testar", fontSize = 12.sp, color = TextWhite)
                            }

                            IconButton(
                                onClick = { showSqlDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Code, contentDescription = "Ver SQL", tint = EmeraldGreen)
                            }
                        }
                    }
                }
            }

            // Section Title: Linhas da tabela
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Registros (${dadosList.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar Linha", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Empty State
            if (dadosList.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                            Text(
                                text = "Nenhum dado na tabela 'dados'",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Clique em '+ Adicionar Linha' para inserir um registro com usuário e conteúdo no Supabase.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Criar Primeiro Registro", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // List of dados
                items(dadosList, key = { it.id }) { dado ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_dado_${dado.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = EmeraldGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "ID: #${dado.id}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SurfaceDark
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                                            Text(
                                                text = dado.usuario,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextWhite
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (dado.isSyncedWithSupabase) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = "Sincronizado",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CloudQueue,
                                            contentDescription = "Pendente",
                                            tint = GoldWarning,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { itemToDelete = dado },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = ExpenseRedSoft, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            // Conteúdo
                            Text(
                                text = dado.conteudo,
                                fontSize = 14.sp,
                                color = TextWhite,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            if (dado.createdAt.isNotBlank()) {
                                Text(
                                    text = "Criado em: ${dado.createdAt}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
