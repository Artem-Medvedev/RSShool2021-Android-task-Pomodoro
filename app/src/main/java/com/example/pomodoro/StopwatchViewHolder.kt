package com.example.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.StopwatchItemBinding

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentTime.displayTime()
        binding.customViewOne.setPeriod(stopwatch.startTime)
        binding.customViewOne.setCurrent(stopwatch.startTime - stopwatch.currentTime)
        initButtonsListeners(stopwatch)
        when {
            stopwatch.isStarted -> startTimer(stopwatch)
            stopwatch.currentTime <= 0L -> endTimer(stopwatch)
            else -> stopTimer()
        }

    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentTime)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun endTimer(stopwatch: Stopwatch){
        binding.viewItem.setBackgroundColor(resources.getColor(R.color.red))
        binding.startPauseButton.setBackgroundColor(resources.getColor(R.color.red))
        binding.deleteButton.setBackgroundColor(resources.getColor(R.color.red))
        binding.customViewOne.setCurrent(0L)
        binding.startPauseButton.isEnabled = false
        binding.blinkingIndicator.isVisible = false
        val drawable = resources.getDrawable(R.drawable.ic_baseline_play_arrow_24)
        binding.startPauseButton.setImageDrawable(drawable)
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        stopwatch.isStarted = false
        timer?.cancel()
    }

    private fun startTimer(stopwatch: Stopwatch) {
        val drawable = resources.getDrawable(R.drawable.ic_baseline_pause_24)
        binding.startPauseButton.setImageDrawable(drawable)
        binding.startPauseButton.isEnabled = true

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.viewItem.setBackgroundColor(resources.getColor(R.color.white))
        binding.startPauseButton.setBackgroundColor(resources.getColor(R.color.white))
        binding.deleteButton.setBackgroundColor(resources.getColor(R.color.white))

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer() {
        val drawable = resources.getDrawable(R.drawable.ic_baseline_play_arrow_24)
        binding.startPauseButton.setImageDrawable(drawable)
        binding.startPauseButton.isEnabled = true

        timer?.cancel()

        binding.viewItem.setBackgroundColor(resources.getColor(R.color.white))
        binding.startPauseButton.setBackgroundColor(resources.getColor(R.color.white))
        binding.deleteButton.setBackgroundColor(resources.getColor(R.color.white))

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentTime = stopwatch.stopTime - (System.currentTimeMillis() - stopwatch.systemStartTime)
                binding.stopwatchTimer.text = stopwatch.currentTime.displayTime()
                binding.customViewOne.setCurrent(stopwatch.startTime - stopwatch.currentTime)
                if (stopwatch.currentTime <= 0L) onFinish()
            }

            override fun onFinish() {
                binding.stopwatchTimer.text = stopwatch.currentTime.displayTime()
                endTimer(stopwatch)
            }
        }
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val START_TIME = "00:00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}