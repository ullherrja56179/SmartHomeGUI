package expose

import java.lang.NumberFormatException

data class NumericExpose(override val name: String, override val description: String, override val presets: List<String>, private val value_min: Int, private val value_max: Int) : ExposeObject
{
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
}