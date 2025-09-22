# 🍽️ Food Delivery Service

A console-based **Food Delivery Application** implemented in Java.  
The project demonstrates **Object-Oriented Programming principles** and **SOLID design** in practice, including concepts like **Dependency Inversion**, clean separation of concerns, and layered architecture (Domain, Service, View, Persistence).

---

## ✨ Features

- **User Authentication**
  - Customers authenticate with username and password read from `customers.csv`.
  - Incorrect credentials immediately terminate the app (`AuthenticationException`).

- **Browse Foods**
  - Reads menu data from `foods.csv`.
  - Prints available dishes with description, calories, and prices.

- **Shopping Cart**
  - Add or update foods in the cart.
  - If the food already exists in the cart, its quantity is overwritten.
  - Setting quantity to `0` removes the item.
  - Ensures that the total cart value never exceeds the customer's balance (`LowBalanceException`).

- **Order Creation**
  - Convert cart into an order with unique ID and timestamp.
  - Deducts total order price from customer’s balance.
  - Writes confirmed order to `orders.csv`.
  - Empty cart after order creation.

- **Error Handling**
  - `LowBalanceException`: Prevents adding items exceeding customer balance.
  - `AuthenticationException`: Invalid login exits the program.
  - Other critical failures stop execution cleanly.

---

## 🏗️ Architecture Overview

The application is divided into **Domain**, **Service**, **Persistence**, and **View** layers, communicating via interfaces.

