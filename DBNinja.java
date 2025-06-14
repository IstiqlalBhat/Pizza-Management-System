package cpsc4620;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;

/*
 * This file is where most of your code changes will occur You will write the code to retrieve
 * information from the database, or save information to the database
 *
 * The class has several hard coded static variables used for the connection, you will need to
 * change those to your connection information
 *
 * This class also has static string variables for pickup, delivery and dine-in. If your database
 * stores the strings differently (i.e "pick-up" vs "pickup") changing these static variables will
 * ensure that the comparison is checking for the right string in other places in the program. You
 * will also need to use these strings if you store this as boolean fields or an integer.
 *
 *
 */

/**
 * A utility class to help add and retrieve information from the database
 */

public final class DBNinja {
	private static Connection conn;

	// Change these variables to however you record dine-in, pick-up and delivery, and sizes and crusts
	public final static String pickup = "pickup";
	public final static String delivery = "delivery";
	public final static String dine_in = "dinein";

	public final static String size_s = "Small";
	public final static String size_m = "Medium";
	public final static String size_l = "Large";
	public final static String size_xl = "XLarge";

	public final static String crust_thin = "Thin";
	public final static String crust_orig = "Original";
	public final static String crust_pan = "Pan";
	public final static String crust_gf = "Gluten-Free";


