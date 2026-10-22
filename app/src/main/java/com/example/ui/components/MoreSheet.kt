package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreSheet(
    onDismiss: () -> Unit,
    onNavigateToVehicle: () -> Unit,
    onNavigateToPlanning: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mais Opções",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            // Section 1: Meu Veículo & Planejamento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoreSheetCard(
                    title = "Meu Veículo",
                    subtitle = "Custos, propulsão & R$/km",
                    icon = Icons.Outlined.DirectionsCar,
                    iconBg = EmeraldGreen.copy(alpha = 0.2f),
                    iconTint = EmeraldGreen,
                    onClick = {
                        onDismiss()
                        onNavigateToVehicle()
                    },
                    modifier = Modifier.weight(1f).testTag("more_vehicle_card")
                )

                MoreSheetCard(
                    title = "Planejamento",
                    subtitle = "Calendário & Feriados",
                    icon = Icons.Outlined.CalendarMonth,
                    iconBg = MetricBlue.copy(alpha = 0.2f),
                    iconTint = MetricBlue,
                    onClick = {
                        onDismiss()
                        onNavigateToPlanning()
                    },
                    modifier = Modifier.weight(1f).testTag("more_planning_card")
                )
            }

            // Section 2: Despesas & Configurações
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MoreSheetCard(
                    title = "Despesas",
                    subtitle = "Gastos e comprovantes",
                    icon = Icons.Outlined.ReceiptLong,
                    iconBg = ExpenseRedSoft.copy(alpha = 0.2f),
                    iconTint = ExpenseRedSoft,
                    onClick = {
                        onDismiss()
                        onNavigateToExpenses()
                    },
                    modifier = Modifier.weight(1f).testTag("more_expenses_card")
                )

                MoreSheetCard(
                    title = "Configurações",
                    subtitle = "Perfil, dados e reset",
                    icon = Icons.Outlined.Settings,
                    iconBg = FuelOrange.copy(alpha = 0.2f),
                    iconTint = FuelOrange,
                    onClick = {
                        onDismiss()
                        onNavigateToSettings()
                    },
                    modifier = Modifier.weight(1f).testTag("more_settings_card")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MoreSheetCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
