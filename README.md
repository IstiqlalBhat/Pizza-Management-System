# Pizza Management System

## Overview
This Java-based application is designed to help manage pizza shop operations including order processing, inventory management, customer tracking, and reporting. The system supports various order types (dine-in, pickup, and delivery) and maintains a database of customers, orders, pizzas, and inventory.

## Features
- **Order Management**: Create and modify orders for dine-in, pickup, or delivery
- **Customer Management**: Add new customers and retrieve customer information
- **Pizza Customization**: Build custom pizzas with various sizes, crusts, and toppings
- **Inventory Tracking**: Monitor topping inventory levels and restock as needed
- **Order Discounts**: Apply discounts to individual pizzas or entire orders
- **Reporting**: Generate reports for inventory levels, topping popularity, and profitability

## File Structure
- **Menu.java**: Main entry point containing the user interface and menu system
- **DBNinja.java**: Database interface that handles all SQL operations
- **DBConnector.java**: Manages database connection details
- **Pizza.java**: Class representing pizza objects with toppings and pricing
- **Order.java**: Base class for all orders
- **DineinOrder.java**: Specialization for dine-in orders
- **PickupOrder.java**: Specialization for pickup orders
- **DeliveryOrder.java**: Specialization for delivery orders
- **Customer.java**: Customer information management
- **Topping.java**: Represents topping inventory and usage
- **Discount.java**: Discount management for orders and pizzas
- **sql/**: Directory containing SQL schema and scripts

## Database Schema
The system uses a relational database with tables for:
- Customers
- Orders (with subtypes for dine-in, pickup, and delivery)
- Pizzas 
- Toppings
- Discounts
- Inventory tracking

## Usage
1. Run the application through the Menu class
2. Use the numerical menu options to navigate system functionality
3. Follow the prompts to input required information for each operation

## Requirements
- Java Runtime Environment
- SQL Database (MySQL recommended)
- JDBC driver for database connectivity

## Setup
1. Configure your database connection in DBConnector.java
2. Run the SQL scripts in the sql/ directory to set up the database schema
3. Compile and run the Java application 