package pack.logic

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.ScreenUtils
import pack.logger.MLogger
import pack.logic.collision_filter.MyCategory
import pack.logic.contact_listener.MContactListener
import pack.logic.team_id.PlayerRotateData
import pack.logic.team_id.PlayerRotateState
import pack.logic.team_id.TeamId
import kotlin.math.abs
import kotlin.math.tan
import kotlin.random.Random

class PaintMisbehavingLogic() : ApplicationAdapter() {


    private lateinit var camera: OrthographicCamera

    lateinit var world: World

    lateinit var debugRenderer: Box2DDebugRenderer

    private val timeStep = 0.02f;


    var startTime = System.currentTimeMillis()

    private var numTick = 0

    private val logger = MLogger.logger

    private val inputQueue = mutableSetOf<Command>()

    private val allMascot = mutableListOf<Body>()

    private var startMascot = false;

    private var stopAll = false;

    private val velocityMascot = 1.7f;

    private val velocityBullet = 6f;


    private val numTickMinChangeDirection = 50;
    private val numTickMaxChangeDirection = 150;


    private var lastTickSpawnBullet: Int = 0;


    //ít nhất 25 tick mới được bắn một viên đạn
    private val coolDownSpawnBullet = 25;


    private val allBullets: MutableList<Body> = mutableListOf()

    // cho player một circle nằm trên bound
    // có khối lượng nhưng không cho va chạm với bất cứ object nào
    // set thêm sensor cho chắc??
    private lateinit var playerBody: Body


    override fun create() {
        camera = OrthographicCamera()
        // camera view port 20m*20m
        camera.setToOrtho(false, 10f, 10f)

        world = World(Vector2(0f, 0f), true)
        world.setContactListener(MContactListener())
        debugRenderer = Box2DDebugRenderer()
        createBounds()
        logger.info("game created")


        val initPosX = 0.7f
        val distanceBetweenMascot = 0.9f

        repeat(numMascot) {
            val position = Vector2(initPosX + it * distanceBetweenMascot, 2f)
            allMascot.add(createAMascot(it, position))
        }


        // create player body
        createPlayerBody(Vector2(1f, 0.1f), (Math.PI.toFloat() / 2))//90 độ

    }


