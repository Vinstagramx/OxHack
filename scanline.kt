import java.io.File
import java.awt.image.*
import javax.imageio.*

var w = 0
var h = 0
var colours = Array(0) { Point(-1, 0)}

data class Point(
    var id: Int,
    var argb: Int
)

fun main(args: Array<String>) {
    val input = "queens.png"
    val img = ImageIO.read(File(input))
    w = img.getWidth()
    h = img.getHeight()
    colours = Array(w * h) {
        val x = it % w
        val y = it / w
        val argb = img.getRGB(x, y)
        Point(
            it,
            argb
        )
    }

    scanline(150, 150, -65536, -1)

    val clustered = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    colours.forEach {
        val x = it.id % w
        val y = it.id / w
        clustered.setRGB(x, y, it.argb)
    }
    ImageIO.write(clustered, "png", File("test.png"))
}

fun scanline(x: Int, y: Int, newcolour: Int, oldcolour: Int) {
    /*if (y >= h) return
    if (y < 0) return
    if (x >= w) return
    if (x < 0) return*/
    if (oldcolour == newcolour) return
    if (colours[w * y + x].argb != oldcolour) return

    //draw current scanline from start to right
    var x1 = x
    while(x1 < w && x1 >= 0 && colours[w * y + x1].argb == oldcolour){
        colours[w * y + x1] = Point(
            w * y + x1,
            newcolour
        )
        x1++
    }

    //draw current scanline from start to left
    x1 = x - 1
    while(x1 < w && x1 >= 0 && colours[w * y + x1].argb == oldcolour){
        colours[w * y + x1] = Point(
            w * y + x1,
            newcolour
        )
        x1--
    }
    
    //testing for new lines above
    x1 = x
    while(x1 < w && x1 >= 0 && colours[w * y + x1].argb == newcolour){
        if(y > 0 && colours[w * (y - 1) + x1].argb == oldcolour){
            scanline(x1, y - 1, newcolour, oldcolour)
        }
        x1++
    }
    x1 = x - 1
    while(x1 < w && x1 >= 0 && colours[w * y + x1].argb == newcolour){
        if(y > 0 && colours[w * (y - 1) + x1].argb == oldcolour){
            scanline(x1, y - 1, newcolour, oldcolour)
        }
        x1--
    }

    //testing for new lines below
    x1 = x
    while(x1 < w && x1 >= 0 && colours[w * y + x1].argb == newcolour){
        if(y < h - 1 && colours[w * (y + 1) + x1].argb == oldcolour){
            scanline(x1, y + 1, newcolour, oldcolour)
        }
        x1++
    }

    x1 = x - 1
    while(x1 < w && x1 >= 0 && colours[w * y + x1].argb == newcolour){
        if(y < h - 1 && colours[w * (y + 1) + x1].argb == oldcolour){
            scanline(x1, y + 1, newcolour, oldcolour)
        }
        x1--
    }
}