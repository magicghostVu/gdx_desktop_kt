package pack.logic

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.Shape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.ScreenUtils

class MyGdxGame : ApplicationAdapter() {

    lateinit var batch: SpriteBatch
    lateinit var texture: Texture

    lateinit var camera: OrthographicCamera


    lateinit var physicWorld: World

    lateinit var debugRenderer: Box2DDebugRenderer


    // 20ms chạy 1 tích
    val rateUpdate = 20L;

    var lastUpdateTime = getCurrentTimeMs();

    private fun getCurrentTimeMs() = System.currentTimeMillis()


    private fun doSimulation() {
        physicWorld.step(rateUpdate / 1000f, 4, 4)
    }

    override fun create() {
        batch = SpriteBatch()
        texture = Texture("badlogic.jpg")

        camera = OrthographicCamera()
        camera.setToOrtho(false, 100f, 50f)// tính bằng


        val gravity = Vector2(0f, -9.8f)
        physicWorld = World(gravity, true)
        debugRenderer = Box2DDebugRenderer()


        // add body

        val groundDef = BodyDef()
        groundDef.type = BodyDef.BodyType.StaticBody
        groundDef.position.set(Vector2(11f, 5f))

        val groundBody = physicWorld.createBody(groundDef)


        val boxShape = PolygonShape()
        boxShape.setAsBox(10f, 0.5f)

        val fixtureDef = FixtureDef()
        fixtureDef.shape = boxShape

        fixtureDef.restitution = 0.6f;

        //
        val fixture = groundBody.createFixture(fixtureDef)
        boxShape.dispose()

        // create a circle



    }

    override fun dispose() {
        batch.dispose()
        texture.dispose()

        // xoá world
        physicWorld.dispose()
    }

    // run once per frame
    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        camera.update()
        batch.projectionMatrix = camera.combined


        // update logic
        val crTime = getCurrentTimeMs()
        val timeElapsed = crTime - lastUpdateTime
        val numTickWillRun = timeElapsed / rateUpdate
        repeat(numTickWillRun.toInt()) {
            doSimulation()
        }
        if (numTickWillRun > 0) {
            lastUpdateTime = crTime
        }



        batch.begin()
        // all draw call here
        //batch.draw(texture, 0f, 0f)

        debugRenderer.render(physicWorld, batch.projectionMatrix)
        //batch.draw(texture, 0f, 0f)

        batch.end()
    }
}