package com.example.iot_finalproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.iot_finalproject.MainActivity
import com.example.iot_finalproject.databinding.FragmentFirstBinding
import com.example.iot_finalproject.manager.MQTTConnectionParams
import com.example.iot_finalproject.manager.MQTTmanager
import com.example.iot_finalproject.protocols.UIUpdaterInterface

class FirstFragment: Fragment(), UIUpdaterInterface {
    private var binding: FragmentFirstBinding? = null

    private var mqttManager: MQTTmanager? = null
    lateinit var mActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity

        resetUIWithConnection(false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setListener()
    }

    override fun resetUIWithConnection(status: Boolean) {
        binding?.run {
            edAddress.isEnabled = !status
            edTopic.isEnabled = !status
            edMessage.isEnabled = status
            tvConnect.isEnabled = !status
            tvSend.isEnabled = status
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
            val text = tvMessageHistory.text.toString()
            var newText = "$text\n$message"

            tvMessageHistory.text = newText
        }
    }

    private fun setListener() {
        binding?.run {
            tvConnect.setOnClickListener {
                if (!edAddress.text.isNullOrEmpty() && !edTopic.text.isNullOrEmpty()) {
                    val host = "tcp://${edAddress.text.toString()}:1883"
                    val topic = edTopic.text.toString()
                    val connectionParams = MQTTConnectionParams("MQTTSample", host, topic, "", "")

                    mqttManager = MQTTmanager(connectionParams, mActivity, this@FirstFragment)
                } else {
                    updateStatusViewWith("Please enter all valid fields")
                }
            }

            tvSend.setOnClickListener {
                mqttManager?.publish(edMessage.text.toString())

                edMessage.setText("")
            }
        }
    }
}