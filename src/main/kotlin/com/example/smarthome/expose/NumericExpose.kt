package com.example.smarthome.expose

import java.lang.NumberFormatException

data class NumericExpose(override val name: String,
                         override val description: String,
                         override val presets: List<String>,
                         val value_min: Int,
                         val value_max: Int,
                         override val isSet: Boolean,
                         override val isGet: Boolean ) : ExposeObject
{
    var currentValue: String = ((value_min+value_max)/2).toString();
    override fun validateValue(value: Any): Boolean {
        return if (value is String && value.lowercase() in presets) true
        else {
            try {
                (value as String).toInt() in value_min+1 until value_max
            } catch (e: NumberFormatException) {
                println("value is not a number")
                false
            }

        }
    }

    override fun canBeSet(): Boolean {
        return isSet
    }

    override fun canBeGet(): Boolean {
        return isGet
    }
}