package com.example.alarm

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
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

        //데이터를 가져온 뒤 View에 데이터를 표현하는 과정 필요

    }
    private fun initOnOffButton() {
        val onOffButton = findViewById<Button>(R.id.onOffButton)
        onOffButton.setOnClickListener {
            //데이터를 확인
            //OFF ->  알람 제거 / ON -> 알람 등록
            //데이터 저장
        }
    }

    private fun initChangeAlarmTimeButton() {
        val changeAlarmButton = findViewById<Button>(R.id.changeAlarmTimeButton)
        changeAlarmButton.setOnClickListener {

            val calendar = Calendar.getInstance()
            //현재 시간 데이터 가져오기

            TimePickerDialog(this, { picker, hour, minute ->

                val model = saveAlarmModel(hour, minute, false)
                //저장된 데이터를 모델로 구성

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
                onOff = false
        ) //데이터 생성

        val sharedPreferences = getSharedPreferences("time", Context.MODE_PRIVATE)

        with(sharedPreferences.edit()) {
            putString("alarm", model.makeDataForDB())
            putBoolean("onOff", model.onOff)
            commit()
            //edit을 통해 함수 실행 시 commit 필요
        }
        //with() : ()안의 작업과 함께 동작할 작업을 지시한다.

        return model
    }
}