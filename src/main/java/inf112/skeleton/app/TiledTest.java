package inf112.skeleton.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;


import java.util.ArrayList;

public class TiledTest extends ApplicationAdapter implements InputProcessor {
    Texture img;
    TiledMap tiledMap;
    OrthographicCamera camera;
    TiledMapRenderer tiledMapRenderer;
    SpriteBatch sb;
    Texture texture1;
    Texture texture2;
    ArrayList<Player> players;
    Player myPlayer;
    NetworkClient client;
    boolean playerSet = false;

    @Override
    public void create () {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false,w,h);
        camera.update();
        tiledMap = new TmxMapLoader().load("emptyMap.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        Gdx.input.setInputProcessor(this);
        sb = new SpriteBatch();
        texture1 = new Texture(Gdx.files.internal("robot2.png"));


        players = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            players.add(new Player(i+1, new Sprite(texture1)));
        }

        //After connecting to server we will give the correct player control, but atm we will just use the first in list


        try {
            client = new NetworkClient(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        while(!playerSet){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    public void render () {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        sb.setProjectionMatrix(camera.combined);
        sb.begin();

        //draw players

        for (int i = 0; i < players.size(); i++) {
            players.get(i).getSprite().draw(sb);
        }

        for (Player player: players) {
            if(checkWin(player) != null){
                client.sendWin(players.indexOf(player) + 1);

            }
        }
        sb.end();
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float x = Gdx.input.getDeltaX();
        float y = Gdx.input.getDeltaY();
        camera.translate(-x,y);
        return true;
    }
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }
    @Override
    public boolean keyUp(int keycode) {

        if(keycode == Input.Keys.LEFT)
            myPlayer.moveLeft();
        if(keycode == Input.Keys.RIGHT)
            myPlayer.moveRight();
        if(keycode == Input.Keys.UP)
            myPlayer.moveUp();
        if(keycode == Input.Keys.DOWN)
            myPlayer.moveDown();

        if(keycode == Input.Keys.NUM_1)
            tiledMap.getLayers().get(0).setVisible(!tiledMap.getLayers().get(0).isVisible());
        if(keycode == Input.Keys.NUM_2)
            tiledMap.getLayers().get(1).setVisible(!tiledMap.getLayers().get(1).isVisible());

        client.sendCords(myPlayer.getLocation());
        return false;


    }
    @Override
    public boolean keyTyped(char character) {
        return false;
    }
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
    @Override
    public boolean scrolled(int amount) {
        return false;
    }


    //Checks if player will win with their recent move, not very efficient. Needs improvement.
    public Player checkWin(Player playerToCheck){
        int index = tiledMap.getLayers().getIndex("flag");
        MapLayer winLayer = tiledMap.getLayers().get(index);
        for (int i = 0; i < winLayer.getObjects().getCount(); i++) {
            Float flagX = Float.parseFloat(winLayer.getObjects().get("Haz1").getProperties().get("x").toString());
            Float flagY = Float.parseFloat(winLayer.getObjects().get("Haz1").getProperties().get("y").toString());
            Vector2 playerLoc = myPlayer.getLocation();
            if(playerLoc.x == flagX & playerLoc.y == flagY){
                return playerToCheck;
            }
        }
        return null;
    }

    public void moveEnemies(int clientID, Vector2 v){
        Player toBeMoved = players.get(clientID-1);
        toBeMoved.setVector(v);
    }

    public void playerSet(int ID){
        myPlayer = players.get(ID-1);
        System.out.println("My player is now Player " + client.getID());
        playerSet = true;
    }

    public void quit(int playerWonId){
        System.out.println("The game is now over, because Player " + playerWonId + " has won!");
        System.exit(1);
    }

}
