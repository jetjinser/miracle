package utils.process.checkIn

/**
 * 封装数据处理类, 便于签到处理
 * @param checkInData 签到的数据处理类
 * @author jinser
 */
class CheckInModel(private val checkInData: CheckInData) {
    fun getCheckInfoArray() = arrayOf(
        checkInData.card,
        "签 到 成 功",
        "Cuprum ${checkInData.cuprum}",
        "签到天数 ${checkInData.checkInDays}       好感度 ${checkInData.favor}",
        "tips" // TODO tips 540 px 宽
    )

    /**
     * @param checkInDays 签到天数
     * @param favor 当前好感度
     * @return 新的好感度
     * @author jinser
     */
    private fun favorAlgorithm(checkInDays: Int, favor: Int): Int {
        return when {
            checkInDays <= 10 -> favor + (1..2).random()
            checkInDays in 11..20 -> favor + (2..3).random()
            checkInDays in 21..30 -> favor + (3..4).random()
            checkInDays in 31..40 -> favor + (4..5).random()
            checkInDays in 41..50 -> favor + (5..6).random()
            else -> favor + (5..10).random()
        }
    }

    /**
     * @param favor 好感度
     * @param cuprum 当前铜币数量
     * @return 新的铜币数量
     * @author jinser
     */
    private fun cuprumAlgorithm(favor: Int, cuprum: Int): Int {
        return when {
            favor < 10 -> cuprum + (10..20).random()
            favor in (11..20) -> cuprum + (20..30).random()
            favor in (21..30) -> cuprum + (30..40).random()
            favor in (31..40) -> cuprum + (41..50).random()
            favor in (41..50) -> cuprum + (51..60).random()
            else -> cuprum + (50..100).random()
        }
    }
}