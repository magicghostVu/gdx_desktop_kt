package pack.launcher


import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import pack.logic.PaintMisbehavingLogic

fun main(vararg args: String) {
    val config = Lwjgl3ApplicationConfiguration()
    config.setForegroundFPS(50)
    config.setTitle("GDX kt")
    config.setWindowedMode(500, 500)
    Lwjgl3Application(PaintMisbehavingLogic(), config)
}