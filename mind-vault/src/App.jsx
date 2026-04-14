import { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [memories, setMemories] = useState([]);
  const [loading, setLoading] = useState(true);

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

  // NEW: The function to delete a memory
  const handleDelete = (id) => {
    // 1. Tell the backend to delete it from PostgreSQL
    fetch(`http://localhost:8080/api/memory/delete/${id}`, {
      method: 'DELETE',
    })
    .then(response => {
      if (response.ok) {
        // 2. Remove it from the React grid instantly without reloading the page
        setMemories(memories.filter(memory => memory.id !== id));
      } else {
        console.error("Failed to delete memory from backend.");
      }
    })
    .catch(error => console.error("Error connecting to backend:", error));
  };

  return (
    <div className="vault-container">
      <header>
        <h1>🧠 The Mind Vault</h1>
        <p>Your AI-powered second brain.</p>
      </header>

      <div className="search-bar">
        <input type="text" placeholder="Search your memories..." />
        <button>Recall</button>
      </div>

      {loading ? (
        <p style={{ textAlign: "center" }}>Loading your brain...</p>
      ) : (
        <div className="memory-grid">
          {memories.map((memory) => (
            // Switched key from index to memory.id since we need the exact ID for deletion
            <div className="memory-card" key={memory.id} style={{ position: "relative" }}>
              
              {/* NEW: The Delete Button */}
              <button 
                onClick={() => handleDelete(memory.id)}
                style={{
                  position: "absolute",
                  top: "10px",
                  right: "10px",
                  background: "transparent",
                  color: "#ef4444",
                  padding: "5px",
                  fontSize: "1.2rem",
                  border: "none",
                  cursor: "pointer"
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