    // player là một circle nằm trên bound
    private fun createPlayerBody(position: Vector2, initAngular: Float) {
        val bodyDef = BodyDef()
        bodyDef.position.set(position.x, position.y)
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.angularDamping = 10f
        bodyDef.angle = initAngular



        playerBody = world.createBody(bodyDef)
        playerBody.userData = PlayerRotateData(TeamId.DOWN)


        val circleShape = CircleShape()
        circleShape.radius = 0.2f

        val fixtureDef = FixtureDef()
        fixtureDef.shape = circleShape
        fixtureDef.isSensor = true
        fixtureDef.density = 5f

        fixtureDef.filter.categoryBits = MyCategory.PLAYER.category
        fixtureDef.filter.maskBits = MyCategory.PLAYER.getMasksBit()


        playerBody.createFixture(fixtureDef)
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
    private fun createABullet(position: Vector2, direction: Vector2): Body {

        // không biết hướng là gì
        if (direction.len() == 0f) {
            logger.info("can not determine direction of bullet")
            throw IllegalArgumentException("wrong direction")
        }


        val bulletBodyDef = BodyDef()
        bulletBodyDef.type = BodyDef.BodyType.DynamicBody
        bulletBodyDef.position.set(position.x, position.y)

        val bulletBody = world.createBody(bulletBodyDef)

        val bulletShape = CircleShape()
        bulletShape.radius = 0.17f;// bán kính 17cm

        val bulletFixtureDef = FixtureDef()
        bulletFixtureDef.shape = bulletShape
        bulletFixtureDef.density = 1f;
        bulletFixtureDef.isSensor = true
        bulletFixtureDef.filter.categoryBits = MyCategory.BALL.category
        bulletFixtureDef.filter.maskBits = MyCategory.BALL.getMasksBit()




        bulletBody.createFixture(bulletFixtureDef)
        bulletShape.dispose()


        // add force to init velocity
        val v = direction.cpy()
        v.nor()
        v.scl(velocityBullet)

        val f = v.scl(bulletBody.mass / timeStep)
        bulletBody.applyForceToCenter(f, true)

        return bulletBody
    }

    private fun updateMascotDirection() {
        if (stopAll) return
        allMascot.forEach {
            val dataMascot = it.userData as MascotData
            if (dataMascot.tickWillChangeDirection == numTick) {
                //eliminateLinearVelocityMascot(it) // coi như v đã bằng 0, tính ra lực mới

                var x = Random.nextDouble(-1.0, 1.0).toFloat()
                var y = Random.nextDouble(-1.0, 1.0).toFloat()

                if (x == 0f && y == 0f) {
                    x = 0.5f
                    y = 0.5f
                }

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

        mascotFixtureDef.filter.categoryBits = MyCategory.MASCOT.category
        mascotFixtureDef.filter.maskBits = MyCategory.MASCOT.getMasksBit()



        mascotBody.createFixture(mascotFixtureDef)
        mascotShape.dispose()

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


        boundFixtureDef.filter.categoryBits = MyCategory.BOUND.category
        boundFixtureDef.filter.maskBits = MyCategory.BOUND.getMasksBit()


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


    val maxAngularVelocity = Math.PI.toFloat()  // 180 độ mỗi s

    private fun doSimulation() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            inputQueue.add(Command.START_RUN_MASCOT)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            inputQueue.add(Command.STOP_ALL_MASCOT)
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            inputQueue.add(Command.SPAWN_BULLET)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.C)) {
            inputQueue.add(Command.ROTATE_PLAYER_CCW)
        }

        if (Gdx.input.isKeyPressed(Input.Keys.V)) {
            inputQueue.add(Command.ROTATE_PLAYER_CW)
        }


        var torqueApplied = 0f;

        val torqueToApplyValue = Math.PI.toFloat() / 5

        inputQueue.forEach {
            when (it) {
                Command.START_RUN_MASCOT -> {
                    if (!startMascot) {
                        runAllMascot()
                        startMascot = true;
                    }
                }

                Command.SPAWN_BULLET -> {
                    spawnBullet()
                }

                Command.STOP_ALL_MASCOT -> {
                    stopAllMascot()
                }

                Command.ROTATE_PLAYER_CW -> {
                    torqueApplied = if (torqueApplied != 0f) {
                        0f
                    } else {
                        -torqueToApplyValue
                    }
                }

                Command.ROTATE_PLAYER_CCW -> {
                    torqueApplied = if (torqueApplied != 0f) {
                        0f
                    } else {
                        torqueToApplyValue
                    }

                }
            }
        }


        // bỏ lực nếu góc chạm ngưỡng trong frame tiếp theo


        val rotateData = playerBody.userData as PlayerRotateData


        val teamId = rotateData.teamId

        val currentVeloAngle = playerBody.angularVelocity

        var angleAfterThisFrame = playerBody.angle + currentVeloAngle * timeStep

        angleAfterThisFrame = normalAngle(angleAfterThisFrame)

        val valueValid = if (currentVeloAngle > 0) {
            teamId.constrainCCW().valueSatisfy(angleAfterThisFrame)
        } else {
            teamId.constrainCW().valueSatisfy(angleAfterThisFrame)
        }

        // nếu value này không thoả thì set sang reached một trong 2 trạng thái

        // nếu value thoả thì set về normal

        // update state rotate
        if (currentVeloAngle != 0f) {
            if (!valueValid) {
                if (currentVeloAngle >= 0) {
                    rotateData.rotateState = PlayerRotateState.REACHED_CCW_LIMIT
                } else {
                    rotateData.rotateState = PlayerRotateState.REACHED_CW_LIMIT
                }
            } else {
                rotateData.rotateState = PlayerRotateState.NORMAL
            }
        }


        // keep velocity


        val acceptTorque = rotateData.rotateState.acceptTorque(torqueApplied)
        if (acceptTorque && torqueApplied != 0f && abs(playerBody.angularVelocity) < maxAngularVelocity) {
            playerBody.applyTorque(torqueApplied, true)
            //logger.info("apply torque, state is ${rotateData.rotateState}, torque $torqueApplied")
        }




        inputQueue.clear()
        world.step(timeStep, 8, 3)


        // post process

        // thử xoá các viên đạn ra khỏi map
        val iter = allBullets.iterator()
        var someDeleted = false
        while (iter.hasNext()) {
            val b = iter.next()
            if (b.transform.position.y > 9) {
                world.destroyBody(b)
                iter.remove()
                someDeleted = true
            }
        }

        /*if (someDeleted) {
            logger.info("all bullet is ${allBullets.size}")
        }*/

        //
        // tính toán lại hướng
        updateMascotDirection()

        // keep vận tốc
        keepVelocity()





        numTick++

    }


    private fun normalAngle(value: Float): Float {
        var tmp = value * radToDegree
        while (tmp > 180f) {
            tmp -= 360
        }

        while (tmp < -180f) {
            tmp += 360
        }
        return tmp / radToDegree
    }

    //private

    private val radToDegree = 57.29577f
    private fun spawnBullet() {
        // check cool down
        if (numTick - lastTickSpawnBullet > coolDownSpawnBullet) {


            var anglePlayer = playerBody.angle

            // chuẩn hoá angle
            // đưa về dạng -180, 180


            var angleInDegree = anglePlayer * radToDegree

            while (angleInDegree > 180f) {
                angleInDegree -= 360
            }

            while (angleInDegree < -180f) {
                angleInDegree += 360
            }
            anglePlayer = angleInDegree / radToDegree


            if (anglePlayer == Math.PI.toFloat() / 2) {
                anglePlayer = 1.57f
            }
            val y = abs(tan(anglePlayer))
            var x = 1f;

            if (anglePlayer > Math.PI.toFloat() / 2) {
                //logger.info("angle is $anglePlayer")
                x = -1f;
            }

            val vec2Direct = Vector2(x, y)
            vec2Direct.nor()

            //logger.info("create bullet direct at $vec2Direct")

            val bulletBody = createABullet(playerBody.position, vec2Direct)
            allBullets.add(bulletBody)
            lastTickSpawnBullet = numTick
        }
    }


    // add lực nếu như có object nào đó di chuyển chậm hơn hoặc nhanh hơn
    private fun keepVelocity() {
        if (stopAll) return
        if (!startMascot) return
        allMascot.forEach {
            val currentVelocity = it.linearVelocity.cpy()
            // add thêm lực để fix lại
            if (currentVelocity.len() != velocityMascot) {
                val targetVelocity = if (currentVelocity.len() > 0.0f) {
                    currentVelocity.cpy().nor()
                } else {
                    Vector2(1f, 1f).nor()
                }
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
        startMascot

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
        const val numMascot: Int = 8
    }
}