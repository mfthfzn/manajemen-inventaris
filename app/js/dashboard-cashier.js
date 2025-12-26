import { getCookie } from "./cookie.js";

document.addEventListener("DOMContentLoaded", async function (event) {
  event.preventDefault();

  try {
    const email = getCookie("email");
    const tokenSession = getCookie("tokenSession");
    const response = await fetch(
      `http://127.0.0.1:8080/api/session?email=${email}&token=${tokenSession}`,
      {
        method: "GET",
      }
    );

    const data = await response.json();

    if (response.status !== 200 || data.expired == true) {
      window.location.href = "login.html";
    }
  } catch (error) {
    console.error("Error:", error);
  }

  let name = getCookie("name");
  let role = getCookie("role");

  const profileName = document.querySelector(".profile-name");
  const profileRole = document.querySelector(".profile-role");

  profileName.textContent = name;
  profileRole.textContent = role;
});
