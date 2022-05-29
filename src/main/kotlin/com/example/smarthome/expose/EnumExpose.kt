package com.example.smarthome.expose

data class EnumExpose(override val name: String,
                      override val description: String,
                      override val presets: List<String>,
                      val values: List<String>,
                      override val isSet: Boolean,
                      override val isGet: Boolean) : ExposeObject
{
    override fun validateValue(value: Any): Boolean {
        return if (value !is String) false
        else (value in presets || value in values)
    }

    override fun canBeSet(): Boolean {
        return isSet
    }

    override fun canBeGet(): Boolean {
        return isGet
    }
}