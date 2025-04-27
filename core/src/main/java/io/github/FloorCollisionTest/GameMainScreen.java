package io.github.FloorCollisionTest;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.FloorCollisionTest.Engine.Player;
import io.github.FloorCollisionTest.Engine.Tile;


public class GameMainScreen implements Screen {

    private final Main game;
    private final SpriteBatch batch;
    private final ArrayList<Tile> tiles = new ArrayList<>();
    private OrthographicCamera camera;
    private Viewport viewport;
    private Player player;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    public GameMainScreen(Main game) {
        this.game = game;
        this.batch = game.batch;

        camera = new OrthographicCamera();
        viewport = new FitViewport(1920, 1080, camera);
        viewport.apply();
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);

        player = new Player(new Rectangle(0, 100, 64, 128),Player.generateTextureFromAtlas("./assets/PlayerAtlasSprite.png", 64, 128, 0),0.4f);

        tiles.add(new Tile(new Rectangle(100, 100, 64, 64),Tile.generateTextureFromAtlas("./assets/Blocco1.png", 64, 64, 0), 0.5f));
        tiles.add(new Tile(new Rectangle(600, 400, 64, 64),Tile.generateTextureFromAtlas("./assets/TileTest.png", 64, 64, 0), 0.5f));
        tiles.add(new Tile(new Rectangle(800, 600, 64, 64),Tile.generateTextureFromAtlas("./assets/TileTest.png", 64, 64, 0), 0.5f));
        tiles.add(new Tile(new Rectangle(400, 400, 64, 64),Tile.generateTextureFromAtlas("./assets/NOT_EXISTING.png", 64, 64, 0), 0.5f));


        for (int i = 0; i <= 1920; i += 64) {
            tiles.add(new Tile(new Rectangle(i, 0, 64, 64),Tile.generateTextureFromAtlas("./assets/TileTest.png", 64, 64, 0), 0.5f));
        }

        // player attributes
        player.setMovementSpeed(200f);
        player.setJumpHeight(300f);
        player.setGravity(230f);
        player.setDashDistance(400f);
        player.setDashDuration(0.4f);
        player.setDashCooldown(0.5f);



    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.53f, 0.81f, 0.92f, 1f);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        player.updatePosition(delta, player.getGroundHitbox(tiles),player.getTopHitbox(tiles));

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.DashLeft();
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.DashRight();
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.moveLeft(delta,player.getLeftHitbox(tiles));
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.moveRight(delta,player.getRightHitbox(tiles));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            player.jump();
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            player.fallFaster(delta);
        }

        batch.begin();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        player.renderSprite(batch, Player.LINEAR, true);
        player.drawHitboxes(shapeRenderer);
        player.cameraSetup(camera,Player.FOLLOW_SMOOTH);
        for (Tile tile : tiles) {
            tile.updateSprite(delta);
            tile.renderSprite(batch, Tile.LINEAR, true);
        }

        Rectangle groundHitbox = player.getGroundHitbox(tiles);
        Rectangle topHitbox = player.getTopHitbox(tiles);
        Rectangle leftHitbox = player.getLeftHitbox(tiles);
        Rectangle rightHitbox = player.getRightHitbox(tiles);

        
        
        //System.out.println("Ground Hitbox: " + groundHitbox);
        //System.out.println("Top Hitbox: " + topHitbox);
        //System.out.println("Left Hitbox: " + leftHitbox);
        //System.out.println("Right Hitbox: " + rightHitbox);
        
        
        if(topHitbox != null) {
            shapeRenderer.setColor(0, 1, 0, 1); // GREEN
            shapeRenderer.rect(topHitbox.x, topHitbox.y, topHitbox.width, topHitbox.height);
        }
        if(leftHitbox != null) {
            shapeRenderer.setColor(0, 0, 1, 1); // BLUE
            shapeRenderer.rect(leftHitbox.x, leftHitbox.y, leftHitbox.width, leftHitbox.height);
        }
        if(rightHitbox != null) {
            shapeRenderer.setColor(1, 1, 1, 1); // WHITE
            shapeRenderer.rect(rightHitbox.x, rightHitbox.y, rightHitbox.width, rightHitbox.height);
        }
        if(groundHitbox != null) {
            shapeRenderer.setColor(1, 0, 0, 1); // RED
            shapeRenderer.rect(groundHitbox.x, groundHitbox.y, groundHitbox.width, groundHitbox.height);
        }

        shapeRenderer.end();
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height,true);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
    }

    @Override
    public void show() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}