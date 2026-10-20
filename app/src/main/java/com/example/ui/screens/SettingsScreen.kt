package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VehiclePowertrain
import com.example.model.VehicleProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    vehicleProfile: VehicleProfile,
    onOpenNewUserMenu: () -> Unit,
    onNavigateToVehicle: () -> Unit,
    onResetAllData: () -> Unit,
    onNavigateToSupabase: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    authUser: com.example.model.AuthUser? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Zerar todos os dados?") },
            text = { Text("Isso apagará todos os faturamentos e despesas registrados, reiniciando o aplicativo com valores zerados.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetAllData()
                        showResetDialog = false
                        Toast.makeText(context, "Todos os valores foram zerados com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Zerar Tudo", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar", color = TextWhite)
                }
            },
            containerColor = CardDark
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Configurações",
            fontSize = 24.sp,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )

        // Vehicle & Driver Profile Card - Links directly to Meu Veiculo
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToVehicle() }
                .testTag("card_user_vehicle_profile")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EmeraldGreen.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (vehicleProfile.vehiclePowertrain) {
                            VehiclePowertrain.ELETRICO -> Icons.Default.ElectricCar
                            VehiclePowertrain.HIBRIDO, VehiclePowertrain.HIBRIDO_PLUGIN -> Icons.Default.ElectricBolt
                            else -> Icons.Default.DirectionsCar
                        },
                        contentDescription = "Carro",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (vehicleProfile.driverName.isNotBlank()) "Motorista: ${vehicleProfile.driverName}" else "Cadastro de Motorista",
                        fontSize = 12.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (vehicleProfile.modelName.isNotBlank()) "${vehicleProfile.modelName} ${vehicleProfile.manufacturingYear}" else "Cadastre seu veículo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "${vehicleProfile.vehiclePowertrain.displayName} • ${vehicleProfile.averageConsumption} ${if (vehicleProfile.isElectric) "km/kWh" else "km/L"} • ${String.format("%,d km", vehicleProfile.currentMileage.toInt())}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Abrir Meu Veículo",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Action Buttons: Meu Veículo (Custos e Viabilidade) & Novo Usuário
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNavigateToVehicle,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_meu_veiculo"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Meu Veículo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Button(
                onClick = onOpenNewUserMenu,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_menu_new_user"),
                colors = ButtonDefaults.buttonColors(containerColor = CardHighlight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Editar Perfil",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )
            }
        }

        // Settings Items List
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Outlined.AccountCircle,
                    title = "Conta & Login com Google",
                    value = if (authUser?.isLoggedIn == true) authUser.name.ifBlank { "Conectado" } else "Conectar",
                    onClick = onNavigateToLogin
                )
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                SettingsRow(
                    icon = Icons.Outlined.CloudSync,
                    title = "Supabase (Tabela dados)",
                    value = "Anon Key / REST",
                    onClick = onNavigateToSupabase
                )
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                SettingsRow(
                    icon = Icons.Outlined.DirectionsCar,
                    title = "Custos do Veículo e Tarifas Mínimas",
                    value = "Gerenciar",
                    onClick = onNavigateToVehicle
                )
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                SettingsRow(
                    icon = Icons.Outlined.LocalGasStation,
                    title = if (vehicleProfile.isElectric) "Preço da Energia" else "Preço Médio Combustível",
                    value = String.format("R$ %.2f", vehicleProfile.fuelOrEnergyPrice).replace(".", ","),
                    onClick = onNavigateToVehicle
                )
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                SettingsRow(
                    icon = Icons.Outlined.TrackChanges,
                    title = "Meta mensal cadastrada",
                    value = "${MainViewModel.formatCurrency(vehicleProfile.monthlyGoal)}/mês (${if (vehicleProfile.isGoalNetProfit) "Líquido" else "Bruto"})",
                    onClick = onNavigateToVehicle
                )
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                SettingsRow(
                    icon = Icons.Outlined.Apps,
                    title = "Plataformas ativas",
                    value = "99, Uber, InDrive",
                    onClick = {
                        Toast.makeText(context, "Plataformas configuradas: 99, Uber, InDrive, Particular", Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                SettingsRow(
                    icon = Icons.Outlined.FileDownload,
                    title = "Exportar dados",
                    value = "CSV / PDF",
                    onClick = {
                        Toast.makeText(context, "Exportando resumo em CSV e PDF...", Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(color = BorderDark, thickness = 0.5.dp)

                SettingsRow(
                    icon = Icons.Outlined.RestartAlt,
                    title = "Zerar todos os dados do app",
                    value = "Iniciar do zero",
                    onClick = { showResetDialog = true }
                )
            }
        }

        // Bottom Motivational Card
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
                Text(
                    text = "Seu trabalho movimenta sua vida.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "E agora, você tem o controle total.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreen
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MotivationalPill("Mais lucro", Icons.Default.TrendingUp, EmeraldGreen, Modifier.weight(1f))
                    MotivationalPill("Menos gastos", Icons.Default.Savings, ExpenseRedSoft, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dirija com metas claras. Construa um amanhã melhor! 💚",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightEmeraldGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Text(text = title, fontSize = 14.sp, color = TextWhite, fontWeight = FontWeight.Medium)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = value, fontSize = 13.sp, color = TextSecondary)
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun MotivationalPill(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDark)
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(label, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
