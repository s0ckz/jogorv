package modelos.monstros;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import modelos.ImportadorModelos;
import utils.Propriedades;

import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;

public class GerenciadorMonstros {
	
	private static GerenciadorMonstros instance = null;
	
	private Random random = new Random();
	
	public static GerenciadorMonstros getInstance() {
		if (instance == null) {
			instance = new GerenciadorMonstros();
		}
		return instance;
	}
	
	public Collection<Node> getMonstros(int quantidade) throws IOException {
		return getMonstro(Propriedades.getDiretorioMonstros(), "monstro", 1, quantidade);
	}

	private Collection<Node> getMonstro(String diretorio, String tipo, int totalDisponivel, int quantidade) throws IOException {
		Collection<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < quantidade; i++) {
			Node monstro = ImportadorModelos.getInstance().carregarModeloMd5(diretorio, getNome(tipo, quantidade));
			nodes.add(monstro);
		}
		return nodes;
	}

	private void rotacionar(Node meioNode, float f) {
		meioNode.getLocalRotation().fromAngleAxis(f, Vector3f.UNIT_X);
	}

	private Node carregarModeloMd3(String enderecoArquivo)
			throws IOException {
		Node node = ImportadorModelos.getInstance().carregarModeloMd3(
				enderecoArquivo);
		
		node.setModelBound(new BoundingBox());
		node.updateModelBound();
		return node;
	}

	private String getNome(String nome, int quantidade) {
		return nome + (random.nextInt(quantidade) + 1);
	}

}
