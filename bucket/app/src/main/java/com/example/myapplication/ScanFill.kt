package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap

fun Fill(image: Bitmap, ind: Int, replace: Int, target: Int): Bitmap {
    val w = image.width
    val h = image.height
    val buffer = createBitmap(w, h, Bitmap.Config.ARGB_8888, true)
    (0..(w - 1)).forEach { i ->
        (0..(h - 1)).forEach {j ->
            buffer.setPixel(i, j, image.getPixel(i, j))
        }
    }
    val s = mutableListOf<Int>()
    s.add(ind)
    while (s.size > 0) {
        val p = s.removeAt(s.size - 1)
        val x = p % w
        val y = p / w
        if (p >= 0 && buffer.getPixel(x, y) == target) {
            var j = y
            while (j >= 0 && buffer.getPixel(x, j) == target) j--
            j++
            var left = false
            var right = false
            while (j < h && buffer.getPixel(x, j) == target) {
                buffer.setPixel(x, j, replace);
                val index = w * j + x
                if (!left && x > 0 && buffer.getPixel(x - 1, j) == target) {
                    s.add(index - 1)
                    left = true
                } else if (left && x == 1 && buffer.getPixel(0, j) == target) {
                    left = false
                }

                if (!right && x < w - 1 && buffer.getPixel(x + 1, j) == target) {
                    s.add(index + 1)
                    right = true
                } else if (right && x < w - 1 && buffer.getPixel(x + 1, j) == target) {
                    right = false
                }
                j++
            }
        }
    }

    return buffer
}