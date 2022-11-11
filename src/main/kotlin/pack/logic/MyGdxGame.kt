package pack.logic

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import com.badlogic.gdx.utils.ScreenUtils
import org.slf4j.Logger
import pack.logger.MLogger

class MyGdxGame : ApplicationAdapter() {

    lateinit var batch: SpriteBatch
    lateinit var texture: Texture

    lateinit var camera: OrthographicCamera


    lateinit var physicWorld: World

    lateinit var debugRenderer: Box2DDebugRenderer

    private val logger: Logger = MLogger.logger


    private val rateUpdate = 16L;

    private var lastUpdateTime: Long = getCurrentTimeMs();


    lateinit var boxBody: Body;


    private fun getCurrentTimeMs() = System.currentTimeMillis()


    private fun doSimulation() {
        physicWorld.step(rateUpdate / 1000f, 8, 3)
        //logger.info("box state is ${boxBody.transform.position}, ${boxBody.transform.rotation}")
        /*if (boxBody.linearVelocity.len() == 0.0f) {
            logger.info("box state is ${boxBody.transform.position}")
            val boxShape = boxBody.fixtureList[0].shape as PolygonShape
            for (i in 0 until boxShape.vertexCount) {
                val v = Vector2()
                boxShape.getVertex(i, v)
                val vv = boxBody.getWorldPoint(v)
                //logger.info("vv is $vv")
            }
        }*/
    }

    override fun create() {
        batch = SpriteBatch()
        texture = Texture("badlogic.jpg")

        camera = OrthographicCamera()
        camera.setToOrtho(false, 20f, 20f)// tính bằng


        val gravity = Vector2(0f, -9.8f)
        physicWorld = World(gravity, true)
        debugRenderer = Box2DDebugRenderer()


        // add body

        val groundBodyDef = BodyDef()
        groundBodyDef.type = BodyDef.BodyType.StaticBody
        groundBodyDef.position.set(Vector2(0f, 0f))

        val groundBody = physicWorld.createBody(groundBodyDef)


        val boxGroundShape = PolygonShape()
        boxGroundShape.setAsBox(4.5f, 0.5f)

        val groundFixtureDef = FixtureDef()
        groundFixtureDef.shape = boxGroundShape

        groundFixtureDef.restitution = 0.3f;

        //fixture của cái nền nhà
        val groundFixture = groundBody.createFixture(groundFixtureDef)


        // các bước tạo fixture
        // 1 tạo body def (cái này có thể dùng lại được để tạo body khác)
        // 2 tạo body (dùng world và body def)
        // 3 tạo shape
        // 4 tạo fixture def từ shape
        // 5 tạo fixture từ body tại (1)
        // 6 dispose shape


        boxGroundShape.dispose()

        // create a circle fixture

        val circleBodyDef = BodyDef()
        circleBodyDef.type = BodyDef.BodyType.DynamicBody
        circleBodyDef.position.set(Vector2(5f, 4f))
        val circleBody = physicWorld.createBody(circleBodyDef)
        circleBody.linearDamping = 0.3f

        val circleShape = CircleShape()
        circleShape.radius = 0.15f;


        val circleFixtureDef = FixtureDef()
        circleFixtureDef.shape = circleShape

        circleFixtureDef.density = 1f;
        logger.info("density circle is ${circleFixtureDef.density}")
        val circleFixture = circleBody.createFixture(circleFixtureDef)


        logger.info("circle mass is ${circleBody.mass}")

        circleShape.dispose()

        //circleBody.linearVelocity = Vector2(0f, 8f)


        // tạo thêm box

        val boxBodyDef = BodyDef()
        boxBodyDef.type = BodyDef.BodyType.DynamicBody
        boxBodyDef.position.set(Vector2(4f, 4f))
        boxBody = physicWorld.createBody(boxBodyDef)


        val boxShape = PolygonShape()
        boxShape.setAsBox(0.5f, 0.5f)
        val boxFixtureDef = FixtureDef()
        boxFixtureDef.shape = boxShape
        boxFixtureDef.density = 1f;
        boxFixtureDef.restitution = 0.4f;


        // mass data được tính sau khi create fixture
        // gồm có:
        // khối lượng
        // trọng tâm
        // quán tính xoay
        val boxFixture = boxBody.createFixture(boxFixtureDef)


        //boxBody.fixtureList

        //boxBody.worldCenter

        boxBody.linearVelocity = Vector2(0f, 8f)
        boxBody.angularVelocity = 3.14f // tính theo radian/s


        logger.info("box body ${boxBody.transform.position}")


        /*val distanceJointDef = DistanceJointDef()
        distanceJointDef.initialize(

        )*/
        /*val joinDef = RevoluteJointDef()


        joinDef.bodyA = boxBody
        joinDef.bodyB = circleBody

        joinDef.localAnchorA.set(1f, 1f)
        joinDef.localAnchorB.set(0.5f, 0.5f)


        val pi = 3.1416f;


        physicWorld.contactList

        joinDef.lowerAngle = 2 * pi * 0.125f;// 90 độ

        joinDef.upperAngle = -2 * pi * 0.125f;

        // bật limmit thì mới có hiệu lực
        joinDef.enableLimit = true;*/

        /*joinDef.enableMotor = true;
        joinDef.motorSpeed = 0f
        joinDef.maxMotorTorque = 10f*/


        //physicWorld.createJoint(joinDef)



        boxShape.dispose()
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


        // collect input
        // apply input


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


        // render
        /*batch.begin()
        // all draw call here
        //batch.draw(texture, 0f, 0f)
        batch.draw(texture, 0f, 0f)
        batch.end()*/

        debugRenderer.render(physicWorld, batch.projectionMatrix)
    }
}