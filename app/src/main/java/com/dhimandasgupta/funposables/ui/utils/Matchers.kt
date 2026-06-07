package com.dhimandasgupta.funposables.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import okhttp3.internal.toImmutableList

sealed class TextLinkMatch {
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

fun AnnotatedString.Builder.appendTextWithLinks(
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

fun noStyle() = TextLinkStyles(
    style = SpanStyle()
)

fun linkStyles(
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

// Separator class for vanity subscriber portions.
// Intentionally excludes whitespace so matches do not accidentally consume the following words.
private const val VANITY_PHONE_SEP = "[-\u2013\u2014.]"

// Numeric NANP: optional +1 / 1, area code, then 3 + 4 digits
private val NumericPhoneRegex = Regex(
    pattern = "(?<!\\w)(?:\\+?1$PHONE_SEP?)?\\(?\\d{3}\\)?$PHONE_SEP?\\d{3}$PHONE_SEP?\\d{4}(?!\\w)"
)

// Vanity NANP: optional +1 / 1, area code, then 7+ subscriber chars made of digits/letters,
// optionally separated by hyphen/en dash/em dash/dot, with at least one letter.
private val VanityPhoneRegex = Regex(
    pattern = "(?<!\\w)(?:\\+?1$PHONE_SEP?)?\\(?\\d{3}\\)?$PHONE_SEP?" +
            "(?=(?:[A-Z0-9]$VANITY_PHONE_SEP?){7,}(?![A-Z0-9]))" +
            "(?=[A-Z0-9$VANITY_PHONE_SEP]*[A-Z])" +
            "[A-Z0-9]+(?:$VANITY_PHONE_SEP[A-Z0-9]+)*(?!\\w)",
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