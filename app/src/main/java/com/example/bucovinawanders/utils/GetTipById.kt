package com.example.bucovinawanders.utils

fun getTipById(idTip: Int): String {
    return when (idTip) {
        1 -> "Fortress"
        2 -> "Church"
        3 -> "Museum"
        4 -> "Park"
        5 -> "Monastery"
        else -> "Unknown"
    }
}