package pack.logic

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils

class MyGdxGame : ApplicationAdapter() {

    lateinit var batch: SpriteBatch
    lateinit var texture: Texture

    override fun create() {
        batch = SpriteBatch()
        texture = Texture("badlogic.jpg")
    }

    override fun dispose() {
        batch.dispose()
        texture.dispose()
    }

    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f)
        batch.begin()

        batch.draw(texture, 0f, 0f)

        batch.end()
    }
}