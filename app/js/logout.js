import {getCookie} from "./cookie.js"

document
  .getElementById("exitButton")
  .addEventListener("click", async function() {
    try {
      const email = getCookie("email");
      const tokenSession = getCookie("tokenSession");
      const response = await fetch(
        `http://127.0.0.1:8080/api/session?email=${email}&token=${tokenSession}`,
        {
          method: "DELETE",
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include'
        }
      );

      const data = await response.json();

      if (response.status === 200 && data.removed == true) {
        window.location.href = "login.html";
      }
    } catch (error) {
      console.error("Error:", error);
    }
  });
