package modelos.animados;

public class Animacao {
	
	private String nome;
	private int initialFrame;
	private int endFrame;

	public Animacao(String nome, int initialFrame, int endFrame) {
		this.nome = nome;
		this.initialFrame = initialFrame;
		this.endFrame = endFrame;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getInitialFrame() {
		return initialFrame;
	}

	public void setInitialFrame(int initialFrame) {
		this.initialFrame = initialFrame;
	}

	public int getEndFrame() {
		return endFrame;
	}

	public void setEndFrame(int endFrame) {
		this.endFrame = endFrame;
	}
	
	public String toString() {
		return getNome() + " " + getInitialFrame() + " " + getEndFrame();
	}

}
