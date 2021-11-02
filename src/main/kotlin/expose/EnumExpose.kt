package expose

data class EnumExpose(override val name: String, override val description: String, override val presets: List<String>, private val values: List<String>) : ExposeObject
{
    override fun validateValue(value: Any): Boolean {
        TODO("Not yet implemented")
    }
}