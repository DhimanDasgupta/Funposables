package com.dhimandasgupta.funposables.composables

import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.dhimandasgupta.funposables.ui.common.bottomFadeForLazyColumn
import com.dhimandasgupta.funposables.ui.common.verticalScrollbarForLazyColumn
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
fun ExpandableCollapsableItems(modifier: Modifier = Modifier) {
  val accountItem =
    persistentListOf(
      Item.AccountItem("Dhiman Dasgupta", "$12,345.67"),
      Item.AccountItem("Paramita Banerjee", "$12,345.67"),
      Item.AccountItem("Nilanjan Sen", "$12,345.67"),
      Item.AccountItem("Shraboni Basu Mallick", "$12,345.67"),
      Item.AccountItem("Indranil Sen", "$12,345.67"),
      Item.AccountItem("Sanhita Dasgupta", "$12,345.67"),
    )

  val normalItems =
    buildList<Item> {
        repeat(100) { index ->
          add(Item.NormalItem("This is Item : $index"))
        }
      }
      .toPersistentList()

  val items = (accountItem + normalItems).toPersistentList()

  ExpandableCardList(
    items = items,
    modifier =
      modifier
        .fillMaxSize()
        .padding(
          start =
            WindowInsets.displayCutout
              .union(WindowInsets.navigationBars)
              .asPaddingValues()
              .calculateStartPadding(LayoutDirection.Ltr),
          top =
            WindowInsets.displayCutout
              .union(WindowInsets.statusBars)
              .asPaddingValues()
              .calculateTopPadding(),
          end =
            WindowInsets.displayCutout
              .union(WindowInsets.navigationBars)
              .asPaddingValues()
              .calculateEndPadding(LayoutDirection.Ltr),
          bottom =
            WindowInsets.displayCutout
              .union(WindowInsets.navigationBars)
              .asPaddingValues()
              .calculateBottomPadding(),
        ),
  )
}

@Composable
private fun ExpandableCardList(
  items: ImmutableList<Item>, // list of items (ex: account names)
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  val scrollState = rememberLazyListState()

  LazyColumn(
    state = scrollState,
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScrollbarForLazyColumn(
          lazyListState = scrollState,
          width = 4.dp,
          thumbWidth = 2.dp,
          minThumbHeight = 32.dp,
          endPadding = 2.dp,
        )
        .bottomFadeForLazyColumn(
          lazyListState = scrollState,
          heightPercentage = 1f,
          color = colorScheme.primary,
          maxAlpha = 0.75f,
        ),
  ) {
    // Header row
    item(
      key = "header",
      contentType = "header",
    ) {
      HeaderItem(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
        expanded = expanded,
        onExpandChanged = { expanded = it },
      )
    }

    itemsIndexed(
      items = items,
      key = { index, item -> item.hashCode() + index },
      contentType = { _, item -> item.javaClass.name },
    ) { index, item ->
      when (item) {
        is Item.AccountItem ->
          AccountCard(
            index = index,
            expanded = expanded,
            accountName = item.name,
            accountBalance = item.amount,
          )
        is Item.NormalItem -> NormalCard(item = item.name)
      }
    }
  }
}

@Composable
private fun HeaderItem(
  modifier: Modifier = Modifier,
  expanded: Boolean,
  onExpandChanged: (Boolean) -> Unit,
) {
  Row(
    modifier = modifier.clickable { onExpandChanged(!expanded) },
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector =
          if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
        contentDescription = null,
      )
      Spacer(Modifier.width(8.dp))
      Text(
        text = "Cash Accounts",
        style = typography.titleMedium,
      )
    }

    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = "$575,147.23", // replace it with dynamic data
        style = typography.titleMedium,
      )
      Text(
        text = "Total available balance",
        style = typography.bodySmall,
      )
    }
  }
}

@Composable
private fun AccountCard(
  modifier: Modifier = Modifier,
  index: Int = 0,
  expanded: Boolean = false,
  accountName: String,
  accountBalance: String,
) {
  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .zIndex(index.toFloat() * -1f)
        .layout { measurable, constraints ->
          val placeable = measurable.measure(constraints)
          val height = if (expanded || index == 0) placeable.height else 0
          layout(placeable.width, height) {
            if (expanded || index == 0) {
              placeable.placeRelative(0, 0)
            } else {
              placeable.placeRelative(0, -1 * placeable.height)
            }
          }
        },
    border = CardDefaults.outlinedCardBorder(enabled = true),
    elevation =
      CardDefaults.cardElevation(
        defaultElevation = 12.dp,
        pressedElevation = 0.dp,
        focusedElevation = 0.dp,
        hoveredElevation = 0.dp,
        draggedElevation = 0.dp,
        disabledElevation = 0.dp,
      ),
    shape = RoundedCornerShape(12.dp),
  ) {
    Row(
      modifier = Modifier.padding(16.dp).fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = accountName,
        style = typography.bodyLarge,
      )
      Text(
        text = accountBalance, // Example balance per account
        style = typography.bodyLarge,
      )
    }
  }
}

@Composable
private fun NormalCard(
  modifier: Modifier = Modifier,
  item: String,
) {
  Text(
    text = "This is Item : $item",
    modifier =
      modifier
        .fillMaxWidth()
        .padding(16.dp)
        .border(1.dp, colorScheme.primary, RoundedCornerShape(12.dp))
        .padding(horizontal = 12.dp, vertical = 8.dp),
  )
}

sealed interface Item {
  data class AccountItem(val name: String, val amount: String) : Item

  data class NormalItem(val name: String) : Item
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun ExpandableCollapsableItemsPreView() {
  FunposablesTheme {
    ExpandableCollapsableItems()
  }
}
