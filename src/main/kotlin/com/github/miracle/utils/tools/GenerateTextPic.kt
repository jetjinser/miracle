package com.github.miracle.utils.tools

import sun.font.FontDesignMetrics
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage

const val IMG_WIDTH = 1080
const val MARGIN = 40

class GenerateTextPic(val text: String, val authorName: String) {
    val cFont = Font("楷体", Font.PLAIN, 54)
    val lineHeight = FontDesignMetrics.getMetrics(cFont).height + 10
    var lines = text.split("\n", "\r", "\r\r")
    val noExtraLines = emptyList<String>().toMutableList()

    fun removeExtraEmpty() {
        lines.forEach {
            if (!(it.isEmpty() && noExtraLines.last().isEmpty())) {
                noExtraLines.add(it)
            }
        }
    }

    // 计算图片高度
    fun createTextPic(): BufferedImage {
        // 宽度1080
        var totalLineCount = 0
        removeExtraEmpty()
        noExtraLines.forEach { line ->
            if (line.isEmpty()) {
                totalLineCount++
            } else {
                var textWidth = 0 // 不换行的一行宽度
                line.forEach { textWidth += FontDesignMetrics.getMetrics(cFont).charWidth(it) }
                print(textWidth)
                val lineCount = (textWidth / (1080 - MARGIN * 2)) + 1
                totalLineCount += lineCount
            }
        }
        totalLineCount += 7
        return drawText(tablet(totalLineCount * lineHeight))
    }

    // 创建图片
    private fun tablet(height: Int): BufferedImage {
        return BufferedImage(IMG_WIDTH, height, BufferedImage.TYPE_INT_ARGB).also {
            it.createGraphics().apply {
                color = Color.WHITE
                fillRect(0, 0, IMG_WIDTH, height)
                dispose()
            }
        }
    }

    private fun drawText(tablet: BufferedImage): BufferedImage {
        tablet.createGraphics().apply {
            font = cFont
            color = Color.BLACK
            setRenderingHints(
                mapOf(
                    RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
                    RenderingHints.KEY_STROKE_CONTROL to RenderingHints.VALUE_STROKE_DEFAULT,
                    RenderingHints.KEY_TEXT_ANTIALIASING to RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            )

            var currentTextTop = lineHeight * 2
            var drawCount = 0
            noExtraLines.forEach { line ->
                if (line.isEmpty()) {
                    currentTextTop += lineHeight
                } else {
                    var textWidth = 0
                    var drawLine = ""
                    line.forEach {
                        val singleTextWidth = FontDesignMetrics.getMetrics(cFont).charWidth(it)
                        textWidth += singleTextWidth
                        drawLine += it
                        if (textWidth > (1080 - MARGIN * 2 - singleTextWidth)) {
                            drawString(drawLine, MARGIN, currentTextTop)
                            drawCount++
                            currentTextTop += lineHeight
                            textWidth = 0
                            drawLine = ""
                        }
                    }
                    drawString(drawLine, MARGIN, currentTextTop)
                    drawCount++
                    currentTextTop += lineHeight // add new line for \n
                }
            }
            drawString(" —— by $authorName", MARGIN, currentTextTop + lineHeight)
            drawCount++
            print(drawCount)
        }
        return tablet
    }


}