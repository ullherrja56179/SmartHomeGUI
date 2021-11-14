package com.example.smarthome.expose

data class BinaryExpose (override val name: String,
                         override val description: String,
                         override val presets: List<String>,
                         private val value_on: String,
                         private val value_off: String,
                         override val isSet: Boolean,
                         override val isGet: Boolean) : ExposeObject
{
    override fun canBeSet(): Boolean {
        return isSet
    }

    override fun canBeGet(): Boolean {
        return isGet
    }

    override fun validateValue(value: Any): Boolean {
        if (value !is String) return false
        if (presets.isNotEmpty() && presets.contains(value)) return true
        return (value.lowercase() == value_on.lowercase()) || (value.lowercase() == value_off.lowercase())
    }
}