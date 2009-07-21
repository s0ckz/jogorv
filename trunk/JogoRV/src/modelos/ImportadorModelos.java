package modelos;

import static com.jme.util.resource.ResourceLocatorTool.locateResource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.model.converters.FormatConverter;
import com.jmex.model.converters.MaxToJme;
import com.jmex.model.converters.Md3ToJme;
import com.jmex.model.converters.ObjToJme;
import com.model.md5.importer.MD5Importer;

public class ImportadorModelos {
	
	private FormatConverter maxToJme = new MaxToJme();
	
	private FormatConverter md3ToJme = new Md3ToJme();
	
	private FormatConverter objToJme = new ObjToJme();
	
	private MD5Importer importer = MD5Importer.getInstance();
	
	private BinaryImporter binaryImporter = new BinaryImporter(); 
	
	private Map<String, byte[]> mapaOutputStreams = new HashMap<String, byte[]>();
	
	private static ImportadorModelos instance;
	
	public static ImportadorModelos getInstance() {
		if (instance == null) {
			instance = new ImportadorModelos();
		}
		return instance;
	}

	public Node carregarModeloMax(String enderecoArquivo) throws IOException {
		return carregarModeloMax(enderecoArquivo, new File(enderecoArquivo).getParent());
	}

	public Node carregarModeloObj(String enderecoArquivo) throws FileNotFoundException, MalformedURLException, IOException {
		return carregarModeloObj(enderecoArquivo, new File(enderecoArquivo).getParent());
	}

	public Node carregarModeloMd5(String diretorio, String modelo) throws IOException {
		return carregarModeloMd5(diretorio + "/" + modelo + ".md5mesh", 
				diretorio + "/" + modelo + ".md5anim", 
				diretorio + "/" + modelo + ".jpg");
	}

	public Node carregarModeloMd5(String enderecoArquivoMesh, String enderecoArquivoAnimacao, String textura) throws IOException {
	    SimpleResourceLocator locator = new SimpleResourceLocator(new File(enderecoArquivoMesh).getParentFile().toURI());
	    ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, locator);
	    ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, locator);
	    
		URL urlMesh = new File(enderecoArquivoMesh).toURI().toURL();
		URL urlAnim =  new File(enderecoArquivoAnimacao).toURI().toURL();
		importer.cleanup();
		importer.load(urlMesh, UUID.randomUUID().toString(), urlAnim, UUID.randomUUID().toString(), Controller.RT_CYCLE);
		Node node = (Node) importer.getMD5Node();
		URL tex = getTextura(textura);
		if (tex != null) {
			node.getChildren().get(0).setRenderState(criarTextura(tex));
		}
		return node;
	}

	public Node carregarModeloMd3(String enderecoArquivo) throws IOException {
		return carregarModeloMd3(enderecoArquivo, new File(enderecoArquivo).getParent());
	}

	public Node carregarModeloMd3(String enderecoArquivo, String enderecoTexturas) throws IOException {
	    SimpleResourceLocator locator = new SimpleResourceLocator(new File(enderecoArquivo).getParentFile().toURI());
	    ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, locator);
	    ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, locator);
	
		byte[] bytes = getByteArray(enderecoArquivo, enderecoArquivo, md3ToJme);
		
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		
		Node node = (Node) binaryImporter.load(in);
		URL textura = new File(substituirExtensao(enderecoArquivo, "tga")).toURI().toURL();
		node.setRenderState(criarTextura(textura));
		node.setModelBound(new BoundingBox());
		node.updateModelBound();
//		carregarTexturasMd3(node, enderecoArquivo, enderecoTexturas);
		return node;
	}
	
