// --- INTERACTIVE BACKGROUND EFFECT ---
document.addEventListener('mousemove', (e) => {
    const glow = document.getElementById('glow');
    // Makes the glowing orb follow the mouse coordinates
    glow.style.left = e.clientX + 'px';
    glow.style.top = e.clientY + 'px';
});

// --- SAVE MEMORY LOGIC (UPDATED WITH SCRAPER) ---
document.getElementById("saveBtn").addEventListener("click", async () => {
  let statusText = document.getElementById("status");
  statusText.innerText = "Reading page and sending to AIBrain...";
  statusText.style.color = "var(--cyan)";

  try {
    // 1. Get the current active tab
    let [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    
    // 2. Inject a script to scrape the text from the page
    const injectionResults = await chrome.scripting.executeScript({
      target: { tabId: tab.id },
      func: () => {
        // Grab all the visible text on the page, clean up extra spaces
        return document.body.innerText.replace(/\s+/g, ' ').trim();
      }
    });

    // 3. Extract the scraped text from the result
    const pageText = injectionResults[0].result;

    // 4. Package the data (Title, URL, AND Content) and send it to your backend
    let response = await fetch("http://localhost:8080/api/memory/save", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        title: tab.title,
        url: tab.url,
        content: pageText // <-- The new payload!
      })
    });

    if (response.ok) {
      statusText.innerText = "Saved to Memory!";
      statusText.style.color = "var(--violet)";
    } else {
      statusText.innerText = "Error: Backend refused it.";
      statusText.style.color = "red";
    }
  } catch (error) {
    statusText.innerText = "Error: Is your Spring Boot server running?";
    statusText.style.color = "red";
    console.error(error);
  }
});

// --- SEARCH MEMORY LOGIC ---
document.getElementById('searchBtn').addEventListener('click', async () => {
    const queryText = document.getElementById('searchInput').value;
    const resultsDiv = document.getElementById('results');
    
    if (!queryText) return;

    resultsDiv.innerHTML = "<i>Searching AI memory...</i>";

    try {
        // Send the question to your Spring Boot server
        const response = await fetch('http://localhost:8080/api/memory/search', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ query: queryText })
        });

        const matches = await response.json();
        
        // Clear the "Searching..." text
        resultsDiv.innerHTML = "";

        if (matches.length === 0) {
            resultsDiv.innerHTML = "<p>No memories found.</p>";
            return;
        }

        // Loop through the results and display them
        matches.forEach(match => {
            const resultHtml = `
                <div class="result-item">
                    <a href="${match.url}" target="_blank">${match.title}</a>
                </div>
            `;
            resultsDiv.innerHTML += resultHtml;
        });

    } catch (error) {
        resultsDiv.innerHTML = "<p style='color:red;'>Error connecting to AI brain.</p>";
        console.error(error);
    }
});