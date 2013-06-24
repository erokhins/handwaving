package org.hanuna.handwaving

import java.awt.image.BufferedImage
import java.lang.Math.*
import java.awt.Color
import util.MutableMatrixInt
import util.traverseLines
import java.util.ArrayList

/**
 * @author: erokhins
 */

val SEGMENT_SIZE_X = 10
val SEGMENT_SIZE_Y = 10
val LIKE_RADIUS = 5
val LIKE_BOUNDARY = 2000
val SEG_BOUNDARY = 100

val MIN_LEAP_X = 20
val MIN_LEAP_Y = 6

val points = ArrayList<Point>()

class Point(val x: Int, val y: Int)
val NO_POINT = Point(-1, -1)


class Segment(val startX: Int, val startY: Int)

class Matcher(val img: BufferedImage, val prev: BufferedImage) {
    val g2 = img.createGraphics()!!
    val width = 640
    val height = 480
    val mat = MutableMatrixInt(width, height, {(x, y) -> img.getRGB(x, y) shr 16 and 255})
    val prevMat = MutableMatrixInt(width, height, {(x, y) -> prev.getRGB(x, y) shr 16 and 255})

    val markSegments = MutableMatrixInt(width/SEGMENT_SIZE_X, height/SEGMENT_SIZE_Y, {(x,y) -> 0})

    fun getSub(x: Int, y: Int, pX: Int, pY: Int): Int {
        return abs(mat[x,y] - prevMat[pX, pY])
    }

    fun likeCount(seg: Segment, prevSeg: Segment): Int {
        var sum = 0;
        for (i in 0..SEGMENT_SIZE_X-1) {
            for (j in 0..SEGMENT_SIZE_Y-1) {
                sum += getSub(seg.startX+i, seg.startY+j, prevSeg.startX+i, prevSeg.startY+j)
            }
        }
        return sum
    }

    fun prevSegment(seg: Segment): Boolean {
        for (i in -LIKE_RADIUS..LIKE_RADIUS) {
            for (j in -LIKE_RADIUS..LIKE_RADIUS) {
                val prevSeg = Segment(seg.startX + 2*i, seg.startY + 2*j)
                if (likeCount(seg, prevSeg) < LIKE_BOUNDARY) {
                    return true
                }
            }
        }
        return false
    }

    fun fillSegment(seg: Segment, color: Color = Color.RED) {
        g2.setColor(color)
        g2.fillRect(seg.startX, seg.startY, SEGMENT_SIZE_X, SEGMENT_SIZE_Y)
    }

    var countSeg = 0;
    var sumX = 0;
    var sumY = 0;

    fun markSegment(i: Int, j: Int) {
        markSegments[i,j] = 1
        countSeg++
        sumX += i
        sumY += j
    }

    fun getRadius(sX: Int, sY: Int, count: Int): Int {
        var sum = 0
        markSegments.traverseLines { (x, y, v) ->
            if (v > 0) {
                sum += abs(sX - x) + abs(sY-y)
            }
        }
        return sum/ count
    }

    fun markAllNew() {
        for (i in 2..img.getWidth()/SEGMENT_SIZE_X - 3) {
            for (j in 2..img.getHeight()/SEGMENT_SIZE_Y - 3) {
                val seg = Segment(i * SEGMENT_SIZE_X, j * SEGMENT_SIZE_Y)
                if (!prevSegment(seg)) {
                    markSegment(i, j)
                }
            }
        }
        fillAll()
    }

    fun fillAll() {
        if (countSeg > SEG_BOUNDARY) {
            val x = sumX/countSeg
            val y = sumY/countSeg
            markSegments.traverseLines { (x,y,v) ->
                if (v > 0) {
                    //fillSegment(Segment(x*SEGMENT_SIZE_X, y*SEGMENT_SIZE_Y))
                }
            }
            points.add(Point(x, y))
            //println("$x $y")
            /*val r = getRadius(x, y, countSeg)
            println(r)
            for (i in -1..1) {
                for (j in -1..1) {
                    fillSegment(Segment((x+r*i)*SEGMENT_SIZE_X, (y+r*j)*SEGMENT_SIZE_Y), Color.CYAN)
                }
            }
            fillSegment(Segment(x*SEGMENT_SIZE_X, y*SEGMENT_SIZE_Y), Color.green)*/
        } else {
            points.add(NO_POINT)
            //println('-')
        }
    }

}


var prev: BufferedImage = BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);


fun isLeft(p1: Point, p2: Point, p3: Point): Boolean {
    return p1.x < p2.x && p2.x < p3.x && p3.x - p1.x > MIN_LEAP_X
}

fun isRight(p1: Point, p2: Point, p3: Point): Boolean {
    return p1.x > p2.x && p2.x > p3.x && p1.x - p3.x > MIN_LEAP_X
}

fun isDown(p1: Point, p2: Point, p3: Point): Boolean {
    return p1.y < p2.y && p2.y < p3.y && p3.y - p1.y > MIN_LEAP_Y
}

fun isUp(p1: Point, p2: Point, p3: Point): Boolean {
    return p1.y > p2.y && p2.y > p3.y && p1.y - p3.y > MIN_LEAP_Y
}

fun runAction() {
    val size = points.size
    if (size < 10) {
        return
    }
    val last = points.get(size - 1)
    val p3 = points.get(size - 2)
    val p2 = points.get(size - 3)
    val p1 = points.get(size - 4)

    if (last == NO_POINT && p3 != NO_POINT && p2 != NO_POINT && p1 != NO_POINT) {
        var call = ""
        if (isLeft(p1, p2, p3)) {
            call = "left.sh"
            println("left")
        } else if(isRight(p1, p2, p3)) {
            call = "right.sh"
            println("right")
        } else if(isUp(p1, p2, p3)) {
            call = "up.sh"
            println("up")
        } else if(isDown(p1, p2, p3)) {
            call = "down.sh"
            println("down")
        }

        if (call.size > 0) {
            Runtime.getRuntime().exec("sh/$call")
        }
    }
}

fun mark(img : BufferedImage): BufferedImage {
    var tempImg = SimpleViewer.deepCopy(img)!!
    Matcher(img, prev).markAllNew()
    prev = tempImg

    runAction()
    return img
}