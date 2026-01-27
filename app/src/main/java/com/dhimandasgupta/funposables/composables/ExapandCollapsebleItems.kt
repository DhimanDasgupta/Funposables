package com.dhimandasgupta.funposables.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme

@Composable
fun ExpandableCollapsableItems(
    modifier: Modifier = Modifier
) {
    val accounts = listOf(
        "Dhiman Dasgupta",
        "Paramita Banerjee",
        "Nilanjan Sen",
        "Shraboni Basu Mallick",
        "Indranil Sen",
        "Sanhita Dasgupta"
    )

    ExpandableCardList(
        accounts = accounts,
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateStartPadding(
                        LayoutDirection.Ltr
                    ),
                top = WindowInsets.displayCutout.union(WindowInsets.statusBars).asPaddingValues()
                    .calculateTopPadding(),
                end = WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateEndPadding(
                        LayoutDirection.Ltr
                    ),
                bottom = WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
    )
}

@Composable
private fun ExpandableCardList(
    accounts: List<String>, // list of items (ex: account names)
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {

        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Cash Accounts",
                    style = typography.titleMedium,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$575,147.23", // replace with dynamic data
                    style = typography.titleMedium
                )
                Text(
                    text = "Total available balance",
                    style = typography.bodySmall
                )
            }
        }

        LazyColumn {
            itemsIndexed(accounts.reversed()) { index, account ->
                AccountCard(
                    index = index,
                    collapsed = expanded,
                    accountName = account
                )
            }
        }

        /*AnimatedContent(targetState = expanded, label = "") { isExpanded ->

            if (isExpanded) {
                // Expanded → full lazy list
                LazyColumn {
                    itemsIndexed(accounts.reversed()) { index, account ->
                        AccountCard(
                            index = index,
                            accountName = account
                        )
                    }
                }
            } else {
                // Collapsed → show up to 3 overlapping
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = 36f
                        }
                ) {
                    accounts.take(3).reversed().forEachIndexed { index, account ->
                        AccountCard(
                            accountName = account,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = (-index * 36).toFloat()
                                }
                        )
                    }
                }
            }
        }*/

        LazyColumn {
            items(100) { item ->
                Text(
                    "This is Item : $item", modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AccountCard(
    modifier: Modifier = Modifier,
    index: Int = 0,
    collapsed: Boolean = false,
    accountName: String
) {
    val density = LocalDensity.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .graphicsLayer {
                translationY = if (collapsed) 0f else with(density) {
                    (-index * 96).toDp().roundToPx().toFloat()
                }
            }
            .zIndex(index.toFloat() * -1f),
        border = CardDefaults.outlinedCardBorder(
            enabled = true
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            draggedElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = accountName,
                style = typography.bodyLarge
            )
            Text(
                text = "$12,345.67", // Example balance per account
                style = typography.bodyLarge
            )
        }
    }
}


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun ExpandableCollapsableItemsPreView() {
    FunposablesTheme {
        ExpandableCollapsableItems()
    }
}