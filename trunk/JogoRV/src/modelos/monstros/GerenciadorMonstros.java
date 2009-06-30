package modelos.monstros;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import modelos.ImportadorModelos;
import utils.Propriedades;

import com.jme.scene.Node;

public class GerenciadorMonstros {
	
	private enum Formato {
		OBJ, MD5;
	}
	
	private static GerenciadorMonstros instance = null;
	
	//monstro1 = md5, monstro2 = obj, ...
	private static final Formato[] tiposMonstros = 
		{ Formato.MD5, Formato.OBJ };
	
	private Random random = new Random();
	
	public static GerenciadorMonstros getInstance() {
		if (instance == null) {
			instance = new GerenciadorMonstros();
		}
		return instance;
	}
	
	public Collection<Node> getMonstros(int quantidade) throws IOException {
		return getMonstro(Propriedades.getDiretorioMonstros(), "monstro", quantidade);
	}

	private Collection<Node> getMonstro(String diretorio, String tipo, int quantidade) throws IOException {
		Collection<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < quantidade; i++) {
			Node monstro = carregarMonstro(diretorio, tipo, tiposMonstros.length);
			nodes.add(monstro);
		}
		return nodes;
	}

	private Node carregarMonstro(String diretorio, String tipo, int totalDisponivel)
			throws IOException {
		int indice = random.nextInt(totalDisponivel);
		String nome = tipo + (indice + 1);
		if (tiposMonstros[indice] == Formato.MD5) {
			return ImportadorModelos.getInstance().carregarModeloMd5(diretorio, nome);
		} else if (tiposMonstros[indice] == Formato.OBJ) {
			return ImportadorModelos.getInstance().carregarModeloObj(diretorio + "/" + nome + ".obj");
		}
		return null;
	}

}
