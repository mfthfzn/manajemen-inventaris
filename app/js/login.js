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

const emailValue = document.getElementById("email");
const passwordValue = document.getElementById("password");

document
  .querySelector(".login-form")
  .addEventListener("submit", async function (event) {
    event.preventDefault();
    const messageError = document.querySelector(".message-error");

    messageError.textContent = "";

    try {
      const response = await fetch("http://127.0.0.1:8080/api/session", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: `email=${encodeURIComponent(
          emailValue.value
        )}&password=${encodeURIComponent(passwordValue.value)}`,
        credentials: "include",
      });

      const data = await response.json();
      console.log("Response:", data);

      if (response.status === 200) {
        window.location.href = "dashboard-cashier.html";
      } else {
        messageError.textContent = data.message;
        // emailValue.value = "";
        // passwordValue.value = "";
      }
    } catch (error) {
      console.error("Error:", error);
      messageError.textContent = "Terjadi kesalahan saat login!";
    }
  });

document.addEventListener("DOMContentLoaded", async function (event) {

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
    const role = getCookie("role");
    console.log(data)

    if (response.status === 200 && role == "CASHIER" && data.expired == false) {
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
