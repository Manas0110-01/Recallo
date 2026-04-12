document.getElementById("saveBtn").addEventListener("click", async () => {
  
  // Ask Chrome what tab is currently active
  let [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
  
  let statusText = document.getElementById("status");
  statusText.innerText = "Sending to AIBrain...";
  statusText.style.color = "orange";

  // Package the data and send it to your Spring Boot server
  try {
    let response = await fetch("http://localhost:8080/api/memory/save", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        title: tab.title,
        url: tab.url
      })
    });

    if (response.ok) {
      statusText.innerText = "Saved to Memory!";
      statusText.style.color = "green";
    } else {
      statusText.innerText = "Error: Backend refused it.";
      statusText.style.color = "red";
    }
  } catch (error) {
    statusText.innerText = "Error: Is your Spring Boot server running?";
    statusText.style.color = "red";
  }
});