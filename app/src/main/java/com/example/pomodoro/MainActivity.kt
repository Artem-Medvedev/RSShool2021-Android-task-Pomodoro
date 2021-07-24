package com.example.pomodoro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver{

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.addNewStopwatchButton.setOnClickListener {
            val min = if(binding.editTextNumber.text.isEmpty()) 0L
                      else binding.editTextNumber.text.toString().toLong()
            val hours = if(binding.editTextHours.text.isEmpty()) 0L
                        else binding.editTextHours.text.toString().toLong()
           when{
               min == 0L && hours == 0L -> Toast.makeText(applicationContext,"You must enter a number!",Toast.LENGTH_SHORT).show()
               else -> {
                   stopwatches.add(Stopwatch(nextId++, min*60000+hours*3600000,min*60000+hours*3600000, 0L, min*60000+hours*3600000, false))
                   stopwatchAdapter.submitList(stopwatches.toList())
               }
           }

        }
    }

    override fun onBackPressed() {
        Log.d("CDA", "onBackPressed Called")
        val setIntent = Intent(Intent.ACTION_MAIN)
        setIntent.addCategory(Intent.CATEGORY_HOME)
        setIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(setIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        var startedTimerId: Int? = null
        for (i in stopwatches.indices)
            if (stopwatches[i].isStarted) startedTimerId = i
        if (startedTimerId != null) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, stopwatches[startedTimerId].stopTime)
            startIntent.putExtra(SYSTEM_ON_TIMER_START_TIME_MS, stopwatches[startedTimerId].systemStartTime)
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun start(id: Int) {
        changeStopwatch(id,  true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, isStarted: Boolean) {
        for(i in 0 until stopwatches.size) {
            if (stopwatches[i].id == id)
                stopwatches[i] = Stopwatch(id, stopwatches[i].startTime, stopwatches[i].currentTime, System.currentTimeMillis(), stopwatches[i].currentTime, isStarted)
            else if (stopwatches[i].isStarted)
                stopwatches[i] = Stopwatch(stopwatches[i].id, stopwatches[i].startTime, stopwatches[i].stopTime, stopwatches[i].systemStartTime, stopwatches[i].currentTime, false)
            stopwatchAdapter.submitList(stopwatches.toList())

        }
    }
}