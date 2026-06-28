package com.dhimandasgupta.funposables

import com.dhimandasgupta.funposables.ui.utils.MarkdownBlock
import org.junit.Assert.assertNotNull
import org.junit.Test

class MarkdownHelperTest {
  @Test
  fun testMarkdownBlockKeys() {
    val heading = MarkdownBlock.Heading(1, "Test")
    assertNotNull(heading.key)

    val paragraph = MarkdownBlock.Paragraph("Test")
    assertNotNull(paragraph.key)

    val codeBlock = MarkdownBlock.CodeBlock("kotlin", "val x = 1")
    assertNotNull(codeBlock.key)

    val blockquote = MarkdownBlock.Blockquote("Test")
    assertNotNull(blockquote.key)

    val unorderedList = MarkdownBlock.UnorderedList(listOf("1"))
    assertNotNull(unorderedList.key)

    val orderedList = MarkdownBlock.OrderedList(listOf("1"))
    assertNotNull(orderedList.key)

    val horizontalRule = MarkdownBlock.HorizontalRule
    assertNotNull(horizontalRule.key)

    val image = MarkdownBlock.Image("alt", "url")
    assertNotNull(image.key)

    val table = MarkdownBlock.Table(listOf("h"), listOf(), listOf(listOf("r")))
    assertNotNull(table.key)
  }
}
