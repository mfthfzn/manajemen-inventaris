import { getCookie } from "./cookie.js";

const visibilityLogo = document.querySelector(".visibility-logo");
const password = document.querySelector("#password");

visibilityLogo.addEventListener("click", function () {
  if (password.type == "password") {
    password.type = "text";
    visibilityLogo.src = "assets/show.svg";
  } else {
    password.type = "password";
    visibilityLogo.src = "assets/hide.svg";
  }
});

document
  .querySelector(".login-form")
  .addEventListener("submit", async function (event) {
    event.preventDefault();
    const messageError = document.querySelector(".message-error");

    messageError.textContent = "";

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
      const response = await fetch("http://127.0.0.1:8080/api/session", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(
          password
        )}`,
        credentials: "include",
      });

      const data = await response.json();
      console.log("Response:", data);

      if (response.status === 200) {
        window.location.href = "dashboard-cashier.html";
      } else {
        messageError.textContent = data.message;
      }
    } catch (error) {
      console.error("Error:", error);
      messageError.textContent = "Terjadi kesalahan saat login!";
    }
  });

document.addEventListener("DOMContentLoaded", async function (event) {
  event.preventDefault();

  try {
    const email = getCookie("email");
    const tokenSession = getCookie("tokenSession");
    const response = await fetch(`http://127.0.0.1:8080/api/session?email=${email}&token=${tokenSession}`, {
      method: "GET"
    });

    const data = await response.json();
    const role = getCookie("role");

    if (
      response.status === 200 &&
      role == "CASHIER" &&
      data.expired == false
    ) {
      window.location.href = "dashboard-cashier.html";
    } else if (
      response.status === 200 &&
      role === "INVENTORY_STAFF" &&
      data.expired == false
    ) {
      window.location.href = "dashboard-invetory.html";
    }
  } catch (error) {
    console.error("Error:", error);
  }
});
