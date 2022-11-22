package pack.logic.rotate_data

class AngleConstrain(
    private val constrainsType: ConstrainsType,
    private val value: Float
) {
    fun valueSatisfy(value: Float): Boolean {
        return when (constrainsType) {
            ConstrainsType.NOT_BIGGER_THAN -> {
                value <= this.value
            }

            ConstrainsType.NOT_SMALLER_THAN -> {
                value >= this.value
            }
        }
    }
}