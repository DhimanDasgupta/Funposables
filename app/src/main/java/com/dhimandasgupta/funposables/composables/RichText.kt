package com.dhimandasgupta.funposables.composables

import android.util.Xml
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.em
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import kotlin.random.Random

@Composable
fun RichText(
    modifier: Modifier
) {
    val html = """
        This </br>is a sample<br /> HTML string.<br>
        Take my $500 for this.
        <p>Hello there how are <sup>you</sup>. If you want to <sub><b><u><i>contact me</i></u></b></sub> then reach me via <i><a href='tel:+910000000000'>phone</a></i> or via <a href='https://www.example.com'>website</a>.</p>
        <ol>
            <li>Name one</li>
            <li>Item two item with https://www.example.com</li>
            <li><b>Third item</b></li>
        </ol>
        <p>
        Visit https://www.example.com or call 1-800-622-4357.
        You can also use <a href='https://www.google.com'>this website</a>.
        1-800-662-HELP and 1-800-662-HELP or 1—800—662—HELP or 1-800-622-HELP
        </p>
        <p>Contact options:</p>
        <ul>
            <li>Call 1-800-662-HELP</li>
            <li>Visit https://www.example.com</li>
            <li><b>Emergency:</b> use  <a href='tel:+910000000000'>phone</a> support</li>
        </ul>
        <ol>
            <li>First item</li>
            <li>Second item with https://www.example.com</li>
            <li><b>Third item</b></li>
        </ol>
        """.trimIndent()

    val linkColor = colorScheme.primary
    var annotatedString by remember { mutableStateOf(AnnotatedString("")) }
    LaunchedEffect(html) {
        annotatedString = htmlToAnnotatedString(
            html = html,
            linkColor = linkColor
        )
    }

    Column(
        modifier = modifier
            .padding(
                start = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateStartPadding(LayoutDirection.Ltr),
                top = WindowInsets
                    .displayCutout.union(insets = WindowInsets.statusBars)
                    .asPaddingValues()
                    .calculateTopPadding(),
                end = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateEndPadding(LayoutDirection.Ltr),
                bottom = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = annotatedString,
            style = typography.labelMedium,
            color = colorScheme.onPrimary
        )
    }
}

private val AddExtraSpaceBetween = Random.nextBoolean()

private val UrlRegex = Regex(
    pattern = """https://[^\s<>"']+""",
    option = RegexOption.IGNORE_CASE
)

private val PhoneRegex = Regex(
    pattern = """\b(?:\+?\d{1,3}[-‐‑‒–—.\s]?)?\(?\d{3}\)?[-‐‑‒–—.\s]?\d{3}[-‐‑‒–—.\s]?[A-Z0-9]{4}\b""",
    option = RegexOption.IGNORE_CASE
)

private val AutoLinkRegex = Regex(
    pattern = """https://[^\s<>"']+|\b(?:\+?\d{1,3}[-‐‑‒–—.\s]?)?\(?\d{3}\)?[-‐‑‒–—.\s]?\d{3}[-‐‑‒–—.\s]?[A-Z0-9]{4}\b""",
    option = RegexOption.IGNORE_CASE
)

private sealed class HtmlListContext {
    data object Unordered : HtmlListContext()

    data class Ordered(
        var nextIndex: Int
    ) : HtmlListContext()
}

suspend fun htmlToAnnotatedString(
    html: String,
    linkColor: Color = Color(0xFF1565C0)
): AnnotatedString = withContext(Dispatchers.Default) {
    currentCoroutineContext().ensureActive()

    return@withContext try {
        val wrappedHtml = "<root>${
            html.replace("<br>", "\n")
                .replace("</br>", "\n")
                .replace("<br />", "\n")
        }</root>"

        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(StringReader(wrappedHtml))
        }

        buildAnnotatedString {
            currentCoroutineContext().ensureActive()

            parser.nextTag() // Move to <root>

            parseChildren(
                parser = parser,
                builder = this,
                parentTag = "root",
                linkColor = linkColor,
                listStack = mutableListOf()
            )
        }
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        println("Error parsing HTML: ${e.message}")
        buildAnnotatedString {}
    }
}

