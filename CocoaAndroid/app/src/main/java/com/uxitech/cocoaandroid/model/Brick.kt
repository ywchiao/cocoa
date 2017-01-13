package com.uxitech.cocoaandroid.model

import com.uxitech.cocoaandroid.comm.BTState
import com.uxitech.cocoaandroid.comm.BluetoothService


/**
 * Created by kuanyu on 2016/12/12.
 */

enum class BrickType {
    ACTION, LOOP, BRANCHING,
}

enum class BrickTag(val v: Int) {
    TOGGLE_LED(1),
    NONE(-1),  // no action
}

abstract class Brick(imgId: Int) {
    abstract val type: BrickType
    abstract val tag: BrickTag

    val imageId = imgId

    /*
     * Command: JSON string
     */
    fun evaluate(command: String) {
        if (BluetoothService.getState() != BTState.CONNECTED) { return }

        // Check that there's actually something to send
        if (command.isNotEmpty()) {
            // Get the message bytes and tell the BluetoothChatService to write
            val send = command.toByteArray()
            BluetoothService.write(send)
        }
    }
}

abstract class ActionBrick(imgId: Int) : Brick(imgId) {
    override val type = BrickType.ACTION

    abstract val component: Arduino.Component
    abstract fun start(): String
}

class BrickToggleLed(imgId: Int) : ActionBrick(imgId) {
    override val component = Arduino.Led()
    override val tag = BrickTag.TOGGLE_LED

    override fun start(): String {
        val actions = component.actions
        val command = actions["toggle"]?.invoke() as String

        evaluate(command)

        return command
    }
}

class BrickNone(imgId: Int) : ActionBrick(imgId) {
    override val component = Arduino.Led()
    override val tag = BrickTag.NONE

    override fun start(): String {
        return "{ }"
    }
}
