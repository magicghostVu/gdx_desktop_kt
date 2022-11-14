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

    private val timeStep = 0.02f


    var startTime = System.currentTimeMillis()

    private var numTick = 0

    private val logger = MLogger.logger

    private val inputQueue = mutableSetOf<Command>()

    private val allMascot = mutableListOf<Body>()

    private var startMascot = false;

    private var stopAll = false;

    private val velocityMascot = 1.7f;


    private val numTickMinChangeDirection = 50;
    private val numTickMaxChangeDirection = 150;


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
            allMascot.add(createAMascot(it, position))
        }
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

    private fun updateMascotDirection() {
        if (stopAll) return
        allMascot.forEach {
            val dataMascot = it.userData as MascotData
            if (dataMascot.tickWillChangeDirection == numTick) {
                //eliminateLinearVelocityMascot(it) // coi như v đã bằng 0, tính ra lực mới

                val x = Random.nextDouble(-1.0, 1.0).toFloat()
                val y = Random.nextDouble(-1.0, 1.0).toFloat()
                val f = Vector2(x, y)
                f.nor()
                f.scl(velocityMascot) // len 2
                f.scl(it.mass / timeStep)
                it.applyForceToCenter(f, true)

                val nextTickChangeDirect = getNextTickWillChangeDirection()
                dataMascot.tickWillChangeDirection = nextTickChangeDirect
            }
        }
    }

    // sử dụng cricle cho mascot
    // đường kính 0.8m
    private fun createAMascot(id: Int, position: Vector2): Body {
        val mascotBodyDef = BodyDef()
        mascotBodyDef.type = BodyDef.BodyType.DynamicBody
        mascotBodyDef.position.set(position.x, position.y)
        val mascotBody = world.createBody(mascotBodyDef)
        //mascotBody.isFixedRotation = true
        val mascotShape = CircleShape()
        mascotShape.radius = 0.4f
        val mascotFixtureDef = FixtureDef()
        mascotFixtureDef.shape = mascotShape
        mascotFixtureDef.density = 1f
        // nảy lại tối đa
        mascotFixtureDef.restitution = 1f;
        mascotBody.createFixture(mascotFixtureDef)
        mascotShape.dispose()


        //
        //val tickChangeDirect = getNextTickWillChangeDirection()
        mascotBody.userData = MascotData(id)
        //logger.info("mascot $id will change direction at $tickChangeDirect")

        return mascotBody
    }

    private fun getNextTickWillChangeDirection(): Int {
        return Random.nextInt(numTickMinChangeDirection, numTickMaxChangeDirection + 1) + numTick
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
        val numTickExpected = timeElapsed / (timeStep * 1000f)
        val numTickWillRun = numTickExpected - numTick
        repeat(numTickWillRun.toInt()) {
            doSimulation()
        }


        debugRenderer.render(world, camera.combined)
    }


    private fun doSimulation() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            inputQueue.add(Command.START_RUN_MASCOT)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            inputQueue.add(Command.STOP_ALL_MASCOT)
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
                Command.STOP_ALL_MASCOT -> {
                    stopAllMascot()
                }
            }
        }

        // update all mascot
        // giữ vận tốc
        //random lại hướng sau một thời gian

        //


        inputQueue.clear()
        world.step(timeStep, 8, 3)


        //
        // tính toán lại hướng
        updateMascotDirection()

        // keep vận tốc
        keepVelocity()


        numTick++

    }

    //private


    // add lực nếu như có object nào đó di chuyển chậm hơn
    private fun keepVelocity() {
        if (stopAll) return
        allMascot.forEach {
            val currentVelocity = it.linearVelocity.cpy()
            // add thêm lực để fix lại
            if (currentVelocity.len() != velocityMascot) {
                val targetVelocity = currentVelocity.cpy().nor()
                targetVelocity.scl(velocityMascot) // v này sẽ có len bằng 2 và giữ nguyên hướng hiện tại
                val deltaV = Vector2(
                    targetVelocity.x - currentVelocity.x,
                    targetVelocity.y - currentVelocity.y
                )

                val f = deltaV.scl(it.mass / timeStep)
                it.applyForceToCenter(f, true)
            }
        }
    }

    private fun stopAllMascot() {
        if (stopAll) return
        stopAll = true;
        logger.info("stop all")
        // dùng add force để triệt tiêu tất cả vận tốc của các body
        allMascot.forEach {
            eliminateLinearVelocityMascot(it)
        }
    }

    private fun eliminateLinearVelocityMascot(mascotBody: Body): Unit {
        val currentVelocity = mascotBody.linearVelocity
        if (currentVelocity.len() == 0.0f) {
            return
        }
        // lấy ngược lại
        val vToAdd = Vector2(-currentVelocity.x, -currentVelocity.y)
        val r = mascotBody.mass / (timeStep)
        val f = vToAdd.scl(r)
        mascotBody.applyForceToCenter(f, true)
    }

    private fun runAllMascot() {
        // vận tốc cố định 2m/s nhưng hướng khác nhau
        // cần tính toán lại
        allMascot.forEach {
            var x = Random.nextFloat()
            var y = Random.nextFloat()

            if (x < 0.5f) {
                x = -x
            }

            if (y < 0.5f) {
                y = -y;
            }
            val force = Vector2(x, y)
            force.nor() // normalize
            force.scl(velocityMascot) // v len sẽ bằng 2
            val rate = it.mass / (timeStep)
            force.scl(rate)
            it.applyForceToCenter(force, true)

            val mascotData = it.userData as MascotData
            mascotData.tickWillChangeDirection = getNextTickWillChangeDirection()
        }
        logger.info("run all mascot")
    }


    override fun dispose() {
        world.dispose()
    }

    companion object {
        val numMascot: Int = 8
    }
}