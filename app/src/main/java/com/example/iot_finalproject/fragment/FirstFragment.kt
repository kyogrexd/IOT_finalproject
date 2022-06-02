package com.example.iot_finalproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.iot_finalproject.MainActivity
import com.example.iot_finalproject.R
import com.example.iot_finalproject.databinding.FragmentFirstBinding
import com.example.iot_finalproject.manager.MQTTConnectionParams
import com.example.iot_finalproject.manager.MQTTmanager
import com.example.iot_finalproject.protocols.UIUpdaterInterface
import com.google.gson.Gson

class FirstFragment: Fragment(), UIUpdaterInterface {
    private var binding: FragmentFirstBinding? = null

    lateinit var mActivity: MainActivity
    private var mqttManager: MQTTmanager? = null
    private var isTurnOn = false
    private var isAuto = true
    private var count = 0
    private var speed = 0
    private var btnType = BtnType.OFF

    data class DataReq(val isAuto: Boolean, val isTurnOn: Boolean, val speed: Int)
    data class DataRes(val isTurnOn: Boolean, val speed: Int, val count: Int)

    enum class BtnType {OFF, LOW, MID, HIGH}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity
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
                tvConnect.visibility = View.GONE
                gpConsole.visibility = View.VISIBLE
            } else {
                updateStatusViewWith("Disconnected")
                tvConnect.visibility = View.VISIBLE
                gpConsole.visibility = View.GONE
            }
        }
    }

    override fun updateStatusViewWith(status: String) {
        binding?.tvStatus?.text = status
    }

    override fun update(message: String) {
        binding?.run {

            val res = Gson().fromJson(message, DataRes::class.java)
            count = res.count
            isTurnOn = res.isTurnOn
            speed = res.speed
            if (!isAuto) {
                when (speed) {
                    0 -> btnType = BtnType.OFF
                    64 -> btnType = BtnType.LOW
                    128 -> btnType = BtnType.MID
                    255 -> btnType = BtnType.HIGH
                }

                changeButtonStyle(btnType)
            }
            tvCount.text = "目前人數:$count"
            tvFan.text = if(isTurnOn) "風扇狀態: ON" else "風扇狀態: OFF"
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
            }

            tvDisconnect.setOnClickListener {
                mqttManager?.disconnect()
            }

            tvSwitch.setOnClickListener {
                isAuto = !isAuto
                tvSwitch.setBackgroundResource(if (isAuto) R.drawable.btn_turn_off else R.drawable.btn_turn_on)
                tvSwitch.text = if (isAuto) "MANUAL" else "AUTO"
                llConsole.visibility = if (isAuto) View.GONE else View.VISIBLE
                changeButtonStyle(btnType)
            }

            tvOff.setOnClickListener {
                btnType = BtnType.OFF
                changeButtonStyle(btnType)
                val json = Gson().toJson(DataReq(false, false, 0))
                mqttManager?.publish(json.toString())
            }
            tvLow.setOnClickListener {
                btnType = BtnType.LOW
                changeButtonStyle(btnType)
                val json = Gson().toJson(DataReq(false, true, 64))
                mqttManager?.publish(json.toString())
            }
            tvMid.setOnClickListener {
                btnType = BtnType.MID
                changeButtonStyle(btnType)
                val json = Gson().toJson(DataReq(false, true, 128))
                mqttManager?.publish(json.toString())
            }
            tvHigh.setOnClickListener {
                btnType = BtnType.HIGH
                changeButtonStyle(btnType)
                val json = Gson().toJson(DataReq(false, true, 255))
                mqttManager?.publish(json.toString())
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