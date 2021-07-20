package com.example.alarm

data class AlarmDisplayModel(
    val hour: Int,
    val minute: Int,
    var onOff: Boolean
) {
    val timeText: String
        get() {
            val h = "%02d".format(if(hour < 12) hour else hour - 12)
            //오전과 오후를 12시 기준으로 계산하기 위해 if문 사용
            //12시가 넘어가면 그 값에서 12시 만큼을 차감

            val m = "%02d".format(minute)

            return "$h:$m"
        }

    val ampmText: String
        get() {
            return if(hour < 12) "AM" else "PM"
            //12 미만은 오전, 이상은 오후
        }

    val onOffText: String
        get() {
            return if (onOff) "알람 끄기" else "알람 켜기"
        }


    fun makeDataForDB() : String {
        return "$hour:$minute"
    }
}