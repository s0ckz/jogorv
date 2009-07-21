package modelos.monstros;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import modelos.animados.GerenciadorModelosAnimados;
import modelos.animados.ModeloAnimado;
import utils.Propriedades;

public class GerenciadorMonstros {
	
	private static GerenciadorMonstros instance = null;
	
	private Random random = new Random();
	
	public static GerenciadorMonstros getInstance() {
		if (instance == null) {
			instance = new GerenciadorMonstros();
		}
		return instance;
	}
	
	public Collection<ModeloAnimado> getMonstros(int quantidade) throws IOException {
		return getMonstro(Propriedades.getDiretorioMonstros(), "monstro", quantidade);
	}

	private Collection<ModeloAnimado> getMonstro(String diretorio, String tipo, int quantidade) throws IOException {
		Collection<ModeloAnimado> nodes = new LinkedList<ModeloAnimado>();
		for (int i = 0; i < quantidade; i++) {
			ModeloAnimado monstro = carregarMonstro(diretorio, tipo, 1);
			nodes.add(monstro);
		}
		return nodes;
	}

	private ModeloAnimado carregarMonstro(String diretorio, String tipo, int totalDisponivel)
			throws IOException {
		int indice = random.nextInt(totalDisponivel);
		String nome = tipo + (indice + 1);
		return GerenciadorModelosAnimados.getInstance().carregarModeloAnimadoMd3(diretorio + "/" + nome + ".md3");
	}

}
