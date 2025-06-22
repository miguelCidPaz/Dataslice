# 📦 DataSlice Orquestador

🌐 Idiomas disponibles: [🇬🇧 English](README.md) | [🇪🇸 Español](README.es.md)

**DataSlice** es un orquestador de archivos ligero, modular y eficiente. Su misión es clara: permitir el análisis masivo de datos en formato tabular sin requerir clusters, sin frameworks complejos, y con una arquitectura desacoplada que lo hace extremadamente flexible.

> ⚠️ **Atención**: Este orquestador **no incluye** ningún motor de análisis ni procesamiento. Funciona conectando adaptadores de entrada con bloques de salida y delega la ejecución real a un motor externo.

---

## 🚀 Características principales

- ✅ Soporte nativo para archivos **CSV**, **JSON**, **JSONL** y **PARQUET** incluso en simultáneo.
- 🔌 Arquitectura basada en **adaptadores y bloques de lectura**, fácil de extender.
- 🧱 Pensado para trabajar con motores de procesamiento externos, ya sea propios o de terceros.
- 🧠 Preparado para manejar flujos de **millones de registros por archivo** sin explotar la memoria.
- 🧰 Incluye herramientas adicionales como **generador de datasets en streaming**.

---

## 📂 Estructura básica

```bash
├── models/             # Estructuras de datos comunes
├── utils/              # Logs, herramientas auxiliares
└── runner/             # Punto de entrada para ejecutar con motor externo
```

---

## 🧩 Integración con DataKeyring

**DataSlice** no contiene lógica de lectura de formatos.  
Toda la lectura de archivos (**CSV**, **JSON**, **JSONL**, **Parquet**) se delega al módulo externo **[DataKeyring](https://github.com/miguelCidPaz/datakeyring)**, un llavero de formatos optimizado para procesamiento por lotes.

> 🧼 Esta separación permite mantener el orquestador limpio, desacoplado y fácilmente extensible a nuevos formatos o motores de lectura.

---

## 🧠 Sobre condiciones de carrera

**IMPORTANTE**: Este orquestador está optimizado para alto rendimiento. En escenarios con múltiples hilos o cargas masivas, **no se impone control artificial de sincronización**.

> ⚠️ Si conectas un motor no preparado para flujos asíncronos o paralelos, podrías encontrarte con errores intermitentes (por ejemplo, `null` en lectura o escritura).

**Consejo**: asegúrate de que tu motor receptor implemente buffers o semáforos adecuados. No es responsabilidad del orquestador garantizar la consistencia en entornos no controlados.

---

## 📈 ¿Para qué sirve?

- Validaciones a gran escala
- Benchmarks controlados
- Ingesta masiva de datos
- Casos de entrenamiento para ML
- Procesamiento distribuido desacoplado

---

## 📦 Instalación y uso

```bash
# Clona el repositorio
$ git clone https://github.com/tuusuario/dataslice-orchestrator.git

# Importa como proyecto Maven en Eclipse o STS

# Lanza una prueba con un dataset de ejemplo
$ mvn clean install
$ java -jar target/dataslice-0.0.1-SNAPSHOT.jar data/
```

---

## 📜 Licencia

Este proyecto se distribuye bajo licencia **Apache 2.0**.

Puedes usarlo, modificarlo y distribuirlo libremente siempre que respetes los términos de la licencia original.

---

## 🙌 Créditos

Desarrollado por [Miguel Cid](https://www.linkedin.com/in/miguel-cid-paz-picon/) como parte de su stack de herramientas para APIs predictivas.

¡Gracias por usar DataSlice! 🚀