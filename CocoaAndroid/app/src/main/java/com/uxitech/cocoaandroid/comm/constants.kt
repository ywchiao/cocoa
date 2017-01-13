package com.uxitech.cocoaandroid.comm

/**
 * Created by kuanyu on 2016/12/26.
 */

val NOTHING = -1

/* Intent Actions */
enum class ConnectRequest(val v: Int) {
    CONNECT_DEVICE(1),
    ENABLE_BT(2),
    // WiFi ...
}

/* Types of Message */
enum class MessageType(val v: Int) {
    STATE_CHANGE(1),
    READ(2),
    WRITE(3),
    DEVICE_NAME(4),
    TOAST(5),
}

/* Keys for getting data inside Handler */
enum class HandlerKey(val v: String) {
    DEVICE_NAME("device_item"),
    TOAST("toast"),
}
