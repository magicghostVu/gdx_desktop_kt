package pack.logic

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.ScreenUtils
import pack.logger.MLogger
import kotlin.random.Random

class PaintMisbehavingLogic() : ApplicationAdapter() {


    private lateinit var camera: OrthographicCamera

    lateinit var world: World

    lateinit var debugRenderer: Box2DDebugRenderer

    private val timeStep = 20L


    var startTime = System.currentTimeMillis()

    var numTick = 0L

    private val logger = MLogger.logger

    private val inputQueue = mutableSetOf<Command>()

    private val allMascot = mutableListOf<Body>()

    private var startMascot = false;

    override fun create() {
        camera = OrthographicCamera()
        // camera view port 20m*20m
        camera.setToOrtho(false, 20f, 20f)

        world = World(Vector2(0f, 0f), true)
        debugRenderer = Box2DDebugRenderer()
        createBounds()
        logger.info("game created")


        val initPosX = 0.7f
        val distanceBetweenMascot = 0.9f



        repeat(numMascot) {
            val position = Vector2(initPosX + it * distanceBetweenMascot, 2f)
            allMascot.add(createAMascot(position))
        }

        //createAMascot(Vector2(0.8f, 2f))

        //createABullet(Vector2(1f, 1f))*/
    }

    // 4 cạnh chặn
    private fun createBounds() {
        createABound(Vector2(0.1f, 4.2f), 0.2f, 8f)// trái
        createABound(Vector2(4.2f, 8.3f), 8f, 0.2f)// trên
        createABound(Vector2(8.3f, 4.2f), 0.2f, 8f)// phải
        createABound(Vector2(4.2f, 0.1f), 8f, 0.2f) // dưới
    }


    // sử dụng circle shape cho bullet
    // bullet radius 0.17
    private fun createABullet(position: Vector2) {
        val bulletBodyDef = BodyDef()
        bulletBodyDef.type = BodyDef.BodyType.DynamicBody
        bulletBodyDef.position.set(position.x, position.y)
        val bulletBody = world.createBody(bulletBodyDef)

        val bulletShape = CircleShape()
        bulletShape.radius = 0.17f;// đường kính 17cm

        val bulletFixtureDef = FixtureDef()
        bulletFixtureDef.shape = bulletShape
        bulletFixtureDef.density = 1f;
        bulletBody.createFixture(bulletFixtureDef)
        bulletShape.dispose()
    }


    // sử dụng cricle cho mascot
    // đường kính 0.8m
    private fun createAMascot(position: Vector2): Body {
        val mascotBodyDef = BodyDef()
        mascotBodyDef.type = BodyDef.BodyType.DynamicBody
        mascotBodyDef.position.set(position.x, position.y)
        val mascotBody = world.createBody(mascotBodyDef)
        mascotBody.isFixedRotation = true
        val mascotShape = CircleShape()
        mascotShape.radius = 0.4f
        val mascotFixtureDef = FixtureDef()
        mascotFixtureDef.shape = mascotShape
        mascotFixtureDef.density = 1f
        // nảy lại tối đa
        mascotFixtureDef.restitution = 1f;
        mascotBody.createFixture(mascotFixtureDef)
        mascotShape.dispose()
        return mascotBody
    }

    // chú ý rằng box này có origin tại tâm của box
    // nên cần tính toán vị trí cho đúng
    private fun createABound(position: Vector2, width: Float, height: Float) {
        val boundBodyDef = BodyDef()
        boundBodyDef.type = BodyDef.BodyType.StaticBody
        boundBodyDef.position.set(position.x, position.y)
        val boundBody = world.createBody(boundBodyDef)
        val boundShape = PolygonShape()
        boundShape.setAsBox(width / 2, height / 2)
        val boundFixtureDef = FixtureDef()
        boundFixtureDef.shape = boundShape
        boundFixtureDef.restitution = 1f;
        boundBody.createFixture(boundFixtureDef)
        boundShape.dispose()
    }


    override fun render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f)
        camera.update()


        val timeElapsed = System.currentTimeMillis() - startTime
        val numTickExpected = timeElapsed / timeStep
        val numTickWillRun = numTickExpected - numTick
        repeat(numTickWillRun.toInt()) {
            doSimulation()
        }


        debugRenderer.render(world, camera.combined)
    }


    private fun doSimulation() {


        // collect input

        // apply input

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            inputQueue.add(Command.START_RUN_MASCOT)
        }


        inputQueue.forEach {
            when (it) {
                Command.START_RUN_MASCOT -> {
                    // apply impulse cho tất cả các mascot
                    if (!startMascot) {
                        runAllMascot()
                        startMascot = true;
                    }
                }
                Command.SPAWN_BULLET -> {

                }
            }
        }

        // update all mascot
        // giữ vận tốc
        //random lại hướng sau một thời gian


        inputQueue.clear()
        world.step(timeStep / 1000f, 8, 3)
        numTick++

        // post process các event xảy ra sau khi step
    }

    private fun runAllMascot() {

        // vận tốc cố định 3m/s nhưng hướng khác nhau
        // cần tính toán lại
        val v = 2.0f
        allMascot.forEach {
            val x = Random.nextFloat()
            val y = Random.nextFloat()
            val direction = Vector2(x, y)
            direction.nor()
            direction.scl(v)
            it.applyLinearImpulse(direction, it.transform.position, true)
        }

        logger.info("run all mascot")
    }

    override fun dispose() {
        world.dispose()
    }

    companion object {
        val numMascot: Int = 6
    }
}