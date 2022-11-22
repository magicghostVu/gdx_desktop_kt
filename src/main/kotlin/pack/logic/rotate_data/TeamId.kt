package pack.logic.rotate_data


enum class TeamId(val startAngle: Float) {
    LEFT(0f) {
        override fun constrainCW(): AngleConstrain {
            return AngleConstrain(ConstrainsType.NOT_SMALLER_THAN, degreeToRad(-90f))
        }

        override fun constrainCCW(): AngleConstrain {
            return AngleConstrain(ConstrainsType.NOT_BIGGER_THAN, degreeToRad(90f))
        }
    },

    RIGHT(3.1416f) { // bắt đầu ở 180

        override fun constrainCW(): AngleConstrain {
            return AngleConstrain(ConstrainsType.NOT_SMALLER_THAN, degreeToRad(90f))// không nhỏ hơn 90
        }

        override fun constrainCCW(): AngleConstrain {
            return AngleConstrain(ConstrainsType.NOT_BIGGER_THAN, degreeToRad(-90f))
        }

    },
    UP(-1.5708f) { // bắt đầu ở -90
        override fun constrainCW(): AngleConstrain {
            return AngleConstrain(ConstrainsType.NOT_SMALLER_THAN, degreeToRad(-150f))
        }

        override fun constrainCCW(): AngleConstrain {
            return AngleConstrain(ConstrainsType.NOT_BIGGER_THAN, -30f)
        }
    },
    DOWN(1.5708f) { //bắt đầu ở 90 độ
        override fun constrainCW(): AngleConstrain {
            return AngleConstrain(ConstrainsType.NOT_SMALLER_THAN, degreeToRad(0f))
        }

        override fun constrainCCW(): AngleConstrain {
            return AngleConstrain(ConstrainsType.NOT_BIGGER_THAN, degreeToRad(180f))
        }
    };

    abstract fun constrainCW(): AngleConstrain
    abstract fun constrainCCW(): AngleConstrain


    fun angleValid(angle: Float, previousVelocity: Float): Boolean {
        return if (previousVelocity > 0) {
            constrainCCW().valueSatisfy(angle)
        } else {
            constrainCW().valueSatisfy(angle)
        }
    }

    companion object {
        private val piFloat = Math.PI.toFloat()

        private fun degreeToRad(degree: Float): Float {
            return degree * (piFloat / 180)
        }
    }
}