package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Expense
import com.example.ui.components.CategoryIcon
import com.example.ui.components.SubFilterTabs
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExpenseTabFilter
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ExpensesScreen(
    expenses: List<Expense>,
    selectedFilter: ExpenseTabFilter,
    onFilterSelected: (ExpenseTabFilter) -> Unit,
    onAddExpenseClick: () -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Todas", "Categorias", "Comprovantes")
    val selectedIndex = when (selectedFilter) {
        ExpenseTabFilter.TODAS -> 0
        ExpenseTabFilter.CATEGORIAS -> 1
        ExpenseTabFilter.COMPROVANTES -> 2
    }

    val totalExpenses = expenses.sumOf { it.amount }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Despesas",
                            fontSize = 24.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total: ${MainViewModel.formatCurrency(totalExpenses)}",
                            fontSize = 13.sp,
                            color = ExpenseRedSoft,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Sub-filter tabs
            item {
                SubFilterTabs(
                    tabs = tabs,
                    selectedIndex = selectedIndex,
                    onTabSelected = { idx ->
                        val f = when (idx) {
                            0 -> ExpenseTabFilter.TODAS
                            1 -> ExpenseTabFilter.CATEGORIAS
                            else -> ExpenseTabFilter.COMPROVANTES
                        }
                        onFilterSelected(f)
                    }
                )
            }

            // Expenses List
            items(expenses, key = { it.id }) { expense ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CategoryIcon(category = expense.category)

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = expense.category,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "${expense.description} • ${expense.dateStr}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = MainViewModel.formatCurrency(expense.amount),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }
            }
        }

        // Action Button: + Nova despesa (Pinned at bottom)
        Button(
            onClick = onAddExpenseClick,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .height(52.dp)
                .testTag("btn_new_expense"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nova despesa",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
