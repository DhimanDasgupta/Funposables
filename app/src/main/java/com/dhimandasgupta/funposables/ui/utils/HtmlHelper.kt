package com.dhimandasgupta.funposables.ui.utils

import android.util.Xml
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import java.io.StringReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.xmlpull.v1.XmlPullParser

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
): AnnotatedString =
  withContext(dispatcher) {
    return@withContext try {
      val wrappedHtml =
        "<root>${
            this@convertToAnnotatedString.replace("<br>", "\n")
                .replace("</br>", "\n")
                .replace("<br />", "\n")
        }</root>"

      val parser =
        Xml.newPullParser().apply {
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
          includeAutoLinks = true,
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
 * Recursively parses the children of an XML tag and appends their content to the AnnotatedString
 * builder.
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
  includeAutoLinks: Boolean = true,
) {
  var parsedEvents = 0

  while (currentCoroutineContext().isActive) {
    currentCoroutineContext().ensureActive()

    /** Making sure that the dispatcher runs other tasks while parsing the HTML. */
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
          listStack = listStack,
        )
      }

      XmlPullParser.TEXT -> {
        builder.appendTextWithLinks(
          text = parser.text,
          params = params,
          includeAutoLinks = includeAutoLinks,
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
  listStack: MutableList<HtmlListContext>,
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

    "b",
    "strong" -> {
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
        listStack = listStack,
      )
    }
  }
}

private suspend fun subscriptContent(
  builder: AnnotatedString.Builder,
  params: RichTextHelperParams,
  parser: XmlPullParser,
  tagName: String,
  listStack: MutableList<HtmlListContext>,
) {
  builder.withStyle(
    SpanStyle(
      baselineShift = BaselineShift.Subscript,
      fontSize = params.superScriptFontSize.em,
    )
  ) {
    parseChildren(
      parser = parser,
      builder = builder,
      parentTag = tagName,
      params = params,
      listStack = listStack,
    )
  }
}

private suspend fun superscriptContent(
  builder: AnnotatedString.Builder,
  params: RichTextHelperParams,
  parser: XmlPullParser,
  tagName: String,
  listStack: MutableList<HtmlListContext>,
) {
  builder.withStyle(
    SpanStyle(
      baselineShift = BaselineShift.Superscript,
      fontSize = params.superScriptFontSize.em,
    )
  ) {
    parseChildren(
      parser = parser,
      builder = builder,
      parentTag = tagName,
      params = params,
      listStack = listStack,
    )
  }
}

private suspend fun boldOrStrongContent(
  builder: AnnotatedString.Builder,
  parser: XmlPullParser,
  tagName: String,
  params: RichTextHelperParams,
  listStack: MutableList<HtmlListContext>,
) {
  builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
    parseChildren(
      parser = parser,
      builder = builder,
      parentTag = tagName,
      params = params,
      listStack = listStack,
    )
  }
}

private suspend fun anchorContent(
  parser: XmlPullParser,
  builder: AnnotatedString.Builder,
  params: RichTextHelperParams,
  tagName: String,
  listStack: MutableList<HtmlListContext>,
) {
  val href = parser.getAttributeValue(null, "href").orEmpty()

  if (href.isNotBlank()) {
    builder.withLink(
      LinkAnnotation.Url(
        url = href,
        styles = linkStyles(params.linkColor),
      )
    ) {
      parseChildren(
        parser = parser,
        builder = builder,
        parentTag = tagName,
        params = params,
        listStack = listStack,
        includeAutoLinks = false,
      )
    }
  } else {
    parseChildren(
      parser = parser,
      builder = builder,
      parentTag = tagName,
      params = params,
      listStack = listStack,
      includeAutoLinks = true,
    )
  }
}

private suspend fun italicsContent(
  builder: AnnotatedString.Builder,
  parser: XmlPullParser,
  tagName: String,
  params: RichTextHelperParams,
  listStack: MutableList<HtmlListContext>,
) {
  builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
    parseChildren(
      parser = parser,
      builder = builder,
      parentTag = tagName,
      params = params,
      listStack = listStack,
    )
  }
}

private suspend fun underlinedContent(
  builder: AnnotatedString.Builder,
  parser: XmlPullParser,
  tagName: String,
  params: RichTextHelperParams,
  listStack: MutableList<HtmlListContext>,
) {
  builder.withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
    parseChildren(
      parser = parser,
      builder = builder,
      parentTag = tagName,
      params = params,
      listStack = listStack,
    )
  }
}

private suspend fun listItemContent(
  listStack: MutableList<HtmlListContext>,
  builder: AnnotatedString.Builder,
  parser: XmlPullParser,
  tagName: String,
  params: RichTextHelperParams,
) {
  when (val currentList = listStack.lastOrNull()) {
    is HtmlListContext.Ordered -> {
      builder.append("${currentList.nextIndex}. ")
      listStack[listStack.lastIndex] = currentList.copy(nextIndex = currentList.nextIndex + 1)
    }

    HtmlListContext.Unordered,
    null -> {
      builder.append("\u2022 ")
    }
  }

  parseChildren(
    parser = parser,
    builder = builder,
    parentTag = tagName,
    params = params,
    listStack = listStack,
  )
}

private suspend fun orderedListContent(
  parser: XmlPullParser,
  listStack: MutableList<HtmlListContext>,
  builder: AnnotatedString.Builder,
  tagName: String,
  params: RichTextHelperParams,
) {
  val startIndex = parser.getAttributeValue(null, "start")?.toIntOrNull() ?: 1

  listStack.add(HtmlListContext.Ordered(nextIndex = startIndex))

  parseChildren(
    parser = parser,
    builder = builder,
    parentTag = tagName,
    params = params,
    listStack = listStack,
  )

  listStack.removeLastOrNull()
}

private suspend fun unorderedListContent(
  listStack: MutableList<HtmlListContext>,
  parser: XmlPullParser,
  builder: AnnotatedString.Builder,
  tagName: String,
  params: RichTextHelperParams,
) {
  listStack.add(HtmlListContext.Unordered)

  parseChildren(
    parser = parser,
    builder = builder,
    parentTag = tagName,
    params = params,
    listStack = listStack,
  )

  listStack.removeLastOrNull()
}

private suspend fun paragraphContent(
  builder: AnnotatedString.Builder,
  parser: XmlPullParser,
  tagName: String,
  params: RichTextHelperParams,
  listStack: MutableList<HtmlListContext>,
) {
  val index = builder.pushStyle(ParagraphStyle())
  parseChildren(
    parser = parser,
    builder = builder,
    parentTag = tagName,
    params = params,
    listStack = listStack,
  )
  builder.pop(index)
}

private sealed class HtmlListContext {
  data object Unordered : HtmlListContext()

  data class Ordered(val nextIndex: Int) : HtmlListContext()
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
  val h1Style: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 2.0.em),
  val h2Style: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 1.7.em),
  val h3Style: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 1.5.em),
  val h4Style: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 1.3.em),
  val h5Style: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 1.2.em),
  val h6Style: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 1.1.em),
) {
  companion object {
    val Default = RichTextHelperParams()
  }
}
