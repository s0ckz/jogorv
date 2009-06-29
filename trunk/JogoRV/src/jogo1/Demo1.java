package jogo1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import jmetest.terrain.TestTerrain;
import modelos.vegetacao.GerenciadorVegetacao;

import com.jme.app.SimpleGame;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.ChaseCamera;
import com.jme.input.InputSystem;
import com.jme.input.ThirdPersonHandler;
import com.jme.input.joystick.Joystick;
import com.jme.input.joystick.JoystickInput;
import com.jme.input.thirdperson.ThirdPersonJoystickPlugin;
import com.jme.light.DirectionalLight;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.CullState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jmex.terrain.TerrainPage;
import com.jmex.terrain.util.FaultFractalHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;

public class Demo1 extends SimpleGame {
    private static final Logger logger = Logger
            .getLogger(Demo1.class.getName());

    private Node m_character;

    private ChaseCamera cameraPerseguidora;

    private TerrainPage terreno;

    private Random random = new Random();

    public static void main(String[] args) {
        try {
            JoystickInput.setProvider(InputSystem.INPUT_SYSTEM_LWJGL);
        } catch (Exception e) {
            logger.logp(Level.SEVERE, Demo1.class.toString(),
                    "main(args)", "Exception", e);
        }
        Demo1 app = new Demo1();
        app.setConfigShowMode(ConfigShowMode.AlwaysShow);
        app.start();
    }

    protected void simpleInitGame() {
        display.setTitle("Demonstração 1 - Realidade Virtual");
        
        configurarPersonagem();
        configurarTerreno();
        configurarPlantas();
        configurarArvores();
        configurarCameraPerseguidora();
        configurarEntrada();
        configurarJoystick();
    }

	protected void simpleUpdate() {
        cameraPerseguidora.update(tpf);
        float camMinHeight = terreno.getHeight(cam.getLocation()) + 2f;
        if (!Float.isInfinite(camMinHeight) && !Float.isNaN(camMinHeight)
                && cam.getLocation().y <= camMinHeight) {
            cam.getLocation().y = camMinHeight;
            cam.update();
        }

        float characterMinHeight = terreno.getHeight(m_character
                .getLocalTranslation())+((BoundingBox)m_character.getWorldBound()).yExtent;
        if (!Float.isInfinite(characterMinHeight) && !Float.isNaN(characterMinHeight)) {
            m_character.getLocalTranslation().y = characterMinHeight;
        }
    }

    private void configurarPersonagem() {
        Sphere b = new Sphere("bola", new Vector3f(), 20, 20, 5);
        b.setModelBound(new BoundingBox());
        b.updateModelBound();
        m_character = new Node("char node");
        rootNode.attachChild(m_character);
        m_character.attachChild(b);
        m_character.updateWorldBound(); // We do this to allow the camera setup access to the world bound in our setup code.

        TextureState ts = display.getRenderer().createTextureState();
        ts.setEnabled(true);
        ts.setTexture(
            TextureManager.loadTexture(
            Demo1.class.getClassLoader().getResource(
            "jmetest/data/images/Monkey.jpg"),
            Texture.MinificationFilter.BilinearNearestMipMap,
            Texture.MagnificationFilter.Bilinear));
        m_character.setRenderState(ts);
    }
    
    private void configurarTerreno() {
        rootNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);

        display.getRenderer().setBackgroundColor(
                new ColorRGBA(0.5f, 0.5f, 0.5f, 1));

        DirectionalLight dr = new DirectionalLight();
        dr.setEnabled(true);
        dr.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        dr.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        dr.setDirection(new Vector3f(0.5f, -0.5f, 0));

