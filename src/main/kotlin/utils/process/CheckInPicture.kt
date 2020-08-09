package utils.process

import sun.misc.BASE64Encoder
import java.awt.BasicStroke
import java.awt.Color
import java.awt.RenderingHints
import java.awt.Stroke
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

object CheckInPicture {
    fun produce(url: String): BufferedImage {
        val avatarImage = scaleByPercentage(ImageIO.read(URL(url)))

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

    private fun scaleByPercentage(inputImage: BufferedImage): BufferedImage {
        val newWidth = 120
        val newHeight = 120

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
}