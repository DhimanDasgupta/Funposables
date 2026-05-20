package com.dhimandasgupta.funposables.composables

import android.content.ClipData
import android.util.Xml
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

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
        1-800-662-HELP and 1-800-662-HELP or 1—800—662—HELP or 1-800-622-HELP
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

    val linkColor = colorScheme.primary
    val customLinkColor = colorScheme.error
    var annotatedString by remember { mutableStateOf(AnnotatedString("")) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(html) {
        annotatedString = htmlToAnnotatedString(
            html = html,
            linkColor = linkColor,
            customLinkColor = customLinkColor,
            customLinkShouldBeUnderlined = false,
            clickListeners = mapOf(
                "Hello" to LinkInteractionListener {
                    Toast.makeText(context, "You clicked on Hello", Toast.LENGTH_SHORT).show()
                },
                "$500" to LinkInteractionListener {
                    scope.launch {
                        clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("", "$500")))
                    }
                    Toast.makeText(context, "You have copied $500", Toast.LENGTH_SHORT).show()
                },
                "strong" to LinkInteractionListener {
                    Toast.makeText(context, "You clicked on strong", Toast.LENGTH_SHORT).show()
                },
                "underlined" to LinkInteractionListener {
                    Toast.makeText(context, "You clicked on underlined", Toast.LENGTH_SHORT).show()
                },
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
            text = annotatedString,
            style = typography.labelMedium,
            color = colorScheme.onPrimary
        )

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

private val UrlRegex = Regex(
    pattern = """https://[^\s<>"']+""",
    option = RegexOption.IGNORE_CASE
)

private val MailToRegex = Regex(
    pattern = """mailto:[^\s<>"']+""",
    option = RegexOption.IGNORE_CASE
)

private val PhoneRegex = Regex(
    pattern = """\b(?:\+?\d{1,3}[-‐‑‒–—.\s]?)?\(?\d{3}\)?[-‐‑‒–—.\s]?\d{3}[-‐‑‒–—.\s]?[A-Z0-9]{4}\b""",
    option = RegexOption.IGNORE_CASE
)

private val AutoLinkRegex = Regex(
    pattern = listOf(
        UrlRegex.pattern,
        MailToRegex.pattern,
        PhoneRegex.pattern,
    ).joinToString("|"),
    option = RegexOption.IGNORE_CASE
)

private sealed class HtmlListContext {
    data object Unordered : HtmlListContext()

    data class Ordered(
        val nextIndex: Int
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
    linkColor: Color = Color.Unspecified,
    customLinkColor: Color = Color.Unspecified,
    customLinkShouldBeUnderlined: Boolean = false,
    clickListeners: Map<String, LinkInteractionListener> = emptyMap(),
    onException: (Exception) -> Unit = {}
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
                customLinkColor = customLinkColor,
                customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                clickListeners = clickListeners,
                listStack = mutableListOf(),
                includeAutoLinks = true
            )
        }
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        println("Error parsing HTML: ${e.message}")
        onException(e)
        buildAnnotatedString {}
    }
}

