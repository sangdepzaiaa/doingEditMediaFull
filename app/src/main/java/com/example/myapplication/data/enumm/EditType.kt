package com.example.myapplication.data.enumm

enum class EditType(val value: String) {
    NOTHING("nothing"),
    AUDIO_CUTTER("audio_cutter"),
    VIDEO_TO_AUDIO("video_to_audio"),
    VOICE_CHANGE("voice_change"),
    TEXT_TO_AUDIO("text_to_audio"),
    AUDIO_VOLUME("audio_volume"),
    AUDIO_MERGER("audio_merger"),
    AUDIO_MIXER("audio_mixer"),
    AUDIO_CONVERTER("audio_converter"),
    AUDIO_EFFECT_CHANGER("audio_effect_changer"),
    AUDIO_SPEED("audio_speed");

    companion object {
        fun fromValue(value: String): EditType {
            return entries.find { it.value == value } ?: NOTHING
        }
    }
}