
# 📦 DataSlice Orchestrator

🌐 Available languages: [🇬🇧 English](README.md) | [🇪🇸 Spanish](README.es.md)

**DataSlice** is a lightweight, modular, and efficient file orchestrator. Its mission is clear: enable massive analysis of tabular data without requiring clusters, complex frameworks, and with a decoupled architecture that makes it extremely flexible.

> ⚠️ **Note**: This orchestrator **does not include** any processing or analysis engine. It connects input adapters with output blocks and delegates execution to an external engine.

---

## 🚀 Key Features

- ✅ Native support for **CSV**, **JSON**, **JSONL**, and **PARQUET** files—even simultaneously.
- 🔌 Architecture based on **adapters and read blocks**, easy to extend.
- 🧱 Designed to work with external processing engines, either custom or third-party.
- 🧠 Built to handle **millions of rows per file** without blowing up memory.
- 🧰 Includes extra tools like a **streaming dataset generator**.

---

## 📂 Basic Structure

```bash
├── models/             # Common data structures
├── utils/              # Logs, helper tools
└── runner/             # Entry point to run with an external engine
```

---

## 🧩 Integration with DataKeyring

**DataSlice** does not include file reading logic.  
All file reading (**CSV**, **JSON**, **JSONL**, **Parquet**) is delegated to the external module **[DataKeyring](https://github.com/miguelCidPaz/datakeyring)**, a format keyring optimized for batch processing.

> 🧼 This separation keeps the orchestrator clean, decoupled, and easily extensible to new formats or reading engines.

---

## 🧠 About Race Conditions

**IMPORTANT**: This orchestrator is optimized for high performance. In multi-threaded or massive-load scenarios, **no artificial synchronization is imposed**.

> ⚠️ If you connect a backend not designed for async or parallel flows, you may encounter intermittent issues (e.g., `null` during read or write).

**Tip**: Make sure your receiving engine implements proper buffers or semaphores. Ensuring consistency in uncontrolled environments is **not** the orchestrator's responsibility.

---

## 📈 Use Cases

- Large-scale validations
- Controlled benchmarks
- Massive data ingestion
- ML training scenarios
- Decoupled distributed processing

---

## 📦 Installation and Usage

```bash
# Clone the repository
$ git clone https://github.com/tuusuario/dataslice-orchestrator.git

# Import as a Maven project in Eclipse or STS

# Run a test with a sample dataset
$ mvn clean install
$ java -jar target/dataslice-0.0.1-SNAPSHOT.jar data/
```

---

## 📜 License

This project is distributed under the **Apache 2.0** license.

You are free to use, modify, and distribute it as long as you respect the terms of the original license.

---

## 🙌 Credits

Developed by [Miguel Cid](https://www.linkedin.com/in/miguel-cid-paz-picon/) as part of his modular stack for predictive API tools.

Thanks for using DataSlice! 🚀
