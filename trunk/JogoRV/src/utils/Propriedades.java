package utils;

public class Propriedades {
	
	public static String getDiretorioModelos() {
		return "modelos/";
	}

	public static String getDiretorioPlantas() {
		return getDiretorioVegetacao() + "plantas/";
	}

	private static String getDiretorioVegetacao() {
		return getDiretorioModelos() + "vegetacao/";
	}

	public static String getDiretorioArvores() {
		return getDiretorioVegetacao() + "arvores/";
	}

}
