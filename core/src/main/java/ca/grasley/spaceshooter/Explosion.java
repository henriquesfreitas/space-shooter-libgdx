package ca.grasley.spaceshooter;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Explosion {

    private Animation<TextureRegion> explosionAnimation;
    private float stateTimer;
    private Rectangle boundingBox;

    public Explosion(Texture texture, float totalAnimationTime, Rectangle boundingBox) {
        this.boundingBox = boundingBox;

        //split texture into animation frames
        TextureRegion[][] textureRegion2D = TextureRegion.split(texture, 64, 64);

        //convert 2D array into 1D and store in a list
        TextureRegion[] textureRegion1D = new TextureRegion[16];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                textureRegion1D[index] = textureRegion2D[i][j];
                index++;
            }
        }

        //create animation
        explosionAnimation = new Animation<TextureRegion>(totalAnimationTime / 16, textureRegion1D);
        stateTimer = 0;
    }

    public void update(float deltaTime) {
        stateTimer += deltaTime;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(explosionAnimation.getKeyFrame(stateTimer), boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
    }

    public boolean isFinished() {
        return explosionAnimation.isAnimationFinished(stateTimer);
    }
}