private suspend fun parseChildren(
    parser: XmlPullParser,
    builder: AnnotatedString.Builder,
    parentTag: String,
    linkColor: Color,
    customLinkColor: Color,
    customLinkShouldBeUnderlined: Boolean,
    clickListeners: Map<String, LinkInteractionListener> = emptyMap(),
    listStack: MutableList<HtmlListContext>,
    includeAutoLinks: Boolean = true
) {
    var parsedEvents = 0

    while (currentCoroutineContext().isActive) {
        currentCoroutineContext().ensureActive()

        /**
         * Making sure that the dispatcher runs other tasks while parsing the HTML.
         * */
        parsedEvents++
        if (parsedEvents % 5 == 0) {
            yield()
        }

        when (parser.next()) {
            XmlPullParser.START_TAG -> {
                parseTag(
                    parser = parser,
                    builder = builder,
                    customLinkColor = customLinkColor,
                    customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                    linkColor = linkColor,
                    clickListeners = clickListeners,
                    listStack = listStack
                )
            }

            XmlPullParser.TEXT -> {
                builder.appendTextWithLinks(
                    text = parser.text,
                    customLinkColor = customLinkColor,
                    customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                    linkColor = linkColor,
                    clickListeners = clickListeners,
                    includeAutoLinks = includeAutoLinks
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
    customLinkColor: Color,
    customLinkShouldBeUnderlined: Boolean,
    clickListeners: Map<String, LinkInteractionListener> = emptyMap(),
    listStack: MutableList<HtmlListContext>
) {
    when (val tagName = parser.name.lowercase()) {
        "p" -> {
            val index = builder.pushStyle(ParagraphStyle())
            parseChildren(
                parser = parser,
                builder = builder,
                parentTag = tagName,
                linkColor = linkColor,
                customLinkColor = customLinkColor,
                customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                clickListeners = clickListeners,
                listStack = listStack
            )
            builder.pop(index)
        }

        "ul" -> {
            listStack.add(HtmlListContext.Unordered)

            parseChildren(
                parser = parser,
                builder = builder,
                parentTag = tagName,
                linkColor = linkColor,
                customLinkColor = customLinkColor,
                customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                clickListeners = clickListeners,
                listStack = listStack
            )

            listStack.removeLastOrNull()
        }

        "ol" -> {
            val startIndex = parser.getAttributeValue(null, "start")?.toIntOrNull() ?: 1

            listStack.add(HtmlListContext.Ordered(nextIndex = startIndex))

            parseChildren(
                parser = parser,
                builder = builder,
                parentTag = tagName,
                linkColor = linkColor,
                customLinkColor = customLinkColor,
                customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                clickListeners = clickListeners,
                listStack = listStack
            )

            listStack.removeLastOrNull()
        }

        "li" -> {
            when (val currentList = listStack.lastOrNull()) {
                is HtmlListContext.Ordered -> {
                    builder.append("${currentList.nextIndex}. ")
                    listStack[listStack.lastIndex] = currentList.copy(
                        nextIndex = currentList.nextIndex + 1
                    )
                }

                HtmlListContext.Unordered, null -> {
                    builder.append("\u2022 ")
                }
            }

            parseChildren(
                parser = parser,
                builder = builder,
                parentTag = tagName,
                linkColor = linkColor,
                customLinkColor = customLinkColor,
                customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                clickListeners = clickListeners,
                listStack = listStack
            )
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
                    customLinkColor = customLinkColor,
                    customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
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
                    customLinkColor = customLinkColor,
                    customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
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
                    parseChildren(
                        parser = parser,
                        builder = builder,
                        parentTag = tagName,
                        customLinkColor = customLinkColor,
                        customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                        linkColor = linkColor,
                        clickListeners = clickListeners,
                        listStack = listStack,
                        includeAutoLinks = false
                    )
                }
            } else {
                parseChildren(
                    parser = parser,
                    builder = builder,
                    parentTag = tagName,
                    linkColor = linkColor,
                    customLinkColor = customLinkColor,
                    customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                    listStack = listStack,
                    includeAutoLinks = true
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
                    customLinkColor = customLinkColor,
                    customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                    clickListeners = clickListeners,
                    listStack = listStack
                )
            }
        }

        "sup" -> {
            builder.withStyle(
                SpanStyle(
                    baselineShift = BaselineShift.Superscript,
                    fontSize = 0.61.em
                )
            ) {
                parseChildren(
                    parser = parser,
                    builder = builder,
                    parentTag = tagName,
                    linkColor = linkColor,
                    customLinkColor = customLinkColor,
                    customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                    clickListeners = clickListeners,
                    listStack = listStack
                )
            }
        }

        "sub" -> {
            builder.withStyle(
                SpanStyle(
                    baselineShift = BaselineShift.Subscript,
                    fontSize = 0.61.em
                )
            ) {
                parseChildren(
                    parser = parser,
                    builder = builder,
                    parentTag = tagName,
                    linkColor = linkColor,
                    customLinkColor = customLinkColor,
                    customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
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
                customLinkColor = customLinkColor,
                customLinkShouldBeUnderlined = customLinkShouldBeUnderlined,
                clickListeners = clickListeners,
                listStack = listStack
            )
        }
    }
}

private fun AnnotatedString.Builder.appendTextWithLinks(
    text: String,
    linkColor: Color,
    customLinkColor: Color,
    customLinkShouldBeUnderlined: Boolean,
    clickListeners: Map<String, LinkInteractionListener>,
    includeAutoLinks: Boolean
) {
    if (text.isEmpty()) return

    val customMatches = if (clickListeners.isNotEmpty()) {
        val patterns = clickListeners.keys.filter { it.isNotEmpty() }
        if (patterns.isNotEmpty()) {
            val combinedPattern = patterns
                .sortedByDescending { it.length }
                .joinToString("|") { Regex.escape(it) }
            Regex(combinedPattern).findAll(text).map { matchResult ->
                TextLinkMatch.Custom(
                    start = matchResult.range.first,
                    endExclusive = matchResult.range.last + 1,
                    text = matchResult.value,
                    listener = clickListeners[matchResult.value]!!
                )
            }
        } else emptySequence()
    } else emptySequence()

    val autoMatches = if (includeAutoLinks) {
        AutoLinkRegex.findAll(text).mapNotNull { matchResult ->
            val matchedText = matchResult.value
            val url = when {
                matchedText.isHttpsUrl() -> matchedText
                matchedText.isMailToUrl() -> matchedText
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

    val matches = (customMatches + autoMatches)
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
                        styles = if (customLinkShouldBeUnderlined) linkStyles(customLinkColor) else noStyle(),
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
                        styles = linkStyles(linkColor),
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

private fun String.isHttpsUrl() = UrlRegex.matches(this)

private fun String.isMailToUrl() = MailToRegex.matches(this)

private fun String.isPhoneNumber() = PhoneRegex.matches(this) && any(Char::isDigit)

private fun String.toTelUrl() = "tel:${
    buildString {
        this@toTelUrl.forEachIndexed { index, character ->
            when {
                character.isDigit() -> append(character)
                character == '+' && index == 0 -> append(character)
                character.isLetter() -> append(character.toPhoneKeypadDigit())
            }
        }
    }
}"

private fun Char.toPhoneKeypadDigit() = when (uppercaseChar()) {
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

private fun noStyle() = TextLinkStyles(
    style = SpanStyle()
)

private fun linkStyles(
    linkColor: Color
) = TextLinkStyles(
    style = SpanStyle(
        color = linkColor,
        textDecoration = TextDecoration.Underline
    )
)