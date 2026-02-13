package com.example.loveapp

data class HeartFly(
    val id: Long,
    var x: Float,
    var y: Float,
    val vx: Float, // Скорость по горизонтали
    val vy: Float  // Скорость по вертикали (отрицательная = вверх)
) {
}