package ca.grasley.spaceshooter;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public abstract class Ship {

    //ship characteristics
    float movementSpeed;
    int shield;

    //position and dimensions
    Rectangle boundingBox;

    //laser information
    float laserWidth, laserHeight;
    float timeBetweenShots;
    float laserMovementSpeed;
    float timeSinceLastShot = 0;

    //graphics
    TextureRegion shipTextureRegion, shieldTextureRegion, laserTextureRegion;

    public Ship(float yCenter, float xCenter, float width, float height, float movementSpeed, int shield,
                float laserWidth, float laserHeight, float laserMovementSpeed, float timeBetweenShots,
                TextureRegion shipTextureRegion, TextureRegion shieldTextureRegion, TextureRegion laserTextureRegion) {
        this.movementSpeed = movementSpeed;
        this.shield = shield;
        this.boundingBox = new Rectangle(xCenter - width / 2, yCenter - height / 2, width, height);

        this.shipTextureRegion = shipTextureRegion;
        this.shieldTextureRegion = shieldTextureRegion;
        this.laserTextureRegion = laserTextureRegion;
        this.laserWidth = laserWidth;
        this.laserHeight = laserHeight;
        this.laserMovementSpeed = laserMovementSpeed;
        this.timeBetweenShots = timeBetweenShots;
    }

    public void update(float deltaTime){
        timeSinceLastShot += deltaTime;
    }

    public boolean canFireLaser(){
        return timeSinceLastShot >= timeBetweenShots;
    }

    public abstract Laser[] fireLasers();

    public boolean intersects(Rectangle otherRectangle) {
        return boundingBox.overlaps(otherRectangle);
    }

    public void translate(float xChange, float yChange) {
        boundingBox.setPosition(boundingBox.x+xChange, boundingBox.y+yChange);
    }

    public void draw(Batch batch) {
        batch.draw(shipTextureRegion, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        if (shield > 0) {
            batch.draw(shieldTextureRegion, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        }
    }

    public boolean hitAndCheckDestroyed(Laser laser) {
        if(shield > 0) {
            shield--;
            return false;
        }
        return true;
    }
}
