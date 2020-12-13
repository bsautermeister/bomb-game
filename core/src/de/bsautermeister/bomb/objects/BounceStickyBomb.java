package de.bsautermeister.bomb.objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.bsautermeister.bomb.contact.Bits;

public class BounceStickyBomb extends Bomb {
    private static final float[] POLYGON_VERTICES = new float[] {
        0, .15f, .15f, .05f, .1f, -.15f, -.1f, -.15f, -.15f, .05f
    };

    private boolean ticking;
    private final float initialTickingTime;
    private float tickingTimer;

    private static final float INITIAL_STICKY_DELAY = 0.5f;
    private float stickyTimer;


    private JointDef stickyJointDef;
    private Joint stickyJoint;

    public BounceStickyBomb(World world, float tickingTime, float bodyRadius, float detonationRadius) {
        super(world, bodyRadius, detonationRadius, 1f);
        this.initialTickingTime = tickingTime;
        this.tickingTimer = initialTickingTime;
        stickyTimer = INITIAL_STICKY_DELAY;
    }

    @Override
    protected Body createBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        Body body = getWorld().createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.set(POLYGON_VERTICES);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.friction = 0.8f;
        fixtureDef.density = 10.0f;
        fixtureDef.restitution = 0.8f;
        fixtureDef.filter.categoryBits = Bits.BOMB;
        fixtureDef.filter.groupIndex = 1;
        fixtureDef.filter.maskBits = Bits.GROUND;
        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        shape.dispose();

        return body;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isTicking()) {
            tickingTimer = Math.max(0f, tickingTimer - delta);
            stickyTimer = Math.max(0f, stickyTimer - delta);
        }

        if (stickyJointDef != null) {
            getWorld().createJoint(stickyJointDef);
            stickyJointDef = null;
        }
    }

    @Override
    public boolean doExplode() {
        return tickingTimer <= 0;
    }

    @Override
    public void beginContact(Fixture otherFixture) {
        ticking = true;

        if (stickyJoint == null && stickyTimer <= 0f) {
            Body otherBody = otherFixture.getBody();
            WeldJointDef jointDef = new WeldJointDef();
            jointDef.initialize(getBody(), otherBody, otherBody.getPosition());
            stickyJointDef = jointDef;
        }
    }

    @Override
    public void endContact(Fixture otherFixture) {
        if (stickyJoint == null) return;

        Body otherBody = otherFixture.getBody();
        if (stickyJoint.getBodyA() == otherBody || stickyJoint.getBodyB() == otherBody) {
            // AFAIK Box2D internally takes care of destroying the joint. Here we just want to get
            // get rid of the dead reference.
            stickyJoint = null;
        }
    }

    public boolean isTicking() {
        return ticking;
    }

    public float getTickingProgress() {
        return 1f - tickingTimer / initialTickingTime;
    }

    @Override
    public boolean isFlashing() {
        float progress = getTickingProgress();
        return progress > 0.20f && progress <= 0.30f
                || progress > 0.5f && progress <= 0.60f
                || progress > 0.8f && progress <= 1f;
    }

    public static class KryoSerializer extends Serializer<BounceStickyBomb> {

        private final World world;

        public KryoSerializer(World world) {
            this.world = world;
        }

        @Override
        public void write(Kryo kryo, Output output, BounceStickyBomb object) {
            output.writeFloat(object.initialTickingTime);
            output.writeFloat(object.getBodyRadius());
            output.writeFloat(object.getDetonationRadius());
            output.writeFloat(object.tickingTimer);
            output.writeBoolean(object.ticking);
            output.writeFloat(object.stickyTimer);
            kryo.writeObject(output, object.getBody().getPosition());
            kryo.writeObject(output, object.getBody().getAngle());
            kryo.writeObject(output, object.getBody().getLinearVelocity());
            kryo.writeObject(output, object.getBody().getAngularVelocity());
        }

        @Override
        public BounceStickyBomb read(Kryo kryo, Input input, Class<? extends BounceStickyBomb> type) {
            BounceStickyBomb bomb = new BounceStickyBomb(
                    world,
                    input.readFloat(),
                    input.readFloat(),
                    input.readFloat()
            );
            bomb.tickingTimer = input.readFloat();
            bomb.ticking = input.readBoolean();
            bomb.stickyTimer = input.readFloat();
            bomb.getBody().setTransform(kryo.readObject(input, Vector2.class), input.readFloat());
            bomb.getBody().setLinearVelocity(kryo.readObject(input, Vector2.class));
            bomb.getBody().setAngularVelocity(input.readFloat());
            return bomb;
        }
    }
}
