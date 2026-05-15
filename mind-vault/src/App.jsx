import { useState, useEffect, useRef } from 'react'
import './App.css'

function App() {
  const [activeTab, setActiveTab] = useState('chat');
  const [memories, setMemories] = useState([]);
  const [loading, setLoading] = useState(true);
  
  const [query, setQuery] = useState("");
  const [isThinking, setIsThinking] = useState(false);
  const [daysLimit, setDaysLimit] = useState(0); 
  const [chatHistory, setChatHistory] = useState([]); 

  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatHistory, isThinking]);

  useEffect(() => {
    fetch('http://localhost:8080/api/memory/all')
      .then(response => response.json())
      .then(data => {
        setMemories(data.reverse());
        setLoading(false);
      })
      .catch(error => console.error("Error fetching memories:", error));
  }, []);

  const handleDelete = (id) => {
    fetch(`http://localhost:8080/api/memory/delete/${id}`, { method: 'DELETE' })
    .then(response => {
      if (response.ok) setMemories(memories.filter(memory => memory.id !== id));
    })
  };

  // UPDATED: The Streaming Fetch Request
  const handleAsk = async () => {
    if (!query.trim()) return; 
    
    const userMessage = { role: 'user', text: query };
    const currentHistory = [...chatHistory]; 
    
    // Add the user message, PLUS an empty AI message that we will "type" into
    setChatHistory([...chatHistory, userMessage, { role: 'ai', text: "" }]);
    setQuery(""); 
    setIsThinking(true); 

    try {
      const response = await fetch('http://localhost:8080/api/memory/ask', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          query: userMessage.text, 
          days: daysLimit === 0 ? null : daysLimit,
          history: currentHistory
        })
      });

      // Open the reader to catch words as they stream in
      const reader = response.body.getReader();
      const decoder = new TextDecoder("utf-8");
      let done = false;

      setIsThinking(false); // We connected! Now start typing...

      while (!done) {
        const { value, done: readerDone } = await reader.read();
        done = readerDone;
        
        if (value) {
          const chunk = decoder.decode(value, { stream: true });
          
          // FIX: Deep copy to prevent React Strict Mode stuttering
          setChatHistory(prev => {
            const newHistory = [...prev];
            const lastIndex = newHistory.length - 1;
            const lastMessage = newHistory[lastIndex];

            newHistory[lastIndex] = {
              ...lastMessage,
              text: lastMessage.text + chunk
            };

            return newHistory;
          });
        }
      }
    } catch (error) {
      console.error("Stream Error:", error);
      setChatHistory(prev => {
        const newHistory = [...prev];
        const lastIndex = newHistory.length - 1;
        const lastMessage = newHistory[lastIndex];
        
        newHistory[lastIndex] = {
          ...lastMessage,
          text: "⚠️ The brain is disconnected. Check your backend!"
        };
        
        return newHistory;
      });
      setIsThinking(false);
    }
  };

  const clearChat = () => setChatHistory([]);

  const handleCopy = (text) => {
    navigator.clipboard.writeText(text);
  };

  return (
    <div className="vault-container">
      <header style={{ marginBottom: "30px" }}>
        <h1 style={{ fontSize: "2.5rem", margin: "0 0 10px 0", background: "linear-gradient(to right, #a855f7, #3b82f6)", WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent" }}>
          Recallo
        </h1>
        <p style={{ color: "#a1a1aa", margin: 0, fontSize: "1.1rem" }}>Your AI-powered second brain.</p>
      </header>

      <nav className="nav-tabs">
        <button className={`tab-btn ${activeTab === 'chat' ? 'active' : ''}`} onClick={() => setActiveTab('chat')}>
          💬 Neural Chat
        </button>
        <button className={`tab-btn ${activeTab === 'vault' ? 'active' : ''}`} onClick={() => setActiveTab('vault')}>
          🗄️ Data Vault ({memories.length})
        </button>
      </nav>

      {/* --- TAB 1: THE CHAT INTERFACE --- */}
      {activeTab === 'chat' && (
        <div style={{ display: "flex", flexDirection: "column", height: "70vh" }}>
          
          <div className="chat-window" style={{ flexGrow: 1, overflowY: "auto", padding: "20px", background: "#121214", borderRadius: "16px", border: "1px solid #27272a", marginBottom: "20px" }}>
            
            {chatHistory.length === 0 ? (
              <div style={{ height: "100%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", color: "#71717a" }}>
                <h2 style={{ margin: 0, color: "#a1a1aa" }}>What do you want to recall?</h2>
                <p>Ask a question, and I'll search your saved memories.</p>
              </div>
            ) : (
              <>
                <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: "20px" }}>
                  <button onClick={clearChat} style={{ background: "transparent", border: "1px solid #ef4444", color: "#ef4444", padding: "6px 12px", borderRadius: "6px", cursor: "pointer", fontSize: "0.85rem" }}>Clear Chat</button>
                </div>

                {chatHistory.map((msg, index) => (
                  <div key={index} style={{ display: "flex", justifyContent: msg.role === 'user' ? "flex-end" : "flex-start", marginBottom: "20px" }}>
                    
                    <div style={{
                      position: "relative", 
                      maxWidth: "85%",
                      padding: "16px 20px",
                      lineHeight: "1.6",
                      whiteSpace: "pre-wrap",
                      fontSize: "1.05rem",
                      background: msg.role === 'user' ? "linear-gradient(135deg, #3b82f6, #2563eb)" : "#18181b",
                      color: "white",
                      border: msg.role === 'user' ? "none" : "1px solid #27272a",
                      borderBottomRightRadius: msg.role === 'user' ? "4px" : "16px",
                      borderBottomLeftRadius: msg.role === 'ai' ? "4px" : "16px",
                      borderTopLeftRadius: "16px", borderTopRightRadius: "16px",
                      boxShadow: "0 4px 12px rgba(0,0,0,0.1)"
                    }}>
                      
                      {/* If the message is empty, it means we are waiting for the very first word to arrive */}
                      {msg.role === 'ai' && msg.text === "" ? (
                         <span style={{ color: "#a1a1aa", fontStyle: "italic" }}>Recallo is thinking...</span>
                      ) : (
                         msg.text
                      )}
                      
                      {msg.role === 'ai' && msg.text !== "" && !isThinking && (
                        <button onClick={() => handleCopy(msg.text)} title="Copy to clipboard" style={{
                          position: "absolute", bottom: "-30px", left: "10px",
                          background: "transparent", border: "none", color: "#71717a", cursor: "pointer", fontSize: "0.9rem"
                        }}>
                          📋 Copy
                        </button>
                      )}
                    </div>
                  </div>
                ))}
                <div ref={chatEndRef} />
              </>
            )}
          </div>

          <div className="search-bar">
            <select value={daysLimit} onChange={(e) => setDaysLimit(Number(e.target.value))}>
              <option value={0}>All Time</option>
              <option value={7}>Last 7 Days</option>
              <option value={30}>Last 30 Days</option>
            </select>

            <input 
              type="text" 
              placeholder="Ask your second brain..." 
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAsk()}
              disabled={isThinking}
            />
            <button onClick={handleAsk} disabled={isThinking}>
              {isThinking ? "..." : "Recall"}
            </button>
          </div>
        </div>
      )}

      {/* --- TAB 2: THE DATA VAULT --- */}
      {activeTab === 'vault' && (
        <div>
          <h2 style={{ color: "#e4e4e7", borderBottom: "1px solid #27272a", paddingBottom: "15px", marginBottom: "25px" }}>
            Raw Memory Data
          </h2>
          
          {loading ? (
            <p style={{ textAlign: "center", color: "#a1a1aa" }}>Accessing database...</p>
          ) : memories.length === 0 ? (
            <p style={{ textAlign: "center", color: "#71717a" }}>Your vault is empty. Use your Chrome extension to save articles!</p>
          ) : (
            <div className="memory-grid">
              {memories.map((memory) => (
                <div className="memory-card" key={memory.id} style={{ position: "relative" }}>
                  <button 
                    onClick={() => handleDelete(memory.id)}
                    style={{ position: "absolute", top: "15px", right: "15px", background: "transparent", color: "#ef4444", padding: "5px", fontSize: "1.2rem", border: "none", cursor: "pointer" }}
                    title="Delete this chunk"
                  >🗑️</button>
                  <h3 style={{ fontSize: "1.1rem", margin: "0 0 10px 0", paddingRight: "30px", color: "#e4e4e7" }}>{memory.title}</h3>
                  <a href={memory.url} target="_blank" rel="noreferrer" style={{ color: "#3b82f6", fontSize: "0.85rem", textDecoration: "none", display: "inline-block", marginBottom: "15px" }}>🔗 Source URL</a>
                  <p style={{ fontSize: "0.9rem", color: "#a1a1aa", margin: 0, lineHeight: "1.6" }}>
                    {memory.content ? memory.content.substring(0, 160) + "..." : "No text content."}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default App