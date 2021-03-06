package de.bsautermeister.bomb.objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.bsautermeister.bomb.contact.Bits;

public class AirStrikeBomb extends Bomb {
    private boolean groundContact;

    public AirStrikeBomb(World world, float bodyRadius, float detonationRadius) {
        super(world, bodyRadius, 3, detonationRadius, 0.05f);
    }

    @Override
    protected void defineBody(BodyDef bodyDef) {
        bodyDef.gravityScale = 0f;
        // no linear damping, otherwise it would get slower and slower
        bodyDef.linearDamping = 0f;
    }

    @Override
    protected void defineFilter(Filter filter) {
        super.defineFilter(filter);
        filter.maskBits = Bits.GROUND | Bits.BALL;
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public boolean doExplode() {
        return groundContact;
    }

    @Override
    public boolean impactedByExplosions() {
        return false;
    }

    @Override
    public void beginContact(Fixture otherFixture) {
        super.beginContact(otherFixture);
        groundContact = true;
    }

    @Override
    public void endContact(Fixture otherFixture) {
        super.endContact(otherFixture);
    }

    @Override
    public boolean isFlashing() {
        return true;
    }

    @Override
    public boolean isTicking() {
        return false;
    }

    @Override
    public boolean isSticky() {
        return false;
    }

    public static class KryoSerializer extends Serializer<AirStrikeBomb> {

        private final World world;

        public KryoSerializer(World world) {
            this.world = world;
        }

        @Override
        public void write(Kryo kryo, Output output, AirStrikeBomb object) {
            output.writeFloat(object.getBodyRadius());
            output.writeFloat(object.getDetonationRadius());
            output.writeBoolean(object.groundContact);
            kryo.writeObject(output, object.getBody().getPosition());
            kryo.writeObject(output, object.getBody().getAngle());
            kryo.writeObject(output, object.getBody().getLinearVelocity());
            kryo.writeObject(output, object.getBody().getAngularVelocity());
        }

        @Override
        public AirStrikeBomb read(Kryo kryo, Input input, Class<AirStrikeBomb> type) {
            AirStrikeBomb bomb = new AirStrikeBomb(
                    world,
                    input.readFloat(),
                    input.readFloat()
            );
            bomb.groundContact = input.readBoolean();
            bomb.getBody().setTransform(kryo.readObject(input, Vector2.class), input.readFloat());
            bomb.getBody().setLinearVelocity(kryo.readObject(input, Vector2.class));
            bomb.getBody().setAngularVelocity(input.readFloat());
            return bomb;
        }
    }
}
