package pack.logic.contact_listener


import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import pack.logger.MLogger

class MContactListener : ContactListener {

    private val logger = MLogger.logger

    override fun beginContact(contact: Contact) {
        logger.info("begin contact")
    }

    override fun endContact(contact: Contact) {

    }

    override fun preSolve(contact: Contact, oldManifold: Manifold) {

    }

    override fun postSolve(contact: Contact, impulse: ContactImpulse) {

    }
}