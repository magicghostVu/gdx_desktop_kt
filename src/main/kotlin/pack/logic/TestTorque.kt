package pack.logic

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.ScreenUtils
import pack.logger.MLogger

class TestTorque : ApplicationAdapter() {

    private lateinit var camera: OrthographicCamera

    private lateinit var world: World

    lateinit var debugRenderer: Box2DDebugRenderer

    private val timeStep = 0.02f;


    private val startTime = System.currentTimeMillis()

    private var numTick = 0

    private val logger = MLogger.logger

    lateinit var circleBody: Body

    override fun create() {
        camera = OrthographicCamera()
        camera.setToOrtho(false, 20f, 20f)
        world = World(Vector2(0f, 0f), true)
        debugRenderer = Box2DDebugRenderer()

        circleBody = createCircleBody(Vector2(10f, 10f))

    }

    private fun createCircleBody(position: Vector2): Body {
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position.set(position.x, position.y)
        bodyDef.angularDamping = 0.3f
        val circleBody = world.createBody(bodyDef)
        val circleShape = CircleShape()
        circleShape.radius = 1f
        val circleFixtureDef = FixtureDef()
        circleFixtureDef.shape = circleShape
        circleFixtureDef.density = 1f
        circleBody.createFixture(circleFixtureDef)
        circleShape.dispose()
        return circleBody
    }


    override fun dispose() {

    }


    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f)
        camera.update()

        val timeElapsed = System.currentTimeMillis() - startTime
        val timeStepMilli = (timeStep * 1000).toInt()
        val numTickExpect = timeElapsed / timeStepMilli
        val numTickRun = numTickExpect - numTick
        repeat(numTickRun.toInt()) {
            doSimulation()
        }

        debugRenderer.render(world, camera.combined)
    }

    private val pi = 3.14159f


    var addedForce = false

    private fun doSimulation() {

        // some logic
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (!addedForce) {
                val i = circleBody.massData.I
                val torque = i * pi / timeStep // setup để cho quay 180 độ/1s (pi rad/s)
                circleBody.applyTorque(torque, true)
                logger.info("apply torque")
                addedForce = true
            }
        }
        world.step(timeStep, 8, 3)
        numTick++;
    }

}