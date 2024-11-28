package ca.grasley.spaceshooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

public class GameScreen implements Screen {

    //screen
    private Camera camera;
    private Viewport viewport;

    //graphics
    private SpriteBatch batch;
    private TextureAtlas textureAtlas;
    private Texture explosionTexture;

    private TextureRegion[] backgrounds;

    private TextureRegion playerShipTextureRegion, playerShieldTextureRegion,
        enemyShipTextureRegion, enemyShieldTextureRegion, playerLaserTextureRegion, enemyLaserTextureRegion;

    //timing
    private float[] backgroundOffsets = {0,0,0,0};
    private float backgroundMaxSrollingSpeed;
    private float timeBetweenEnemySpawns = 1f;
    private float enemySpawnTimer = 0f;

    //world parameters
    private final int WORLD_WIDTH = 42;
    private final int WORLD_HEIGHT = 128;
    private final float TOUCH_MOVEMENT_THRESHOLD = 0.5f;

    //game objects
    private PlayerShip playerShip;
    private LinkedList<EnemyShip> enemyShipList;
    private LinkedList<Laser> playerLaserList;
    private LinkedList<Laser> enemyLaserList;
    private LinkedList<Explosion> explosionList;

    GameScreen() {

        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        //set up the texture atlas
        textureAtlas = new TextureAtlas("images.atlas");

        backgrounds = new TextureRegion[4];
        backgrounds[0] = textureAtlas.findRegion("Starscape00");
        backgrounds[1] = textureAtlas.findRegion("Starscape01");
        backgrounds[2] = textureAtlas.findRegion("Starscape02");
        backgrounds[3] = textureAtlas.findRegion("Starscape03");

        backgroundMaxSrollingSpeed = (float) WORLD_HEIGHT / 4;

        //initialize the player ship
        playerShipTextureRegion = textureAtlas.findRegion("playerShip2_blue");
        playerShieldTextureRegion = textureAtlas.findRegion("shield2");
        enemyShipTextureRegion = textureAtlas.findRegion("enemyRed3");
        enemyShieldTextureRegion = textureAtlas.findRegion("shield1");
        enemyShieldTextureRegion.flip(false, true);
        playerLaserTextureRegion = textureAtlas.findRegion("laserBlue03");
        enemyLaserTextureRegion = textureAtlas.findRegion("laserRed03");

        explosionTexture = new Texture(Gdx.files.internal("explosion.png"));

        //set up the game objects
        playerShip = new PlayerShip(WORLD_HEIGHT/4, WORLD_WIDTH/2, 10, 10, 40, 6,
            0.4f, 4, 45, 0.5f,
            playerShipTextureRegion, playerShieldTextureRegion, playerLaserTextureRegion);
        enemyShipList = new LinkedList<EnemyShip>();

        playerLaserList = new LinkedList<Laser>();
        enemyLaserList = new LinkedList<Laser>();

        explosionList = new LinkedList<>();

        batch = new SpriteBatch();
    }

    @Override
    public void render(float delta) {
        batch.begin();

        //scrolling background
        renderBackground(delta);

        detectInput(delta);
        playerShip.update(delta);

        spawnEnemyShips(delta);

        ListIterator<EnemyShip> iterator = enemyShipList.listIterator();
        while(iterator.hasNext()) {
            EnemyShip enemyShip = iterator.next();
            moveEnemy(enemyShip,delta);
            enemyShip.update(delta);
            enemyShip.draw(batch);
        }

        //player ship
        playerShip.draw(batch);

        renderLasers(delta);

        renderExplosions(delta);

        detectCollisions();

        batch.end();
    }

    private void renderExplosions(float delta) {
        ListIterator<Explosion> iterator = explosionList.listIterator();
        while(iterator.hasNext()) {
            Explosion explosion = iterator.next();
            explosion.update(delta);
            if(explosion.isFinished()) {
                iterator.remove();
            }else{
                explosion.draw(batch);
            }
        }
    }

    private void spawnEnemyShips(float delta) {
        enemySpawnTimer += delta;

        if(enemySpawnTimer > timeBetweenEnemySpawns) {
            enemyShipList.add(new EnemyShip(WORLD_HEIGHT-5, SpaceShooterGame.random.nextFloat()*(WORLD_WIDTH-10)+5, 10, 10, 40, 1,
                0.3f, 5, 50, 0.8f,
                enemyShipTextureRegion, enemyShieldTextureRegion, enemyLaserTextureRegion));

            enemySpawnTimer -= timeBetweenEnemySpawns;
        }
    }

    private void moveEnemy(EnemyShip enemyShip, float delta) {
        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -enemyShip.boundingBox.x;
        downLimit = WORLD_HEIGHT/2-enemyShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - enemyShip.boundingBox.x - enemyShip.boundingBox.width;
        upLimit = WORLD_HEIGHT- enemyShip.boundingBox.y - enemyShip.boundingBox.height;

        float xMove = enemyShip.getDirectionVector().x * enemyShip.movementSpeed * delta;
        float yMove = enemyShip.getDirectionVector().y * enemyShip.movementSpeed * delta;

        if(xMove > 0) {
            xMove = Math.min(xMove, rightLimit);
        } else {
            xMove = Math.max(xMove, leftLimit);
        }

        if(yMove > 0) {
            yMove = Math.min(yMove, upLimit);
        } else {
            yMove = Math.max(yMove, downLimit);
        }

        enemyShip.translate(xMove, yMove);
    }