        CullState cs = display.getRenderer().createCullState();
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);
        rootNode.setRenderState(cs);

        lightState.detachAll();
        lightState.attach(dr);

        FaultFractalHeightMap heightMap = new FaultFractalHeightMap(257, 32, 0,
                255, 0.75f);
        Vector3f terrainScale = new Vector3f(10, 1, 10);
        heightMap.setHeightScale(0.001f);
        terreno = new TerrainPage("Terrain", 33, heightMap.getSize(),
                terrainScale, heightMap.getHeightMap());

        terreno.setDetailTexture(1, 16);
        rootNode.attachChild(terreno);

        ProceduralTextureGenerator pt = new ProceduralTextureGenerator(
                heightMap);
        pt.addTexture(new ImageIcon(TestTerrain.class.getClassLoader()
                .getResource("jmetest/data/texture/grassb.png")), -128, 0, 128);
        pt.addTexture(new ImageIcon(TestTerrain.class.getClassLoader()
                .getResource("jmetest/data/texture/dirt.jpg")), 0, 128, 255);
        pt.addTexture(new ImageIcon(TestTerrain.class.getClassLoader()
                .getResource("jmetest/data/texture/highest.jpg")), 128, 255,
                384);

        pt.createTexture(512);

        TextureState ts = display.getRenderer().createTextureState();
        ts.setEnabled(true);
        Texture t1 = TextureManager.loadTexture(pt.getImageIcon().getImage(),
                Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear, true);
        ts.setTexture(t1, 0);

        Texture t2 = TextureManager.loadTexture(Demo1.class
                .getClassLoader()
                .getResource("jmetest/data/texture/Detail.jpg"),
                Texture.MinificationFilter.Trilinear, Texture.MagnificationFilter.Bilinear);
        ts.setTexture(t2, 1);
        t2.setWrap(Texture.WrapMode.Repeat);

        t1.setApply(Texture.ApplyMode.Combine);
        t1.setCombineFuncRGB(Texture.CombinerFunctionRGB.Modulate);
        t1.setCombineSrc0RGB(Texture.CombinerSource.CurrentTexture);
        t1.setCombineOp0RGB(Texture.CombinerOperandRGB.SourceColor);
        t1.setCombineSrc1RGB(Texture.CombinerSource.PrimaryColor);
        t1.setCombineOp1RGB(Texture.CombinerOperandRGB.SourceColor);

        t2.setApply(Texture.ApplyMode.Combine);
        t2.setCombineFuncRGB(Texture.CombinerFunctionRGB.AddSigned);
        t2.setCombineSrc0RGB(Texture.CombinerSource.CurrentTexture);
        t2.setCombineOp0RGB(Texture.CombinerOperandRGB.SourceColor);
        t2.setCombineSrc1RGB(Texture.CombinerSource.Previous);
        t2.setCombineOp1RGB(Texture.CombinerOperandRGB.SourceColor);
        terreno.setRenderState(ts);

        FogState fs = display.getRenderer().createFogState();
        fs.setDensity(0.5f);
        fs.setEnabled(true);
        fs.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
        fs.setEnd(1000);
        fs.setStart(500);
        fs.setDensityFunction(FogState.DensityFunction.Linear);
        fs.setQuality(FogState.Quality.PerVertex);
        rootNode.setRenderState(fs);
    }
    
    private void configurarPlantas() {
    	try {
    		Collection<Node> plantas = GerenciadorVegetacao.getInstance().getPlantas(120);
    		for (Node planta : plantas) {
    			planta.setLocalTranslation(calcularPosicaoAleatoria());
    			planta.getLocalRotation().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X);
    			planta.getLocalTranslation().y = terreno.getHeight(planta.getLocalTranslation());
    			rootNode.attachChild(planta);
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    private void configurarArvores() {
    	try {
    		Collection<Node> arvores = GerenciadorVegetacao.getInstance().getArvores(20);
    		for (Node arvore : arvores) {
    			arvore.setLocalTranslation(calcularPosicaoAleatoria());
    			arvore.getLocalRotation().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X);
    			arvore.getLocalTranslation().y = terreno.getHeight(arvore.getLocalTranslation());
    			arvore.setLocalScale(5.0f);
    			rootNode.attachChild(arvore);
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    private Vector3f calcularPosicaoAleatoria() {
        Vector3f stepScale = terreno.getStepScale();
        int xz = terreno.getSize();
        // in this case X and Z are equal --> terrain is a quad
        float realTerrainSizeX = xz * stepScale.x;
        float realTerrainSizeZ = xz * stepScale.z;
        float realPosMaxX = realTerrainSizeX / 2;
        float realPosMaxZ = realTerrainSizeZ / 2;
        float x = random.nextFloat() * realTerrainSizeX - realPosMaxX;
        float z = random.nextFloat() * realTerrainSizeZ - realPosMaxZ;
        float height = terreno.getHeight(x, z);
        while (Float.isNaN(height))
        {
            x = random.nextFloat() * realTerrainSizeX - realPosMaxX;
            z = random.nextFloat() * realTerrainSizeZ - realPosMaxZ;
            height = terreno.getHeight(x, z);
        }
        return new Vector3f(x, height, z);
    }

    private void configurarCameraPerseguidora() {
        Vector3f targetOffset = new Vector3f();
        targetOffset.y = ((BoundingBox) m_character.getWorldBound()).yExtent * 1.5f;
        cameraPerseguidora = new ChaseCamera(cam, m_character);
        cameraPerseguidora.setTargetOffset(targetOffset);
    }

    private void configurarEntrada() {
        HashMap<String, Object> handlerProps = new HashMap<String, Object>();
        handlerProps.put(ThirdPersonHandler.PROP_DOGRADUAL, "true");
        handlerProps.put(ThirdPersonHandler.PROP_TURNSPEED, ""+(1.0f * FastMath.PI));
        handlerProps.put(ThirdPersonHandler.PROP_LOCKBACKWARDS, "false");
        handlerProps.put(ThirdPersonHandler.PROP_CAMERAALIGNEDMOVE, "true");
        input = new ThirdPersonHandler(m_character, cam, handlerProps);
        input.setActionSpeed(100f);
    }
    
    private void configurarJoystick() {
        ArrayList<Joystick> joys = JoystickInput.get().findJoysticksByAxis("X Axis", "Y Axis", "Z Axis", "Z Rotation");
        Joystick joy = joys.size() >= 1 ? joys.get(0) : null;
        if (joy != null) {
            ThirdPersonJoystickPlugin plugin = new ThirdPersonJoystickPlugin(joy, joy.findAxis("X Axis"), joy.findAxis("Y Axis"), joy.findAxis("Z Axis"), joy.findAxis("Z Rotation"));
            ((ThirdPersonHandler)input).setJoystickPlugin(plugin);
            cameraPerseguidora.getMouseLook().setJoystickPlugin(plugin);
        }
    }
}
