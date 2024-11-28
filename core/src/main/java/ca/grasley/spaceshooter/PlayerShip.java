package ca.grasley.spaceshooter;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerShip extends Ship {

    public PlayerShip(float yCenter, float xCenter, float width, float height, float movementSpeed, int shield,
                      float laserWidth, float laserHeight, float laserMovementSpeed, float timeBetweenShots,
                      TextureRegion shipTextureRegion, TextureRegion shieldTextureRegion, TextureRegion laserTextureRegion) {
        super(yCenter, xCenter, width, height, movementSpeed, shield, laserWidth, laserHeight, laserMovementSpeed,
            timeBetweenShots, shipTextureRegion, shieldTextureRegion, laserTextureRegion);
    }

    @Override
    public Laser[] fireLasers() {
        Laser[] lasers = new Laser[2];
        lasers[0] = new Laser(boundingBox.x+boundingBox.width*0.07f, boundingBox.y+boundingBox.height*0.45f, laserWidth, laserHeight, laserMovementSpeed, laserTextureRegion);
        lasers[1] = new Laser(boundingBox.x+boundingBox.width*0.93f, boundingBox.y+boundingBox.height*0.45f, laserWidth, laserHeight, laserMovementSpeed, laserTextureRegion);

        timeSinceLastShot = 0;

        return lasers;
    }
}
