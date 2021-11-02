package expose

interface ExposeObject {
    val name: String
    val description: String
    val presets: List<String>

    fun validateValue(value: Any): Boolean
}