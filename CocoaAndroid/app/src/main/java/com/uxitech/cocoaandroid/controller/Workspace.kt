package com.uxitech.cocoaandroid.controller

import android.app.Activity
import android.os.Handler
import android.os.Message
import android.view.View
import kotlin.concurrent.*

import com.github.salomonbrys.kotson.jsonObject

import com.uxitech.cocoaandroid.model.*
import com.uxitech.cocoaandroid.comm.BTState
import com.uxitech.cocoaandroid.comm.BluetoothService
import com.uxitech.cocoaandroid.comm.HandlerKey
import com.uxitech.cocoaandroid.comm.MessageType
import com.uxitech.cocoaandroid.util.MyException
import com.uxitech.cocoaandroid.util.UIFactory.showLongToast
import com.uxitech.cocoaandroid.util.UIFactory.showShortToast
import com.uxitech.cocoaandroid.view.DockView
import com.uxitech.cocoaandroid.view.EditSpaceView


/**
 * Created by kuanyu on 2016/12/12.
 *
 * TODO: Decouple object _activity from Workspace
 */

object Workspace {
    private lateinit var _activity: Activity

    fun init(activity: Activity) {
        _activity = activity

        BluetoothService.initHandler(BluetoothHandler)
    }

    object BluetoothHandler: Handler() {  // Move to other module later
        var isDeviceConnected = false

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MessageType.STATE_CHANGE.v -> handleStateChange(msg.arg1)
                MessageType.WRITE.v -> handleWrite(msg)
                MessageType.READ.v -> handleRead(msg)
                MessageType.DEVICE_NAME.v -> handleDeviceInfo(msg)
                MessageType.TOAST.v -> handleToast(msg)
            }
        }

        private fun handleStateChange(msgState: Int) {
            when(msgState) {
                BTState.CONNECTED.v -> {
                    isDeviceConnected = true
                    _activity.invalidateOptionsMenu()
                    showLongToast(_activity, "Device connected")
                }

                BTState.CONNECTING.v -> { }

                BTState.LISTEN.v, BTState.NONE.v -> { }

                BTState.DISCONNECTED.v -> {
                    isDeviceConnected = false
                    _activity.invalidateOptionsMenu()
                    showLongToast(_activity, "Device maybe disconnected")
                }
            }
        }  // END handleStateChange()

        private fun handleWrite(msg: Message) {
            val readBuf = msg.obj as ByteArray
            val msgWrite = kotlin.text.String(readBuf)
            // do something
        }

        private fun handleRead(msg: Message) {
            val readBuf = msg.obj as ByteArray
            val msgRead = kotlin.text.String(readBuf,
                offset = 0,
                length = msg.arg1)
            // do something
        }

        private fun handleDeviceInfo(msg: Message) {
            val key = HandlerKey.DEVICE_NAME.v
            val connectedDeviceName = msg.data?.getString(key)
        }

        private fun handleToast(msg: Message) {
            val key = HandlerKey.TOAST.v
            val text = msg.data?.getString(key) as String
            showShortToast(_activity, text)
        }
    }  // End BluetoothHandler


    object Toolbox {
        var dummySwitch = 1  // for test, remove it later

        fun getBrickMaterials(): List<Brick> {
            val bricks: List<Brick> = createBrickMaterials(dummySwitch)

            if (dummySwitch == 1)
                dummySwitch = 2
            else
                dummySwitch = 1

            return bricks
        }

        fun loadBrickMaterials(bricks: MutableList<Brick>) {
            DockView.adapter.setItems(bricks)
        }

        fun createBrick(tagValue: Int, imgId: Int): Brick {
            return when (tagValue) {
                BrickTag.TOGGLE_LED.v -> BrickToggleLed(imgId)
                BrickTag.NONE.v -> BrickNone(imgId)
                else -> throw MyException("createBrick() failed: unexpected brick tag.")
            }
        }

        fun onDockChange(view: View) {
            val materials = Toolbox.getBrickMaterials() as MutableList<Brick>
            loadBrickMaterials(materials)
        }

        fun onEditSpaceClear(view: View) {
            EditSpaceView.adapter.clearItems()
        }

        fun onEvalBricks(view: View) {
            // sendMessageTest()
            val bricks = EditSpaceView.adapter.getItems()

            thread {
                bricks
                    .map { it as ActionBrick }
                    .forEach {
                        val cmd = it.start()
                        showShortToast(_activity, cmd)
                        Thread.sleep(1500)
                    }
            }
        }

        /*
         *  Test function for bricks evaluation
         *  TODO: brick eval
         */
        enum class Action(val v: Int) {
            TURN_OFF(1),
            TURN_ON(2),
            ADJUST(3),
        }

        private var _ledAction = Action.TURN_OFF
        fun sendMessageTest() {
            val message = jsonObject(
                "component" to "LED",
                "action" to _ledAction.v,
                "params" to jsonObject(
                    "brightness" to 142
                )
            ).toString()

            _ledAction = if (_ledAction == Action.TURN_ON)
                             Action.TURN_OFF
                         else
                             Action.TURN_ON

            if (BluetoothService.getState() != BTState.CONNECTED) {
                return
            }

            // Check that there's actually something to send
            if (message.isNotEmpty()) {
                // Get the message bytes and tell the BluetoothChatService to write
                val send = message.toByteArray()
                BluetoothService.write(send)
            }
        }
    }
}