    private void detectInput(float delta) {
        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -playerShip.boundingBox.x;
        downLimit = -playerShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - playerShip.boundingBox.x - playerShip.boundingBox.width;
        upLimit = WORLD_HEIGHT/2 - playerShip.boundingBox.y - playerShip.boundingBox.height;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && rightLimit > 0) {
            float xChange = playerShip.movementSpeed * delta;
            xChange = Math.min(xChange, rightLimit);
            playerShip.translate(xChange, 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && leftLimit < 0) {
            float xChange = playerShip.movementSpeed * delta;
            xChange = Math.max(-xChange, leftLimit);
            playerShip.translate(xChange, 0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && upLimit > 0) {
            float yChange = playerShip.movementSpeed * delta;
            yChange = Math.min(yChange, upLimit);
            playerShip.translate(0f, yChange);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && downLimit < 0) {
            float yChange = playerShip.movementSpeed * delta;
            yChange = Math.max(-yChange, downLimit);
            playerShip.translate(0f, yChange);
        }

        if(Gdx.input.isTouched()){
            float xTouchPixels = Gdx.input.getX();
            float yTouchPixels = Gdx.input.getY();

            Vector2 touchPoint = new Vector2(xTouchPixels, yTouchPixels);
            touchPoint = viewport.unproject(touchPoint);

            Vector2 playerShipCenter = new Vector2(playerShip.boundingBox.x + playerShip.boundingBox.width / 2f,
                    playerShip.boundingBox.y + playerShip.boundingBox.height / 2f);

            float touchDistance = touchPoint.dst(playerShipCenter);

            if(touchDistance > TOUCH_MOVEMENT_THRESHOLD) {
                float xTouchDifference = touchPoint.x - playerShipCenter.x;
                float yTouchDifference = touchPoint.y - playerShipCenter.y;

                float xMove = xTouchDifference / touchDistance * playerShip.movementSpeed * delta;
                float yMove = yTouchDifference / touchDistance * playerShip.movementSpeed * delta;

                if(xMove > 0) {
                    xMove = Math.min(xMove, rightLimit);
                } else {
                    xMove = Math.max(xMove, leftLimit);
                }

                if(yMove > 0) {
                    yMove = Math.min(yMove, upLimit);
                } else {
                    yMove = Math.max(yMove, downLimit);
                }

                playerShip.translate(xMove, yMove);
            }
        }
    }

    private void detectCollisions() {

        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            ListIterator<EnemyShip> enemyIterator = enemyShipList.listIterator();
            while (enemyIterator.hasNext()) {
                EnemyShip enemyShip = enemyIterator.next();
                if (enemyShip.intersects(laser.getBoundingBox())) {
                    if(enemyShip.hitAndCheckDestroyed(laser)){
                        enemyIterator.remove();
                        explosionList.add(new Explosion(explosionTexture, 0.7f, new Rectangle(enemyShip.boundingBox)));
                    }
                    iterator.remove();
                    break;
                }
            }
        }

        iterator = enemyLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            if (playerShip.intersects(laser.getBoundingBox())) {
                if(playerShip.hitAndCheckDestroyed(laser)){
                    explosionList.add(new Explosion(explosionTexture, 0.7f, new Rectangle(playerShip.boundingBox)));
                    playerShip.shield = 10;
                }
                iterator.remove();
            }
        }
    }

    private void renderLasers(float delta) {
        //lasers
        if(playerShip.canFireLaser()){
            Laser[] lasers = playerShip.fireLasers();
            for (Laser laser : lasers) {
                playerLaserList.add(laser);
            }
        }

        ListIterator<EnemyShip> iteratorEnemyShip = enemyShipList.listIterator();
        while (iteratorEnemyShip.hasNext()) {
            EnemyShip enemyShip = iteratorEnemyShip.next();
            if(enemyShip.canFireLaser()){
                Laser[] lasers = enemyShip.fireLasers();
                enemyLaserList.addAll(Arrays.asList(lasers));
            }
        }


        //draw lasers
        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y += laser.movementSpeed * delta;
            if(laser.boundingBox.y > WORLD_HEIGHT) {
                iterator.remove();
            }
        }

        iterator = enemyLaserList.listIterator();
        while (iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y -= laser.movementSpeed * delta;
            if(laser.boundingBox.y + laser.boundingBox.height < 0) {
                iterator.remove();
            }
        }
    }

    private void renderBackground(float delta) {
        backgroundOffsets[0] += delta * backgroundMaxSrollingSpeed / 8;
        backgroundOffsets[1] += delta * backgroundMaxSrollingSpeed / 4;
        backgroundOffsets[2] += delta * backgroundMaxSrollingSpeed / 2;
        backgroundOffsets[3] += delta * backgroundMaxSrollingSpeed;

        for( int layer = 0; layer < backgroundOffsets.length; layer++ ) {
            if(backgroundOffsets[layer] > WORLD_HEIGHT) {
                backgroundOffsets[layer] = 0;
            }
            batch.draw(backgrounds[layer], 0, -backgroundOffsets[layer], WORLD_WIDTH, WORLD_HEIGHT);
            batch.draw(backgrounds[layer], 0, -backgroundOffsets[layer] + WORLD_HEIGHT, WORLD_WIDTH, WORLD_HEIGHT);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void show() {

    }
}
