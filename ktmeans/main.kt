import java.io.File
import java.awt.image.*
import javax.imageio.*
import kotlin.random.*

val k = 5
val clusters = Array(5) {
    mutableListOf<Pair<Int, Int>>()
}
val centroids = Array(k) {
    Centroid(
        it,
        Random.nextInt(0, 256),
        Random.nextInt(0, 256),
        Random.nextInt(0, 256)
    )
}


// consider each point as a r3 vector
fun main(args: Array<String>) {
    val input = "img.jpeg"
    val output = "out.jpeg"
    val img = ImageIO.read(File(input))
}

data class Centroid(
    var id: Int,
    var r: Int,
    var g: Int,
    var b: Int
)