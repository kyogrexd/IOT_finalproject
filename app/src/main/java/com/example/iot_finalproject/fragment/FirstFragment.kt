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
    private var count = 0

    data class Data(val isTurnOn: Boolean, val speed: Int)

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
        }
        //更新狀態顯示
        if (status){
            updateStatusViewWith("Connected")
        } else {
            updateStatusViewWith("Disconnected")
        }
    }

    override fun updateStatusViewWith(status: String) {
        binding?.tvStatus?.text = status
    }

    override fun update(message: String) {
        binding?.run {

            if (message == "plus") {
                count ++
                tvCount.text = count.toString()
            }
//            val text = tvMessageHistory.text.toString()
//            var newText = "$text\n$message"
//
//            tvMessageHistory.text = newText
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

            tvSend.setOnClickListener {
//                val json  = Gson().toJson(User("kyogre", 20))
//                mqttManager?.publish(json.toString())
//
//                edMessage.setText("")
            }

            tvSwitch.setOnClickListener {
                isTurnOn = !isTurnOn
                if (isTurnOn) {
                    tvSwitch.setBackgroundResource(R.drawable.btn_turn_off)
                    tvSwitch.text = "OFF"
                    val json = Gson().toJson(Data(isTurnOn, 1000))
                    mqttManager?.publish(json.toString())

                } else {
                    tvSwitch.setBackgroundResource(R.drawable.btn_turn_on)
                    tvSwitch.text = "ON"
                    val json = Gson().toJson(Data(isTurnOn, 0))
                    mqttManager?.publish(json.toString())
                }
            }
        }
    }
}