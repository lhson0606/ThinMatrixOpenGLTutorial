package renderEngine;

import entities.Camera;
import entities.Entity;
import models.TexturedModel;
import shader.Light;
import shader.StaticShader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterRenderer {
    private final StaticShader shader = new StaticShader();
    private final Renderer renderer = new Renderer(shader);
    private final Map<TexturedModel, List<Entity>> entities = new HashMap<>();

    public MasterRenderer() {

    }

    public void render(Light sun, Camera camera) {
        renderer.prepare();
        shader.start();
        shader.loadLight(sun);
        shader.loadViewMatrix(camera);
        renderer.render(entities);
        shader.stop();
        entities.clear();
    }

    public void processEntity(Entity entity) {
        TexturedModel entityModel = entity.getModel();
        List<Entity> batch = entities.get(entityModel);
        if (batch != null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<>();
            newBatch.add(entity);
            entities.put(entityModel, newBatch);
        }
    }

    public void cleanUp() {
        shader.cleanUp();
    }
}
