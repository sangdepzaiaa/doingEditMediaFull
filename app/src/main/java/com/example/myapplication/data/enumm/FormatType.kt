package com.example.myapplication.data.enumm

enum class FormatType(val value: String) {
    WAV("wav"),
    MP3("mp3"),
    AAC("aac"),
    FLAC("flac"),
    OGG("ogg");

    val extension: String
        get() = ".$value"

    val mimeType: String
        get() = when (this) {
            MP3 -> "audio/mpeg"
            AAC -> "audio/aac"
            WAV -> "audio/wav"
            FLAC -> "audio/flac"
            OGG -> "audio/ogg"
        }

    val ffmpegCodec: String
        get() = when (this) {
            MP3 -> "libmp3lame"
            AAC -> "aac"
            WAV -> "pcm_s16le"
            FLAC -> "flac"
            OGG -> "libvorbis"
        }

    val qualityCodecs: String
        get() = when(this) {
            MP3-> "-c:a libmp3lame -qscale:a 2"
            AAC-> "-c:a aac -b:a 192k"
            WAV-> "-c:a pcm_s16le"
            FLAC-> "-c:a flac -compression_level 5"
            OGG-> "-c:a libvorbis -qscale:a 5"
        }

    companion object {
        fun fromValue(value: String): FormatType {
            return FormatType.entries.find { it.value == value } ?: MP3
        }
    }
}
