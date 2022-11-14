package pack.logic

import pack.logger.MLogger

class MascotData(val id: Int) {
    var tickWillChangeDirection: Int = -1
        set(value) {
            MLogger.logger.info("mascot $id will change direct at $value")
            field = value
        }

}