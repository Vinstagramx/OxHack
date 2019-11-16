import java.io.File
import java.awt.image.*
import javax.imageio.*
import java.util.TreeSet
import java.util.PriorityQueue
import kotlin.random.*
import kotlin.math.*
import kotlin.collections.*

// figure out a way to dynamically set these values?

var k = 5
var whiteReduction = 15
var blackReduction = 150
var initialBlur = 1
var secondBlur = 1

var clusters = Array(k) {
    mutableListOf<Point>()
}
var centroids = Array(k) {
    Point(
        it,
        Random.nextInt(0, 256),
        Random.nextInt(0, 256),
        Random.nextInt(0, 256),
        PointType.CENTROID
    )
}
var regions = mutableListOf<Region>()
var checked = sortedSetOf<Int>()

var w = 0
var h = 0
var colours = Array(0) { Point(-1, 0, 0, 0, PointType.COLOUR) }

val input = "input.jpg"
val km = "kmeans.png"
val edges = "edge_detection.png"
val denoise = "noise_reduction.png"

// consider each point as a r3 vector
fun main(args: Array<String>) {
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
            (argb ushr (0 * 8)) and 0x000000FF,
            PointType.COLOUR
        )
    }

    blur(initialBlur, false)

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
            colours[it.id] = Point(it.id, centroid.r, centroid.g, centroid.b, PointType.COLOUR)
            clustered.setRGB(x, y, conv)
        }
    }
    ImageIO.write(clustered, "png", File(km))
    // KMEANS DONE

    blur(secondBlur, false)
    edge()
    colours.forEach {
        val x = it.id % w
        val y = it.id / w
        clustered.setRGB(x, y, convert(it))
    }
    ImageIO.write(clustered, "png", File(edges))
    // EDGES DONE

    gaps(whiteReduction, PointType.WHITE)
    gaps(blackReduction, PointType.BLACK)
    // gaps(15, PointType.WHITE)
    colours.forEach {
        val x = it.id % w
        val y = it.id / w
        clustered.setRGB(x, y, convert(it))
    }
    ImageIO.write(clustered, "png", File(denoise))
}

fun gaps(t: Int, rm: PointType) {
    var id = 0
    regions = mutableListOf<Region>()
    checked = sortedSetOf<Int>()
    colours.indices.forEach {
        if (!(it in checked)) {
            val r = Region(id, 0, sortedSetOf<Int>(), colours[it].t)
            regions.add(r)
            val queue = PriorityQueue<Int>()
            queue.add(it)
            while (queue.size > 0) {
                val front = queue.poll()!!
                val x = front % w
                val y = front / w
                if (!(front in checked)) {
                    if (colours[front].t == r.t) {
                        checked.add(front)
                        r.pixels.add(front)
                        r.size++
                        ((x - 1)..(x + 1)).forEach { i ->
                            ((y - 1)..(y + 1)).forEach { j ->
                                val flag = when {
                                    i >= w -> false
                                    i < 0 -> false
                                    j >= h -> false
                                    j < 0 -> false
                                    else -> true
                                }
                                if (flag) queue.add(j * w + i)
                            }
                        }
                    }
                }
            }
            id++
        }
    }
    regions.forEach { r ->
        if (r.size < t) {
            r.pixels.forEach {
                colours[it] = when {
                    r.t == PointType.WHITE && rm == r.t -> Point(it, 0, 0, 0, PointType.BLACK)
                    r.t == PointType.BLACK && rm == r.t -> Point(it, 255, 255, 255, PointType.WHITE)
                    else -> colours[it]
                }
            }
        }
    }
    // regions.forEach { reg ->
    //     val r = Random.nextInt(0, 256)
    //     val g = Random.nextInt(0, 256)
    //     val b = Random.nextInt(0, 256)
    //     reg.pixels.forEach {
    //         colours[it] = Point(it, r, g, b, PointType.COLOUR)
    //     }
    // }
}

fun blur(r: Int, weighted: Boolean) {
    val temp = colours.map {
        val x = it.id % w
        val y = it.id / w
        val dummy = Point(it.id, 0, 0, 0, PointType.COLOUR)
        var divisor = 0
        ((x - r)..(x + r)).forEach { i ->
            ((y - r)..(y + r)).forEach { j ->
                val weight = when {
                    weighted -> 1 + r - arrayOf(i - x, j - y).map { abs(it) }.max()!!
                    else -> 1
                }
                var flag = when {
                    i >= w -> false
                    i < 0 -> false
                    j >= h -> false
                    j < 0 -> false
                    else -> true
                }
                if (flag) {
                    divisor += weight
                    val c = colours[j * w + i]
                    dummy.r += weight * c.r
                    dummy.g += weight * c.g
                    dummy.b += weight * c.b
                }
            }
        }
        dummy.r /= divisor
        dummy.g /= divisor
        dummy.b /= divisor
        dummy
    }
    temp.indices.forEach {
        colours[it] = temp[it]
    }
}

fun edge() {
    val temp = colours.map {
        val x = it.id % w
        val y = it.id / w
        val surrounding = mutableListOf<Point?>()
        ((x - 1)..(x + 1)).forEach { i ->
            ((y - 1)..(y + 1)).forEach { j ->
                surrounding.add(when {
                    i >= w -> null
                    i < 0 -> null
                    j >= h -> null
                    j < 0 -> null
                    else -> colours[j * w + i]
                })
            }
        }
        if (surrounding.filterNotNull().filter { s -> !((s.r == it.r) and (s.g == it.g) and (s.b == it.b)) }.size < 2) {
            Point(it.id, 255, 255, 255, PointType.WHITE)
        } else {
            Point(it.id, 0, 0, 0, PointType.BLACK)
        }
    }
    temp.indices.forEach {
        colours[it] = temp[it]
    }
}

fun convert(p: Point): Int {
    // kotlin has 0xFF000000 as long
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
                clusters[it].map { it.b }.sum() / length,
                PointType.CENTROID
            )
        }
    }

    // clustering
    val newClusters = Array(k) { mutableListOf<Point>() }
    colours.forEach { c ->
        val dists = centroids.map { dist(c, it) }
        newClusters[dists.indexOf(dists.min())].add(c)
    }

    // checking for changes
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
    var b: Int,
    var t: PointType
)

data class Region(
    var id: Int,
    var size: Int,
    var pixels: TreeSet<Int>,
    var t: PointType
)

enum class PointType {
    COLOUR,
    BLACK,
    WHITE,
    CENTROID
}