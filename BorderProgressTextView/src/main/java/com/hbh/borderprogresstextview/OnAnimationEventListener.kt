package com.hbh.borderprogresstextview

interface OnAnimationEventListener {
    /**
     * 进度条动画开始下一次播放
     */
    fun onAnimationRepeat() {}

    /**
     * 进度条动画结束
     */
    fun onAnimationEnd() {}
}