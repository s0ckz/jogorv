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
import modelos.animados.GerenciadorModelosAnimados;
import modelos.animados.ModeloAnimado;
import modelos.monstros.GerenciadorMonstros;
import modelos.vegetacao.GerenciadorVegetacao;

import com.jme.app.SimpleGame;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.ChaseCamera;
import com.jme.input.InputSystem;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.KeyInputListener;
import com.jme.input.ThirdPersonHandler;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.InputActionInterface;
import com.jme.input.joystick.Joystick;
import com.jme.input.joystick.JoystickInput;
import com.jme.input.thirdperson.ThirdPersonJoystickPlugin;
import com.jme.intersection.BoundingCollisionResults;
import com.jme.intersection.CollisionResults;
import com.jme.light.DirectionalLight;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.state.FogState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jmex.terrain.TerrainPage;
import com.jmex.terrain.util.FaultFractalHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;

public class Demo3 extends SimpleGame {
    private static final Logger logger = Logger
            .getLogger(Demo3.class.getName());

    private Node nodoPersonagem;
    
    private ModeloAnimado personagem;

    private ChaseCamera cameraPerseguidora;

    private TerrainPage terreno;
    
    private Random random = new Random();
    
    private Collection<Node> monstros;
    
    private Quaternion rotQuat1 = new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X);

    private float angle = 0;
    
    private float timeInSeconds = 0.0f;
    
	private boolean atacando;
    
    private CollisionResults resultadoColisao = criarResultadoColisao();
    
    private Node nodoColisao = new Node();
    
    private ThirdPersonHandler thirdPersonHandler;
    
	private KeyInputListener acaoPersonagem = criarAcaoPersonagem();
    
    public static void main(String[] args) {
        try {
            JoystickInput.setProvider(InputSystem.INPUT_SYSTEM_LWJGL);
        } catch (Exception e) {
            logger.logp(Level.SEVERE, Demo3.class.toString(),
                    "main(args)", "Exception", e);
        }
        Demo3 app = new Demo3();
        app.setConfigShowMode(ConfigShowMode.AlwaysShow);
        app.start();
    }

	protected void simpleInitGame() {
        try {
        	display.setTitle("Demonstração 3 - Realidade Virtual");
        	configurarColisao();
			configurarPersonagem();
			configurarTerreno();
//			configurarPlantas();
			configurarArvores();
			configurarMonstros();
			configurarCameraPerseguidora();
			configurarEntrada();
			configurarJoystick();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	protected void simpleUpdate() {
        cameraPerseguidora.update(tpf);
        float camMinHeight = terreno.getHeight(cam.getLocation()) + 2f;
        if (!Float.isInfinite(camMinHeight) && !Float.isNaN(camMinHeight)
                && cam.getLocation().y <= camMinHeight) {
            cam.getLocation().y = camMinHeight;
            cam.update();
        }

        float characterMinHeight = terreno.getHeight(nodoPersonagem
                .getLocalTranslation());
        if (!Float.isInfinite(characterMinHeight) && !Float.isNaN(characterMinHeight)) {
            nodoPersonagem.getLocalTranslation().y = characterMinHeight;
        }
        
        atualizarMonstros();
        atualizarAnimacao();
        detectarColisoes();
    }

	private void atualizarAnimacao() {
		if (!atacando) {
			if (!thirdPersonHandler.isNowTurning() && !thirdPersonHandler.isStrafeAlignTarget() &&
					!thirdPersonHandler.isWalkingBackwards() && !thirdPersonHandler.isWalkingForward()) {
		    	personagem.setAnimacaoAtual("parado");
			} else {
		    	personagem.setAnimacaoAtual("andando");
			}
		}
	}

	private void detectarColisoes() {
		nodoPersonagem.calculateCollisions(nodoColisao, resultadoColisao);
	}

    private CollisionResults criarResultadoColisao() {
		return new BoundingCollisionResults() {
			public void processCollisions() {
				if (getNumber() > 0 ) {
					nodoPersonagem.getLocalTranslation().x -= 0.5f;
					nodoPersonagem.getLocalTranslation().y -= 0.5f;
					nodoPersonagem.getLocalTranslation().z -= 0.5f;
					input.setEnabled(false);
					clear();
				} else
					input.setEnabled(true);
			}
		};
	}

	private void configurarColisao() {
		rootNode.attachChild(nodoColisao);
	}

    private void configurarPersonagem() throws IOException {
    	personagem = GerenciadorModelosAnimados.getInstance().carregarModeloAnimadoMd3("obj/cruzado.md3");
    	personagem.setVelocidade(10.0f);
    	nodoPersonagem = personagem.getNode();
    	nodoPersonagem.setModelBound(new BoundingBox());
    	nodoPersonagem.updateModelBound();
        nodoPersonagem.setName("char node");
        rootNode.attachChild(nodoPersonagem);
        nodoPersonagem.updateWorldBound(); // We do this to allow the camera setup access to the world bound in our setup code.
        
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

        Texture t2 = TextureManager.loadTexture(Demo3.class
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
    
    private void configurarMonstros() {
    	try {
    		monstros = GerenciadorMonstros.getInstance().getMonstros(15);
    		for (Node monstro : monstros) {
    			monstro.setLocalTranslation(calcularPosicaoAleatoria());
    			monstro.getLocalRotation().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X);
    			monstro.getLocalTranslation().y = terreno.getHeight(monstro.getLocalTranslation()) + 20;
    			nodoColisao.attachChild(monstro);
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void atualizarMonstros() {
		if (timer.getTimeInSeconds() - timeInSeconds > 0.04f) {
			angle += 0.05f;
			if (angle > FastMath.TWO_PI) {
				angle = 0;
			}
	
			for (Node monstro : monstros) {
				monstro.getLocalRotation().fromAngleAxis(-FastMath.HALF_PI, new Vector3f(1, 0, 0));
				monstro.getLocalRotation().multLocal(rotQuat1.fromAngleAxis(-angle, Vector3f.UNIT_Z));
	        	monstro.getLocalTranslation().y = terreno.getHeight(monstro.getLocalTranslation()) + 20;
	        }
			timeInSeconds = timer.getTimeInSeconds();
		}
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
    			nodoColisao.attachChild(arvore);
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
        targetOffset.y = ((BoundingBox) nodoPersonagem.getWorldBound()).yExtent * 1.5f;
        cameraPerseguidora = new ChaseCamera(cam, nodoPersonagem);
        cameraPerseguidora.setTargetOffset(targetOffset);
    }

    private void configurarEntrada() {
        HashMap<String, Object> handlerProps = new HashMap<String, Object>();
        handlerProps.put(ThirdPersonHandler.PROP_DOGRADUAL, "true");
        handlerProps.put(ThirdPersonHandler.PROP_TURNSPEED, ""+(1.0f * FastMath.PI));
        handlerProps.put(ThirdPersonHandler.PROP_LOCKBACKWARDS, "false");
        handlerProps.put(ThirdPersonHandler.PROP_CAMERAALIGNEDMOVE, "true");
        input = thirdPersonHandler = new ThirdPersonHandler(nodoPersonagem, cam, handlerProps);
        input.setActionSpeed(100f);
        
        KeyInput.get().addListener(acaoPersonagem);
    }

	private KeyInputListener criarAcaoPersonagem() {
		return new KeyInputListener() {

			public void onKey(char character, int keyCode, boolean pressed) {
				if (keyCode == KeyInput.KEY_SPACE) {
					atacando = pressed;
					if (atacando) {
						personagem.setAnimacaoAtual("atacando");
					}
				}
			}
		};
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