//	private void carregarTexturasMd3(Node node, String enderecoArquivo, String enderecoTexturas) throws IOException {
//		Map<String, String> mapaTexturas = getMapaTexturasMd3(substituirExtensao(enderecoArquivo, "skin"), enderecoTexturas);
//		for (Entry<String, String> entry : mapaTexturas.entrySet()) {
//			String nodeName = entry.getKey();
//			String texturePath = entry.getValue();
//			URL tex= new File(texturePath).toURI().toURL();
//			Spatial child = node.getChild(nodeName);
//			if (child != null) {
//				child.setRenderState(criarTextura(tex));
//			}
//		}
//	}

	private TextureState criarTextura(URL tex) {
		DisplaySystem display = DisplaySystem.getDisplaySystem();
		TextureState ts = display.getRenderer().createTextureState();
		ts.setTexture(TextureManager.loadTexture(tex,Texture.MinificationFilter.BilinearNearestMipMap,Texture.MagnificationFilter.Bilinear));
		ts.setEnabled(true);
		return ts;
	}

	private URL getTextura(String textura) {
		return locateResource(ResourceLocatorTool.TYPE_TEXTURE, textura);
	}

//	private Map<String, String> getMapaTexturasMd3(String arquivoSkins, String enderecoTexturas) throws IOException {
//		enderecoTexturas = enderecoTexturas.replace('\\', '/');
//		Map<String, String> mapaTexturas = new HashMap<String, String>();
//		BufferedReader br = new BufferedReader(new FileReader(arquivoSkins));
//		String line = null;
//		while ((line = br.readLine()) != null) {
//			line = line.trim();
//			String[] tokens = line.split("\\s*,\\s*");
//			if (tokens.length == 2) {
//				mapaTexturas.put(tokens[0], enderecoTexturas + "/" + tokens[1]);
//			}
//		}
//		return mapaTexturas;
//	}

	private Node carregarModeloObj(String enderecoArquivo, String enderecoTexturas) throws FileNotFoundException, MalformedURLException, IOException {
		objToJme.setProperty("mtllib", new File(enderecoTexturas).toURI().toURL());
	 	objToJme.setProperty("texdir", new File(enderecoTexturas).toURI().toURL());
		byte[] bytes = getByteArray(enderecoArquivo, enderecoTexturas, objToJme);
		
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		
		Node node = new Node();
		Spatial spatial = (Spatial)binaryImporter.load(in);
		spatial.setModelBound(new BoundingBox());
		spatial.updateModelBound();
		
		node.attachChild(spatial);
		
		return node;
	}

	private Node carregarModeloMax(String enderecoArquivo, String enderecoTexturas) throws FileNotFoundException, MalformedURLException, IOException {
        SimpleResourceLocator locator = new SimpleResourceLocator(new File(enderecoArquivo).getParentFile().toURI());
        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, locator);
        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, locator);

		byte[] bytes = getByteArray(enderecoArquivo, enderecoTexturas, maxToJme);
		
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		
		Node node = new Node();
		Spatial spatial = (Spatial)binaryImporter.load(in);
		spatial.setModelBound(new BoundingBox());
		spatial.updateModelBound();
		
		node.attachChild(spatial);
		return node;
	}

	private byte[] getByteArray(String enderecoArquivo, String enderecoTexturas, FormatConverter importer) throws FileNotFoundException,
			MalformedURLException, IOException {
		String key = getKey(enderecoArquivo, enderecoTexturas);
		byte[] bytes = mapaOutputStreams.get(key);
		
		if (bytes == null) {
			ByteArrayOutputStream bytearrayoutputstream = 
				carregarModelo(importer, enderecoArquivo);
			bytes = bytearrayoutputstream.toByteArray();
			mapaOutputStreams.put(key, bytes);
		}
		return bytes;
	}

	private ByteArrayOutputStream carregarModelo(FormatConverter importer, String enderecoArquivo) throws MalformedURLException, IOException {
		FileInputStream is = new FileInputStream(new File(enderecoArquivo));
		importer.setProperty("texurl" ,new File(enderecoArquivo).toURI().toURL());
		importer.setProperty("texdir" ,new File(enderecoArquivo).toURI().toURL());
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		importer.convert(is, bytearrayoutputstream);
		return bytearrayoutputstream;
	}

	private String getKey(String enderecoArquivo, String enderecoTexturas) {
		return enderecoArquivo + "." + enderecoTexturas;
	}

	private String substituirExtensao(String enderecoArquivo, String extensao) {
		return enderecoArquivo.substring(0, enderecoArquivo.indexOf('.')) + "." + extensao;
	}

}
