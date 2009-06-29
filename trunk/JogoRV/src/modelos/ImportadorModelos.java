package modelos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.jme.bounding.BoundingBox;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.util.export.binary.BinaryImporter;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.model.converters.MaxToJme;

public class ImportadorModelos {
	
	private MaxToJme maxtojme = new MaxToJme();
	
	private BinaryImporter binaryImporter = new BinaryImporter(); 
	
	private Map<String, byte[]> mapaOutputStreams = new HashMap<String, byte[]>();
	
	private static ImportadorModelos instance;
	
	public static ImportadorModelos getInstance() {
		if (instance == null) {
			instance = new ImportadorModelos();
		}
		return instance;
	}

	public Node carregarModelo(String enderecoArquivo) throws IOException {
		return carregarModelo(enderecoArquivo, new File(enderecoArquivo).getParent());
	}
	
	private Node carregarModelo(String enderecoArquivo, String enderecoTexturas) throws FileNotFoundException, MalformedURLException, IOException {
        SimpleResourceLocator locator = new SimpleResourceLocator(new File(enderecoArquivo).getParentFile().toURI());
        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, locator);
        ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, locator);

		byte[] bytes = getByteArrayMax(enderecoArquivo, enderecoTexturas);
		
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		
		Node node = new Node();
		Spatial spatial = (Spatial)binaryImporter.load(in);
		spatial.setModelBound(new BoundingBox());
		spatial.updateModelBound();
		
		node.attachChild(spatial);
		return node;
	}

	private byte[] getByteArrayMax(String enderecoArquivo, String enderecoTexturas) throws FileNotFoundException,
			MalformedURLException, IOException {
		String key = getKey(enderecoArquivo, enderecoTexturas);
		byte[] bytes = mapaOutputStreams.get(key);
		
		if (bytes == null) {
			FileInputStream is = new FileInputStream(new File(enderecoArquivo));
			maxtojme.setProperty("texurl" ,new File(enderecoArquivo).toURI().toURL());
			maxtojme.setProperty("texdir" ,new File(enderecoArquivo).toURI().toURL());
			ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
			maxtojme.convert(is, bytearrayoutputstream);
			bytes = bytearrayoutputstream.toByteArray();
			mapaOutputStreams.put(key, bytes);
		}
		return bytes;
	}

	private String getKey(String enderecoArquivo, String enderecoTexturas) {
		return enderecoArquivo + "." + enderecoTexturas;
	}

}
