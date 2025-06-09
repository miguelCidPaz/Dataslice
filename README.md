# 📦 DataSlice Orchestrator

🌐 Available languages: [🇬🇧 English](README.md) | [🇪🇸 Español](README.es.md)

**DataSlice** is a lightweight, modular, and efficient file orchestrator. Its mission is clear: enable massive analysis of tabular data without requiring clusters, complex frameworks, or heavy infrastructure.

> ⚠️ **Warning**: This orchestrator **does NOT include** any analysis or processing engine. It works by connecting input adapters to output blocks and delegates the actual execution to an external engine.

---

## 🚀 Key Features

- ✅ Native support for **CSV**, **JSON**, and **JSONL** files, even simultaneously.
- 🔌 Adapter and block-based architecture, easy to extend.
- 🧱 Designed to work with external processing engines, either custom or third-party.
- 🧠 Built to handle **millions of records per file** without crashing memory.
- 🧰 Includes additional tools like a **streaming dataset generator**.

---

## 📂 Basic Structure

```bash
├── adapters/           # Loaders for each file type (CSV, JSON, JSONL...)
├── models/             # Common data structures
├── utils/              # Logs, helper tools
├── runner/             # Entry point to run with external engine
└── example/            # Simple usage cases
```

---

## 🧠 On Race Conditions

**IMPORTANT**: This orchestrator is optimized for high performance. In multi-threaded or massive load scenarios, **no artificial synchronization control is enforced**.

> ⚠️ If you connect a motor not ready for asynchronous or parallel flows, you may encounter intermittent errors (e.g., `null` during read/write).

**Tip**: Make sure your target engine implements proper buffers or semaphores. It is not the orchestrator's responsibility to guarantee consistency in uncontrolled environments.

---

## 📈 Use Cases

- Large-scale validations
- Controlled benchmarks
- Massive data ingestion
- ML training pipelines
- Decoupled distributed processing

---

## 🛠️ Additional Tool: Dataset Generator

Includes a dataset generator that:

- Writes directly to disk using streams
- Doesn't collapse memory
- Supports customizable column and volume configuration

⚠️ *Caution: a CSV with 100 million rows may take up several GB. Use wisely.*

---

## 📦 Installation and Usage

```bash
# Clone the repository
$ git clone https://github.com/youruser/dataslice-orchestrator.git

# Import as Maven project in Eclipse or STS

# Run a sample dataset test
$ mvn clean install
$ java -jar target/dataslice-0.0.1-SNAPSHOT.jar data/
```

---

## 📜 License

This project is distributed under the **Apache 2.0** license.

You may use, modify, and distribute it freely as long as you comply with the original license terms.

---

## 🙌 Credits

Developed by [Miguel Cid](https://www.linkedin.com/in/miguel-cid-paz-picon/) as part of his predictive API tools stack.

Thanks for using DataSlice! 🚀