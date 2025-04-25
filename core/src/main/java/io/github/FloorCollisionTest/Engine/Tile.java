package io.github.FloorCollisionTest.Engine;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/*
    Tile can't be moved by the player
    contains every basic configuration for a tile and is a basic component
*/
public class Tile {
    private final Rectangle[] tileHitboxes;
    private Animation<Texture> tileAnimation;
    private Texture currentFrame;
    private float stateTime;
    public static final int COLLIDING_TOP = 0;
    public static final int COLLIDING_BOTTOM = 1;
    public static final int COLLIDING_LEFT = 2;
    public static final int COLLIDING_RIGHT = 3;

    public static final int LINEAR = 0;
    public static final int EASE_IN_OUT = 1;
    public static final int EASE_IN = 2;
    public static final int EASE_OUT = 3;
    

    
    // returns an array of hitboxes for the tile required for handle collision specifically for top, bottom, left and right
    public static Rectangle[] generateTileHitboxes(Rectangle tileHitbox){
        Rectangle[] tileHitboxes = new Rectangle[4];
        
        // top hitbox
        tileHitboxes[COLLIDING_TOP] = new Rectangle(tileHitbox.x, tileHitbox.y, tileHitbox.width, 1);
        //  bottom hitbox
        tileHitboxes[COLLIDING_BOTTOM] = new Rectangle(tileHitbox.x, tileHitbox.y + tileHitbox.height, tileHitbox.width, 1);
        // left hitbox
        tileHitboxes[COLLIDING_LEFT] = new Rectangle(tileHitbox.x, tileHitbox.y, 1, tileHitbox.height);
        // right hitbox
        tileHitboxes[COLLIDING_RIGHT] = new Rectangle(tileHitbox.x + tileHitbox.width, tileHitbox.y, 1, tileHitbox.height);

        return tileHitboxes;

    }

    // utility method to generate a texture from an atlas
    public static Texture[] generateTextureFromAtlas(String path, int frameWidth, int frameHeight,int padding) {
        if(path == null || path.isEmpty()) {
            return null;
        }
        if(frameWidth <= 0 || frameHeight <= 0) {
            return null;
        }
        if(padding < 0) {
            return null;
        }

        Texture sheetTexture = new Texture(Gdx.files.internal(path));

        Pixmap fullPixmap = Tile.getPixmapFromTexture(sheetTexture);

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


    /*
        Tile requires a dimension and an arrayOfTextures. Also requires a frameDuration that specify
        how much time requires to the animation to update
    */
    public Tile(Rectangle tileHitbox, Texture[] tileTextures, float frameDuration) {
        if(tileTextures == null || tileTextures.length == 0) {
            this.tileHitboxes = null;
            this.tileAnimation = null;
            this.stateTime = 0f;
            return;
        }
        if(frameDuration <= 0) {
            this.tileHitboxes = null;
            this.tileAnimation = null;
            this.stateTime = 0f;
            return;
        }
        if(tileHitbox == null) {
            this.tileHitboxes = null;
            this.tileAnimation = null;
            this.stateTime = 0f;
            return;
        }


        this.tileHitboxes = generateTileHitboxes(tileHitbox);
        this.tileAnimation = new Animation<>(frameDuration, tileTextures);
        this.stateTime = 0f;
    }

    public Tile(Rectangle tileHitbox,Animation<Texture> tileAnimation) {
        this.tileHitboxes = generateTileHitboxes(tileHitbox);
        this.tileAnimation = tileAnimation;
        this.stateTime = 0f;
    }

    public Rectangle getTileHitbox() {
        return new Rectangle(tileHitboxes[0].x, tileHitboxes[0].y, tileHitboxes[0].width, tileHitboxes[2].height);
    }

    public float getX() {
        return tileHitboxes[0].x;
    }
    
    public float getY() {
        return tileHitboxes[0].y;
    }

    public float getWidth() {
        return tileHitboxes[0].width;
    }

    public float getHeight() {
        return tileHitboxes[2].height;
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
        float duration = tileAnimation.getAnimationDuration();
        float t = Math.min(stateTime / duration, 1f); 
    
        switch (AnimationStyle) {
            case LINEAR: 
                this.currentFrame = tileAnimation.getKeyFrame(stateTime, looping);
                break;
            case EASE_IN_OUT:
                float easedInOutT = (float)(-0.5f * (Math.cos(Math.PI * t) - 1));
                float easedInOutTime = easedInOutT * duration;
                this.currentFrame = tileAnimation.getKeyFrame(easedInOutTime, looping);
                break;    
            case EASE_IN: 
                float easeInT = t * t;
                float easeInTime = easeInT * duration;
                this.currentFrame = tileAnimation.getKeyFrame(easeInTime, looping);
                break;
            case EASE_OUT: 
                float easeOutT = 1 - (1 - t) * (1 - t);
                float easeOutTime = easeOutT * duration;
                this.currentFrame = tileAnimation.getKeyFrame(easeOutTime, looping);
                break;
            default:
                this.currentFrame = tileAnimation.getKeyFrame(stateTime, looping);
                break;
        }
    
        batch.draw(this.currentFrame, tileHitboxes[0].x, tileHitboxes[0].y, tileHitboxes[0].width, tileHitboxes[2].height);
    }
    
    /*
     methods for collision detection
    */
    // generic collision
    public boolean isColliding(Rectangle hitbox) {
        for (Rectangle tileHitbox : tileHitboxes) {
            if (tileHitbox.overlaps(hitbox)) {
                return true;
            }
        }
        return false;
    }

    /*
     0 = top
     1 = bottom
     2 = left
     3 = right
    */
    public boolean isColliding(Rectangle hitbox, int typeOfCollision) {
        return tileHitboxes[typeOfCollision].overlaps(hitbox);
    }



    
}
