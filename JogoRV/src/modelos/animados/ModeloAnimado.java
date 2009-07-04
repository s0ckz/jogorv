package modelos.animados;

import java.util.Map;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jmex.model.animation.KeyframeController;

public class ModeloAnimado {

	private Node node;
	
	private String animacaoAtual = null;

	private Map<String, Animacao> animacoes;

	public ModeloAnimado(Node node, Map<String, Animacao> animacoes) {
		this.node = node;
		this.animacoes = animacoes;
	}
	
	public String getAnimacaoAtual() {
		return animacaoAtual;
	}

	public void setAnimacaoAtual(String animacaoAtual) {
		if (this.animacaoAtual == null || !this.animacaoAtual.equals(animacaoAtual)) {
			this.animacaoAtual = animacaoAtual;
			for (Spatial spatial : node.getChildren()) {
				KeyframeController kfc = (KeyframeController) spatial.getController(0);
				Animacao animacao = animacoes.get(animacaoAtual);
				if (animacao == null)
					throw new RuntimeException("Animacao " + animacaoAtual + " não existe!");
				kfc.setMinTime(animacao.getInitialFrame());
				kfc.setMaxTime(animacao.getEndFrame());
			}
		}
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public void setVelocidade(float velocidade) {
		for (Spatial spatial : node.getChildren()) {
			KeyframeController kfc = (KeyframeController) spatial.getController(0);
			kfc.setSpeed(velocidade);
		}
	}

}
