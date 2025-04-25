package io.github.FloorCollisionTest.Engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class TileMap {
    private Tile[][] map;
    private int mapWidth;
    private int mapHeight;

    /*
        Requires a .json file that contains every tile content
    */
    public TileMap(String jsonFile) {
        Json json = new Json();
        JsonValue mapData = json.fromJson(null, Gdx.files.internal(jsonFile));

        mapWidth = mapData.getInt("width");
        mapHeight = mapData.getInt("height");

        map = new Tile[mapWidth][mapHeight];

        JsonValue tiles = mapData.get("tiles");
        for (int y = 0; y < mapHeight; y++) {
            JsonValue row = tiles.get(y);
            for (int x = 0; x < mapWidth; x++) {
                JsonValue tileData = row.get(x);
                JsonValue spriteArray = tileData.get("sprites");
                float frameDuration = tileData.getFloat("frameDuration");
                Texture[] tileTextures = new Texture[spriteArray.size];
                float tileWidth = 0;
                float tileHeight = 0;
                
                for (int i = 0; i < spriteArray.size; i++) {
                    JsonValue sprite = spriteArray.get(i);
                    tileTextures[i] = new Texture(sprite.getString("file"));
                    tileWidth = sprite.getFloat("width");
                    tileHeight = sprite.getFloat("height");
                }

                Animation<Texture> tileAnimation = new Animation<>(frameDuration, tileTextures);
                map[x][y] = new Tile(new Rectangle(x * tileWidth, y * tileHeight, tileWidth, tileHeight), tileAnimation);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                map[x][y].renderSprite(batch,Tile.LINEAR,true);
            }
        }
    }
}
