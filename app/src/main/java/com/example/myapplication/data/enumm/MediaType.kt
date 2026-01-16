package com.example.myapplication.data.enumm

enum class MediaType(val value: String) {
    AUDIO("AUDIO"),
    VIDEO("VIDEO");
    companion object {
        fun fromValue(value: String): MediaType {
            return MediaType.entries.find { it.value == value } ?: AUDIO
        }
    }
}