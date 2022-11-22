package pack.logic.rotate_data

data class PlayerRotateData(
    val teamId: TeamId,
    var rotateState: PlayerRotateState = PlayerRotateState.NORMAL,
    var previousAngularVelocity: Float = 0f
) {

    fun updateState(currentAngle: Float) {
        // chỉ update nếu như frame trước có xoay
        if (previousAngularVelocity != 0f) {
            val currentAngleValid = teamId.angleValid(currentAngle, previousAngularVelocity)
            rotateState = if (!currentAngleValid) {
                if (previousAngularVelocity > 0) {
                    PlayerRotateState.REACHED_CCW_LIMIT
                } else {
                    PlayerRotateState.REACHED_CW_LIMIT
                }
            } else {
                PlayerRotateState.NORMAL
            }
        }
    }


}