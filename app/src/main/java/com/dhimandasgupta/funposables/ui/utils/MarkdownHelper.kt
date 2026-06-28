package com.dhimandasgupta.funposables.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/* Configuration for styling the rendered Markdown content.
 *
 * @param bodyStyle Base text style for body/paragraph text.
 * @param h1Style Text style for level-1 headings.
 * @param h2Style Text style for level-2 headings.
 * @param h3Style Text style for level-3 headings.
 * @param h4Style Text style for level-4 headings.
 * @param h5Style Text style for level-5 headings.
 * @param h6Style Text style for level-6 headings.
 * @param codeTextStyle Text style for inline code and code blocks.
 * @param codeBackgroundColor Background color for inline code spans.
 * @param codeBlockBackgroundColor Background color for fenced code blocks.
 * @param blockquoteBorderColor Left-border color for blockquotes.
 * @param blockquoteTextColor Text color inside blockquotes.
 * @param dividerColor Color of the horizontal rule divider.
 * @param linkColor Color applied to link text.
 * @param urlLinkStyles Optional [TextLinkStyles] for URL links.
 * @param linkClickListeners Optional map of link-text to [LinkInteractionListener] for handling clicks.
 * @param tableBorderColor Border color for table cells.
 * @param tableHeaderBackgroundColor Background color for table header row.
 * @param tableHeaderStyle Text style for table header cells.
 */
@Immutable
data class WFMarkdownStyle(
  val bodyStyle: TextStyle = TextStyle.Default,
  val h1Style: TextStyle = TextStyle.Default.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold),
  val h2Style: TextStyle = TextStyle.Default.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
  val h3Style: TextStyle = TextStyle.Default.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
  val h4Style: TextStyle = TextStyle.Default.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
  val h5Style: TextStyle = TextStyle.Default.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
  val h6Style: TextStyle = TextStyle.Default.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
  val codeTextStyle: TextStyle = TextStyle.Default.copy(fontFamily = FontFamily.Monospace),
  val codeBackgroundColor: Color = Color.Unspecified,
  val codeBlockBackgroundColor: Color = Color.Unspecified,
  val blockquoteBorderColor: Color = Color.Unspecified,
  val blockquoteTextColor: Color = Color.Unspecified,
  val dividerColor: Color = Color.Unspecified,
  val linkColor: Color = Color.Unspecified,
  val urlLinkStyles: TextLinkStyles? = null,
  val linkClickListeners: Map<String, LinkInteractionListener> = emptyMap(),
  val tableBorderColor: Color = Color.Unspecified,
  val tableHeaderBackgroundColor: Color = Color.Unspecified,
  val tableHeaderStyle: TextStyle = TextStyle.Default.copy(fontWeight = FontWeight.Bold),
)

/**
 * Renders Markdown text as Jetpack Compose composables.
 *
 * Parses the provided [markdown] string and emits a vertical column of composable blocks —
 * headings, paragraphs, lists, code blocks, blockquotes, and horizontal rules. Inline formatting
 * (bold, italic, strikethrough, inline code, links) is supported within all text-bearing blocks.
 *
 * Supported Markdown syntax:
 * - `# … ######` headings
 * - `**bold**` / `__bold__`
 * - `*italic*` / `_italic_`
 * - `***bold italic***`
 * - `~~strikethrough~~`
 * - `` `inline code` ``
 * - `[text](url)` links
 * - Unordered lists (`-` or `*` prefix)
 * - Ordered lists (`1.` prefix)
 * - Fenced code blocks (triple back-ticks)
 * - `>` blockquotes
 * - `---` / `***` / `___` horizontal rules
 * - `![alt](url)` images
 * - `| col | col |` tables with `|---|---|` separator
 * - [ ] Unchecked item
 * - [X] Checked item
 * - [ ] Another unchecked item
 */
