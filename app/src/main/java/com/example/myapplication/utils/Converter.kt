package com.example.myapplication.utils

import androidx.room.TypeConverter
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.FormatType
import com.example.myapplication.data.enumm.MediaType

class Converter {

    // ---- EditType ----
    @TypeConverter
    fun fromEditType(type: EditType): String = type.value

    @TypeConverter
    fun toEditType(value: String): EditType = EditType.fromValue(value)

    // ---- MediaType ----
    @TypeConverter
    fun fromMediaType(type: MediaType): String = type.value

    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.fromValue(value)

    // ---- FormatType ----
    @TypeConverter
    fun fromFormatType(type: FormatType): String = type.value

    @TypeConverter
    fun toFormatType(value: String): FormatType = FormatType.fromValue(value)
}