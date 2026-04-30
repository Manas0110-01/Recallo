import { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [memories, setMemories] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // NEW: State for the AI Chat
  const [query, setQuery] = useState("");
  const [aiAnswer, setAiAnswer] = useState("");
  const [isThinking, setIsThinking] = useState(false);

  useEffect(() => {
    fetch('http://localhost:8080/api/memory/all')
      .then(response => response.json())
      .then(data => {
        setMemories(data.reverse());
        setLoading(false);
      })
      .catch(error => {
        console.error("Error fetching memories:", error);
        setLoading(false);
      });
  }, []);

  const handleDelete = (id) => {
    fetch(`http://localhost:8080/api/memory/delete/${id}`, {
      method: 'DELETE',
    })
    .then(response => {
      if (response.ok) {
        setMemories(memories.filter(memory => memory.id !== id));
      }
    })
    .catch(error => console.error("Error connecting to backend:", error));
  };

  // NEW: The function that asks Gemini a question
  const handleAsk = () => {
    if (!query.trim()) return; // Don't send empty questions
    
    setIsThinking(true);
    setAiAnswer(""); // Clear the old answer

    fetch('http://localhost:8080/api/memory/ask', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ query: query })
    })
    .then(response => response.text()) // Gemini returns a plain string
    .then(data => {
      setAiAnswer(data);
      setIsThinking(false);
    })
    .catch(error => {
      console.error("AI Error:", error);
      setAiAnswer("The brain is disconnected. Check your backend!");
      setIsThinking(false);
    });
  };

  return (
    <div className="vault-container">
      <header>
        <h1>🧠 The Mind Vault</h1>
        <p>Your AI-powered second brain.</p>
      </header>

      {/* UPDATED: The Search Bar is now connected to state */}
      <div className="search-bar">
        <input 
          type="text" 
          placeholder="Ask your second brain a question..." 
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleAsk()} // Press Enter to ask
        />
        <button onClick={handleAsk}>
          {isThinking ? "Thinking..." : "Recall"}
        </button>
      </div>

      {/* NEW: The AI Response Box */}
      {aiAnswer && (
        <div style={{
          background: "linear-gradient(to right, #1e1e24, #2a2a35)",
          padding: "25px",
          borderRadius: "12px",
          borderLeft: "4px solid #8b5cf6",
          marginBottom: "40px",
          lineHeight: "1.6",
          fontSize: "1.05rem"
        }}>
          <h3 style={{ color: "#a78bfa", marginTop: 0, marginBottom: "15px" }}>✨ AI Synthesis</h3>
          <p style={{ margin: 0 }}>{aiAnswer}</p>
        </div>
      )}

      {loading ? (
        <p style={{ textAlign: "center" }}>Loading your brain...</p>
      ) : (
        <div className="memory-grid">
          {memories.map((memory) => (
            <div className="memory-card" key={memory.id} style={{ position: "relative" }}>
              <button 
                onClick={() => handleDelete(memory.id)}
                style={{
                  position: "absolute", top: "10px", right: "10px",
                  background: "transparent", color: "#ef4444", padding: "5px",
                  fontSize: "1.2rem", border: "none", cursor: "pointer"
                }}
                title="Delete this memory"
              >
                🗑️
              </button>
              <h3 style={{ fontSize: "1.1rem", marginBottom: "8px", paddingRight: "25px" }}>{memory.title}</h3>
              <a href={memory.url} target="_blank" rel="noreferrer" style={{ color: "#06b6d4", fontSize: "0.9rem", textDecoration: "none" }}>
                Source Link
              </a>
              <p style={{ fontSize: "0.95rem", color: "#a1a1aa", marginTop: "12px", lineHeight: "1.5" }}>
                {memory.content ? memory.content.substring(0, 150) + "..." : "No text content."}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default App