package com.github.miracle.utils.tools

import java.math.BigInteger
import java.util.regex.Pattern

/**
 * chinese number to arab number converter
 */
object CnNum2ArabNum {
    private val CHN_NUM_PATTERN = Pattern.compile("[一二三四五六七八九][十百千]?")
    private val CHN_UNITS: MutableMap<Char, Int> = HashMap()
    private val CHN_NUMS: MutableMap<Char, Int> = HashMap()

    /**
     * 将小于一万的汉字数字 [String]，转换为数字 [BigInteger]
     *
     * @param chnNum
     * @return 转换后的数字
     */
    private fun getNumber(chnNum: String): BigInteger {
        var number = BigInteger.valueOf(0)
        val m = CHN_NUM_PATTERN.matcher(chnNum)
        m.reset(chnNum)
        while (m.find()) {
            val subNumber = m.group()
            if (subNumber.length == 1) {
                number = number.add(CHN_NUMS[subNumber[0]]?.toLong()?.let { BigInteger.valueOf(it) })
            } else if (subNumber.length == 2) {
                number = number.add(
                    CHN_NUMS[subNumber[0]]?.toLong()?.let {
                        BigInteger.valueOf(it).multiply(
                            CHN_UNITS[subNumber[1]]?.toLong()?.let { it1 ->
                                BigInteger.valueOf(
                                    it1
                                )
                            }
                        )
                    }
                )
            }
        }
        return number
    }

    /**
     * 将汉字 [String] 转换为数字 [Int]
     *
     * @param chnNum
     * @return 转换后的数字
     */
    fun parseNumber(chnNum: String): Int {
        var s = chnNum
        s = s.replace("(?<![一二三四五六七八九])十".toRegex(), "一十").replace("零".toRegex(), "")
        val pattern = Pattern.compile("[万亿]")
        val m = pattern.matcher(s)
        var result = BigInteger.valueOf(0)
        var index = 0
        while (m.find()) {
            val end = m.end()
            val multiple = CHN_UNITS[m.group()[0]]!!
            val num = s.substring(index, m.start())
            result = result.add(getNumber(num)).multiply(BigInteger.valueOf(multiple.toLong()))
            index = end
        }
        val num = s.substring(index)
        result = result.add(getNumber(num))
        return result.toInt()
    }

    init {
        CHN_UNITS['十'] = 10
        CHN_UNITS['百'] = 100
        CHN_UNITS['千'] = 1000
        CHN_UNITS['万'] = 10000
        CHN_UNITS['亿'] = 10000000
        CHN_NUMS['一'] = 1
        CHN_NUMS['二'] = 2
        CHN_NUMS['三'] = 3
        CHN_NUMS['四'] = 4
        CHN_NUMS['五'] = 5
        CHN_NUMS['六'] = 6
        CHN_NUMS['七'] = 7
        CHN_NUMS['八'] = 8
        CHN_NUMS['九'] = 9
    }
}