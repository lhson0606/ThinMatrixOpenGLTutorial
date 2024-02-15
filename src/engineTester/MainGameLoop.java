package engineTester;

import entities.Camera;
import entities.Entity;
import models.TexturedModel;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;
import renderEngine.*;
import models.RawModel;
import shader.Light;
import shader.StaticShader;
import textures.ModelTexture;

public class MainGameLoop {
    public static void main(String[] args) {

        DisplayManager.createDisplay();
        Loader loader = new Loader();
        StaticShader shader = new StaticShader();
        MasterRenderer renderer = new MasterRenderer();

        RawModel model = OBJLoader.loadObjModel("stall", loader);

        TexturedModel staticModel = new TexturedModel(model, new ModelTexture(loader.loadTexture("stallTexture")));
        ModelTexture texture = staticModel.getTexture();
        texture.setShineDamper(10);
        texture.setRefectivity(1);

        Entity entity = new Entity(staticModel, new Vector3f(0,0,-25),0,0,0,1);
        Light light = new Light(new Vector3f(0,0,-20), new Vector3f(1,1,1));

        Camera camera = new Camera();

        while(!Display.isCloseRequested()){
            entity.increaseRotation(0,1,0);
            camera.move();
            light.setPosition(camera.getPosition());
            renderer.processEntity(entity);
            renderer.render(light, camera);
            DisplayManager.updateDisplay();
        }

        shader.cleanUp();
        loader.cleanUp();
        renderer.cleanUp();
        DisplayManager.closeDisplay();
    }
}
