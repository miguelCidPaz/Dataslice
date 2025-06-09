package com.mcp.dataslice.models;

public class InputProductBlock {
	private final boolean reanalyze;
    private final String[] reportConfig; // [0] = path, [1] = format

    /**
     * Crea un bloque de entrada para procesar un dataset previamente analizado
     * Este bloque se utiliza en la segunda fase del análisis, donde se puede realizar un reanálisis
     * y/o generar un reporte final.
     *
     * @param Data       El esquema ya procesado (macro o parcial enriquecido).
     * @param reanalyze     Indica si se debe aplicar un análisis adicional sobre el esquema recibido.
     *                      Si es {@code true}, se volverán a aplicar reglas de enriquecimiento.
     *                      Si es {@code false}, el esquema se usará tal cual.
     * @param reportConfig  Parámetros para la generación del reporte.
     *                      {@code reportConfig[0]} especifica la ruta de salida del reporte.
     *                      {@code reportConfig[1]} indica el formato del reporte (por ejemplo, "html").
     *                      Si es {@code null}, no se generará ningún reporte.
     */
    public InputProductBlock(boolean reanalyze, String[] reportConfig) {
        this.reanalyze = reanalyze;
        this.reportConfig = reportConfig;
    }

	public boolean isReanalyze() {
		return reanalyze;
	}

	public String[] getReportConfig() {
		return reportConfig;
	}
}
