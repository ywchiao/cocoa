package com.uxitech.cocoaandroid.util

import java.util.Random

/**
 * Created by kuanyu on 2016/12/14.
 */

fun dice(min: Int, max: Int): Int {
    val rand = Random()
    return rand.nextInt(max - min + 1) + min
}

fun MyException(message: String): Throwable {
    return Throwable(message)
}

fun <T> collToStringArray(coll: Collection<T>, stringify: (T) -> String)
    : Array<String> {

    val array = Array<String>(coll.size, { idx -> "element $idx" })

    coll.forEachIndexed { idx, element ->
        array[idx] = stringify(element)
    }

    return array
}