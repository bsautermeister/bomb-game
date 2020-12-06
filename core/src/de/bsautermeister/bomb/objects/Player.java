package de.bsautermeister.bomb.objects;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.badlogic.gdx.utils.Logger;

import de.bsautermeister.bomb.Cfg;
import de.bsautermeister.bomb.contact.Bits;
import de.bsautermeister.bomb.utils.PhysicsUtils;

public class Player {
    private final static Logger LOG = new Logger(Player.class.getSimpleName(), Cfg.LOG_LEVEL);

    private World world;
    private Body ballBody;
    private Body fixedSensorBody;

    private Vector2 startPosition;
    private float radius;

    private float lifeRatio;
    private int score;

    private boolean blockJumpUntilRelease;
    private int groundContacts;

    public Player(World world, Vector2 startPosition, float radius) {
        this.world = world;
        this.startPosition = startPosition;
        this.radius = radius;
        this.ballBody = createBody(radius);
        reset();
    }

    public void reset() {
        lifeRatio = 1f;
        ballBody.setTransform(startPosition, 0);
        ballBody.setActive(true);
        fixedSensorBody.setTransform(startPosition, 0);
    }

    private Body createBody(float radius) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        Body body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0.9f;
        fixtureDef.density = 10.0f;
        fixtureDef.filter.categoryBits = Bits.BALL;
        fixtureDef.filter.groupIndex = 1;
        fixtureDef.filter.maskBits = Bits.GROUND;
        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        shape.dispose();

        BodyDef fixedSensorBodyDef = new BodyDef();
        fixedSensorBodyDef.fixedRotation = true;
        fixedSensorBodyDef.type = BodyDef.BodyType.DynamicBody;
        fixedSensorBody = world.createBody(fixedSensorBodyDef);

        FixtureDef groundSensorFixtureDef = new FixtureDef();
        EdgeShape groundSensorShape = new EdgeShape();
        groundSensorShape.set(-radius * 0.66f, -radius * 1.1f,
                radius * 0.66f, -radius * 1.1f);
        groundSensorFixtureDef.filter.categoryBits = Bits.BALL_SENSOR;
        groundSensorFixtureDef.filter.maskBits = Bits.GROUND;
        groundSensorFixtureDef.filter.groupIndex = 1;
        groundSensorFixtureDef.shape = groundSensorShape;
        groundSensorFixtureDef.isSensor = true;

        fixedSensorBody.createFixture(groundSensorFixtureDef).setUserData(this);
        groundSensorShape.dispose();

        JointDef jointDef = new WheelJointDef();
        jointDef.bodyA = fixedSensorBody;
        jointDef.bodyB = body;

        world.createJoint(jointDef);

        return body;
    }

    public void control(boolean up, boolean left, boolean right) {
        if (right) {
            if (hasGroundContact()) {
                ballBody.applyTorque(-3f, true);
            }
            ballBody.applyForceToCenter(12.5f, 0, true);
        }
        if (left) {
            if (hasGroundContact()) {
                ballBody.applyTorque(3f, true);
            }
            ballBody.applyForceToCenter(-12.5f, 0, true);
        }
        if (up && hasGroundContact() && !blockJumpUntilRelease) {
            blockJumpUntilRelease = true;
            ballBody.applyLinearImpulse(0f, 12.5f, ballBody.getWorldCenter().x, ballBody.getWorldCenter().y, true);
        }
        if (!up) {
            blockJumpUntilRelease = false;
        }
    }

    public void update(float delta) {
        if (!isDead()) {
            lifeRatio = Math.min(1f, lifeRatio + Cfg.PLAYER_SELF_HEALING_PER_SECOND * delta);

            float lowestPositionY = -ballBody.getPosition().y - radius;
            score = Math.max(score, (int)(lowestPositionY * 10));

            PhysicsUtils.applyAirResistance(ballBody, 0.066f);
        }
    }

    private static final Circle impactCircle = new Circle();
    private static final Circle playerCircle = new Circle();
    public boolean impact(Vector2 position, float radius) {
        float blastDistance = PhysicsUtils.applyBlastImpact(ballBody, position, radius, 1f);

        Vector2 bodyPosition = ballBody.getPosition();
        impactCircle.set(position, radius);
        playerCircle.set(bodyPosition, getRadius());
        if (Intersector.overlaps(impactCircle, playerCircle)) {
            float damage = -1f / radius * blastDistance + 1.5f;
            lifeRatio = Math.max(0f, lifeRatio - damage);
            LOG.debug("Applied damage: " + damage);
        }

        if (isDead()) {
            ballBody.setActive(false);
            return true;
        }

        return false;
    }

    public void beginGroundContact() {
        groundContacts++;
    }

    public void endGroundContact() {
        groundContacts--;
    }

    public boolean hasGroundContact() {
        return groundContacts > 0;
    }

    public Vector2 getPosition() {
        return ballBody.getPosition();
    }

    public float getRotation() {
        return ballBody.getAngle() * MathUtils.radiansToDegrees;
    }

    public float getRadius() {
        return radius;
    }

    public float getLifeRatio() {
        return lifeRatio;
    }

    public boolean isDead() {
        return lifeRatio <= 0f;
    }

    public int getScore() {
        return score;
    }
}
