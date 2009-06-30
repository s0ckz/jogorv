package modelos.vegetacao;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import modelos.ImportadorModelos;
import utils.Propriedades;

import com.jme.scene.Node;
import com.jme.scene.state.BlendState;
import com.jme.system.DisplaySystem;

public class GerenciadorVegetacao {
	
	private static GerenciadorVegetacao instance = null;
	
	private Random random = new Random();
	
	public static GerenciadorVegetacao getInstance() {
		if (instance == null) {
			instance = new GerenciadorVegetacao();
		}
		return instance;
	}
	
	public Collection<Node> getPlantas(int quantidade) throws IOException {
		return getVegetacao(Propriedades.getDiretorioPlantas(), "planta", 3, quantidade);
	}

	public Collection<Node> getArvores(int quantidade) throws IOException {
		return getVegetacao(Propriedades.getDiretorioArvores(), "arvore", 1, quantidade);
	}
	
	private Collection<Node> getVegetacao(String diretorio, String tipo, int totalDisponivel, int quantidade) throws IOException {
		Collection<Node> nodes = new LinkedList<Node>();
		for (int i = 0; i < quantidade; i++) {
			Node planta = ImportadorModelos.getInstance().carregarModeloMax(
					diretorio + getNome(tipo, totalDisponivel));
			
	        DisplaySystem display = DisplaySystem.getDisplaySystem();
	        BlendState as = display.getRenderer().createBlendState();

	        as.setBlendEnabled(true);
	        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
	        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
	        as.setTestEnabled(true);
	        as.setTestFunction(BlendState.TestFunction.GreaterThan);
	        as.setEnabled(true);

	        planta.setRenderState(as);
	        planta.updateRenderState();			
			
			nodes.add(planta);
		}
		return nodes;
	}

	private String getNome(String nome, int quantidade) {
		return nome + (random.nextInt(quantidade) + 1) +  ".3ds";
	}

}
