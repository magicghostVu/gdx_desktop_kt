package pack.logic.team_id

enum class PlayerRotateState {
    NORMAL {
        override fun acceptTorque(torque: Float): Boolean {
            return true
        }
    },
    REACHED_CW_LIMIT {
        override fun acceptTorque(torque: Float): Boolean {
            return torque > 0f
        }
    },
    REACHED_CCW_LIMIT {
        override fun acceptTorque(torque: Float): Boolean {
            return torque < 0f;
        }
    };

    abstract fun acceptTorque(torque: Float): Boolean
}