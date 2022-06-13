package com.example.iot_finalproject.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.iot_finalproject.MainActivity
import com.example.iot_finalproject.R
import com.example.iot_finalproject.TimerService
import com.example.iot_finalproject.ViewAnimation
import com.example.iot_finalproject.databinding.FragmentFirstBinding
import com.example.iot_finalproject.manager.MQTTConnectionParams
import com.example.iot_finalproject.manager.MQTTmanager
import com.example.iot_finalproject.protocols.UIUpdaterInterface
import com.google.gson.Gson
import java.util.regex.Matcher
import java.util.regex.Pattern

class FirstFragment: Fragment(), UIUpdaterInterface {
    private var binding: FragmentFirstBinding? = null

    lateinit var mActivity: MainActivity
    private var mqttManager: MQTTmanager? = null
    private var isTurnOn = false
    private var isAuto = true
    private var count = 0
    private var speed = 0
    private var btnType = BtnType.OFF
    private lateinit var timerIntent: Intent
    private var time = ""

    data class Data(val isAuto: Boolean, val isTurnOn: Boolean, val speed: Int, val count: Int)

    enum class BtnType {OFF, LOW, MID, HIGH}

    private val updateTime: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val currentTime = intent.getIntExtra(TimerService.TIME_EXTRA, 0)
            Log.e("FirestFragment", "Waiting Timer: $currentTime sec")
            val timeLeft = (time.toInt() - currentTime).toString()
            binding?.edTimer?.setText(timeLeft)
            if (currentTime == time.toInt()) {
                val json = Gson().toJson(Data(isAuto, isTurnOn, speed, count))
                mqttManager?.publish(json.toString())
                mActivity.stopService(timerIntent)
                binding?.edTimer?.setText("")
                binding?.edTimer?.isEnabled = true
                binding?.tvOk?.isEnabled = true
                Toast.makeText(mActivity, "計時結束", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivity.unregisterReceiver (updateTime)
        mActivity.stopService(timerIntent)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        resetUIWithConnection(false)
        setListener()
    }

    override fun resetUIWithConnection(status: Boolean) { //true: Connected, false: Disconnected
        binding?.run {
            edMessage.isEnabled = status
            tvConnect.isEnabled = !status
            tvDisconnect.isEnabled = status
            tvSend.isEnabled = status
            tvSwitch.isEnabled = status

            //更新狀態顯示
            if (status){
                updateStatusViewWith("Connected")
                gpInit.visibility = View.GONE
                gpDisplay.visibility = View.VISIBLE
                clConsoles.visibility = if (isAuto) View.GONE else View.VISIBLE
                tvSwitch.setBackgroundResource(if (!isAuto) R.drawable.btn_turn_off else R.drawable.btn_turn_on)
                tvSwitch.text = if (!isAuto) "MANUAL" else "AUTO"
            } else {
                updateStatusViewWith("Disconnected")
                gpInit.visibility = View.VISIBLE
                gpDisplay.visibility = View.GONE
                clConsoles.visibility = View.GONE
                updateDisplayStatus()
            }
        }
    }

    override fun updateStatusViewWith(status: String) {
        binding?.tvStatus?.text = status
    }

    override fun update(message: String) {
        binding?.run {

            val res = Gson().fromJson(message, Data::class.java)
            count = res.count
            isTurnOn = res.isTurnOn
            if (isAuto) speed = res.speed

            updateDisplayStatus()
        }
    }

    private fun updateDisplayStatus() {
        binding?.run {
            tvCount.text = "$count"
            tvCount.setTextColor(mActivity.getColor(
                if (count <= 2) R.color.gray_C4C4C4
                else if (count in 3..4) R.color.blue_4A8FE1
                else if (count in 5..6) R.color.green_4ECF6A
                else R.color.red_FF0000))

            tvFan.text = if(isTurnOn) "風扇狀態: ON" else "風扇狀態: OFF"
//                if (isAuto) {
//                if(isTurnOn) "風扇狀態: ON" else "風扇狀態: OFF"
//            } else {
//                when (btnType) {
//                    BtnType.OFF -> "風扇狀態: OFF"
//                    else -> "風扇狀態: ON"
//                }
//            }
            tvSpeed.text = when (speed) {
                    150 -> "風扇強度: 弱"
                    200 -> "風扇強度: 中"
                    255 -> "風扇強度: 強"
                    else -> "風扇強度: OFF"
                }
//                if (!isAuto) {
//                when (btnType) {
//                    BtnType.LOW -> "風扇強度: 弱"
//                    BtnType.MID -> "風扇強度: 中"
//                    BtnType.HIGH -> "風扇強度: 強"
//                    else -> "風扇強度: OFF"
//                }
//            } else {
//                when (speed) {
//                    150 -> "風扇強度: 弱"
//                    200 -> "風扇強度: 中"
//                    255 -> "風扇強度: 強"
//                    else -> "風扇強度: OFF"
//                }
//            }
        }
    }

    private fun setListener() {
        binding?.run {
            tvConnect.setOnClickListener {
                val host = "tcp://broker.emqx.io:1883"
                val topic = "mqttTest"
                val connectionParams = MQTTConnectionParams("MQTTSample", host, topic, "", "")

                mqttManager = MQTTmanager(connectionParams, mActivity, this@FirstFragment)
                mqttManager?.connect()

                isAuto = true
                isTurnOn = false
                speed = 0
                count = 0

                timerIntent = Intent(mActivity, TimerService::class.java)
                mActivity.registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))
            }

            tvDisconnect.setOnClickListener {
                isAuto = true
                isTurnOn = false
                speed = 0
                count = 0

                val json = Gson().toJson(Data(isAuto, isTurnOn, speed, count))
                mqttManager?.publish(json.toString())
                mqttManager?.disconnect()

                mActivity.unregisterReceiver (updateTime)
                mActivity.stopService(timerIntent)
                binding?.edTimer?.setText("")
                binding?.edTimer?.isEnabled = true
                binding?.tvOk?.isEnabled = true
            }

            tvSwitch.setOnClickListener {
                isAuto = !isAuto
                tvSwitch.setBackgroundResource(if (!isAuto) R.drawable.btn_turn_off else R.drawable.btn_turn_on)
                tvSwitch.text = if (!isAuto) "MANUAL" else "AUTO"
                clConsoles.visibility = if (isAuto) View.GONE else View.VISIBLE
                changeButtonStyle(btnType)

                if (isAuto) {
                    if (count in 0..2) {
                        speed = 0
                        isTurnOn = false
                    }
                    else if (count in 3..4) {
                        speed = 150
                        isTurnOn = true
                    }
                    else if (count in 5..6) {
                        speed = 200
                        isTurnOn = true
                    }
                    else {
                        speed = 255
                        isTurnOn = true
                    }
                }

                val json = Gson().toJson(Data(isAuto, isTurnOn, speed, count))
                mqttManager?.publish(json.toString())
            }

            tvOff.setOnClickListener {
                btnType = BtnType.OFF
                changeButtonStyle(btnType)
                isTurnOn = false
                speed = 0
            }
            tvLow.setOnClickListener {
                btnType = BtnType.LOW
                changeButtonStyle(btnType)
                isTurnOn = true
                speed = 150
            }
            tvMid.setOnClickListener {
                btnType = BtnType.MID
                changeButtonStyle(btnType)
                isTurnOn = true
                speed = 200
            }
            tvHigh.setOnClickListener {
                btnType = BtnType.HIGH
                changeButtonStyle(btnType)
                isTurnOn = true
                speed = 255
            }
            tvOk.setOnClickListener {
                speed = when (btnType) {
                    BtnType.LOW -> 150
                    BtnType.MID -> 200
                    BtnType.HIGH -> 255
                    else -> 0
                }
                  if (!edTimer.text.isNullOrEmpty() && edTimer.text.isNotBlank()) {
                      val p: Pattern = Pattern.compile("[0-9]*")
                      val m: Matcher = p.matcher(edTimer.text.toString())
                      if (m.matches() && edTimer.text.toString() != "0") {
                          Toast.makeText(mActivity, "計時開始", Toast.LENGTH_SHORT).show()
                          time = edTimer.text.toString()
                          timerIntent.putExtra(TimerService.TIME_EXTRA, 0)
                          mActivity.startService(timerIntent)
                          edTimer.isEnabled = false
                          tvOk.isEnabled = false
                      } else Toast.makeText(mActivity, "無法輸入數字 0 或是其他文字", Toast.LENGTH_SHORT).show()
                  } else {
                      val json = Gson().toJson(Data(false, isTurnOn, speed, count))
                      mqttManager?.publish(json.toString())
                  }
            }
        }
    }

    private fun changeButtonStyle(btnType: BtnType) {
        binding?.run {
            tvOff.setBackgroundResource(R.drawable.btn_gray)
            tvLow.setBackgroundResource(R.drawable.btn_gray)
            tvMid.setBackgroundResource(R.drawable.btn_gray)
            tvHigh.setBackgroundResource(R.drawable.btn_gray)

            when (btnType) {
                BtnType.OFF -> tvOff.setBackgroundResource(R.drawable.btn_blue)
                BtnType.LOW -> tvLow.setBackgroundResource(R.drawable.btn_blue)
                BtnType.MID -> tvMid.setBackgroundResource(R.drawable.btn_blue)
                BtnType.HIGH -> tvHigh.setBackgroundResource(R.drawable.btn_blue)
            }
        }
    }
}