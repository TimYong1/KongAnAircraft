package com.tourcoo.util

import android.view.View

import java.util.concurrent.atomic.AtomicInteger

object ViewIDGenerator {
    private val nextGeneratedID = AtomicInteger(1)

    /**
     *用于单位转换的实用程序类
     */ /**
     *生成可分配给@see[View]的唯一ID。
     *向后兼容任何Android版本。
     */
    fun generateViewId(): Int {
        while (true) {
            val result = nextGeneratedID.get()
            var newValue = result + 1
            if (newValue > 0x00FFFFFF)
                newValue = 1 // Roll over to 1, not 0.
            if (nextGeneratedID.compareAndSet(result, newValue)) {
                return result
            }
        }
    }
}