@Composable
private fun rememberDefaultMarkdownStyle(): WFMarkdownStyle {
  val typography = typography // LocalWFTypography.current
  val colors = colorScheme // LocalWFColorsPalette.current
  return WFMarkdownStyle(
    /*bodyStyle = typography.bodyDefault,
    h1Style = typography.headlineLgStrong.copy(fontSize = 30.sp),
    h2Style = typography.headlineLgStrong.copy(fontSize = 26.sp),
    h3Style = typography.headlineDefaultStrong.copy(fontSize = 22.sp),
    h4Style = typography.headlineSmStrong.copy(fontSize = 18.sp),
    h5Style = typography.bodyDefaultStrong.copy(fontSize = 16.sp),
    h6Style = typography.bodyDefaultStrong.copy(fontSize = 14.sp),
    codeTextStyle = typography.bodySm.copy(fontFamily = FontFamily.Monospace),
    codeBackgroundColor = colors.bg.fill.primary.default,
    codeBlockBackgroundColor = colors.bg.fill.primary.default,
    blockquoteBorderColor = colors.border.primary.default,
    blockquoteTextColor = colors.text.tertiary,
    dividerColor = colors.border.secondary.default,
    linkColor = colors.text.link.default,
    tableBorderColor = colors.border.secondary.default,
    tableHeaderBackgroundColor = colors.bg.fill.primary.default,
    tableHeaderStyle = typography.bodyDefaultStrong*/
  )
}

// region Block-level Rendering

@Composable
fun RenderBlock(
  block: MarkdownBlock,
  style: WFMarkdownStyle,
) {
  when (block) {
    is MarkdownBlock.Heading -> RenderHeading(block, style)
    is MarkdownBlock.Paragraph -> RenderParagraph(block, style)
    is MarkdownBlock.CodeBlock -> RenderCodeBlock(block, style)
    is MarkdownBlock.Blockquote -> RenderBlockquote(block, style)
    is MarkdownBlock.UnorderedList -> RenderUnorderedList(block, style)
    is MarkdownBlock.OrderedList -> RenderOrderedList(block, style)
    is MarkdownBlock.HorizontalRule -> RenderHorizontalRule(style)
    is MarkdownBlock.Image -> RenderImage(block)
    is MarkdownBlock.Table -> RenderTable(block, style)
  }
}

@Composable
private fun RenderHeading(
  heading: MarkdownBlock.Heading,
  style: WFMarkdownStyle,
) {
  val textStyle =
    when (heading.level) {
      1 -> style.h1Style
      2 -> style.h2Style
      3 -> style.h3Style
      4 -> style.h4Style
      5 -> style.h5Style
      else -> style.h6Style
    }
  Text(
    text = rememberParsedMarkdown(heading.text, style),
    style = textStyle,
    modifier = Modifier.fillMaxWidth().semantics { testTag = "WFMarkdownText_H${heading.level}" },
  )
}

@Composable
private fun RenderParagraph(
  paragraph: MarkdownBlock.Paragraph,
  style: WFMarkdownStyle,
) {
  Text(
    text = rememberParsedMarkdown(paragraph.text, style),
    style = style.bodyStyle,
    modifier = Modifier.fillMaxWidth().semantics { testTag = "WFMarkdownText_Paragraph" },
  )
}

@Composable
private fun RenderCodeBlock(
  codeBlock: MarkdownBlock.CodeBlock,
  style: WFMarkdownStyle,
) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .background(
          color = style.codeBlockBackgroundColor,
          shape = RoundedCornerShape(4.dp),
        )
        .padding(8.dp)
        .horizontalScroll(rememberScrollState())
        .semantics { testTag = "WFMarkdownText_CodeBlock" }
  ) {
    Text(
      text = codeBlock.code,
      style = style.codeTextStyle,
    )
  }
}

@Composable
private fun RenderBlockquote(
  blockquote: MarkdownBlock.Blockquote,
  style: WFMarkdownStyle,
) {
  val borderColor = style.blockquoteBorderColor
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .drawBehind {
          drawLine(
            color = borderColor,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 4.dp.toPx(),
          )
        }
        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
        .semantics { testTag = "WFMarkdownText_Blockquote" }
  ) {
    Text(
      text = rememberParsedMarkdown(blockquote.text, style),
      style =
        style.bodyStyle.copy(
          fontStyle = FontStyle.Italic,
          color = style.blockquoteTextColor,
        ),
    )
  }
}

