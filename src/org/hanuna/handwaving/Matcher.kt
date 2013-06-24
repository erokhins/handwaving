package org.hanuna.handwaving

import java.awt.image.BufferedImage

/**
 * @author: erokhins
 */

fun mark(img : BufferedImage): BufferedImage {
    img.createGraphics()!!.fillRect(0,0, 20, 20);

    return img
}