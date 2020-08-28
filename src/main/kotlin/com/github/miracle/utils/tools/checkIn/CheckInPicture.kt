package com.github.miracle.utils.tools.checkIn

import com.zzhoujay.lowpoly.LowPoly
import sun.font.FontDesignMetrics
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

/**
 * 生成签到图片
 * @author jinser
 */
class CheckInPicture(private val url: String, private val checkInModel: CheckInModel) {
    enum class BackgroundImageType {
        LoyPoly,
        Gaussian
    }

    fun generate(backgroundImageType: BackgroundImageType): BufferedImage {
        val image = ImageIO.read(URL(url))
        return compound(image, backgroundImageType)
    }

    private fun scaleByPercentage(inputImage: BufferedImage): BufferedImage {
        val newWidth = 256
        val newHeight = 256

        // 获取原始图像透明度类型
        val type = inputImage.colorModel.transparency
        val width = inputImage.width
        val height = inputImage.height

        // 开启抗锯齿
        val renderingHints =
            RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        // 使用高质量压缩
        renderingHints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY

        val img = BufferedImage(newWidth, newHeight, type)
        val graphics2d = img.createGraphics()
        graphics2d.setRenderingHints(renderingHints)

        graphics2d.drawImage(inputImage, 0, 0, newWidth, newHeight, 0, 0, width, height, null)
        graphics2d.dispose()

        return img
    }

    private fun smallAvatar(image: BufferedImage): BufferedImage {
        val avatarImage = scaleByPercentage(image)

        val width = avatarImage.width

        val formatAvatarImage = width.let { BufferedImage(it, it, BufferedImage.TYPE_4BYTE_ABGR) }
        var graphic = formatAvatarImage.createGraphics()

        val borderF = 1
        val shape = Ellipse2D.Double(
            borderF.toDouble(),
            borderF.toDouble(),
            (width - borderF * 2).toDouble(),
            (width - borderF * 2).toDouble()
        )


        graphic?.apply {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            clip = shape
            drawImage(avatarImage, borderF, borderF, width - borderF * 2, width - borderF * 2, null)
            dispose()
        }

        graphic = formatAvatarImage.createGraphics()

        val borderS = 3
        val s: Stroke = BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        graphic?.apply {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            stroke = s
            color = Color.WHITE
            drawOval(borderS, borderS, width - borderS * 2, width - borderS * 2)
            dispose()
        }

        return formatAvatarImage
    }

    private fun tablet(): BufferedImage {
        return BufferedImage(540, 160, BufferedImage.TYPE_INT_ARGB).also {
            it.createGraphics().apply {
                color = Color(0, 0, 0, 90)
                fillRect(0, 0, 540, 160)
                dispose()
            }
        }
    }

    private fun writtenTablet(tablet: BufferedImage): BufferedImage {
        val width = tablet.width  // 540
        val cFont = Font("Microsoft JhengHei", Font.BOLD, 24)
        tablet.createGraphics().apply {
            font = cFont
            setRenderingHints(
                mapOf(
                    RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
                    RenderingHints.KEY_STROKE_CONTROL to RenderingHints.VALUE_STROKE_DEFAULT,
                    RenderingHints.KEY_TEXT_ANTIALIASING to RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )
            )


            val textHeight = FontDesignMetrics.getMetrics(cFont).height - 5
            val textArray = checkInModel.checkInfoArray

            var temp = textHeight
            for (text in textArray.dropLast(1)) {
                if (text != null) {
                    var textWidth = 0
                    text.forEach { textWidth += FontDesignMetrics.getMetrics(cFont).charWidth(it) }
                    drawString(text, (width - textWidth) / 2, temp)
                    temp += textHeight
                }
            }

            val tip = textArray.last()
            var tipWidth = 0
            val tipsFont = Font("Microsoft JhengHei", Font.BOLD, 20)
            font = tipsFont
            if (tip != null) {
                tip.forEach { tipWidth += FontDesignMetrics.getMetrics(tipsFont).charWidth(it) }
                drawString(tip, (width - tipWidth) / 2, temp + 6)
            }
        }
        return tablet
    }

    private fun handlerColor(tImage: BufferedImage): BufferedImage {
        val image = BufferedImage(tImage.width, tImage.height, BufferedImage.TYPE_4BYTE_ABGR).also {
            it.createGraphics().apply {
                drawImage(tImage, 0, 0, null)
                dispose()
            }
        }

        val img = image.getScaledInstance(640, 640, Image.SCALE_DEFAULT)
        val ig = BufferedImage(640, 640, BufferedImage.TYPE_4BYTE_ABGR).also {
            it.graphics.apply {
                drawImage(img, 0, 0, null)
                dispose()
            }
        }

        val alpha = 190
        for (x in 50 until 590) {
            for (y in 425 until 585) {
                var rgb = ig.getRGB(x, y)
                rgb = (alpha.shl(24)).or(rgb.and(0x00ffffff))
                ig.setRGB(x, y, rgb)
            }
        }

        return ig
    }

    private fun lowPoly(image: BufferedImage): BufferedImage {
        val byteArrayInputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", byteArrayInputStream)
        val inputStream = ByteArrayInputStream(byteArrayInputStream.toByteArray())
        val outputStream = ByteArrayOutputStream()
        LowPoly.generate(inputStream, outputStream, 30, 1.0F, true, "png", false, 30)
        val stream = ByteArrayInputStream(outputStream.toByteArray())
        return ImageIO.read(stream)
    }

    private fun compound(image: BufferedImage, backgroundImageType: BackgroundImageType): BufferedImage {
        val smallAvatar = smallAvatar(image)

        val bigAvatar = when (backgroundImageType) {
            BackgroundImageType.LoyPoly -> lowPoly(image)
            BackgroundImageType.Gaussian -> GaussianBlur.blur(image, 15)
        }

        val backgroundImage = handlerColor(bigAvatar)
        val tablet = writtenTablet(tablet())

        backgroundImage.createGraphics().apply {
            drawImage(tablet, 50, 425, null)
            drawImage(
                smallAvatar,
                (backgroundImage.width - smallAvatar.width) / 2,
                ((backgroundImage.width - smallAvatar.width) / 2) - 55,
                null
            )
            dispose()
        }
        return backgroundImage
    }
}