@Composable
private fun RenderUnorderedList(
  list: MarkdownBlock.UnorderedList,
  style: WFMarkdownStyle,
) {
  Column(modifier = Modifier.semantics { testTag = "WFMarkdownText_UnorderedList" }) {
    list.items.forEach { item ->
      Row(modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)) {
        Text(
          text = "\u2022",
          style = style.bodyStyle,
          modifier = Modifier.padding(end = 8.dp),
        )
        Text(
          text = rememberParsedMarkdown(item, style),
          style = style.bodyStyle,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun RenderOrderedList(
  list: MarkdownBlock.OrderedList,
  style: WFMarkdownStyle,
) {
  Column(modifier = Modifier.semantics { testTag = "WFMarkdownText_OrderedList" }) {
    list.items.forEachIndexed { index, item ->
      Row(modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)) {
        Text(
          text = "${index + 1}.",
          style = style.bodyStyle,
          modifier = Modifier.width(24.dp),
        )
        Text(
          text = rememberParsedMarkdown(item, style),
          style = style.bodyStyle,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun RenderHorizontalRule(style: WFMarkdownStyle) {
  HorizontalDivider(
    modifier =
      Modifier.fillMaxWidth().padding(vertical = 4.dp).semantics {
        testTag = "WFMarkdownText_HorizontalRule"
      },
    thickness = 1.dp,
    color = style.dividerColor,
  )
}

@Composable
private fun RenderImage(image: MarkdownBlock.Image) {
  val context = LocalContext.current
  val builder = ImageRequest.Builder(context)
  val imageModel = builder.data(image.url).crossfade(true).build()
  AsyncImage(
    model = imageModel,
    contentDescription = image.altText,
    contentScale = ContentScale.FillWidth,
    modifier =
      Modifier.fillMaxWidth().heightIn(max = 300.dp).semantics { testTag = "WFMarkdownText_Image" },
  )
}

@Composable
private fun RenderTable(
  table: MarkdownBlock.Table,
  style: WFMarkdownStyle,
) {
  val borderColor = style.tableBorderColor

  Column(modifier = Modifier.fillMaxWidth().semantics { testTag = "WFMarkdownText_Table" }) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .height(IntrinsicSize.Min)
          .background(style.tableHeaderBackgroundColor)
    ) {
      table.headers.forEachIndexed { _, header ->
        TableCell(
          text = header,
          style = style,
          textStyle = style.tableHeaderStyle,
          borderColor = borderColor,
        )
      }
    }
    table.rows.forEach { row ->
      Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        row.forEachIndexed { _, cell ->
          TableCell(
            text = cell,
            style = style,
            textStyle = style.bodyStyle,
            borderColor = borderColor,
          )
        }
      }
    }
  }
}

@Composable
private fun RowScope.TableCell(
  text: String,
  style: WFMarkdownStyle,
  textStyle: TextStyle,
  borderColor: Color,
) {
  Box(
    modifier = Modifier.border(width = 0.5.dp, color = borderColor).padding(8.dp),
    contentAlignment = Alignment.CenterStart,
  ) {
    Text(
      text = rememberParsedMarkdown(text.trim(), style),
      style = textStyle,
    )
  }
}

// endregion

// region Inline Markdown Parsing

@Composable
private fun rememberParsedMarkdown(text: String, style: WFMarkdownStyle): AnnotatedString {
  return produceState(initialValue = AnnotatedString(""), text, style) {
      value = parseInlineMarkdown(text, style)
    }
    .value
}

private suspend fun parseInlineMarkdown(
  text: String,
  style: WFMarkdownStyle,
  includeAutoLinks: Boolean = true,
): AnnotatedString = buildAnnotatedString {
  // Map WFMarkdownStyle back to RichTextHelperParams for logic reuse
  val helperParams =
    RichTextHelperParams(
      linkColor = style.linkColor,
      clickListeners = style.linkClickListeners,
      customLinkShouldBeUnderlined = style.urlLinkStyles != null,
    )

  // We don't just use tokenizeInline here, we use the complex logic from appendTextWithLinks
  // to handle nested tokens and autolinks correctly.
  val tokens = tokenizeInline(text)
  tokens.forEach { token ->
    when (token) {
      is InlineToken.Plain -> append(token.text)
      is InlineToken.Bold ->
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
          appendInlineTokens(tokenizeInline(token.text), style)
        }

      is InlineToken.Italic ->
        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
          appendInlineTokens(tokenizeInline(token.text), style)
        }

      is InlineToken.BoldItalic ->
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
          appendTextWithLinks(token.text, helperParams, includeAutoLinks)
        }

      is InlineToken.Strikethrough ->
        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
          appendTextWithLinks(token.text, helperParams, includeAutoLinks)
        }

      is InlineToken.InlineCode ->
        withStyle(
          SpanStyle(
            fontFamily = FontFamily.Monospace,
            background = style.codeBackgroundColor,
          )
        ) {
          append(token.text)
        }

      is InlineToken.Link -> {
        appendLinkToken(token, style)
      }
    }
  }
}

private fun AnnotatedString.Builder.appendLinkToken(
  token: InlineToken.Link,
  style: WFMarkdownStyle,
) {
  val listener = style.linkClickListeners[token.text]
  if (listener != null) {
    pushLink(
      LinkAnnotation.Clickable(
        tag = token.text,
        styles = style.urlLinkStyles,
        linkInteractionListener = listener,
      )
    )
  } else {
    pushLink(
      LinkAnnotation.Url(
        url = token.url,
        styles = style.urlLinkStyles,
        linkInteractionListener = null,
      )
    )
  }
  withStyle(
    SpanStyle(
      color = style.linkColor,
      textDecoration = TextDecoration.Underline,
    )
  ) {
    append(token.text)
  }
  pop()
}

private fun AnnotatedString.Builder.appendInlineTokens(
  tokens: List<InlineToken>,
  style: WFMarkdownStyle,
) {
  // This is essentially recursive inline parsing
  val helperParams =
    RichTextHelperParams(linkColor = style.linkColor, clickListeners = style.linkClickListeners)
  tokens.forEach { token ->
    // Simpler implementation as nested styles in markdown are usually handled by the tokenizer
    appendTextWithLinks(token.toString(), helperParams, true)
  }
}

// endregion

// region Inline Tokenizer

private sealed class InlineToken {
  data class Plain(val text: String) : InlineToken()

  data class Bold(val text: String) : InlineToken()

  data class Italic(val text: String) : InlineToken()

  data class BoldItalic(val text: String) : InlineToken()

  data class Strikethrough(val text: String) : InlineToken()

  data class InlineCode(val text: String) : InlineToken()

  data class Link(
    val text: String,
    val url: String,
  ) : InlineToken()
}

@Suppress("kotlin:S103", "kotlin:S5843")
private val inlineRegex =
  Regex(
    """`([^`]+)`""" +
      """|""" +
      """\[([^\]]+)]\(([^)]+)\)""" +
      """|""" +
      """\*{3}(.+?)\*{3}""" +
      """|""" +
      """_{3}(.+?)_{3}""" +
      """|""" +
      """\*{2}(.+?)\*{2}""" +
      """|""" +
      """_{2}(.+?)_{2}""" +
      """|""" +
      """~~(.+?)~~""" +
      """|""" +
      """\*(.+?)\*""" +
      """|""" +
      """(?<![a-zA-Z0-9])_(.+?)_(?![a-zA-Z0-9])"""
  )

private fun tokenizeInline(input: String): List<InlineToken> {
  val tokens = mutableListOf<InlineToken>()
  var lastIndex = 0

  for (match in inlineRegex.findAll(input)) {
    if (match.range.first > lastIndex) {
      tokens.add(InlineToken.Plain(input.substring(lastIndex, match.range.first)))
    }
    val token =
      when {
        match.groups[1] != null -> InlineToken.InlineCode(match.groups[1]!!.value)
        match.groups[2] != null ->
          InlineToken.Link(
            text = match.groups[2]!!.value,
            url = match.groups[3]!!.value,
          )
        match.groups[4] != null -> InlineToken.BoldItalic(match.groups[4]!!.value)
        match.groups[5] != null -> InlineToken.BoldItalic(match.groups[5]!!.value)
        match.groups[6] != null -> InlineToken.Bold(match.groups[6]!!.value)
        match.groups[7] != null -> InlineToken.Bold(match.groups[7]!!.value)
        match.groups[8] != null -> InlineToken.Strikethrough(match.groups[8]!!.value)
        match.groups[9] != null -> InlineToken.Italic(match.groups[9]!!.value)
        match.groups[10] != null -> InlineToken.Italic(match.groups[10]!!.value)
        else -> InlineToken.Plain(match.value)
      }
    tokens.add(token)
    lastIndex = match.range.last + 1
  }

  if (lastIndex < input.length) {
    tokens.add(InlineToken.Plain(input.substring(lastIndex)))
  }
  return tokens
}

// endregion

// region Block-level Parser

@OptIn(ExperimentalUuidApi::class)
sealed class MarkdownBlock {
  abstract val key: String

  data class Heading(
    val level: Int,
    val text: String,
    override val key: String =
      "heading_$level" + "_${Uuid.generateV7()}_${Clock.System.now().nanosecondsOfSecond}",
  ) : MarkdownBlock()

  data class Paragraph(
    val text: String,
    override val key: String =
      "paragraph_${text.hashCode()}" +
        "_${Uuid.generateV7()}_${Clock.System.now().nanosecondsOfSecond}",
  ) : MarkdownBlock()

  data class CodeBlock(
    val language: String,
    val code: String,
    override val key: String =
      "codeblock_${language}_${code.hashCode()}" +
        "_${Uuid.generateV7()}_${Clock.System.now().nanosecondsOfSecond}",
  ) : MarkdownBlock()

  data class Blockquote(
    val text: String,
    override val key: String =
      "blockquote_${text.hashCode()}" +
        "_${Uuid.generateV7()}_${Clock.System.now().nanosecondsOfSecond}",
  ) : MarkdownBlock()

  data class UnorderedList(
    val items: ImmutableList<String>,
    override val key: String =
      "unorderedlist_${items.hashCode()}" +
        "_${Uuid.generateV7()}_${Clock.System.now().nanosecondsOfSecond}",
  ) : MarkdownBlock()

  data class OrderedList(
    val items: ImmutableList<String>,
    override val key: String =
      "orderedlist_${items.hashCode()}" +
        "_${Uuid.generateV7()}_${Clock.System.now().nanosecondsOfSecond}",
  ) : MarkdownBlock()

  data object HorizontalRule : MarkdownBlock() {
    override val key: String =
      "horizontal_rule" + "_${Uuid.generateV7()}_${Clock.System.now().nanosecondsOfSecond}"
  }

  data class Image(
    val altText: String,
    val url: String,
    override val key: String =
      "image_${altText.hashCode()}_${url.hashCode()}" +
        "_${Uuid.generateV7()}_${Clock.System.now().nanosecondsOfSecond}",
  ) : MarkdownBlock()

  data class Table(
    val headers: ImmutableList<String>,
    val alignments: ImmutableList<TableAlignment>,
    val rows: ImmutableList<ImmutableList<String>>,
    override val key: String =
      "table_${headers.hashCode()}_${rows.hashCode()}" +
        "_${Uuid.generateV7()}_${Clock.System.now().nanosecondsOfSecond}",
  ) : MarkdownBlock()
}

enum class TableAlignment {
  LEFT,
  CENTER,
  RIGHT,
}

private val headingRegex = Regex("""^(#{1,6})\s+(.+)$""")
private val unorderedListItemRegex = Regex("""^[-*]\s+(.+)$""")
private val orderedListItemRegex = Regex("""^\d+\.\s+(.+)$""")
private val horizontalRuleRegex = Regex("""^(\s*([-*_])\s*){3,}$""")
private val codeBlockFenceRegex = Regex("""^```(.*)$""")
private val blockquoteRegex = Regex("""^>\s?(.*)$""")
private val imageRegex = Regex("""^!\[([^\]]*)\]\(([^)]+)\)$""")
private val tableRowRegex = Regex("""^\|(.+)\|\s*$""")
private val tableSeparatorRegex = Regex("""^\|([\s:]*-{3,}[\s:]*\|)+\s*$""")

@Suppress("kotlin:S3776")
suspend fun parseMarkdownBlocks(markdown: String): ImmutableList<MarkdownBlock> {
  val coroutineContext = currentCoroutineContext()
  val blocks = mutableListOf<MarkdownBlock>()
  val lines = markdown.lines().toImmutableList()
  var i = 0

  while (i < lines.size) {
    coroutineContext.ensureActive()

    val line = lines[i]
    val trimmedLine = line.trimEnd()

    if (trimmedLine.isEmpty()) {
      i++
      continue
    }

    val codeFenceMatch = codeBlockFenceRegex.matchEntire(trimmedLine)
    if (codeFenceMatch != null) {
      i = parseCodeBlock(codeFenceMatch, lines, i, blocks)
      continue
    }

    if (horizontalRuleRegex.matches(trimmedLine)) {
      blocks.add(MarkdownBlock.HorizontalRule)
      i++
      continue
    }

    val headingMatch = headingRegex.matchEntire(trimmedLine)
    if (headingMatch != null) {
      val level = headingMatch.groups[1]!!.value.length
      val text = headingMatch.groups[2]!!.value.trim()
      blocks.add(MarkdownBlock.Heading(level, text))
      i++
      continue
    }

    val imageMatch = imageRegex.matchEntire(trimmedLine)
    if (imageMatch != null) {
      val altText = imageMatch.groups[1]!!.value
      val url = imageMatch.groups[2]!!.value
      blocks.add(MarkdownBlock.Image(altText, url))
      i++
      continue
    }

    if (isTableStart(lines, i)) {
      i = parseTable(lines, i, blocks)
      continue
    }

    val blockquoteMatch = blockquoteRegex.matchEntire(trimmedLine)
    if (blockquoteMatch != null) {
      i = parseBlockquote(lines, i, blocks)
      continue
    }

    val ulMatch = unorderedListItemRegex.matchEntire(trimmedLine)
    if (ulMatch != null) {
      i = parseUnorderedList(lines, i, blocks)
      continue
    }

    val olMatch = orderedListItemRegex.matchEntire(trimmedLine)
    if (olMatch != null) {
      i = parseOrderedList(lines, i, blocks)
      continue
    }

    i = parseParagraph(lines, i, blocks)
  }
  return blocks.toImmutableList()
}

private fun parseCodeBlock(
  codeFenceMatch: MatchResult,
  lines: ImmutableList<String>,
  startIndex: Int,
  blocks: MutableList<MarkdownBlock>,
): Int {
  val language = codeFenceMatch.groups[1]?.value.orEmpty().trim()
  val codeLines = mutableListOf<String>()
  var i = startIndex + 1
  while (i < lines.size) {
    val closingMatch = codeBlockFenceRegex.matchEntire(lines[i].trimEnd())
    if (closingMatch != null && closingMatch.groups[1]?.value.orEmpty().trim().isEmpty()) {
      break
    }
    codeLines.add(lines[i])
    i++
  }
  if (i < lines.size) i++
  blocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
  return i
}

private fun parseBlockquote(
  lines: ImmutableList<String>,
  startIndex: Int,
  blocks: MutableList<MarkdownBlock>,
): Int {
  val quoteLines = mutableListOf<String>()
  var i = startIndex
  while (i < lines.size) {
    val qMatch = blockquoteRegex.matchEntire(lines[i].trimEnd())
    if (qMatch != null) {
      quoteLines.add(qMatch.groups[1]?.value.orEmpty())
      i++
    } else {
      break
    }
  }
  blocks.add(MarkdownBlock.Blockquote(quoteLines.joinToString("\n")))
  return i
}

private fun parseUnorderedList(
  lines: ImmutableList<String>,
  startIndex: Int,
  blocks: MutableList<MarkdownBlock>,
): Int {
  val items = mutableListOf<String>()
  var i = startIndex
  while (i < lines.size) {
    val itemMatch = unorderedListItemRegex.matchEntire(lines[i].trimEnd())
    if (itemMatch != null) {
      items.add(itemMatch.groups[1]!!.value)
      i++
    } else {
      break
    }
  }
  blocks.add(MarkdownBlock.UnorderedList(items.toImmutableList()))
  return i
}

private fun parseOrderedList(
  lines: ImmutableList<String>,
  startIndex: Int,
  blocks: MutableList<MarkdownBlock>,
): Int {
  val items = mutableListOf<String>()
  var i = startIndex
  while (i < lines.size) {
    val itemMatch = orderedListItemRegex.matchEntire(lines[i].trimEnd())
    if (itemMatch != null) {
      items.add(itemMatch.groups[1]!!.value)
      i++
    } else {
      break
    }
  }
  blocks.add(MarkdownBlock.OrderedList(items.toImmutableList()))
  return i
}

private fun parseParagraph(
  lines: ImmutableList<String>,
  startIndex: Int,
  blocks: MutableList<MarkdownBlock>,
): Int {
  val paragraphLines = mutableListOf<String>()
  var i = startIndex
  while (i < lines.size) {
    val pLine = lines[i].trimEnd()
    if (
      pLine.isEmpty() ||
        headingRegex.matchEntire(pLine) != null ||
        horizontalRuleRegex.matches(pLine) ||
        codeBlockFenceRegex.matchEntire(pLine) != null ||
        blockquoteRegex.matchEntire(pLine) != null ||
        unorderedListItemRegex.matchEntire(pLine) != null ||
        orderedListItemRegex.matchEntire(pLine) != null ||
        imageRegex.matchEntire(pLine) != null ||
        isTableStart(lines, startIndex + paragraphLines.size)
    ) {
      break
    }
    paragraphLines.add(pLine)
    i++
  }
  if (paragraphLines.isNotEmpty()) {
    blocks.add(MarkdownBlock.Paragraph(paragraphLines.joinToString(" ")))
  }
  return i
}

private fun isTableStart(
  lines: ImmutableList<String>,
  index: Int,
): Boolean {
  if (index + 1 >= lines.size) return false
  val currentLine = lines[index].trimEnd()
  val nextLine = lines[index + 1].trimEnd()
  return tableRowRegex.matches(currentLine) && tableSeparatorRegex.matches(nextLine)
}

private fun parseTable(
  lines: ImmutableList<String>,
  startIndex: Int,
  blocks: MutableList<MarkdownBlock>,
): Int {
  var i = startIndex
  val headerLine = lines[i].trimEnd()
  val headers = parseTableRow(headerLine)
  i++

  val separatorLine = lines[i].trimEnd()
  val alignments = parseTableAlignments(separatorLine)
  i++

  val rows =
    buildList {
        while (i < lines.size) {
          val rowLine = lines[i].trimEnd()
          if (!tableRowRegex.matches(rowLine)) break
          val cells = parseTableRow(rowLine)
          val paddedCells =
            if (cells.size < headers.size) {
              cells + List(headers.size - cells.size) { "" }
            } else {
              cells.take(headers.size)
            }
          add(paddedCells.toImmutableList())
          i++
        }
      }
      .toImmutableList()

  blocks.add(MarkdownBlock.Table(headers.toImmutableList(), alignments.toImmutableList(), rows))
  return i
}

private fun parseTableRow(line: String): List<String> {
  val trimmed = line.trim()
  val inner =
    if (trimmed.startsWith("|")) {
      trimmed.substring(1)
    } else {
      trimmed
    }
  val withoutTrailing =
    if (inner.endsWith("|")) {
      inner.substring(0, inner.length - 1)
    } else {
      inner
    }
  return withoutTrailing.split("|").map { it.trim() }
}

private fun parseTableAlignments(line: String): List<TableAlignment> {
  val cells = parseTableRow(line)
  return cells.map { cell ->
    val trimmed = cell.trim()
    when {
      trimmed.startsWith(":") && trimmed.endsWith(":") -> TableAlignment.CENTER
      trimmed.endsWith(":") -> TableAlignment.RIGHT
      else -> TableAlignment.LEFT
    }
  }
}

// endregion

// region Previews

@Suppress("kotlin:S103")
private const val SIMPLE_MARKDOWN =
  """# Heading 1
## Heading 2
### Heading 3

This is a **bold** and *italic* paragraph with `inline code` and a [link](https://www.wellsfargo.com).

---

- First item
- Second item with **bold**
- Third item

1. Step one
2. Step two
3. Step three

> This is a blockquote with *emphasis*.

```kotlin
fun main() {
    println("Hello, World!")
}
```

Normal text with ~~strikethrough~~ and ***bold italic*** content.

![Logo](https://en.wikipedia.org/wiki/File:Wells_Fargo_Logo_(2020).svg)

| Feature | Status | Notes |
|---------|--------|-------|
| Bold | Done | **works** |
| Italic | Done | *works* |
| Tables | Done | you're looking at it |

Visit [Wells Fargo](https://www.wellsfargo.com) for banking.

Check out **[Kotlin Docs](https://kotlinlang.org)** or the *[Android Guide](https://developer.android.com)*.

- [ ] Unchecked item
- [x] Checked item
- [ ] Another unchecked item
"""
