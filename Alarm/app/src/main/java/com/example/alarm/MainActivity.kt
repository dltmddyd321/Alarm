package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initOnOffButton()
        initChangeAlarmTimeButton()
        //View 초기화

        val model = fetchDataFromSharedPrefrerences()
        renderView(model)
        //데이터를 가져온 뒤 View에 데이터를 표현하는 과정 필요

    }
    private fun initOnOffButton() {
        val onOffButton = findViewById<Button>(R.id.onOffButton)
        onOffButton.setOnClickListener {
            val model = it.tag as? AlarmDisplayModel ?: return@setOnClickListener
            //AlarmDisplayModel을 통해 tag에 모델을 저장

            val newModel = saveAlarmModel(model.hour, model.minute, model.onOff.not())
            renderView(newModel)
            //데이터 저장

            if(newModel.onOff) {
                //켜진 경우 -> 알람 등록
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, newModel.hour)
                    set(Calendar.MINUTE, newModel.minute)

                    if(before(Calendar.getInstance())) {
                        //현재 시간보다 이전일 경우
                        add(Calendar.DATE, 1)
                    }
                }

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                //알람 매니저 서비스 가져오기

                val intent = Intent(this, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                //FLAG_UPDATE_CURRENT -> 새로운 값이 들어오면 바로 업데이트

                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY, //하루 뒤 실행
                        pendingIntent
                )
                //하루에 한번 씩 pendingIntent 실행
                //setInexactRepeating -> 정시에 반복 실행되는 함수
                //RTC_WAKEUP -> 지정된 시간에 기기의 절전 모드를 해제하여 대기 중인 Intent 실행

            } else {
                cancelAlarm()
                //꺼진 경우 -> 알람 제거
            }
        }
    }

    private fun initChangeAlarmTimeButton() {
        val changeAlarmButton = findViewById<Button>(R.id.changeAlarmTimeButton)
        changeAlarmButton.setOnClickListener {

            val calendar = Calendar.getInstance()
            //현재 시간 데이터 가져오기

            TimePickerDialog(this, { picker, hour, minute ->

                val model = saveAlarmModel(hour, minute, false)
                renderView(model)
                //View Update
                //저장된 데이터를 모델로 구성

                cancelAlarm()

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
            //시간을 설정 가능하도록 하고, 그 설정된 시간 데이터를 저장
        }
    }

    private fun saveAlarmModel(
            hour:Int,
            minute: Int,
            onOff: Boolean
    ) : AlarmDisplayModel {
        val model = AlarmDisplayModel(
                hour = hour,
                minute = minute,
                onOff = onOff
        ) //데이터 생성

        val sharedPreferences = getSharedPreferences("time", Context.MODE_PRIVATE)

        with(sharedPreferences.edit()) {
            putString(ALARM_KEY, model.makeDataForDB())
            putBoolean(ONOFF_KEY, model.onOff)
            commit()
            //edit을 통해 함수 실행 시 commit 필요
        }
        //with() : ()안의 작업과 함께 동작할 작업을 지시한다.

        return model
    }

    private fun fetchDataFromSharedPrefrerences() : AlarmDisplayModel {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        // 데이터를 가져오기 위한 preferences 선언

        val timeDBValue = sharedPreferences.getString(ALARM_KEY, "9:30") ?: "9:30"
        // Nullable에 대한 예외처리 후 값을 가져오기

        val onOffDBValue = sharedPreferences.getBoolean(ONOFF_KEY, false)
        // Boolean에는 Nullable 없음

        val alarmData = timeDBValue.split(":")

        val alarmModel = AlarmDisplayModel(
                hour = alarmData[0].toInt(),
                minute = alarmData[1].toInt(),
                onOff = onOffDBValue
        ) //알람 모델을 설정하여 시, 분, DB 등록

        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, Intent(this,AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)
        //보정 -> 알람이 등록 처리 여부에 대한 예외 처리
        //Pending Intent -> 수행시킬 작업 및 Intent와 그것을 수행하는 주체를 지정하기 위한 정보를 명시할 수 있는 기능
        //FLAG_NO_CREATE -> 값이 있으면 생성하고 없으면 생성하지 않음

        if((pendingIntent == null) and alarmModel.onOff) {
            //알람은 꺼져있지만 데이터는 활성화되어 있는 경우
            alarmModel.onOff = false
        } else if((pendingIntent != null) and alarmModel.onOff.not()) {
            //알람은 켜져있지만 데이터는 비활성화되어 있는 경우
            pendingIntent.cancel()
        }
        return alarmModel
    }

    private fun renderView(model: AlarmDisplayModel) {
        //화면에 Text 반영 함수
        findViewById<TextView>(R.id.ampmTextView).apply {
            text = model.ampmText
        }

        findViewById<TextView>(R.id.timeTextView).apply {
            text = model.timeText
        }

        findViewById<Button>(R.id.onOffButton).apply {
            text = model.onOffText

            tag = model
            //tag : 특정 데이터를 저장할 수 있는 일종의 주머니
        }
    }

    private fun cancelAlarm() {
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, Intent(this,AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)
        pendingIntent?.cancel()
        //기존 알람 삭제
    }

    companion object {
        private const val SHARED_PREFERENCES_NAME = "time"

        private const val ALARM_KEY = "alarm"
        //key값이 변하면 안되기 때문에 상수로 선언

        private const val ONOFF_KEY = "onOff"
        private const val ALARM_REQUEST_CODE = 1000
    }
}