private suspend fun parseChildren(
    parser: XmlPullParser,
    builder: AnnotatedString.Builder,
    parentTag: String,
    linkColor: Color,
    listStack: MutableList<HtmlListContext>
) {
    var parsedEvents = 0

    while (true) {
        currentCoroutineContext().ensureActive()

        parsedEvents++
        if (parsedEvents % 50 == 0) {
            yield()
        }

        when (parser.next()) {
            XmlPullParser.START_TAG -> {
                parseTag(
                    parser = parser,
                    builder = builder,
                    linkColor = linkColor,
                    listStack = listStack
                )
            }

            XmlPullParser.TEXT -> {
                builder.appendTextWithAutoLinks(
                    text = parser.text,
                    linkColor = linkColor
                )
            }

            XmlPullParser.END_TAG -> {
                if (parser.name.equals(parentTag, ignoreCase = true)) {
                    return
                }
            }

            XmlPullParser.END_DOCUMENT -> {
                return
            }
        }
    }
}

private suspend fun parseTag(
    parser: XmlPullParser,
    builder: AnnotatedString.Builder,
    linkColor: Color,
    listStack: MutableList<HtmlListContext>
) = coroutineScope {
    currentCoroutineContext().ensureActive()

    when (val tagName = parser.name.lowercase()) {
        "p" -> {
            builder.withStyle(
                ParagraphStyle()
            ) {
                launch {
                    parseChildren(
                        parser = parser,
                        builder = builder,
                        parentTag = tagName,
                        linkColor = linkColor,
                        listStack = listStack
                    )
                }
            }

            if (builder.isNotBlank() && AddExtraSpaceBetween) {
                builder.append("\n\n")
            }
        }

        "ul" -> {
            if (builder.isNotBlank() && !builder.endsWithNewLine() && AddExtraSpaceBetween) {
                builder.append("\n")
            }

            listStack.add(HtmlListContext.Unordered)

            parseChildren(
                parser = parser,
                builder = builder,
                parentTag = tagName,
                linkColor = linkColor,
                listStack = listStack
            )

            listStack.removeLastOrNull()

            if (builder.isNotBlank() && !builder.endsWithDoubleNewLine() && AddExtraSpaceBetween) {
                builder.append("\n")
            }
        }

        "ol" -> {
            if (builder.isNotBlank() && !builder.endsWithNewLine() && AddExtraSpaceBetween) {
                builder.append("\n")
            }

            val startIndex = parser.getAttributeValue(null, "start")?.toIntOrNull() ?: 1

            listStack.add(HtmlListContext.Ordered(nextIndex = startIndex))

            parseChildren(
                parser = parser,
                builder = builder,
                parentTag = tagName,
                linkColor = linkColor,
                listStack = listStack
            )

            listStack.removeLastOrNull()

            if (builder.isNotBlank() && !builder.endsWithDoubleNewLine() && AddExtraSpaceBetween) {
                builder.append("\n")
            }
        }

        "li" -> {
            if (builder.isNotBlank() && !builder.endsWithNewLine() && AddExtraSpaceBetween) {
                builder.append("\n")
            }

            when (val currentList = listStack.lastOrNull()) {
                is HtmlListContext.Ordered -> {
                    builder.append("${currentList.nextIndex}. ")
                    currentList.nextIndex++
                }

                HtmlListContext.Unordered, null -> {
                    builder.append("• ")
                }
            }

            parseChildren(
                parser = parser,
                builder = builder,
                parentTag = tagName,
                linkColor = linkColor,
                listStack = listStack
            )

            if (!builder.endsWithNewLine() && AddExtraSpaceBetween) {
                builder.append("\n")
            }
        }

        "u" -> {
            builder.withStyle(
                SpanStyle(
                    textDecoration = TextDecoration.Underline
                )
            ) {
                parseChildren(
                    parser = parser,
                    builder = builder,
                    parentTag = tagName,
                    linkColor = linkColor,
                    listStack = listStack
                )
            }
        }

        "i" -> {
            builder.withStyle(
                SpanStyle(
                    fontStyle = FontStyle.Italic
                )
            ) {
                parseChildren(
                    parser = parser,
                    builder = builder,
                    parentTag = tagName,
                    linkColor = linkColor,
                    listStack = listStack
                )
            }
        }

        "a" -> {
            val href = parser.getAttributeValue(null, "href").orEmpty()

            if (href.isNotBlank()) {
                builder.withLink(
                    LinkAnnotation.Url(
                        url = href,
                        styles = linkStyles(linkColor)
                    )
                ) {
                    parseChildrenWithoutAutoLinks(
                        parser = parser,
                        builder = builder,
                        parentTag = tagName,
                        linkColor = linkColor,
                        listStack = listStack
                    )
                }
            } else {
                parseChildren(
                    parser = parser,
                    builder = builder,
                    parentTag = tagName,
                    linkColor = linkColor,
                    listStack = listStack
                )
            }
        }

        "b", "strong" -> {
            builder.withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold
                )
            ) {
                parseChildren(
                    parser = parser,
                    builder = builder,
                    parentTag = tagName,
                    linkColor = linkColor,
                    listStack = listStack
                )
            }
        }

        "sup" -> {
            builder.withStyle(
                SpanStyle(
                    baselineShift = BaselineShift.Superscript,
                    fontSize = 0.75.em
                )
            ) {
                parseChildren(
                    parser = parser,
                    builder = builder,
                    parentTag = tagName,
                    linkColor = linkColor,
                    listStack = listStack
                )
            }
        }

        "sub" -> {
            builder.withStyle(
                SpanStyle(
                    baselineShift = BaselineShift.Subscript,
                    fontSize = 0.75.em
                )
            ) {
                parseChildren(
                    parser = parser,
                    builder = builder,
                    parentTag = tagName,
                    linkColor = linkColor,
                    listStack = listStack
                )
            }
        }

        else -> {
            parseChildren(
                parser = parser,
                builder = builder,
                parentTag = tagName,
                linkColor = linkColor,
                listStack = listStack
            )
        }
    }
}

