package pack.launcher


import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import pack.logic.MyGdxGame

fun main(vararg args: String) {
    val config = Lwjgl3ApplicationConfiguration()
    config.setForegroundFPS(60)
    config.setTitle("GDX kt")
    Lwjgl3Application(MyGdxGame(), config)
}