package com.example.myapplication

import java.util.TreeSet
import java.util.PriorityQueue
import kotlin.random.*
import kotlin.math.*
import kotlin.*
import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

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

    constructor(img: Bitmap) {
        w = img.width
        h = img.height
        colours = Array(w * h) {
            val x = it % w
            val y = it / w
            val argb = img.getPixel(x, y)
            Point(
                    it,
                    (argb ushr (2 * 8)) and 0x000000FF,
                    (argb ushr (1 * 8)) and 0x000000FF,
                    (argb ushr (0 * 8)) and 0x000000FF,
                    PointType.COLOUR
            )
        }
    }

    fun write(): Bitmap {
        val buffer = createBitmap(w, h, Bitmap.Config.ARGB_8888, true)
        colours.forEach {
            val x = it.id % w
            val y = it.id / w
            buffer.setPixel(x, y, convert(it))
        }
        return buffer
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
                val dists = centroids.map { dist(c, it) }
                newClusters[dists.indexOf(dists.min())].add(c)
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

    fun edge(tolerance: Int) {
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
            if (surrounding.filterNotNull().filter { s ->
                        !((s.r == it.r) && (s.g == it.g) && (s.b == it.b))
                    }.size < tolerance) {
                Point(it.id, 255, 255, 255, PointType.WHITE)
            } else {
                Point(it.id, 0, 0, 0, PointType.BLACK)
            }
        }
        temp.indices.forEach {
            colours[it] = temp[it]
        }
    }

    fun fixGaps(t: Int, rm: PointType) {
        var id = 0
        regions = mutableListOf<Region>()
        checked = sortedSetOf<Int>()
        colours.indices.forEach {
            if (!(it in checked)) {
                val r = Region(id, 0, sortedSetOf<Int>(), colours[it].t)
                id++
                val s = mutableListOf<Int>()
                s.add(it)
                regions.add(r)
                while (s.size > 0) {
                    val p = s.removeAt(s.size - 1)
                    if (p >= 0 && !(p in checked)) {
                        val x = p % w
                        val y = p / w
                        var j = y
                        while (j >= 0 && colours[w * j + x].t == r.t) j--
                        j++
                        var left = false
                        var right = false
                        while (j < h && colours[w * j + x].t == r.t && !((w * j + x) in checked)) {
                            val index = w * j + x
                            r.size++
                            r.pixels.add(index)
                            checked.add(index)
                            if (!left && x > 0 && colours[index - 1].t == r.t && !((index - 1) in checked)) {
                                s.add(index - 1)
                                left = true
                            } else if (left && x == 1 && colours[w * j].t != r.t && !((w * j) in checked)) {
                                left = false
                            }

                            if (!right && x < w - 1 && colours[index + 1].t == r.t && !((index + 1) in checked)) {
                                s.add(index + 1)
                                right = true
                            } else if (right && x < w - 1 && colours[index + 1].t != r.t && !((index + 1) in checked)) {
                                right = false
                            }
                            j++
                        }
                    }
                }
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