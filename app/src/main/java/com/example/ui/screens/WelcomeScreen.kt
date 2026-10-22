package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun WelcomeScreen(
    appName: String = "Motorista Pro",
    onExistingUserClick: () -> Unit,
    onNewUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION: Branding & Tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Text(
                        text = appName,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        letterSpacing = (-0.5).sp
                    )
                }

                Text(
                    text = "Seu trabalho, em números.",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Normal
                )
            }

            // CENTER SECTION: Car Hero with stylized green graph waves
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background artistic glowing graph curves
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(200.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Subtle background radial glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                EmeraldGreen.copy(alpha = 0.22f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.45f
                        )
                    )

                    // Ascending trend line
                    val path = Path().apply {
                        moveTo(w * 0.15f, h * 0.55f)
                        cubicTo(
                            w * 0.35f, h * 0.50f,
                            w * 0.45f, h * 0.25f,
                            w * 0.65f, h * 0.20f
                        )
                        cubicTo(
                            w * 0.75f, h * 0.18f,
                            w * 0.85f, h * 0.30f,
                            w * 0.92f, h * 0.22f
                        )
                    }
                    drawPath(
                        path = path,
                        color = EmeraldGreen.copy(alpha = 0.8f),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Small upward bar chart graphic at top-right
                    val barWidth = 6.dp.toPx()
                    val barGap = 4.dp.toPx()
                    val barStartX = w * 0.80f
                    val barBaseY = h * 0.35f

                    drawRoundRect(
                        color = EmeraldGreen.copy(alpha = 0.6f),
                        topLeft = Offset(barStartX, barBaseY - 14.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(barWidth, 14.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                    drawRoundRect(
                        color = EmeraldGreen.copy(alpha = 0.75f),
                        topLeft = Offset(barStartX + barWidth + barGap, barBaseY - 22.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(barWidth, 22.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                    drawRoundRect(
                        color = EmeraldGreen,
                        topLeft = Offset(barStartX + (barWidth + barGap) * 2, barBaseY - 32.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(barWidth, 32.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }

                // Front 3/4 Vehicle Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_hero_car),
                        contentDescription = "Veículo $appName",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // BOTTOM SECTION: Welcome Typography + Action Buttons + Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Seja bem-vindo(a)!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Aqui você acompanha seus ganhos,\ngastos e muito mais, de forma simples\ne prática.",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Primary Button: "Já tenho cadastro →"
                Button(
                    onClick = onExistingUserClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_ja_tenho_cadastro"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Já tenho cadastro",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Secondary Button: "👤+ Sou novo aqui"
                OutlinedButton(
                    onClick = onNewUserClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_sou_novo_aqui"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CardDark,
                        contentColor = TextWhite
                    ),
                    border = BorderStroke(1.dp, Color(0xFF1E2D3D)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sou novo aqui",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                    }
                }

                // Footer microcopy
                Text(
                    text = "Cadastre-se em alguns minutos e comece\na usar o $appName.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
