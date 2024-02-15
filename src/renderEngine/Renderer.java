package renderEngine;

import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Matrix4f;
import shader.StaticShader;
import textures.ModelTexture;
import toolbox.Maths;

import java.util.List;
import java.util.Map;

public class Renderer {

    private static final float FOV = 70;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 1000;
    public static final int POSITION_LAYOUT_INDEX = 0;
    public static final int TEX_COORD_LAYOUT_INDEX = 1;
    public static final int NORMAL_LAYOUT_INDEX = 2;
    private final StaticShader shader;

    private Matrix4f projectionMatrix;

    public Renderer(StaticShader shader) {
        this.shader = shader;
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        createProjectionMatrix();
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    public void prepare() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(0, 0, 0, 0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    private void unbindTexturedModel() {
        GL20.glDisableVertexAttribArray(POSITION_LAYOUT_INDEX);
        GL20.glDisableVertexAttribArray(TEX_COORD_LAYOUT_INDEX);
        GL20.glDisableVertexAttribArray(NORMAL_LAYOUT_INDEX);
        GL30.glBindVertexArray(0);
    }

    private void prepareTexturedModel(TexturedModel model) {
        RawModel rawModel = model.getRawModel();
        GL30.glBindVertexArray(rawModel.getVaoID());
        GL20.glEnableVertexAttribArray(POSITION_LAYOUT_INDEX);
        GL20.glEnableVertexAttribArray(TEX_COORD_LAYOUT_INDEX);
        GL20.glEnableVertexAttribArray(NORMAL_LAYOUT_INDEX);
        ModelTexture texture = model.getTexture();
        shader.loadShineVariable(texture.getShineDamper(), texture.getRefectivity());
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
        //this part is not necessary in Java, but it is good to know
        int texPos = shader.getUniformLocation("textureSampler");
        GL20.glUniform1i(texPos, 0);
        //end of unnecessary part
    }

    private void prepareInstance(Entity entity){
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
        shader.loadTransformationMatrix(transformationMatrix);
    }

    public void render(Map<TexturedModel, List<Entity>> entities){
        for(TexturedModel model: entities.keySet()){
            prepareTexturedModel(model);
            List<Entity> batch = entities.get(model);
            for(Entity entity: batch){
                prepareInstance(entity);
                GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, POSITION_LAYOUT_INDEX);
            }
            unbindTexturedModel();
        }
    }

    private void createProjectionMatrix() {
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix = new Matrix4f();
        projectionMatrix.m00 = x_scale;
        projectionMatrix.m11 = y_scale;
        projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
        projectionMatrix.m23 = -1;
        projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
        projectionMatrix.m33 = 0;
    }
}
