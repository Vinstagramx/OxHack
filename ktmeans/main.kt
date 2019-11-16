import java.io.File
import java.awt.image.*
import javax.imageio.*
import kotlin.random.*

val k = 10
var clusters = Array(k) {
    mutableListOf<Point>()
}
val centroids = Array(k) {
    Point(
        it,
        Random.nextInt(0, 256),
        Random.nextInt(0, 256),
        Random.nextInt(0, 256)
    )
}
var w = 0
var h = 0
var colours = Array(0) { Point(-1, 0, 0, 0) }

// consider each point as a r3 vector
fun main(args: Array<String>) {
    val input = "img.jpeg"
    val output = "out.png"
    val img = ImageIO.read(File(input))
    w = img.getWidth()
    h = img.getHeight()
    colours = Array(w * h) {
        val x = it % w
        val y = it / w
        val argb = img.getRGB(x, y)
        Point(
            it,
            (argb ushr (2 * 8)) and 0x000000FF,
            (argb ushr (1 * 8)) and 0x000000FF,
            (argb ushr (0 * 8)) and 0x000000FF
        )
    }
    var i = 0
    while (!iterate())
    {
        println(i++)
    }
    val clustered = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    centroids.forEach { centroid ->
        val conv = convert(centroid)
        clusters[centroid.id].forEach {
            val x = it.id % w
            val y = it.id / w
            clustered.setRGB(x, y, conv)
        }
    }
    ImageIO.write(clustered, "png", File(output))
    println("written?")
}

fun convert(p: Point): Int {
    return -16777216 or (p.r shl (2 * 8)) or (p.g shl (1 * 8)) or (p.b shl (0 * 8))
}

fun iterate(): Boolean {
    // adjust centroids
    centroids.indices.forEach {
        val length = clusters[it].size
        if (length > 0) {
            centroids[it] = Point(
                it,
                clusters[it].map { it.r }.sum() / length,
                clusters[it].map { it.g }.sum() / length,
                clusters[it].map { it.b }.sum() / length
            )
        }
    }
    // clustering
    val newClusters = Array(k) { mutableListOf<Point>() }
    colours.forEach { c ->
        val dists = centroids.map { dist(c, it) }
        newClusters[dists.indexOf(dists.min())].add(c)
    }
    if ((0..(k - 1)).all { newClusters[it] == clusters[it] }) {
        return true
    }
    clusters = newClusters
    return false
}

fun dist(a: Point, b: Point): Int {
    return listOf(a.r - b.r, a.g - b.g, a.b - b.b).map { it * it }.sum()
}

data class Point(
    var id: Int,
    var r: Int,
    var g: Int,
    var b: Int
)