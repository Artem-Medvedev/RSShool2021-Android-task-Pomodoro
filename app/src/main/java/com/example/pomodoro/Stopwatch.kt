package com.example.pomodoro

data class Stopwatch(
    val id: Int,
    val startTime: Long,
    val stopTime: Long,
    val systemStartTime: Long,
    var currentTime: Long,
    var isStarted: Boolean
)
