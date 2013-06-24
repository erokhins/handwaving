package util

/**
 * @author abreslav
 */

public trait Matrix<out T> {
    val width: Int
    val height: Int
    fun get(x: Int, y: Int): T
}

public fun MutableMatrixInt.traverseLines(f: (x: Int, y: Int, value: Int) -> Unit) {
    for (y in 0..height - 1) {
        for (x in 0..width - 1) {
            f(x, y, get(x, y))
        }
    }
    f(0,0, 10)
}

public trait MutableMatrix<T> : Matrix<T> {
    fun set(x: Int, y: Int, value: T)
}

public class MutableMatrixInt (
        override val width: Int,
        override val height: Int,
        initialCellValues: (Int, Int) -> Int
) : MutableMatrix<Int> {

    private val cells: IntArray = IntArray(width * height);

    {
        for (i in 0..width-1) {
            for (j in 0..height-1) {
                set(i, j, initialCellValues(i, j))
            }
        }
    }

    override fun get(x: Int, y: Int): Int {
        return cells[toIndex(x, y)]
    }

    override fun set(x: Int, y: Int, value: Int) {
        cells[toIndex(x, y)] = value
    }

    private fun toIndex(x: Int, y: Int): Int {
        check(x, y)
        return y * width + x
    }

    private fun check(x: Int, y: Int) {
        if (x !in 0..width - 1) {
            throw IndexOutOfBoundsException("x = $x is out of range [0, $width)")
        }
        if (y !in 0..height - 1) {
            throw IndexOutOfBoundsException("y = $y is out of range [0, $height)")
        }
    }
}


public fun <T> MutableMatrix<T>.fill(f: (x: Int, y: Int, value: T) -> T) {
    for (y in 0..height - 1) {
        for (x in 0..width - 1) {
            set(x, y, f(x, y, get(x, y)))
        }
    }
}

public fun <T> MutableMatrix<T>.copyFrom(m: Matrix<T>) {
    for (y in 0..height - 1) {
        for (x in 0..width - 1) {
            set(x, y, m[x, y])
        }
    }
}