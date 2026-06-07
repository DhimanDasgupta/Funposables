package com.dhimandasgupta.funposables.composables

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.funposables.ui.utils.RenderBlock
import com.dhimandasgupta.funposables.ui.utils.RichTextHelperParams
import com.dhimandasgupta.funposables.ui.utils.WFMarkdownStyle
import com.dhimandasgupta.funposables.ui.utils.convertToAnnotatedString
import com.dhimandasgupta.funposables.ui.utils.streamMarkdownBlocks
import kotlinx.coroutines.launch

@Composable
fun RichText(
    modifier: Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current

    val html = """
        This </br>is a sample<br /> HTML string with break tags within.<br>
        Take my $500 for this.
        <p>Hello there how are <sup>you</sup>. If you want to <sub><b><u><i>contact me</i></u></b></sub> then reach me via <i><a href='tel:+919916107291'>phone</a></i> or via <a href='https://www.anthropic.com'>website</a>.</p>
        <ol>
            <li>Name one</li>
            <li>Item two item with https://gemini.google.com/</li>
            <li><b>Third item</b></li>
        </ol>
        <p>
        Visit https://www.jetbrains.com/junie or call 1-800-622-4357.
        You can also use <a href='https://www.google.com'>this website</a>.
        1-800-662-HELP and 1-800-662-HELP or 1—800—662—HELP or 1-800-622-HELP or 1-800-GO-FEDEX or 1-800-CONTACTS or 1-800-MATTRESS or 1-800-GOT-JUNK or 1-800-HURT-NOW or 1-800-DIAL-CASH or 1-800-USA-BANK or 1-800-HOMECARE or 1-800-DENTIST or 1-800-SOLAR-USA or 1-212-888-CATS or 1-888-2-HIRE-US
        </p>
        <p>Contact options:</p>
        <ul>
            <li>Call 1-800-662-HELP</li>
            <li>Visit https://www.openai.com</li>
            <li><b>Emergency:</b> use  <a href='tel:+910123456789'>phone</a> support</li>
        </ul>
        <ol>
            <li>First item</li>
            <li>Second item with https://www.kotlinlang.org</li>
            <li><b>Third item</b></li>
        </ol>
        This is a plain and simple string. This dose not have any style what so ever. 
        Now coming back to hyperlinked texts - here is a <strong>strong with <u>underlined</u></strong> text.
        <ol>
            <li>First item</li>
            <li>Second <u>item</u></li>
            <li><b>Third item</b></li>
            <li>Fourth <sup>item</sup></li>
            <li>Fifth <sub>item</sub></li>
            <li>Sixth <i>item</i></li>
        </ol>
        Email support at mailto:support@example.com
        Or use <strong><i><a href="mailto:support@example.com">email support</a></i>.</strong>
        """.trimIndent()

    val markdown = """
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
    """.trimIndent()

    val linkColor = colorScheme.primary
    val customLinkColor = colorScheme.error
    var annotatedString by remember { mutableStateOf(AnnotatedString("")) }

    // Streaming Markdown State
    val markdownBlocks by streamMarkdownBlocks(markdown).collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    LaunchedEffect(html, markdown) {
        annotatedString = html.convertToAnnotatedString(
            params = RichTextHelperParams(
                linkColor = linkColor,
                customLinkColor = customLinkColor,
                customLinkShouldBeUnderlined = false,
                clickListeners = mapOf(
                    "Hello" to LinkInteractionListener {
                        Toast.makeText(context, "You clicked on Hello", Toast.LENGTH_SHORT).show()
                    },
                    "$500" to LinkInteractionListener {
                        scope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "",
                                        "$500"
                                    )
                                )
                            )
                        }
                        Toast.makeText(context, "You have copied $500", Toast.LENGTH_SHORT).show()
                    },
                    "strong" to LinkInteractionListener {
                        Toast.makeText(context, "You clicked on strong", Toast.LENGTH_SHORT).show()
                    },
                    "underlined" to LinkInteractionListener {
                        Toast.makeText(context, "You clicked on underlined", Toast.LENGTH_SHORT)
                            .show()
                    },
                )
            ),
            onException = { exception ->
                scope.launch {
                    Toast.makeText(context, "Error parsing HTML: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    Column(
        modifier = modifier
            .padding(
                start = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateStartPadding(LayoutDirection.Ltr) + 16.dp,
                end = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateEndPadding(LayoutDirection.Ltr) + 16.dp,
            )
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .padding(
                    top = WindowInsets
                        .displayCutout.union(insets = WindowInsets.statusBars)
                        .asPaddingValues()
                        .calculateTopPadding()
                )
                .fillMaxWidth()
        )

        Text(
            text = "HTML Content:",
            style = typography.titleMedium,
            color = colorScheme.onSurface
        )
        Text(
            text = annotatedString,
            style = typography.labelMedium.copy(color = colorScheme.onSurface),
        )

        Spacer(modifier = Modifier.padding(128.dp))

        Text(
            text = "Markdown Content:",
            style = typography.titleMedium,
            color = colorScheme.onSurface
        )

        WFMarkdownStyle(
            bodyStyle = typography.labelMedium.copy(color = colorScheme.onSurface),
            linkColor = linkColor,
        ).let { style ->
            markdownBlocks.forEach { block -> RenderBlock(block, style) }
        }

        Box(
            modifier = Modifier
                .padding(
                    bottom = WindowInsets
                        .displayCutout.union(insets = WindowInsets.navigationBars)
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
                .fillMaxWidth()
        )
    }
}