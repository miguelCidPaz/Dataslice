package com.mcp.dataslice.models;

public class InputReadBlock {
    private final String[][] rawRows;
    private final String[] headers;
    private final String datasetName;

    /**
     * Crea un bloque de entrada con los datos crudos a ser procesados por el motor.
     * Este bloque representa la fase inicial de análisis, donde los datos aún no han sido estructurados.
     *
     * @param rawRows     Matriz de datos crudos representados como {@code String[][]}, donde cada subarray es una fila.
     *                    Se espera que las filas estén alineadas con el orden de las cabeceras.
     * @param headers     Array de nombres de columna correspondiente a los datos en {@code rawRows}.
     *                    Debe coincidir en longitud con la cantidad de columnas en cada fila.
     * @param datasetName Nombre del dataset actual. Se utiliza para etiquetar los resultados
     *                    y generar identificadores únicos dentro del análisis.
     */
    public InputReadBlock(String[][] rawRows, String[] headers, String datasetName) {
        this.rawRows = rawRows;
        this.headers = headers;
        this.datasetName = datasetName;
    }

	public String[][] getRawRows() {
		return rawRows;
	}

	public String[] getHeaders() {
		return headers;
	}

	public String getDatasetName() {
		return datasetName;
	}

}
