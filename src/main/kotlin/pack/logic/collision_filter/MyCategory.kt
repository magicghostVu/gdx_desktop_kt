package pack.logic.collision_filter

import kotlin.experimental.or

enum class MyCategory(val category: Short) {

    BOUND(0x0001) {
        override fun getMasksBit(): Short {// va chạm mascot
            return MASCOT.category
        }
    },
    BALL(0x0002) {
        override fun getMasksBit(): Short {// va chạm mascot, bound, và các viên khác
            return category
                .or(MASCOT.category)
                .or(BOUND.category)
        }
    },
    PLAYER(0x0004) {
        override fun getMasksBit(): Short {// không va chạm với ai
            return 0x0000
        }
    },
    MASCOT(0x0008) {
        override fun getMasksBit(): Short {// va chạm với bound và ball và các con khác
            return BOUND.category
                .or(BALL.category)
                .or(category)
        }
    };


    abstract fun getMasksBit(): Short

}