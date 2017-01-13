package com.uxitech.cocoaandroid.model

import com.uxitech.cocoaandroid.R

/**
 * Created by kuanyu on 2017/1/12.
 */

object materials {  // for test
    val set1 = listOf(
        BrickNone(R.drawable.ic_up_square),
        BrickNone(R.drawable.ic_down_square),
        BrickNone(R.drawable.ic_left_square),
        BrickNone(R.drawable.ic_right_square),
        BrickToggleLed(R.drawable.ic_lighton_blue),
        BrickToggleLed(R.drawable.ic_lighton_purple)
    )

    val set2 = listOf(
        BrickNone(R.drawable.octocat1_80),
        BrickNone(R.drawable.octocat2_80),
        BrickNone(R.drawable.octocat3_80),
        BrickNone(R.drawable.block_80),
        BrickNone(R.drawable.block_outline_80)
    )
}

fun createBrickMaterials(materialSetId: Int): List<Brick> {
    when (materialSetId) {
        1 -> return materials.set1
        2 -> return materials.set2
        else -> throw Throwable("Unexpected ID for block material!")
    }
}

