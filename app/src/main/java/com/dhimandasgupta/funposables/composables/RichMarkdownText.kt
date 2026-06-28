package com.dhimandasgupta.funposables.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.funposables.ui.utils.MarkdownBlock
import com.dhimandasgupta.funposables.ui.utils.RenderBlock
import com.dhimandasgupta.funposables.ui.utils.WFMarkdownStyle
import com.dhimandasgupta.funposables.ui.utils.parseMarkdownBlocks

@Composable
fun RichTextMarkdownText(modifier: Modifier = Modifier) {
  val markdown =
    """
    # Markdown Support
    This is a **bold** and *italic* text.
    You can also have ***bold and italic***.
    ~~Strikethrough~~ is also supported.
    `Inline code` for developers.

    [JetBrains](https://www.jetbrains.com) link.

    Nested styles: **bold with *italic* inside** and *italic with **bold** inside*.

    - List item 1
    - List item 2 with `code`
    - List item 3

    1. Ordered item 1
    2. Ordered item 2

    > Blockquotes are also nice!

    ---

    Contact: [Email](mailto:support@example.com) or [Phone](tel:+18006624357) or [Phone](tel:1-800-GOT-JUNK).

    # Heading 1 (\#)
    ## Heading 2 (\#\#)
    ### Heading 3 (\#\#\#)
    #### Heading 4 (\#\#\#\#)
    ##### Heading 5 (\#\#\#\#\#)
    ###### Heading 6 (\#\#\#\#\#\#)

    ---
    *Alternative Heading 1*
    ===

    *Alternative Heading 2*
    ---

    ---
    ***
    ___

    *This text is italicized using asterisks.*
    _This text is italicized using underscores._

    **This text is bolded using double asterisks.**
    __This text is bolded using double underscores.__

    ***This text is bold and italicized using triple asterisks.***
    ~~This text has a strikethrough applied.~~

    > This is a standard blockquote.
    >> This is a nested blockquote block.

    * Unordered list item 1
    - Unordered list item 2
    + Unordered list item 3

    1. Ordered list item 1
    2. Ordered list item 2
       1. Nested ordered item (indented 3 spaces or 1 tab)

    - [x] Completed task list item
    - [ ] Incomplete task list item

    Here is some inline code: `val x = 10`

    ``kotlin
    // This is a fenced code block with syntax highlighting
    fun main() {
        println("Hello World")
    }
    ``
    """
      .trimIndent()

  val list = remember { mutableStateListOf<MarkdownBlock>() }
  LaunchedEffect(markdown) {
    list.clear()

    parseMarkdownBlocks(markdown).forEach { item ->
      list.add(item)
    }
  }

  val lazyListState = rememberLazyListState()

  LazyColumn(
    state = lazyListState,
    modifier =
      modifier.padding(
        start =
          WindowInsets.displayCutout
            .union(insets = WindowInsets.navigationBars)
            .asPaddingValues()
            .calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
        end =
          WindowInsets.displayCutout
            .union(insets = WindowInsets.navigationBars)
            .asPaddingValues()
            .calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
      ),
  ) {
    item {
      Spacer(
        modifier =
          Modifier.height(
            WindowInsets.displayCutout
              .union(insets = WindowInsets.systemBars)
              .asPaddingValues()
              .calculateTopPadding()
          )
      )
    }

    itemsIndexed(list) { index, item ->
      RenderBlock(
        block = item,
        style = WFMarkdownStyle(),
      )
    }

    item {
      Spacer(
        modifier =
          Modifier.height(
            WindowInsets.displayCutout
              .union(insets = WindowInsets.systemBars)
              .asPaddingValues()
              .calculateBottomPadding()
          )
      )
    }
  }
}
