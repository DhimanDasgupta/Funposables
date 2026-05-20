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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okhttp3.internal.toImmutableList
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

/**
 * Data class to hold parameters for customizing the behavior and appearance of rich text.
 *
 * @property linkColor The color of links in the text.
 * @property customLinkColor The color of custom links in the text.
 * @property customLinkShouldBeUnderlined Whether custom links should be underlined.
 * @property superScriptFontSize The font size for superscript text.
 * @property clickListeners A map of custom link tags to their respective click listeners.
 */
data class RichTextHelperParams(
    val linkColor: Color = Color.Unspecified,
    val customLinkColor: Color = Color.Unspecified,
    val customLinkShouldBeUnderlined: Boolean = false,
    val superScriptFontSize: Float = 0.61f,
    val clickListeners: Map<String, LinkInteractionListener> = emptyMap(),
) {
    companion object {
        val Default = RichTextHelperParams()
    }
}

/**
 * Extension function to convert an HTML string into an AnnotatedString.
 *
 * @param dispatcher The coroutine dispatcher to use for parsing.
 * @param params Parameters for customizing the appearance and behavior of the text.
 * @param onException A callback for handling exceptions during parsing.
 * @return An AnnotatedString representing the parsed HTML content.
 */
suspend fun String.convertToAnnotatedString(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    params: RichTextHelperParams = RichTextHelperParams.Default,
    onException: (Exception) -> Unit = {},
): AnnotatedString = withContext(dispatcher) {
    return@withContext try {
        val wrappedHtml = "<root>${
            this@convertToAnnotatedString.replace("<br>", "\n")
                .replace("</br>", "\n")
                .replace("<br />", "\n")
        }</root>"

        val parser = Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(StringReader(wrappedHtml))
        }

        buildAnnotatedString {
            currentCoroutineContext().ensureActive()

            parser.nextTag()

            parseChildren(
                parser = parser,
                builder = this,
                parentTag = "root",
                params = params,
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

/**
 * Recursively parses the children of an XML tag and appends their content to the AnnotatedString builder.
 *
 * @param parser The XML parser.
 * @param builder The AnnotatedString builder.
 * @param parentTag The name of the parent tag.
 * @param params Parameters for customizing the appearance and behavior of the text.
 * @param listStack A stack to manage nested lists.
 * @param includeAutoLinks Whether to include automatic links in the text.
 */
private suspend fun parseChildren(
    parser: XmlPullParser,
    builder: AnnotatedString.Builder,
    parentTag: String,
    params: RichTextHelperParams,
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
                    params = params,
                    listStack = listStack
                )
            }

            XmlPullParser.TEXT -> {
                builder.appendTextWithLinks(
                    text = parser.text,
                    params = params,
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

/**
 * Parses an individual XML tag and applies the appropriate styles or actions.
 *
 * @param parser The XML parser.
 * @param builder The AnnotatedString builder.
 * @param params Parameters for customizing the appearance and behavior of the text.
 * @param listStack A stack to manage nested lists.
 */
private suspend fun parseTag(
    parser: XmlPullParser,
    builder: AnnotatedString.Builder,
    params: RichTextHelperParams,
    listStack: MutableList<HtmlListContext>
) {
    when (val tagName = parser.name.lowercase()) {
        "p" -> {
            paragraphContent(builder, parser, tagName, params, listStack)
        }

        "ul" -> {
            unorderedListContent(listStack, parser, builder, tagName, params)
        }

        "ol" -> {
            orderedListContent(parser, listStack, builder, tagName, params)
        }

        "li" -> {
            listItemContent(listStack, builder, parser, tagName, params)
        }

        "u" -> {
            underlinedContent(builder, parser, tagName, params, listStack)
        }

        "i" -> {
            italicsContent(builder, parser, tagName, params, listStack)
        }

        "a" -> {
            anchorContent(parser, builder, params, tagName, listStack)
        }

        "b", "strong" -> {
            boldOrStrongContent(builder, parser, tagName, params, listStack)
        }

        "sup" -> {
            superscriptContent(builder, params, parser, tagName, listStack)
        }

        "sub" -> {
            subscriptContent(builder, params, parser, tagName, listStack)
        }

        else -> {
            parseChildren(
                parser = parser,
                builder = builder,
                parentTag = tagName,
                params = params,
                listStack = listStack
            )
        }
    }
}

private suspend fun subscriptContent(
    builder: AnnotatedString.Builder,
    params: RichTextHelperParams,
    parser: XmlPullParser,
    tagName: String,
    listStack: MutableList<HtmlListContext>
) {
    builder.withStyle(
        SpanStyle(
            baselineShift = BaselineShift.Subscript,
            fontSize = params.superScriptFontSize.em
        )
    ) {
        parseChildren(
            parser = parser,
            builder = builder,
            parentTag = tagName,
            params = params,
            listStack = listStack
        )
    }
}

private suspend fun superscriptContent(
    builder: AnnotatedString.Builder,
    params: RichTextHelperParams,
    parser: XmlPullParser,
    tagName: String,
    listStack: MutableList<HtmlListContext>
) {
    builder.withStyle(
        SpanStyle(
            baselineShift = BaselineShift.Superscript,
            fontSize = params.superScriptFontSize.em
        )
    ) {
        parseChildren(
            parser = parser,
            builder = builder,
            parentTag = tagName,
            params = params,
            listStack = listStack
        )
    }
}

private suspend fun boldOrStrongContent(
    builder: AnnotatedString.Builder,
    parser: XmlPullParser,
    tagName: String,
    params: RichTextHelperParams,
    listStack: MutableList<HtmlListContext>
) {
    builder.withStyle(
        SpanStyle(
            fontWeight = FontWeight.Bold
        )
    ) {
        parseChildren(
            parser = parser,
            builder = builder,
            parentTag = tagName,
            params = params,
            listStack = listStack
        )
    }
}

private suspend fun anchorContent(
    parser: XmlPullParser,
    builder: AnnotatedString.Builder,
    params: RichTextHelperParams,
    tagName: String,
    listStack: MutableList<HtmlListContext>
) {
    val href = parser.getAttributeValue(null, "href").orEmpty()

    if (href.isNotBlank()) {
        builder.withLink(
            LinkAnnotation.Url(
                url = href,
                styles = linkStyles(params.linkColor)
            )
        ) {
            parseChildren(
                parser = parser,
                builder = builder,
                parentTag = tagName,
                params = params,
                listStack = listStack,
                includeAutoLinks = false
            )
        }
    } else {
        parseChildren(
            parser = parser,
            builder = builder,
            parentTag = tagName,
            params = params,
            listStack = listStack,
            includeAutoLinks = true
        )
    }
}

private suspend fun italicsContent(
    builder: AnnotatedString.Builder,
    parser: XmlPullParser,
    tagName: String,
    params: RichTextHelperParams,
    listStack: MutableList<HtmlListContext>
) {
    builder.withStyle(
        SpanStyle(
            fontStyle = FontStyle.Italic
        )
    ) {
        parseChildren(
            parser = parser,
            builder = builder,
            parentTag = tagName,
            params = params,
            listStack = listStack
        )
    }
}

private suspend fun underlinedContent(
    builder: AnnotatedString.Builder,
    parser: XmlPullParser,
    tagName: String,
    params: RichTextHelperParams,
    listStack: MutableList<HtmlListContext>
) {
    builder.withStyle(
        SpanStyle(
            textDecoration = TextDecoration.Underline
        )
    ) {
        parseChildren(
            parser = parser,
            builder = builder,
            parentTag = tagName,
            params = params,
            listStack = listStack
        )
    }
}

private suspend fun listItemContent(
    listStack: MutableList<HtmlListContext>,
    builder: AnnotatedString.Builder,
    parser: XmlPullParser,
    tagName: String,
    params: RichTextHelperParams
) {
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
        params = params,
        listStack = listStack
    )
}

private suspend fun orderedListContent(
    parser: XmlPullParser,
    listStack: MutableList<HtmlListContext>,
    builder: AnnotatedString.Builder,
    tagName: String,
    params: RichTextHelperParams
) {
    val startIndex = parser.getAttributeValue(null, "start")?.toIntOrNull() ?: 1

    listStack.add(HtmlListContext.Ordered(nextIndex = startIndex))

    parseChildren(
        parser = parser,
        builder = builder,
        parentTag = tagName,
        params = params,
        listStack = listStack
    )

    listStack.removeLastOrNull()
}

private suspend fun unorderedListContent(
    listStack: MutableList<HtmlListContext>,
    parser: XmlPullParser,
    builder: AnnotatedString.Builder,
    tagName: String,
    params: RichTextHelperParams
) {
    listStack.add(HtmlListContext.Unordered)

    parseChildren(
        parser = parser,
        builder = builder,
        parentTag = tagName,
        params = params,
        listStack = listStack
    )

    listStack.removeLastOrNull()
}

private suspend fun paragraphContent(
    builder: AnnotatedString.Builder,
    parser: XmlPullParser,
    tagName: String,
    params: RichTextHelperParams,
    listStack: MutableList<HtmlListContext>
) {
    val index = builder.pushStyle(ParagraphStyle())
    parseChildren(
        parser = parser,
        builder = builder,
        parentTag = tagName,
        params = params,
        listStack = listStack
    )
    builder.pop(index)
}

private fun AnnotatedString.Builder.appendTextWithLinks(
    text: String,
    params: RichTextHelperParams,
    includeAutoLinks: Boolean
) {
    if (text.isEmpty()) return

    val customMatches = getCustomMatchesAsSequence(params.clickListeners, text)

    val autoMatches = autoMatchesAsSequence(includeAutoLinks, text)

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
        }.toImmutableList()

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
                        styles = if (params.customLinkShouldBeUnderlined) linkStyles(params.customLinkColor) else noStyle(),
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
                        styles = linkStyles(params.linkColor),
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

private fun autoMatchesAsSequence(
    includeAutoLinks: Boolean,
    text: String
): Sequence<TextLinkMatch.Auto> {
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
    return autoMatches
}

private fun getCustomMatchesAsSequence(
    clickListeners: Map<String, LinkInteractionListener>,
    text: String
): Sequence<TextLinkMatch.Custom> {
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
    return customMatches
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

private val UrlRegex = Regex(
    pattern = """https://[^\s<>"']+""",
    option = RegexOption.IGNORE_CASE
)

private val MailToRegex = Regex(
    pattern = """mailto:[^\s<>"']+""",
    option = RegexOption.IGNORE_CASE
)

// Separator class: hyphen, en dash, em dash, dot, or space
private const val PHONE_SEP = "[-\u2013\u2014.\\s]"

// Numeric NANP: optional +1 / 1, area code, then 3 + 4 digits
private val NumericPhoneRegex = Regex(
    pattern = "(?<!\\w)(?:\\+?1$PHONE_SEP?)?\\(?\\d{3}\\)?$PHONE_SEP?\\d{3}$PHONE_SEP?\\d{4}(?!\\w)"
)

// Vanity NANP: optional +1 / 1, area code, then 7 chars made of digits/letters
// with at least one letter somewhere in the subscriber portion.
private val VanityPhoneRegex = Regex(
    pattern = "(?<!\\w)(?:\\+?1$PHONE_SEP?)?\\(?\\d{3}\\)?$PHONE_SEP?" +
            "(?=[A-Z0-9$PHONE_SEP]{7,}\\b)" +
            "(?=[^A-Z]*[A-Z])" +
            "[A-Z0-9]{3}$PHONE_SEP?[A-Z0-9]{4}(?!\\w)",
    option = RegexOption.IGNORE_CASE
)

// Merged regex used by the parser
private val PhoneRegex = Regex(
    pattern = "${NumericPhoneRegex.pattern}|${VanityPhoneRegex.pattern}",
    option = RegexOption.IGNORE_CASE
)

// Merged regex for auto-linking: URLs, mailto links, and phone numbers
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
