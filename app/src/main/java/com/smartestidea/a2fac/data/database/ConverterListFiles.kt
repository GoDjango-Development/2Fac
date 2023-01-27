package com.smartestidea.a2fac.data.database

import androidx.room.TypeConverter


class ConverterListFiles {
    @TypeConverter
    fun fromList(value: List<String>?): String? = value?.joinToString ( "::" )


    @TypeConverter
    fun toList(string: String?): MutableList<String> = if(string.isNullOrEmpty()) mutableListOf() else string.split("::").toMutableList()

}