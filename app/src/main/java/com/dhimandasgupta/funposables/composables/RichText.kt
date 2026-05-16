package com.dhimandasgupta.funposables.composables

import android.content.ClipData
import android.util.Xml
import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current

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
        This is a plain and simple string. This dose not have any style what so ever. 
        """.trimIndent()

    val linkColor = colorScheme.primary
    var annotatedString by remember { mutableStateOf(AnnotatedString("")) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(html) {
        annotatedString = htmlToAnnotatedString(
            html = html,
            linkColor = linkColor,
            clickListeners = mapOf(
                "Hello" to LinkInteractionListener {
                    Toast.makeText(context, "You clicked on 'Hello'", Toast.LENGTH_SHORT).show()
                },
                "$500" to LinkInteractionListener {
                    scope.launch {
                        clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("", "$500")))
                    }
                    Toast.makeText(context, "You have copied $500", Toast.LENGTH_SHORT).show()
                }
            )
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

private sealed class TextLinkMatch {
    abstract val start: Int
    abstract val endExclusive: Int
    abstract val text: String

    data class Custom(
        override val start: Int,
        override val endExclusive: Int,
        override val text: String,
        val listener: LinkInteractionListener
    ) : TextLinkMatch()

    data class Auto(
        override val start: Int,
        override val endExclusive: Int,
        override val text: String,
        val url: String
    ) : TextLinkMatch()
}

suspend fun htmlToAnnotatedString(
    html: String,
    linkColor: Color = Color(0xFF1565C0),
    clickListeners: Map<String, LinkInteractionListener> = emptyMap()
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
                clickListeners = clickListeners,
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
    clickListeners: Map<String, LinkInteractionListener> = emptyMap(),
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
                    clickListeners = clickListeners,
                    listStack = listStack
                )
            }

            XmlPullParser.TEXT -> {
                builder.appendTextWithAutoLinks(
                    text = parser.text,
                    linkColor = linkColor,
                    clickListeners = clickListeners
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
    clickListeners: Map<String, LinkInteractionListener> = emptyMap(),
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
                        clickListeners = clickListeners,
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
                clickListeners = clickListeners,
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
                clickListeners = clickListeners,
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
                clickListeners = clickListeners,
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
                    clickListeners = clickListeners,
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
                    clickListeners = clickListeners,
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
                        clickListeners = clickListeners,
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
                    clickListeners = clickListeners,
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
                    clickListeners = clickListeners,
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
                    clickListeners = clickListeners,
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
                clickListeners = clickListeners,
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
    clickListeners: Map<String, LinkInteractionListener> = emptyMap(),
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
                    clickListeners = clickListeners,
                    listStack = listStack
                )
            }

            XmlPullParser.TEXT -> {
                builder.appendTextWithCustomLinks(
                    text = parser.text,
                    linkColor = linkColor,
                    clickListeners = clickListeners,
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

private fun AnnotatedString.Builder.appendTextWithAutoLinks(
    text: String,
    linkColor: Color,
    clickListeners: Map<String, LinkInteractionListener>
) {
    appendTextWithLinks(
        text = text,
        linkColor = linkColor,
        clickListeners = clickListeners,
        includeAutoLinks = true
    )
}

private fun AnnotatedString.Builder.appendTextWithCustomLinks(
    text: String,
    linkColor: Color,
    clickListeners: Map<String, LinkInteractionListener>
) {
    appendTextWithLinks(
        text = text,
        linkColor = linkColor,
        clickListeners = clickListeners,
        includeAutoLinks = false
    )
}

private fun AnnotatedString.Builder.appendTextWithLinks(
    text: String,
    linkColor: Color,
    clickListeners: Map<String, LinkInteractionListener>,
    includeAutoLinks: Boolean
) {
    if (text.isEmpty()) return

    val customMatches = clickListeners
        .filterKeys { it.isNotEmpty() }
        .flatMap { (clickableText, listener) ->
            Regex.escape(clickableText)
                .toRegex()
                .findAll(text)
                .map { matchResult ->
                    TextLinkMatch.Custom(
                        start = matchResult.range.first,
                        endExclusive = matchResult.range.last + 1,
                        text = matchResult.value,
                        listener = listener
                    )
                }
        }

    val autoMatches = if (includeAutoLinks) {
        AutoLinkRegex.findAll(text).mapNotNull { matchResult ->
            val matchedText = matchResult.value
            val url = when {
                matchedText.isHttpsUrl() -> matchedText
                matchedText.isPhoneNumber() -> matchedText.toTelUrl()
                else -> null
            }

            url?.let {
                TextLinkMatch.Auto(
                    start = matchResult.range.first,
                    endExclusive = matchResult.range.last + 1,
                    text = matchedText,
                    url = it
                )
            }
        }
    } else {
        emptySequence()
    }

    val matches = (customMatches.asSequence() + autoMatches)
        .sortedWith(
            compareBy<TextLinkMatch> { it.start }
                .thenByDescending { it.endExclusive - it.start }
                .thenBy { if (it is TextLinkMatch.Custom) 0 else 1 }
        )
        .fold(mutableListOf<TextLinkMatch>()) { acceptedMatches, candidate ->
            val overlapsExistingMatch = acceptedMatches.any { accepted ->
                candidate.start < accepted.endExclusive && candidate.endExclusive > accepted.start
            }

            if (!overlapsExistingMatch) {
                acceptedMatches.add(candidate)
            }

            acceptedMatches
        }

    var currentIndex = 0

    matches.forEach { match ->
        if (currentIndex < match.start) {
            append(text.substring(currentIndex, match.start))
        }

        when (match) {
            is TextLinkMatch.Custom -> {
                withLink(
                    LinkAnnotation.Clickable(
                        tag = match.text,
                        styles = linkStyles(linkColor),
                        linkInteractionListener = match.listener
                    )
                ) {
                    append(match.text)
                }
            }

            is TextLinkMatch.Auto -> {
                withLink(
                    LinkAnnotation.Url(
                        url = match.url,
                        styles = linkStyles(linkColor)
                    )
                ) {
                    append(match.text)
                }
            }
        }

        currentIndex = match.endExclusive
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
    val sanitizedPhoneNumber = buildString {
        this@toTelUrl.forEachIndexed { index, character ->
            when {
                character.isDigit() -> append(character)
                character == '+' && index == 0 -> append(character)
                character.isLetter() -> append(character.toPhoneKeypadDigit())
            }
        }
    }

    return "tel:$sanitizedPhoneNumber"
}

private fun Char.toPhoneKeypadDigit(): Char {
    return when (uppercaseChar()) {
        'A', 'B', 'C' -> '2'
        'D', 'E', 'F' -> '3'
        'G', 'H', 'I' -> '4'
        'J', 'K', 'L' -> '5'
        'M', 'N', 'O' -> '6'
        'P', 'Q', 'R', 'S' -> '7'
        'T', 'U', 'V' -> '8'
        'W', 'X', 'Y', 'Z' -> '9'
        else -> this
    }
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