private suspend fun parseChildrenWithoutAutoLinks(
    parser: XmlPullParser,
    builder: AnnotatedString.Builder,
    parentTag: String,
    linkColor: Color,
    listStack: MutableList<HtmlListContext>
) {
    while (true) {
        currentCoroutineContext().ensureActive()

        when (parser.next()) {
            XmlPullParser.START_TAG -> {
                parseTag(
                    parser = parser,
                    builder = builder,
                    linkColor = linkColor,
                    listStack = listStack
                )
            }

            XmlPullParser.TEXT -> {
                builder.append(parser.text)
            }

            XmlPullParser.END_TAG -> {
                if (parser.name.equals(parentTag, ignoreCase = true)) {
                    return
                }
            }

            XmlPullParser.END_DOCUMENT -> {
                return
            }
        }
    }
}

private fun AnnotatedString.Builder.appendTextWithAutoLinks(
    text: String,
    linkColor: Color
) {
    var currentIndex = 0

    AutoLinkRegex.findAll(text).forEach { matchResult ->
        val matchStart = matchResult.range.first
        val matchEndExclusive = matchResult.range.last + 1

        if (currentIndex < matchStart) {
            append(text.substring(currentIndex, matchStart))
        }

        val matchedText = matchResult.value
        val url = when {
            matchedText.isHttpsUrl() -> matchedText
            matchedText.isPhoneNumber() -> matchedText.toTelUrl()
            else -> null
        }

        if (url != null) {
            withLink(
                LinkAnnotation.Url(
                    url = url,
                    styles = linkStyles(linkColor)
                )
            ) {
                append(matchedText)
            }
        } else {
            append(matchedText)
        }

        currentIndex = matchEndExclusive
    }

    if (currentIndex < text.length) {
        append(text.substring(currentIndex))
    }
}

private fun String.isHttpsUrl(): Boolean {
    return UrlRegex.matches(this)
}

private fun String.isPhoneNumber(): Boolean {
    return PhoneRegex.matches(this)
}

private fun String.toTelUrl(): String {
    val sanitizedPhoneNumber = filter { character ->
        character.isDigit() || character == '+'
    }

    return "tel:$sanitizedPhoneNumber"
}

private fun linkStyles(
    linkColor: Color
): TextLinkStyles {
    return TextLinkStyles(
        style = SpanStyle(
            color = linkColor,
            textDecoration = TextDecoration.Underline
        )
    )
}

private fun AnnotatedString.Builder.isNotBlank(): Boolean {
    return toAnnotatedString().text.isNotBlank()
}

private fun AnnotatedString.Builder.endsWithNewLine(): Boolean {
    return toAnnotatedString().text.endsWith("\n")
}

private fun AnnotatedString.Builder.endsWithDoubleNewLine(): Boolean {
    return toAnnotatedString().text.endsWith("\n\n")
}