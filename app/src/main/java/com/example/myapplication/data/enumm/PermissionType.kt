package com.example.myapplication.data.enumm

enum class PermissionType(val value: String) {
    RINGTONES("ringtone"),
    NOTIFICATIONS("notification"),
    ALARMS("alarm"),
    CONTACTS("contact"),
    ALL("all");

    companion object {
        fun fromValue(value: String): PermissionType {
            return PermissionType.entries.find { it.value == value } ?: ALL
        }
    }
}