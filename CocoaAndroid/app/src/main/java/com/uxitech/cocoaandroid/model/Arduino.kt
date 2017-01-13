package com.uxitech.cocoaandroid.model

import com.google.gson.JsonObject
import com.github.salomonbrys.kotson.*
import java.util.HashMap


/**
 * Created by kuanyu on 2017/1/10.
 */

interface Serializable {
    fun createJsonObj(): JsonObject
}

object Arduino {
    abstract class Component : Serializable {
        abstract val tag: String
        /*
         * Actions is a Map<key, value> with
         *   key: Action Name
         *   value: Action function which returns command string that
         *          send to remote device.
         */
        abstract val actions: Map<String, () -> String>
        abstract val params: JsonObject

        override fun createJsonObj(): JsonObject {
            return jsonObject(
                "component" to tag,
                "action" to -1,
                "params" to params
            )
        }
    }

    // In fact, we need to read Led state from
    // remote device. _dummyLedAction just for
    // quick testing.
    enum class LedAction(val v: Int) {
        ON(2), OFF(1)
    }

    var _dummyLedAction = LedAction.OFF

    class Led : Component() {
        override val tag = "LED"

        override var params = jsonObject(
            "brightness" to 142
        )

        override val actions = mapOf(
            "toggle" to { toggle() }
        )

        fun toggle(): String {
            val switch = { actionType: LedAction ->
                if (actionType == LedAction.OFF)
                    LedAction.ON
                else
                    LedAction.OFF
            }

            _dummyLedAction = switch(_dummyLedAction)  // getAsBoolean

            val jsonObj = super.createJsonObj()
            jsonObj["action"] = _dummyLedAction.v

            return jsonObj.toString()
        }
    }
}
