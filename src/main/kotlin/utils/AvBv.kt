package utils

import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions

object AvBv {
    private const val table = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF"
    private var tr = mutableMapOf<String, Int>()

    private val s = listOf(11, 10, 3, 8, 4, 6, 2, 9, 5, 7)
    private const val xor = 177451812
    private const val add = 100618342136696320

    init {
        for (i in 0 until 58) {
            tr[table[i].toString()] = i
        }
    }

    fun avToBv(av: Long): String {
        val x = av.xor(xor.toLong()) + add
        val r = "BV          ".toMutableList()
        for (i in 0 until 10) {
            r[s[i]] = table[(x / IntegerFunctions.pow(58L, i) % 58).toInt()]
        }
        return r.joinToString("")
    }

    fun bvToAv(bv: String): Long {
        require(bv.length == 12)
        var r = 0L
        for (i in 0 until 10) {
            r += tr[bv[s[i]].toString()]!! * IntegerFunctions.pow(58L, i)
        }
        return (r - add).xor(xor.toLong())
    }
}