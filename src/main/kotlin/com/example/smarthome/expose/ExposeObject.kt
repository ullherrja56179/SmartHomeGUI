package com.example.smarthome.expose

interface ExposeObject {
    val name: String
    val description: String
    val presets: List<String>
    val isSet: Boolean
    val isGet: Boolean

    fun validateValue(value: Any): Boolean
    fun canBeSet(): Boolean
    fun canBeGet(): Boolean
}