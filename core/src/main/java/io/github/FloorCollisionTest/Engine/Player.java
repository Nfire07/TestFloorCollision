package io.github.FloorCollisionTest.Engine;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Player {
    
    // fondamental variables for the player
    private final Rectangle[] playerHitboxes;
    private float x,y;
    
    // fondamental variables for the player movement
    private float movementSpeed = 50f;
    private float jumpHeight = 50f;
    private final float dashSpeed = 100f;
    private float dashDuration = 0.2f;
    private float dashDistance = 200f;
    private float initialJumpSpeed = 20f;
    private float dashCooldown = 1.0f;
    private float timeSinceLastDash = dashCooldown;   
    private float gravity = 100f;

    // animation related variables
    private Animation<Texture> animation;
    private Texture currentFrame;
    
    // temporary variables for the player movement
    private float fallSpeed=0f;
    private float jumpSpeed=0f;
    private float stateTime = 0f;
    private float dashTimeRemaining = 0f;
    private float dashDirection = 0f; 
    private boolean isJumping = false;
    private boolean isFalling = false;
    private boolean isDashing = false; 
    private boolean hasDashed = false;
    
    // collision related constants
    public static final int COLLIDING_TOP = 1;
    public static final int COLLIDING_BOTTOM = 0;
    public static final int COLLIDING_LEFT = 2;
    public static final int COLLIDING_RIGHT = 3;

    // animation related constants
    public static final int LINEAR = 0;
    public static final int EASE_IN_OUT = 1;
    public static final int EASE_IN = 2;
    public static final int EASE_OUT = 3;

    // camera related constants
    public static final int FOLLOW = 0;
    public static final int FOLLOW_NO_Y = 1;
    public static final int FOLLOW_AS_OUT_OF_SCREEN = 2;
    public static final int FOLLOW_SMOOTH = 3;
    public static final int FOLLOW_SMOOTH_NO_Y = 4;
    public static final int DEAD_ZONE = 5;
    public static final int ZOOM = 6;
    public static final int SHAKE = 7;

    public Player(Rectangle hitbox,Texture[] textures,float frameDuration) {
        this.playerHitboxes = generatePlayerHitboxes(hitbox);
        this.x = hitbox.x;
        this.y = hitbox.y;
        this.animation = new Animation<>(frameDuration, textures);        
    }


    // returns an array of hitboxes for the tile required for handle collision specifically for top, bottom, left and right
    public static Rectangle[] generatePlayerHitboxes(Rectangle playerHitbox) {
        Rectangle[] playerHitboxes = new Rectangle[4];
    
        
        final float hitboxThickness = 1f;
    
        // Top hitbox
        playerHitboxes[COLLIDING_TOP] = new Rectangle(
            playerHitbox.x, 
            playerHitbox.y, 
            playerHitbox.width, 
            hitboxThickness 
        );
    
        // Bottom hitbox
        playerHitboxes[COLLIDING_BOTTOM] = new Rectangle(
            playerHitbox.x, 
            playerHitbox.y, 
            playerHitbox.width, 
            hitboxThickness
        );
    
        // Left hitbox
        playerHitboxes[COLLIDING_LEFT] = new Rectangle(
            playerHitbox.x, 
            playerHitbox.y+4*hitboxThickness, 
            hitboxThickness, 
            playerHitbox.height
        );
    
        // Right hitbox
        playerHitboxes[COLLIDING_RIGHT] = new Rectangle(
            playerHitbox.x + playerHitbox.width, 
            playerHitbox.y + 4*hitboxThickness, 
            hitboxThickness, 
            playerHitbox.height
        );

        return playerHitboxes;
    }
    // utility method to generate a texture from an atlas
    public static Texture[] generateTextureFromAtlas(String path, int frameWidth, int frameHeight,int padding) {
        Texture sheetTexture = new Texture(Gdx.files.internal(path));

        Pixmap fullPixmap = Player.getPixmapFromTexture(sheetTexture);

        int columns = fullPixmap.getWidth() / frameWidth;
        int rows = fullPixmap.getHeight() / frameHeight;
        int totalFrames = columns * rows;

        Texture[] frames = new Texture[totalFrames];

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                Pixmap framePixmap = new Pixmap(frameWidth, frameHeight, fullPixmap.getFormat());
                framePixmap.drawPixmap(
                    fullPixmap,
                    0, 0,
                    col * (frameWidth + padding), row * (frameHeight + padding),
                    frameWidth, frameHeight
                );
                frames[index++] = new Texture(framePixmap);
                framePixmap.dispose();
            }
        }

        fullPixmap.dispose();
        sheetTexture.dispose(); 

        return frames;
    }

    /*
    Utility method to extract a Pixmap from a Texture.
    */
    private static Pixmap getPixmapFromTexture(Texture texture) {
        texture.getTextureData().prepare();
        return texture.getTextureData().consumePixmap();
    }

    public Rectangle getPlayerHitbox(){
        return new Rectangle(playerHitboxes[0].x, playerHitboxes[0].y, playerHitboxes[0].width, playerHitboxes[2].height);
    }

    public Rectangle getPlayerHitbox(int typeOfCollision){
        return playerHitboxes[typeOfCollision];
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public float getJumpHeight() {
        return jumpHeight;
    }

    public void setJumpHeight(float jumpHeight) {
        this.jumpHeight = jumpHeight;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getGravity() {
        return gravity;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setDashDistance(float dashDistance) {
        this.dashDistance = dashDistance;
    }

    public float getDashDistance() {
        return this.dashDistance;
    }

    public void setDashDuration(float dashDuration) {
        this.dashDuration = dashDuration;
    }

    public float getDashDuration() {
        return this.dashDuration;
    }

    public void setDashCooldown(float dashCooldown) {
        this.dashCooldown = dashCooldown;
    }

    public float getDashCooldown() {
        return this.dashCooldown;
    }

    public void setInitialJumpSpeed(float initialJumpSpeed) {
        this.initialJumpSpeed = initialJumpSpeed;
    }

    public float getInitialJumpSpeed() {
        return this.initialJumpSpeed;
    }

    public void setFrameDuration(float frameDuration) {
        this.animation.setFrameDuration(frameDuration);
    }

    public float getFrameDuration() {
        return this.animation.getAnimationDuration();
    }

    public void setAnimation(Animation<Texture> animation) {
        this.animation = animation;
    }

    public Animation<Texture> getAnimation() {
        return this.animation;
    }

    public Texture getCurrentFrame() {
        return this.currentFrame;
    }
    
    /*
        stateTime variable holds time passed from start of animation
    */
    public void updateSprite(float deltaTime) {
        stateTime += deltaTime;
    }

    /*
        draws the actual sprites passing to the animation the time 
        passed from the start and specifing that the Animation is looping
        the AnimationStyle is a number that specify the type of animation
        0 = linear
        1 = easeInOut
        2 = easeIn
        3 = easeOut
        the default is linear
        the animation is not looping by default, but you can set it to loop by passing true to the method
    */
    public void renderSprite(SpriteBatch batch, int AnimationStyle,boolean looping) {
        float duration = animation.getAnimationDuration();
        float t = Math.min(stateTime / duration, 1f); 
    
        switch (AnimationStyle) {
            case LINEAR: 
                this.currentFrame = animation.getKeyFrame(stateTime, looping);
                break;
            case EASE_IN_OUT:
                float easedInOutT = (float)(-0.5f * (Math.cos(Math.PI * t) - 1));
                float easedInOutTime = easedInOutT * duration;
                this.currentFrame = animation.getKeyFrame(easedInOutTime, looping);
                break;    
            case EASE_IN: 
                float easeInT = t * t;
                float easeInTime = easeInT * duration;
                this.currentFrame = animation.getKeyFrame(easeInTime, looping);
                break;
            case EASE_OUT: 
                float easeOutT = 1 - (1 - t) * (1 - t);
                float easeOutTime = easeOutT * duration;
                this.currentFrame = animation.getKeyFrame(easeOutTime, looping);
                break;
            default:
                this.currentFrame = animation.getKeyFrame(stateTime, looping);
                break;
        }
    
        batch.draw(this.currentFrame,x,y,getPlayerHitbox().width, getPlayerHitbox().height);
    }

    /*
     methods for collision detection
    */
    // generic collision
    public boolean isColliding(Rectangle hitbox) {
        for (Rectangle playerHitbox : playerHitboxes) {
            if (playerHitbox.overlaps(hitbox)) {
                return true;
            }
        }
        return false;
    }

    public Rectangle getGroundHitbox(ArrayList<Tile> tiles){
        ArrayList<Tile> tilesUnderPlayer = new ArrayList<>();

        for(Tile tile : tiles){
            if(tile.getY() <= getPlayerHitbox().y && tile.getY()+tile.getTileHitbox().height>=getPlayerHitbox().y){
                tilesUnderPlayer.add(tile);
            }
        }

        Rectangle playerHitbox = getPlayerHitbox(COLLIDING_BOTTOM);
        for(Tile tile : tilesUnderPlayer){
            Rectangle tileHitbox = tile.getTileHitbox();
            Rectangle checker = new Rectangle(tileHitbox.x,playerHitbox.y,tileHitbox.width,tileHitbox.height);
            if(playerHitbox.overlaps(checker)){
                return tileHitbox;
            }
        }
        return null;   
    }

    public Rectangle getRightHitbox(ArrayList<Tile> tiles) {
        Rectangle playerHitbox = getPlayerHitbox();
        Rectangle rightChecker = new Rectangle(
            playerHitbox.x + playerHitbox.width, 
            playerHitbox.y,
            1, 
            playerHitbox.height
        );
    
        Rectangle closestTileHitbox = null;
        float closestDistance = Float.MAX_VALUE;
    
        for (Tile tile : tiles) {
            Rectangle tileHitbox = tile.getTileHitbox();
            if (rightChecker.overlaps(tileHitbox)) {
                float distance = tileHitbox.x - (playerHitbox.x + playerHitbox.width);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestTileHitbox = tileHitbox;
                }
            }
        }
    
        return closestTileHitbox;
    }

    public Rectangle getLeftHitbox(ArrayList<Tile> tiles) {
        Rectangle playerHitbox = getPlayerHitbox();
        Rectangle leftChecker = new Rectangle(
            playerHitbox.x - 1,
            playerHitbox.y,
            1, 
            playerHitbox.height
        );
    
        Rectangle closestTileHitbox = null;
        float closestDistance = Float.MAX_VALUE;
    
        for (Tile tile : tiles) {
            Rectangle tileHitbox = tile.getTileHitbox();
            if (leftChecker.overlaps(tileHitbox)) {
                float distance = playerHitbox.x - (tileHitbox.x + tileHitbox.width);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestTileHitbox = tileHitbox;
                }
            }
        }
    
        return closestTileHitbox;
    }

    public Rectangle getTopHitbox(ArrayList<Tile> tiles){
        ArrayList<Tile> tilesAbovePlayer = new ArrayList<>();

        for(Tile tile : tiles){
            if(tile.getY() >= getPlayerHitbox().y && tile.getY()<=getPlayerHitbox().y+getPlayerHitbox().height){
                tilesAbovePlayer.add(tile);
            }
        }

        Rectangle playerHitbox = getPlayerHitbox();
        for(Tile tile : tilesAbovePlayer){
            Rectangle tileHitbox = tile.getTileHitbox();
            Rectangle checker = new Rectangle(tileHitbox.x,playerHitbox.y,tileHitbox.width,tileHitbox.height);
            if(playerHitbox.overlaps(checker)){
                return tileHitbox;
            }
        }
        return null;   
    }

    /*
     0 = top
     1 = bottom
     2 = left
     3 = right
    */
    public boolean isColliding(Rectangle hitbox, int typeOfCollision) {
        return playerHitboxes[typeOfCollision].overlaps(hitbox);
    }

    /*
        update the hitboxes position based on the current position of the player
        this method should be called every frame to keep the hitboxes in sync with the player position
    */
    public void updateHitbox() {
        playerHitboxes[COLLIDING_TOP].setPosition(x, y+getPlayerHitbox().height);
        
        playerHitboxes[COLLIDING_BOTTOM].setPosition(x, y);
        
        playerHitboxes[COLLIDING_LEFT].setPosition(x , y+4);
    
        playerHitboxes[COLLIDING_RIGHT].setPosition(x + getPlayerHitbox().width, y+4);
    }

    public void updatePosition(float deltaTime, Rectangle groundHitbox, Rectangle topHitbox,Rectangle leftHitbox, Rectangle rightHitbox) {
        updateDash(deltaTime);
        if (isDashing) return;
    
        updateCooldown(deltaTime);
        bonkOnCeiling(topHitbox);
        handleFalling(deltaTime, groundHitbox);
        handleJumping(deltaTime);


        updateHitbox();
        resolveCollisions(groundHitbox, topHitbox, leftHitbox, rightHitbox);
        updateSprite(deltaTime);
    }

    private void updateDash(float deltaTime) {
        if (isDashing) {
            float dashStep = (dashDistance / dashDuration) * deltaTime; 
            x += dashStep * dashDirection; 
            dashTimeRemaining -= deltaTime;
    
            if (dashTimeRemaining <= 0) {
                isDashing = false; 
                dashTimeRemaining = 0f;
            }
    
            updateHitbox(); 
        }
    }

    public void bonkOnCeiling(Rectangle topHitbox) {
        if (topHitbox != null && isColliding(topHitbox, COLLIDING_TOP)) {
            jumpSpeed = 0f;
            isJumping = false;
            isFalling = true;
    
            updateHitbox(); 
        }
    }

    private void handleFalling(float deltaTime, Rectangle groundHitbox) {
        if (!isJumping && (groundHitbox == null || !isColliding(groundHitbox, COLLIDING_BOTTOM))) {
            fallSpeed += gravity * deltaTime;
            y -= fallSpeed * deltaTime;
            isFalling = true;
        } else if (!isJumping) {
            fallSpeed = 0f;
            isFalling = false;
            hasDashed = false; 
        }
    }

    private void handleJumping(float deltaTime) {
        if (isJumping) {
            y += jumpSpeed * deltaTime;
            jumpSpeed -= gravity * deltaTime;
    
            if (jumpSpeed <= 0) {
                isJumping = false;
                isFalling = true;
            }
        }
    }

    private void updateCooldown(float deltaTime) {
        if (timeSinceLastDash < dashCooldown) {
            timeSinceLastDash += deltaTime;
        }
    }

    public void resolveCollisions(Rectangle groundHitbox, Rectangle topHitbox, Rectangle leftHitbox, Rectangle rightHitbox) {
        if (groundHitbox!=null &&  isColliding(groundHitbox, COLLIDING_BOTTOM)) {
            y = groundHitbox.y + groundHitbox.height;
            fallSpeed = 0f; 
            isFalling = false;
            updateHitbox(); 
        }
        if (topHitbox!=null && isColliding(topHitbox, COLLIDING_TOP)) {
            y = topHitbox.y - getPlayerHitbox().height;
            jumpSpeed = 0f; 
            isJumping = false;
            isFalling = true;
            updateHitbox(); 
        }
        if (leftHitbox!=null && isColliding(leftHitbox, COLLIDING_LEFT)) {
            x = leftHitbox.x + leftHitbox.width;
            updateHitbox(); 
        }
        if (rightHitbox!=null && isColliding(rightHitbox, COLLIDING_RIGHT)) {
            x = rightHitbox.x - getPlayerHitbox().width;
            updateHitbox(); 
        }
    }

    /*
        camera related player methods that defines some basic camera movements
    */
    public void cameraSetup(OrthographicCamera camera,int typeOfCamera){
        float lerp = 0.1f; 
        switch (typeOfCamera) {
            case FOLLOW:
                camera.position.x = x + getPlayerHitbox().width / 2;
                camera.position.y = y + getPlayerHitbox().height / 2;
                break;
            case FOLLOW_NO_Y:
                camera.position.x = x + getPlayerHitbox().width / 2;
                break;
            case FOLLOW_AS_OUT_OF_SCREEN:
                if(x < camera.position.x - camera.viewportWidth / 2) {
                    camera.position.x = x + getPlayerHitbox().width / 2;
                } else if(x > camera.position.x + camera.viewportWidth / 2) {
                    camera.position.x = x - getPlayerHitbox().width / 2;
                }
                if(y < camera.position.y - camera.viewportHeight / 2) {
                    camera.position.y = y + getPlayerHitbox().height / 2;
                } else if(y > camera.position.y + camera.viewportHeight / 2) {
                    camera.position.y = y - getPlayerHitbox().height / 2;
                }
                break;
            case FOLLOW_SMOOTH:
                camera.position.x += (x + getPlayerHitbox().width / 2 - camera.position.x) * lerp;
                camera.position.y += (y + getPlayerHitbox().height / 2 - camera.position.y) * lerp;
                break;
            case FOLLOW_SMOOTH_NO_Y:
                camera.position.x += (x + getPlayerHitbox().width / 2 - camera.position.x) * lerp;
                break;
            case DEAD_ZONE:
                float deadZoneWidth = 200f; 
                float deadZoneHeight = 150f;
            
                float leftBound = camera.position.x - deadZoneWidth / 2;
                float rightBound = camera.position.x + deadZoneWidth / 2;
                float bottomBound = camera.position.y - deadZoneHeight / 2;
                float topBound = camera.position.y + deadZoneHeight / 2;
            
                if (x + getPlayerHitbox().width / 2 < leftBound) {
                    camera.position.x = x + getPlayerHitbox().width / 2;
                } else if (x + getPlayerHitbox().width / 2 > rightBound) {
                    camera.position.x = x + getPlayerHitbox().width / 2;
                }
            
                if (y + getPlayerHitbox().height / 2 < bottomBound) {
                    camera.position.y = y + getPlayerHitbox().height / 2;
                } else if (y + getPlayerHitbox().height / 2 > topBound) {
                    camera.position.y = y + getPlayerHitbox().height / 2;
                }
                break;
            case ZOOM:
                if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
                    camera.zoom += 0.02f; 
                } else if (Gdx.input.isKeyPressed(Input.Keys.X)) {
                    camera.zoom -= 0.02f; 
                }
                camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f); 
                break;
            case SHAKE:
                float shakeIntensity = 1f; 
                float shakeDuration = 0.5f;
                float originalX = camera.position.x;
                float originalY = camera.position.y;
                if (shakeDuration > 0) {
                    camera.position.x = originalX + MathUtils.random(-shakeIntensity, shakeIntensity);
                    camera.position.y = originalY + MathUtils.random(-shakeIntensity, shakeIntensity);
                }
                else {
                    camera.position.x = originalX;
                    camera.position.y = originalY;
                }
                break;
            default: 
                camera.position.x += (x + getPlayerHitbox().width / 2 - camera.position.x) * lerp;
                camera.position.y += (y + getPlayerHitbox().height / 2 - camera.position.y) * lerp;
                break;
        }
        camera.update();
    }

    /*
        player movement methods
    */
    public void moveLeft(float deltaTime,Rectangle leftHitbox){
        if(leftHitbox != null && isColliding(leftHitbox, COLLIDING_LEFT)) {
            x = leftHitbox.x + leftHitbox.width;
        } else {
            x -= this.movementSpeed * deltaTime;
        }
        updateHitbox();
    }

    public void moveRight(float deltaTime,Rectangle rightHitbox){
        if(rightHitbox == null || !isColliding(rightHitbox, COLLIDING_RIGHT)) {
            x += this.movementSpeed * deltaTime;
        }
        updateHitbox();
    }

    public void DashLeft() {
        if (timeSinceLastDash >= dashCooldown && !isDashing && !hasDashed) {
            isDashing = true;
            dashTimeRemaining = dashDuration;
            dashDirection = -1;
            timeSinceLastDash = 0f;
            hasDashed = true; 
        }
    }
    
    public void DashRight() {
        if (timeSinceLastDash >= dashCooldown && !isDashing && !hasDashed) {
            isDashing = true;
            dashTimeRemaining = dashDuration;
            dashDirection = 1; 
            timeSinceLastDash = 0f;
            hasDashed = true; 
        }
    }

    public void fallFaster(float deltaTime) {
        if (isFalling || isJumping) {
            isJumping = false;
    
            fallSpeed += gravity * deltaTime * 4;
    
            float maxFallSpeed = 64f / deltaTime; 
            fallSpeed = Math.min(fallSpeed, maxFallSpeed);
    
            y -= fallSpeed * deltaTime;
    
            updateHitbox();
        }
    }

    public void jump() {
        if (!isJumping && !isFalling) {
            jumpSpeed = initialJumpSpeed+jumpHeight; 
            isJumping = true;       
        }
    }

    /*Debug features */
    @Override
    public String toString () {
        return "Player{" +
                "movementSpeed=" + movementSpeed +
                ", jumpHeight=" + jumpHeight +
                ", isJumping=" + isJumping +
                ", isFalling=" + isFalling +
                ", stateTime=" + stateTime +
                ", gravity=" + gravity +
                ", x=" + x +
                ", y=" + y + "\n" +
                ", fallSpeed=" + fallSpeed +
                ", jumpSpeed=" + jumpSpeed +
                ", dashSpeed=" + dashSpeed +
                ", initialJumpSpeed=" + initialJumpSpeed +
                ", dashCooldown=" + dashCooldown +
                ", timeSinceLastDash=" + timeSinceLastDash +
                "}";
    }

    public void drawHitboxes(ShapeRenderer shapeRenderer) {
        // top hitbox drawn in GREEN
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.rect(
            playerHitboxes[COLLIDING_TOP].x, 
            playerHitboxes[COLLIDING_TOP].y, 
            playerHitboxes[COLLIDING_TOP].width, 
            playerHitboxes[COLLIDING_TOP].height
        );

        // bottom hitbox drawn in RED
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(
            playerHitboxes[COLLIDING_BOTTOM].x, 
            playerHitboxes[COLLIDING_BOTTOM].y, 
            playerHitboxes[COLLIDING_BOTTOM].width, 
            playerHitboxes[COLLIDING_BOTTOM].height
        );

        // left hitbox drawn in BLUE
        shapeRenderer.setColor(0, 0, 1, 1);
        shapeRenderer.rect(
            playerHitboxes[COLLIDING_LEFT].x, 
            playerHitboxes[COLLIDING_LEFT].y, 
            playerHitboxes[COLLIDING_LEFT].width, 
            playerHitboxes[COLLIDING_LEFT].height
        );

        // right hitbox drawn in WHITE
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(
            playerHitboxes[COLLIDING_RIGHT].x, 
            playerHitboxes[COLLIDING_RIGHT].y, 
            playerHitboxes[COLLIDING_RIGHT].width, 
            playerHitboxes[COLLIDING_RIGHT].height
        );
    }

    public void drawAdiacentHitboxes(ArrayList<Tile> tiles,ShapeRenderer shapeRenderer) {
        Rectangle groundHitbox = this.getGroundHitbox(tiles);
        Rectangle topHitbox = this.getTopHitbox(tiles);
        Rectangle leftHitbox = this.getLeftHitbox(tiles);
        Rectangle rightHitbox = this.getRightHitbox(tiles);

        
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
    }
}