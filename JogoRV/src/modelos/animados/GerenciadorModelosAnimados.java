package modelos.animados;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import modelos.ImportadorModelos;

import com.jme.scene.Node;



public class GerenciadorModelosAnimados {
	
	private static GerenciadorModelosAnimados instance = null;
	
	public static GerenciadorModelosAnimados getInstance() {
		if (instance == null) {
			instance = new GerenciadorModelosAnimados();
		}
		return instance;
	}
	
	public ModeloAnimado carregarModeloAnimadoMd3(String enderecoArquivo) throws IOException {
		Node node = ImportadorModelos.getInstance().carregarModeloMd3(enderecoArquivo);
		return new ModeloAnimado(node, getAnimacoes(substituirExtensao(enderecoArquivo, "anim")));
	}

	private Map<String, Animacao> getAnimacoes(String arquivoAnimacao) throws IOException {
		Map<String, Animacao> mapaAnimacoes = new HashMap<String, Animacao>();
		BufferedReader br = new BufferedReader(new FileReader(arquivoAnimacao));
		String line = null;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			String[] tokens = line.split("\\s+");
			if (tokens.length == 3) {
				String nome = tokens[0];
				int initialFrame = Integer.parseInt(tokens[1]);
				int endFrame = Integer.parseInt(tokens[2]);
				mapaAnimacoes.put(nome, new Animacao(nome, initialFrame, endFrame));
			}
		}
		return mapaAnimacoes;
	}

	private String substituirExtensao(String enderecoArquivo, String extensao) {
		return enderecoArquivo.substring(0, enderecoArquivo.indexOf('.')) + "." + extensao;
	}

}
