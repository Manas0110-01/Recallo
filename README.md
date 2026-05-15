# 🧠 Recallo: The AI-Powered Temporal Memory Vault

Recallo is a full-stack AI application and browser extension that allows users to save web snippets, notes, and memories into a high-dimensional vector database. Using **Temporal RAG** (Retrieval-Augmented Generation), Recallo can answer questions based on your saved context while filtering by time and relevance to eliminate AI hallucinations.

## ✨ Features
* **Temporal RAG Pipeline:** Context-aware AI querying with time-filtered semantic search.
* **Browser Extension:** Instantly capture and embed knowledge directly from the web.
* **Mind-Vault Dashboard:** A sleek React interface to manage and interact with your digital memory.
* **Local Embeddings:** Utilizes `AllMiniLmL6V2EmbeddingModel` running locally to convert text into vectors with zero API costs.
* **High-Speed AI Generation:** Powered by Groq/Llama 3.1 and LangChain4j for lightning-fast inference.

## 🛠️ Tech Stack
* **Backend:** Java Spring Boot (v4.0.5), Maven, LangChain4j
* **Frontend:** React, Vite (Mind-Vault UI)
* **Database:** PostgreSQL, Supabase, `pgvector` extension
* **AI Models:** Groq (Llama 3.1), Gemini API, Local MiniLM (Embeddings)

---

## 🚀 Getting Started (Local Development)

### Prerequisites
* Java 17+
* Node.js & npm
* A Supabase project with `pgvector` enabled

### 1. Backend Setup (Spring Boot)
1. Clone the repository:
   ```bash
   git clone [https://github.com/Manas0110-01/recallo.git](https://github.com/Manas0110-01/recallo.git)
   cd recallo
