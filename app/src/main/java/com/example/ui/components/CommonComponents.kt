package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChartBarPoint

@Composable
fun PlatformBadge(
    platform: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (platform.lowercase()) {
        "99" -> Triple(App99Yellow, Color.Black, "99")
        "uber" -> Triple(Color(0xFF1E1E1E), Color.White, "Uber")
        "indrive" -> Triple(AppInDriveGreen, Color.White, "InD")
        else -> Triple(AppPrivateBlue, Color.White, "Part")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CategoryIcon(
    category: String,
    modifier: Modifier = Modifier
) {
    val (icon, tint, bg) = when (category.lowercase()) {
        "combustível", "combustivel" -> Triple(
            Icons.Default.LocalGasStation,
            FuelOrange,
            FuelOrange.copy(alpha = 0.15f)
        )
        "alimentação", "alimentacao" -> Triple(
            Icons.Default.Restaurant,
            ExpenseRed,
            ExpenseRed.copy(alpha = 0.15f)
        )
        "manutenção", "manutencao" -> Triple(
            Icons.Default.Build,
            MetricBlue,
            MetricBlue.copy(alpha = 0.15f)
        )
        "estacionamento" -> Triple(
            Icons.Default.LocalParking,
            MetricBlue,
            MetricBlue.copy(alpha = 0.15f)
        )
        "lavagem" -> Triple(
            Icons.Default.LocalCarWash,
            LightEmeraldGreen,
            LightEmeraldGreen.copy(alpha = 0.15f)
        )
        else -> Triple(
            Icons.Default.ReceiptLong,
            TextSecondary,
            TextSecondary.copy(alpha = 0.15f)
        )
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun PeriodTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            val animBg by animateColorAsState(
                if (isSelected) EmeraldGreen else Color.Transparent,
                label = "tab_bg"
            )
            val textColor by animateColorAsState(
                if (isSelected) Color.White else TextSecondary,
                label = "tab_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(animBg)
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp)
                    .testTag("period_tab_$index"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SubFilterTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark.copy(alpha = 0.7f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            val bg = if (isSelected) EmeraldGreen else Color.Transparent
            val textColor = if (isSelected) Color.White else TextSecondary

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DualBarChart(
    dataPoints: List<ChartBarPoint>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Evolução dos resultados",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldGreen))
                    Text("Ganhos", color = TextSecondary, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                    Text("Despesas", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Canvas for the Bars
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val maxVal = 1200f
            val chartHeight = size.height - 40f
            val chartWidth = size.width
            val stepX = chartWidth / dataPoints.size
            val barWidth = 10f.dp.toPx()

            // Draw horizontal dashed grid lines
            val gridSteps = listOf(0f, 300f, 600f, 900f, 1200f)
            gridSteps.forEach { step ->
                val y = chartHeight - (step / maxVal) * chartHeight
                drawLine(
                    color = BorderDark,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            dataPoints.forEachIndexed { i, pt ->
                val centerX = i * stepX + (stepX / 2)

                // Earnings Bar (Green)
                val earningsHeight = (pt.earnings / maxVal) * chartHeight
                drawRoundRect(
                    color = EmeraldGreen,
                    topLeft = Offset(centerX - barWidth - 2.dp.toPx(), chartHeight - earningsHeight),
                    size = Size(barWidth, earningsHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Expenses Bar (Red)
                val expenseHeight = (pt.expenses / maxVal) * chartHeight
                drawRoundRect(
                    color = ExpenseRed,
                    topLeft = Offset(centerX + 2.dp.toPx(), chartHeight - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Date Label
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#94A3B8")
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawText(pt.label, centerX, size.height - 5f, paint)
                }
            }
        }
    }
}