	private static boolean connect_to_db() throws SQLException, IOException {

		try {
			conn = DBConnector.make_connection();
			return true;
		} catch (SQLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

	}


	public static void addOrder(Order order) throws SQLException, IOException {

		/*
		 * Code to add the order to the database. This includes not just adding the order to the ordert table,
		 * but also handling entries for delivery, dinein, and pickup orders.
		 */
		try {
			connect_to_db();
			String query = "";
			String insertQueryType = "";
			int tableNumber = 0;
			if (order.getOrderID() == 0) {
				query = "INSERT INTO ordert(OrdertCustomerID, OrdertTimeStamp, OrdertCustomerPrice, OrdertBusinessPrice, OrdertType, IsCompleted) VALUES (?, ?, ?, ?, ?, ?)";

				PreparedStatement stmt = conn.prepareStatement(query);
				stmt.setInt(1, order.getCustID());
				stmt.setString(2, order.getDate());
				stmt.setDouble(3, order.getCustPrice());
				stmt.setDouble(4, order.getBusPrice());
				stmt.setString(5, order.getOrderType());
				stmt.setInt(6, order.getIsComplete());
				stmt.executeUpdate();
				int latestOrderID = DBNinja.getLastOrder().getOrderID();

				if (order instanceof DineinOrder) {
					connect_to_db();
					insertQueryType = "INSERT INTO dinein(DineInOrderID, DineInTableNumber) VALUES (?, ?)";
					DineinOrder dinein = (DineinOrder) order;
					tableNumber = dinein.getTableNum();
					PreparedStatement dineInStmt = conn.prepareStatement(insertQueryType);
					dineInStmt.setInt(1, latestOrderID);
					dineInStmt.setInt(2, tableNumber);
					dineInStmt.executeUpdate();
				} else if (order instanceof PickupOrder) {
					connect_to_db();
					insertQueryType = "INSERT INTO pickup(PickUpOrderID) VALUES (?)";
					PreparedStatement pickupStmt = conn.prepareStatement(insertQueryType);
					pickupStmt.setInt(1, latestOrderID);
					pickupStmt.executeUpdate();
				} else {
					connect_to_db();
					insertQueryType = "INSERT INTO delivery(DeliveryOrderID) VALUES (?)";
					PreparedStatement deliveryStmt = conn.prepareStatement(insertQueryType);
					deliveryStmt.setInt(1, latestOrderID);
					deliveryStmt.executeUpdate();
				}

			} else {
				String updateQuery = "UPDATE ordert SET OrdertCustomerPrice=?, OrdertBusinessPrice=?, OrdertType=? WHERE OrdertID=?";

				PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
				updateStmt.setDouble(1, order.getCustPrice());
				updateStmt.setDouble(2, order.getBusPrice());
				updateStmt.setString(3, order.getOrderType());
				updateStmt.setInt(4, order.getOrderID());
				updateStmt.executeUpdate();
			}
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// REMINDER: Always close your database connections.
	}


	public static void addPizza(Pizza pizza) throws SQLException, IOException {
		/*
		 * Inserts a pizza into the database. This involves handling pizza base details and potentially
		 * associated discounts and toppings. Methods are available below for dealing with related entities.
		 */
		try {
			connect_to_db();
			if (pizza.getPizzaID() == 0) {
				String insertQuery = "INSERT INTO pizza(PizzaOrderID, PizzaBusinessPrice, PizzaCustomerPrice, PizzaState, PizzaCrustType, PizzaSize) VALUES (?, ?, ?, ?, ?, ?)";

				PreparedStatement stmt = conn.prepareStatement(insertQuery);
				stmt.setInt(1, pizza.getOrderID());
				stmt.setDouble(2, pizza.getBusPrice());
				stmt.setDouble(3, pizza.getCustPrice());
				stmt.setString(4, pizza.getPizzaState());
				stmt.setString(5, pizza.getCrustType());
				stmt.setString(6, pizza.getSize());
				stmt.executeUpdate();
				System.out.println("Pizza added.");
			} else {
				String updateQuery = "UPDATE pizza SET PizzaCustomerPrice=?, PizzaBusinessPrice=? WHERE PizzaID=?";

				PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
				updateStmt.setDouble(1, pizza.getCustPrice());
				updateStmt.setDouble(2, pizza.getBusPrice());
				updateStmt.setInt(3, pizza.getPizzaID());
				updateStmt.executeUpdate();
			}
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// REMINDER: Always close your database connections.
	}



	public static void applyTopping(Pizza pizza, Topping topping, boolean doubleTopping) throws SQLException, IOException {
		/*
		 * This method updates the topping inventory and associates the topping with the pizza.
		 * - It deducts the needed amount from inventory, taking into account if the topping is doubled.
		 * - It then logs the topping usage on the specified pizza in the pizzatopping table.
		 *
		 * It is assumed that checks for negative inventory are handled externally before this method is called.
		 */
		try {
			connect_to_db();
			double toppingAmountRequired = 0.0;

			switch (pizza.getSize()) {
				case "Small":
					toppingAmountRequired = topping.getPerAMT();
					break;
				case "Medium":
					toppingAmountRequired = topping.getMedAMT();
					break;
				case "Large":
					toppingAmountRequired = topping.getLgAMT();
					break;
				case "XLarge":
					toppingAmountRequired = topping.getXLAMT();
					break;
			}

			if (doubleTopping) {
				toppingAmountRequired *= 2;  // Double the required amount if the topping is doubled
			}

			if (topping.getCurINVT() - toppingAmountRequired < 0) {
				System.out.println("Not enough topping inventory to fulfill this request.");
			} else {
				String updateInventoryQuery = "UPDATE topping SET ToppingCurrentInvLvl = ToppingCurrentInvLvl - ? WHERE ToppingID = ?";
				PreparedStatement inventoryUpdateStmt = conn.prepareStatement(updateInventoryQuery);
				inventoryUpdateStmt.setDouble(1, toppingAmountRequired);
				inventoryUpdateStmt.setInt(2, topping.getTopID());
				inventoryUpdateStmt.executeUpdate();
			}

			// Record the topping use on the pizza
			String recordToppingQuery = "INSERT INTO pizzatopping(PizzaToppingPizzaID, PizzaToppingToppingID, PizzaToppingIsDouble) VALUES (?, ?, ?)";
			PreparedStatement recordToppingStmt = conn.prepareStatement(recordToppingQuery);
			recordToppingStmt.setInt(1, pizza.getPizzaID());
			recordToppingStmt.setInt(2, topping.getTopID());
			recordToppingStmt.setBoolean(3, doubleTopping);
			recordToppingStmt.executeUpdate();
			System.out.println("Topping applied: Pizza ID " + pizza.getPizzaID() + ", Topping ID " + topping.getTopID() + ", Doubled: " + doubleTopping);

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// REMINDER: Always close your database connections.
		}
	}


	public static void applyDiscountToPizza(Pizza pizza, Discount discount) throws SQLException, IOException {
		/*
		 * This method associates a discount with a pizza in the database.
		 * It inserts a record into the specialpizzaoffer table to link the pizza and discount.
		 */
		connect_to_db();
		try {
			String insertDiscountQuery = "INSERT INTO specialpizzaoffer(SpecialPizzaOfferPizzaID, SpecialPizzaOfferDiscountID) VALUES (?, ?)";
			PreparedStatement stmt = conn.prepareStatement(insertDiscountQuery);
			stmt.setInt(1, pizza.getPizzaID());
			stmt.setInt(2, discount.getDiscountID());
			stmt.executeUpdate();
			System.out.println("Discount applied to Pizza ID: " + pizza.getPizzaID());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// REMINDER: Always close your database connections.
	}


	public static void useOrderDiscount(Order o, Discount d) throws SQLException, IOException {

		/*
		 * This method connects a discount with an order in the database
		 *
		 * You might use this, you might not depending on where / how to want to update
		 * this information in the dabast
		 */
		connect_to_db();
		try {
			String sql = "insert into orderdeals(OrderDealsOrderID, OrderDealsDiscountID) values(?, ?)";
			System.out.print(o.getOrderID());
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setInt(1, o.getOrderID());
			preparedStatement.setInt(2, d.getDiscountID());

			preparedStatement.executeUpdate();
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void addCustomer(Customer customer) throws SQLException, IOException {
		/*
		 * This method adds a new customer to the database.
		 */
		connect_to_db();  // Connect to the database once
		try {
			String insertCustomerQuery = "INSERT INTO customer(CustomerFirstName, CustomerLastName, CustomerPhoneNo) VALUES (?, ?, ?)";
			PreparedStatement insertStmt = conn.prepareStatement(insertCustomerQuery);
			insertStmt.setString(1, customer.getFName());
			insertStmt.setString(2, customer.getLName());
			insertStmt.setString(3, customer.getPhone());
			insertStmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		// REMINDER: Always close your database connections.
	}


	public static void completeOrder(Order order) throws SQLException, IOException {
		/*
		 * Marks the specified order in the database as completed and updates the state of the associated pizzas.
		 */
		try {
			connect_to_db();

			// Update the order's completion status
			String orderCompletionQuery = "UPDATE ordert SET IsCompleted = 1 WHERE OrdertID = ?";
			PreparedStatement orderCompletionStmt = conn.prepareStatement(orderCompletionQuery);
			orderCompletionStmt.setInt(1, order.getOrderID());
			orderCompletionStmt.executeUpdate();

			// Update the state of pizzas related to this order
			String pizzaStateUpdateQuery = "UPDATE pizza SET PizzaState = ? WHERE PizzaOrderID = ?";
			PreparedStatement pizzaStateUpdateStmt = conn.prepareStatement(pizzaStateUpdateQuery);
			pizzaStateUpdateStmt.setString(1, "Completed");
			pizzaStateUpdateStmt.setInt(2, order.getOrderID());
			pizzaStateUpdateStmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		// REMINDER: Always close your database connections.
	}



	public static ArrayList<Order> getOrders(boolean openOnly) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Fetches a list of orders from the database, optionally filtered to only include open orders.
		 * - openOnly == true: returns only orders that are not marked as completed.
		 * - openOnly == false: returns all orders.
		 * Results are ordered by the order timestamp in descending order.
		 */
		ArrayList<Order> orders = new ArrayList<>();

		try {
			// Build the SQL query dynamically based on the openOnly parameter
			StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ordert");
			if (openOnly) {
				queryBuilder.append(" WHERE IsCompleted = false");
			}
			queryBuilder.append(" ORDER BY OrdertTimeStamp DESC");

			PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString());
			ResultSet results = stmt.executeQuery();

			// Process each result and create an Order object to add to the list
			while (results.next()) {
				int orderId = results.getInt("OrdertID");
				String orderType = results.getString("OrdertType");
				int customerId = results.getInt("OrdertCustomerID");
				double orderCost = results.getDouble("OrdertCustomerPrice");
				double orderPrice = results.getDouble("OrdertBusinessPrice");
				String orderTimeStamp = results.getString("OrdertTimeStamp");
				int orderCompleteState = results.getInt("IsCompleted");

				orders.add(new Order(orderId, customerId, orderType, orderTimeStamp, orderCost, orderPrice, orderCompleteState));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Ensure the database connection is closed
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return orders;
	}


	public static Order getLastOrder() {
		/*
		 * Retrieves the most recently added order from the database and returns an Order object.
		 * This method assumes there is always at least one order in the database.
		 */
		Order lastOrder = null;
		try {
			connect_to_db();  // Establish connection to the database
			String query = "SELECT * FROM ordert ORDER BY OrdertID DESC LIMIT 1";
			PreparedStatement stmt = conn.prepareStatement(query);  // Prepare the SQL statement
			ResultSet result = stmt.executeQuery();  // Execute the query and get the result set

			if (result.next()) {
				// Extract order details from the result set
				int orderId = result.getInt("OrdertID");
				int custId = result.getInt("OrdertCustomerID");
				String orderType = result.getString("OrdertType");
				String date = result.getString("OrdertTimeStamp");
				double custPrice = result.getDouble("OrdertCustomerPrice");
				double busPrice = result.getDouble("OrdertBusinessPrice");
				int isComplete = result.getInt("IsCompleted");

				// Create a new Order object with the retrieved data
				lastOrder = new Order(orderId, custId, orderType, date, custPrice, busPrice, isComplete);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Ensure the database connection is closed
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return lastOrder;
	}


	public static ArrayList<Order> getOrdersByDate(String date) {
		/*
		 * Retrieves all orders from the database that were placed on or after a specific date.
		 */
		ArrayList<Order> ordersList = new ArrayList<>();

		try {
			connect_to_db();  // Establish a database connection

			// Built the SQL query dynamically to include orders from the specified date onwards
			String ordersQuery = "SELECT * FROM ordert WHERE OrdertTimeStamp >= '" + date + " 00:00:00' ORDER BY OrdertTimeStamp DESC";

			// Execute the query and process the results
			Statement ordersStatement = conn.createStatement();
			ResultSet orderResults = ordersStatement.executeQuery(ordersQuery);

			while (orderResults.next()) {
				int orderID = orderResults.getInt("OrdertID");
				String orderType = orderResults.getString("OrdertType");
				int customerID = orderResults.getInt("OrdertCustomerID");
				double customerPrice = orderResults.getDouble("OrdertCustomerPrice");
				double businessPrice = orderResults.getDouble("OrdertBusinessPrice");
				String timeStamp = orderResults.getString("OrdertTimeStamp");
				int completionStatus = orderResults.getInt("IsCompleted");

				// Create a new Order object and add it to the list
				ordersList.add(new Order(orderID, customerID, orderType, timeStamp, customerPrice, businessPrice, completionStatus));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Ensure the database connection is closed
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

		return ordersList;
	}


	public static ArrayList<Order> getCompletedOrders() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Retrieves all completed orders from the database. Each order is an instance of Order which could
		 * be a subtype like dineinOrder, deliveryOrder, or pickupOrder.
		 */
		ArrayList<Order> completedOrders = new ArrayList<>();

		try {
			// Build the SQL query to fetch completed orders only
			String completedOrdersQuery = "SELECT * FROM ordert WHERE IsCompleted = TRUE ORDER BY OrdertTimeStamp DESC";

			// Execute the query to retrieve the completed orders
			Statement fetchCompletedOrdersStmt = conn.createStatement();
			ResultSet orderResults = fetchCompletedOrdersStmt.executeQuery(completedOrdersQuery);

			// Process the fetched orders and create Order objects to add to the list
			while (orderResults.next()) {
				int orderID = orderResults.getInt("OrdertID");
				String orderType = orderResults.getString("OrdertType");
				int customerID = orderResults.getInt("OrdertCustomerID");
				double customerPrice = orderResults.getDouble("OrdertCustomerPrice");
				double businessPrice = orderResults.getDouble("OrdertBusinessPrice");
				String timeStamp = orderResults.getString("OrdertTimeStamp");
				int completionStatus = orderResults.getInt("IsCompleted");

				// Add the new Order object to the list of completed orders
				completedOrders.add(new Order(orderID, customerID, orderType, timeStamp, customerPrice, businessPrice, completionStatus));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Ensure the database connection is properly closed
			try {
				if (conn != null) conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

		return completedOrders;
	}


	public static ArrayList<Discount> getDiscountList() throws SQLException, IOException {
		/*
		 * Retrieves all available discounts from the database and returns them as an ArrayList of Discount objects.
		 */
		ArrayList<Discount> discounts = new ArrayList<>();

		try {
			connect_to_db(); // Establish a database connection
			String fetchDiscountsQuery = "SELECT * FROM discount"; // SQL query to retrieve all discounts
			PreparedStatement fetchDiscountsStmt = conn.prepareStatement(fetchDiscountsQuery); // Prepare the SQL statement
			ResultSet discountResults = fetchDiscountsStmt.executeQuery(); // Execute the query

			// Iterate through the result set and create Discount objects to add to the list
			while (discountResults.next()) {
				int discountID = discountResults.getInt("DiscountID");
				String discountName = discountResults.getString("DiscountName");
				boolean isPercent = discountResults.getBoolean("IsPercent");
				double discountValue = discountResults.getDouble("DiscountValue");

				// Add new Discount object to the discounts list
				discounts.add(new Discount(discountID, discountName, discountValue, isPercent));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Ensure the database connection is closed in the finally block
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

		return discounts;
	}


	public static Discount findDiscountByName(String name){
		/*
		 * Query the database for a discount using it's name.
		 * If found, then return an OrderDiscount object for the discount.
		 * If it's not found....then return null
		 *
		 */


		return null;
	}


	public static ArrayList<Customer> getCustomerList() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the data for all the customers and return an arrayList of all the customers.
		 * Don't forget to order the data coming from the database appropriately.
		 *
		 */
		ArrayList<Customer> customers = new ArrayList<Customer>();
		try {
			String sql = "SELECT * FROM customer";
			PreparedStatement preparedStatement = conn.prepareStatement(sql);

			ResultSet records = preparedStatement.executeQuery();
			while (records.next()) {
				int customerID = records.getInt("CustomerID");
				String firstName = records.getString("CustomerFirstName");
				String lastName = records.getString("CustomerLastName");
				String phoneNo = records.getString("CustomerPhoneNo");


				customers.add(
						new Customer(customerID, firstName, lastName, phoneNo));

			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}


		//DO NOT FORGET TO CLOSE YOUR CONNECTION
		return customers;
	}

	public static Customer findCustomerByPhone(String phoneNumber){
		/*
		 * Query the database for a customer using a phone number.
		 * If found, then return a Customer object for the customer.
		 * If it's not found....then return null
		 *
		 */

		/*
		 * Query the data for all the customers and return an arrayList of all the customers.
		 * Don't forget to order the data coming from the database appropriately.
		 *
		 */
		return null;
	}


	public static ArrayList<Topping> getToppingList() throws SQLException, IOException {
		ArrayList<Topping> toppingList = new ArrayList<>();
		try {
			connect_to_db();
			String toppingQuery = "SELECT * FROM topping";
			PreparedStatement toppingStmt = conn.prepareStatement(toppingQuery);
			ResultSet toppingResults = toppingStmt.executeQuery();
			while (toppingResults.next()) {
				int toppingID = toppingResults.getInt("ToppingID");
				String toppingName = toppingResults.getString("ToppingName");
				double customerPrice = toppingResults.getDouble("ToppingCustomerPrice");
				double businessPrice = toppingResults.getDouble("ToppingBusinessPrice");
				double personalQuantity = toppingResults.getDouble("ToppingQuantityForPersonal");
				double mediumQuantity = toppingResults.getDouble("ToppingQuantityForMediumUnits");
				double largeQuantity = toppingResults.getDouble("ToppingQuantityForLargeUnits");
				double xLargeQuantity = toppingResults.getDouble("ToppingQuantityForXLargeUnits");
				int minInvLevel = toppingResults.getInt("ToppingMinInvLvl");
				int currentInvLevel = toppingResults.getInt("ToppingCurrentInvLvl");
				toppingList.add(new Topping(toppingID, toppingName, personalQuantity, mediumQuantity, largeQuantity, xLargeQuantity, customerPrice, businessPrice, minInvLevel, currentInvLevel));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return toppingList;
	}

	public static Topping findToppingByName(String name){
		/*
		 * Query the database for the topping using it's name.
		 * If found, then return a Topping object for the topping.
		 * If it's not found....then return null
		 *
		 */


		return null;
	}


	public static void addToInventory(Topping t, double quantity) throws SQLException, IOException {
		/*
		 * Updates the quantity of the topping in the database by the amount specified.
		 *
		 * */
		try {
			connect_to_db();
			String sql = "UPDATE topping SET ToppingCurrentInvLvl = ToppingCurrentInvLvl+? WHERE ToppingID = ?";
			Connection conn = DBConnector.make_connection();
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setDouble(1, quantity);
			preparedStatement.setInt(2, t.getTopID());
			preparedStatement.executeUpdate();
			/*
			 * Adds toAdd amount of topping to topping t.
			 */
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}


		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static double getBaseCustPrice(String pizzaSize, String pizzaCrust) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the database for the base customer price for the given pizza size and crust type.
		 */
		double basePrice = 0.0;
		// Add code to retrieve the base price (for the customer) for the specified size and crust type.
		// Depending on how you store pizzaSize & pizzaCrust in your database, you may have to do a conversion.
		try {
			String selectSQL = "SELECT * FROM basepizza;";
			PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
			ResultSet resultSet = preparedStatement.executeQuery(selectSQL);
			while (resultSet.next()) {
				String retrievedCrustType = resultSet.getString("BasePizzaCrustType");
				String retrievedSize = resultSet.getString("BasePizzaSize");

				if (retrievedCrustType.equals(pizzaCrust) && retrievedSize.equals(pizzaSize)) {
					basePrice = resultSet.getDouble("BasePizzaCustomerPrice");
				}
			}
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		// Do not forget to close your database connection
		return basePrice;
	}

	public static double getBaseBusPrice(String size, String crust) throws SQLException, IOException {
		connect_to_db();
		/*
		 * Query the database fro the base business price for that size and crust pizza.
		 *
		 */
		double bcost = 0.0;
		// add code to get the base cost (for the business) for that size and crust pizza Depending on how
		// you store size and crust in your database, you may have to do a conversion
		try {
			String selectQuery = "select * from basepizza;";


			PreparedStatement statement = conn.prepareStatement(selectQuery);
			ResultSet record = statement.executeQuery(selectQuery);
			while (record.next()) {
				String crusttype = record.getString("BasePizzaCrustType");
				String sizebase = record.getString("BasePizzaSize");


				if (crusttype.equals(crust) && sizebase.equals(size)) {

					bcost = record.getDouble("BasePizzaBusinessPrice");
				}
			}
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		//DO NOT FORGET TO CLOSE YOUR CONNECTION
		return bcost;
	}

	public static void printInventory() throws SQLException, IOException {

		/*
		 * Queries the database and prints the current topping list with quantities.
		 *
		 * The result should be readable and sorted as indicated in the prompt.
		 *
		 */
		try {
			connect_to_db();

			String sql = "SELECT ToppingID, ToppingName, ToppingCurrentInvLvl FROM topping order by ToppingName";
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			ResultSet results = preparedStatement.executeQuery();

			while (results.next()) {
				System.out.println("ToppingID: " + results.getString("ToppingID") + " | Name: " + results.getString("ToppingName") + " | CurrentInvLvl: " + results.getString("ToppingCurrentInvLvl"));
			}

			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}


		//DO NOT FORGET TO CLOSE YOUR CONNECTION


	}

	public static void printToppingPopReport() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ToppingPopularity view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 *
		 * The result should be readable and sorted as indicated in the prompt.
		 *
		 */
		try {
			String maxOrdSql = "SELECT * FROM ToppingPopularity";
			PreparedStatement prepared = conn.prepareStatement(maxOrdSql);
			ResultSet report = prepared.executeQuery();
			int maxOrderID = -1;
			System.out.printf("%-20s  %-4s %n", "Topping", "ToppingCount");
			while (report.next()) {
				String topping = report.getString("Topping");
				Integer toppingCount = report.getInt("ToppingCount");
				System.out.printf("%-20s  %-4s %n", topping, toppingCount);
			}

			//DO NOT FORGET TO CLOSE YOUR CONNECTION
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}


		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void printProfitByPizzaReport() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ProfitByPizza view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 *
		 * The result should be readable and sorted as indicated in the prompt.
		 *
		 */
		try {
			String maxOrdSql = "SELECT * FROM ProfitByPizza";
			PreparedStatement prepared = conn.prepareStatement(maxOrdSql);
			ResultSet report = prepared.executeQuery();
			System.out.printf("%-15s  %-15s  %-10s %-30s%n", "Size", "Crust", "Profit", "OrderMonth");
			while (report.next()) {

				String size = report.getString("Size");
				String crust = report.getString("Crust");
				Double profit = report.getDouble("Profit");
				String orderDate = report.getString("OrderMonth");

				System.out.printf("%-15s  %-15s  %-10s %-30s%n", size, crust, profit, orderDate);

			}

			//DO NOT FORGET TO CLOSE YOUR CONNECTION
			conn.close();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}


		//DO NOT FORGET TO CLOSE YOUR CONNECTION
	}

	public static void printProfitByOrderType() throws SQLException, IOException {
		connect_to_db();
		/*
		 * Prints the ProfitByOrderType view. Remember that this view
		 * needs to exist in your DB, so be sure you've run your createViews.sql
		 * files on your testing DB if you haven't already.
		 *
		 * The result should be readable and sorted as indicated in the prompt.
		 *
		 */
		try {
			String profitQuery = "SELECT * FROM ProfitByOrderType";  // More descriptive query name
			PreparedStatement profitStmt = conn.prepareStatement(profitQuery);  // Updated variable name for clarity
			ResultSet profitResult = profitStmt.executeQuery();  // Updated variable name for clarity
			System.out.printf("%-15s  %-15s  %-18s %-18s %-8s%n", "CustomerType", "OrderMonth", "TotalOrderPrice",
					"TotalOrderCost", "Profit");
			while (profitResult.next()) {

				String customerType = profitResult.getString("CustomerType");
				String orderMonth = profitResult.getString("OrderMonth");
				Double totalPrice = profitResult.getDouble("TotalOrderPrice");
				Double totalCost = profitResult.getDouble("TotalOrderCost");
				Double profit = profitResult.getDouble("Profit");
				System.out.printf("%-15s  %-15s  %-18s %-18s %-8s%n", customerType, orderMonth, totalPrice,
						totalCost, profit);

			}

			// Do not forget to close your connection
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// Do not forget to close your connection
	}

	public static int getMaxPizzaID() throws SQLException, IOException {
		int maxPizzaID = -1; // More descriptive variable name
		try {
			connect_to_db();
			String maxPizzaIDQuery = "SELECT * FROM pizza WHERE PizzaID = (SELECT MAX(PizzaID) FROM pizza)";
			PreparedStatement maxPizzaIDStmt = conn.prepareStatement(maxPizzaIDQuery); // Updated variable name for clarity
			ResultSet maxPizzaIDResult = maxPizzaIDStmt.executeQuery(); // Updated variable name for clarity

			while (maxPizzaIDResult.next()) {
				maxPizzaID = Integer.parseInt(maxPizzaIDResult.getString("PizzaID")); // More descriptive variable name
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		// Do not forget to close your database connection
		return maxPizzaID;
	}



	public static String getCustomerName(int CustID) throws SQLException, IOException {
		/*
		 * This is a helper method to fetch and format the name of a customer
		 * based on a customer ID. This is an example of how to interact with
		 * your database from Java.  It's used in the model solution for this project...so the code works!
		 *
		 * OF COURSE....this code would only work in your application if the table & field names match!
		 *
		 */

		connect_to_db();

		/*
		 * an example query using a constructed string...
		 * remember, this style of query construction could be subject to sql injection attacks!
		 *
		 */
		String cusname1 = "";
		String query = "Select CustomerFirstName, CustomerLastName From customer WHERE CustomerID=" + CustID + ";";
		Statement Stmt = conn.createStatement();
		ResultSet Rset = Stmt.executeQuery(query);

		while(Rset.next()) {
			cusname1 = Rset.getString(1) + " " + Rset.getString(2);
		}

		/*
		 * an example of the same query using a prepared statement...
		 *
		 */
		String cusname2 = "";
		PreparedStatement os;
		ResultSet Rset2;
		String query2;
		query2 = "Select CustomerFirstName, CustomerLastName From customer WHERE CustomerID=?;";
		os = conn.prepareStatement(query2);
		os.setInt(1, CustID);
		Rset2 = os.executeQuery();
		while(Rset2.next()) {
			cusname2 = Rset2.getString("CustomerFirstName") + " " + Rset2.getString("CustomerLastName"); // note the use of field names in the getSting methods
		}

		conn.close();
		return cusname1; // OR cname2
	}
	/*
	 * The next 3 private methods help get the individual components of a SQL datetime object.
	 * You're welcome to keep them or remove them.
	 */
	private static int getYear(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(0,4));
	}

	private static int getMonth(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(5, 7));
	}

	private static int getDay(String date)// assumes date format 'YYYY-MM-DD HH:mm:ss'
	{
		return Integer.parseInt(date.substring(8, 10));
	}

	public static boolean checkDate(int year, int month, int day, String dateOfOrder) {
		if(getYear(dateOfOrder) > year)
			return true;
		else if(getYear(dateOfOrder) < year)
			return false;
		else {
			if(getMonth(dateOfOrder) > month)
				return true;
			else if(getMonth(dateOfOrder) < month)
				return false;
			else {
				if(getDay(dateOfOrder) >= day)
					return true;
				else
					return false;
			}
		}
	}


}