import java.io.File
import java.awt.image.*
import javax.imageio.*
import java.util.TreeSet
import java.util.PriorityQueue
import kotlin.random.*
import kotlin.math.*
import kotlin.system.*
import kotlin.*
import kotlin.collections.*

// figure out a way to dynamically set these values?
fun main(args: Array<String>) {
    val generator = ImageGenerator()
    val t0 = System.currentTimeMillis()

    generator.blur(1, false)
    generator.kmeans(5)
    generator.write("kmeans", "png")
    val t1 = System.currentTimeMillis()

    println("Points clustered in [${t1 - t0}ms]")

    generator.blur(1, false)
    generator.edge()
    generator.write("edge_detection", "png")
    val t2 = System.currentTimeMillis()

    println("Edges detected in [${t2 - t1}ms]")

    generator.fixGaps(15, PointType.WHITE)
    generator.fixGaps(150, PointType.BLACK)
    generator.write("noise_reduction", "png")
    val t3 = System.currentTimeMillis()

    println("Gaps fixed in [${t3 - t2}ms]")
    println("")
    println("Complete in [${t3 - t0}ms]")
}

class ImageGenerator {
    var w: Int
    var h: Int
    var colours: Array<Point>
    var regions: MutableList<Region>
    var checked: TreeSet<Int>

    init {
        w = 0
        h = 0
        colours = Array(0) { Point(-1, 0, 0, 0, PointType.COLOUR) }
        regions = mutableListOf<Region>()
        checked = sortedSetOf<Int>()
    }

    constructor() {
        val img = ImageIO.read(File("input.jpg")) // change me
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
    }

    fun write(name: String, extension: String) {
        val buffer = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        colours.forEach {
            val x = it.id % w
            val y = it.id / w
            buffer.setRGB(x, y, convert(it))
        }
        ImageIO.write(buffer, extension, File("$name.$extension"))
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
                    if (i >= 0 && i < w && j >= 0 && j < h) {
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

    fun kmeans(k: Int) {
        var complete = false
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

        while (!complete) {
            // recalculating centroids
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
                var index = -1
                var min = 2147483647
                centroids.indices.forEach {
                    val distance = dist(c, centroids[it])
                    if (distance < min) {
                        min = distance
                        index = it
                    }
                }
                newClusters[index].add(c)
            }

            // checking for changes
            complete = (0..(k - 1)).all { newClusters[it] == clusters[it] }
            clusters = newClusters
        }

        centroids.forEach { centroid ->
            clusters[centroid.id].forEach {
                colours[it.id] = Point(it.id, centroid.r, centroid.g, centroid.b, PointType.COLOUR)
            }
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
                        i >= w || i < 0 || j >= h || j < 0 -> null
                        else -> colours[j * w + i]
                    })
                }
            }
            if (surrounding.filterNotNull().any { s -> !((s.r == it.r) && (s.g == it.g) && (s.b == it.b)) }) {
                Point(it.id, 0, 0, 0, PointType.BLACK)
            } else {
                Point(it.id, 255, 255, 255, PointType.WHITE)
            }
        }
        temp.indices.forEach {
            colours[it] = temp[it]
        }
    }

    fun fixGaps(t: Int, rm: PointType) {
        println("t: $t, rm: $rm ssss")
        var id = 0
        regions = mutableListOf<Region>()
        checked = sortedSetOf<Int>()
        colours.indices.forEach {
            if (!(it in checked)) {
                println(id)
                val queue = PriorityQueue<Int>()
                val r = Region(id, 0, sortedSetOf<Int>(), colours[it].t)
                queue.add(it)
                regions.add(r)
                while (queue.size > 0) {
                    val q = queue.poll()
                    if (q >= 0 && !(q in checked)) {
                        val x = q % w
                        val y = q / w
                        arrayListOf(0, 1, -1).forEach { o ->
                            var i = x
                            while (i < w && i >= 0 && (y + o) >= 0 && (y + o) < h && colours[w * (y + o) + i].t == r.t) {
                                when (o) {
                                    0 -> {
                                        checked.add(w * y + i)
                                        r.pixels.add(w * y + i)
                                    }
                                    else -> queue.add(w * (y + o) + i)
                                }
                                i++
                            }

                            i = x - 1
                            while (i < w && i >= 0 && (y + o) >= 0 && (y + o) < h && colours[w * (y + o) + i].t == r.t) {
                                when (o) {
                                    0 -> {
                                        checked.add(w * y + i)
                                        r.pixels.add(w * y + i)
                                    }
                                    else -> queue.add(w * (y + o) + i)
                                }
                                i--
                            }
                        }
                    }
                }
            }
            id++
        }
        // regions.forEach { r ->
        //     val red = Random.nextInt(0, 256)
        //     val grn = Random.nextInt(0, 256)
        //     val blu = Random.nextInt(0, 256)
        //     if (r.size < 10000)
        //     {
        //         println(r.pixels.size)
        //         r.pixels.forEach {
        //             println("${r.t}: ${colours[it].t}")
        //             colours[it] = Point(it, red, grn, blu, PointType.COLOUR)
        //         }
        //     }
        // }
        regions.forEach { r ->
            // println(r.size)
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
    }

    fun convert(p: Point): Int {
        // kotlin has 0xFF000000 as long
        return -16777216 or (p.r shl (2 * 8)) or (p.g shl (1 * 8)) or (p.b shl (0 * 8))
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
}

enum class PointType {
    COLOUR,
    BLACK,
    WHITE,
    CENTROID
}