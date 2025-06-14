package cpsc4620;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/*
 * This file is where the front end magic happens.
 *
 * You will have to write the methods for each of the menu options.
 *
 * This file should not need to access your DB at all, it should make calls to the DBNinja that will do all the connections.
 *
 * You can add and remove methods as you see necessary. But you MUST have all of the menu methods (including exit!)
 *
 * Simply removing menu methods because you don't know how to implement it will result in a major error penalty (akin to your program crashing)
 *
 * Speaking of crashing. Your program shouldn't do it. Use exceptions, or if statements, or whatever it is you need to do to keep your program from breaking.
 *
 */

public class Menu {

	public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public static void main(String[] args) throws SQLException, IOException {

		System.out.println("Welcome to Pizzas-R-Us!");

		int menu_option = 0;
		PrintMenu();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String option = reader.readLine();
		menu_option = Integer.parseInt(option);

		while (menu_option != 9) {
			switch (menu_option) {
				case 1:// enter order
					EnterOrder();
					break;
				case 2:// view customers
					viewCustomers();
					break;
				case 3:// enter customer
					EnterCustomer();
					break;
				case 4:// view order
					// open/closed/date
					ViewOrders();
					break;
				case 5:// mark order as complete
					MarkOrderAsComplete();
					break;
				case 6:// view inventory levels
					ViewInventoryLevels();
					break;
				case 7:// add to inventory
					AddInventory();
					break;
				case 8:// view reports
					PrintReports();
					break;
			}
			PrintMenu();
			option = reader.readLine();
			menu_option = Integer.parseInt(option);
		}

	}

	public static boolean CheckRegax(String regex, String input) {
		if(input.length()==0)
			return false;
		Pattern r = Pattern.compile(regex);
		Matcher m = r.matcher(input);
		if(m.results().count() != 0)
			return true;
		else
			return false;
	}

	// allow for a new order to be placed
	public static void EnterOrder() throws SQLException, IOException {

		/*
		 * EnterOrder should do the following:
		 *
		 * Ask if the order is delivery, pickup, or dinein
		 *   if dine in....ask for table number
		 *   if pickup...
		 *   if delivery...
		 *
		 * Then, build the pizza(s) for the order (there's a method for this)
		 *  until there are no more pizzas for the order
		 *  add the pizzas to the order
		 *
		 * Apply order discounts as needed (including to the DB)
		 *
		 * return to menu
		 *
		 * make sure you use the prompts below in the correct order!
		 */



		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String timestamp=dtf.format(now);
		String ordertype=null;
		String choice = "N";
		boolean validInp = false;
		String regex = "^([YyNn]?)$";



			System.out.println("Is this order for an existing customer? Answer y/n: ");
			choice = reader.readLine().trim();


		String address="";

		int customerID=0;
		if(choice.equals("N") || choice.equals("n")) {
			Menu.EnterCustomer();
			ArrayList<Customer> cus=new ArrayList<Customer>();
			cus=DBNinja.getCustomerList();
			customerID=cus.get(cus.size()-1).getCustID();
			address=cus.get(cus.size()-1).getAddress();



		} else {
			System.out.println("Here is the list of existing customers: ");
			viewCustomers();
			System.out.println("Which customer is this order for? Enter customer ID: ");
			customerID = Integer.parseInt(reader.readLine());
		}
		System.out.println("What type of order is this?");
		choice = "1";
		int type = 1;
		validInp = false;
		regex = "^([1-3]?)$";
		while(!validInp) {
			System.out.println("1. Dine-in\n2. Pick-up\n3. Delivery\nEnter Here:");
			choice = reader.readLine();
			validInp = CheckRegax(regex, choice);
			if(validInp) {
				validInp = true;
				type = Integer.parseInt(choice);
			} else
				System.out.println("Provide only valid input");
		}
		Order o=null;
		if(type==1){
			System.out.println("Enter the table number  ");
			Integer tableNumber = Integer.parseInt(reader.readLine());

			o=new DineinOrder(0,customerID,timestamp,0.0,0.0,0,tableNumber);
		} else if(type==2){
			ordertype="pickup";
			o=new PickupOrder(0,customerID,timestamp,0.0,0.0,1,0);
		} else {
			String fulladdress="";
			if(Objects.equals(address, "")){
				System.out.println("What is the StreetNo");
			String customerStreet=reader.readLine();
			System.out.println("What is the City)");
			String customerCity=reader.readLine();
			System.out.println("What is the state)");
			String customerState=reader.readLine();
			System.out.println("What is the Pincode)");
			String customerPinCode=reader.readLine();
			fulladdress=customerStreet+","+customerCity+","+customerState+","+customerPinCode;
			}

			o=new DeliveryOrder(0,customerID,timestamp,0.0,0.0,0,fulladdress);
		}



		int maxOrderID=DBNinja.getLastOrder().getOrderID();
		maxOrderID++;



		DBNinja.addOrder(o);




		double pricetocustomer=0.0;
		double pricetobusiness=0.0;
		System.out.println("Let's Build a pizza");
		int flag = 1;
		Pizza p=null;
		while(flag != -1) {
			p=Menu.buildPizza(maxOrderID);

			DBNinja.addPizza(p);
			pricetocustomer=pricetocustomer+p.getCustPrice();
			pricetobusiness=pricetobusiness+p.getBusPrice();
			o.setCustPrice(pricetocustomer);
			o.setBusPrice(pricetobusiness);
			o.setOrderID(maxOrderID);
			System.out.println("Enter -1 to stop adding pizzas...Enter anything else to continue adding pizzas to the order.");
			flag = Integer.parseInt(reader.readLine());


		}

		System.out.println("Do you want to add discounts to this order? Enter (Y/N):");
		String ordDischoice = reader.readLine();
		if (ordDischoice.equals("Y") || ordDischoice.equals("y")) {
			System.out.println("Getting discount list...");
			int discountflag = 1;
			while (discountflag != -1) {
				ArrayList<Discount> discorder=new ArrayList<Discount>();
				Discount d1=null;
				discorder=DBNinja.getDiscountList();
				for (Discount discount:discorder) {
					System.out.println(discount.toString());
				}
				System.out.println("Select the required discount from the available list. Enter DiscountID. Enter -1 to stop adding discounts:");
				int DiscountID = Integer.parseInt(reader.readLine());
				double custPrice=o.getCustPrice();

				if (DiscountID != -1) {
					for (Discount discount:discorder) {
						if(discount.getDiscountID()==DiscountID){
							d1=discount;
						}
					}
					if(d1.isPercent()) {
						o.setCustPrice(custPrice-((custPrice*d1.getAmount())/100));
					} else {
						o.setCustPrice(custPrice - d1.getAmount());

					}


					DBNinja.useOrderDiscount(o,d1);

				} else
					discountflag = -1;
			}
		}
		o.setOrderID(maxOrderID);
		DBNinja.addOrder(o);

		System.out.println("Finished adding order...Returning to menu...");


	}


	public static void viewCustomers() {
		/*
		 * Prints out all of the customers from the database.
		 */
		try {
			ArrayList<Customer> customers = DBNinja.getCustomerList();  // Retrieve the list of customers
			if (customers != null && !customers.isEmpty()) {  // Check if the list is not null and not empty
				for (Customer customer : customers) {
					System.out.println(customer.toString());  // Print each customer's details
				}
			} else {
				System.out.println("No customers found.");  // Handle the case where no customers are returned
			}
		} catch (SQLException e) {
			System.err.println("SQL Exception occurred while fetching customers: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception occurred while fetching customers: " + e.getMessage());
			e.printStackTrace();
		}
	}



	// Enter a new customer in the database
	public static void EnterCustomer() throws SQLException, IOException {
		/*
		 * Ask for the name of the customer:
		 *   First Name <space> Last Name
		 *
		 * Ask for the  phone number.
		 *   (##########) (No dash/space)
		 *
		 * Once you get the name and phone number, add it to the DB
		 */

		// User Input Prompts...
		System.out.println("What is this customer's name (first <space> last");
		String fullName = reader.readLine();
		String[] names = fullName.split(" ",2);
		System.out.println("What is this customer's phone number (##########) (No dash/space)");
		String phoneNo = reader.readLine();








		Customer customer=new Customer(0,names[0],names[1],phoneNo);
		try {
			DBNinja.addCustomer(customer);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	// View any orders that are not marked as completed
	public static void ViewOrders() throws SQLException, IOException {
		/*
		 * This method allows the user to select between three different views of the Order history:
		 * The program must display:
		 * a.	all open orders
		 * b.	all completed orders
		 * c.	all the orders (open and completed) since a specific date (inclusive)
		 *
		 * After displaying the list of orders (in a condensed format) must allow the user to select a specific order for viewing its details.
		 * The details include the full order type information, the pizza information (including pizza discounts), and the order discounts.
		 *
		 */
		System.out.println("Would you like to:\n(a) display all orders [open or closed]\n(b) display all open orders\n(c) display all completed [closed] orders\n(d) display orders since a specific date");
		String choice =  reader.readLine();
		try  {
			ArrayList<Order> orders = null;
			switch (choice.toUpperCase()) {
				case "A":
					orders = DBNinja.getOrders(false);
					if (orders.isEmpty()) {
						System.out.println("No orders to display, returning to menu.");
						break;
					}
					for (Order order : orders) {
						System.out.println(order.toSimplePrint());
					}
					System.out.println("Which order would you like to see in detail? Enter the number (-1 to exit): ");
					int orderID = Integer.parseInt(reader.readLine());
					if (orderID == -1) {
						break;
					}
					Map<Integer, Order> resultMapA = orders.stream().collect(Collectors.toMap(x -> x.getOrderID(), x -> x));
					resultMapA.get(orderID);
					System.out.println(resultMapA.get(orderID).toString());
					break;

				case "B":
					orders = DBNinja.getOrders(true);
					if (orders.isEmpty()) {
						System.out.println("No orders to display, returning to menu.");
						break;
					}
					for (Order order : orders) {
						System.out.println(order.toSimplePrint());
					}
					System.out.println("Which order would you like to see in detail? Enter the number (-1 to exit): ");
					int orderID_B = Integer.parseInt(reader.readLine());
					if (orderID_B == -1) {
						break;
					}
					Map<Integer, Order> resultMapB = orders.stream().collect(Collectors.toMap(x -> x.getOrderID(), x -> x));
					resultMapB.get(orderID_B);
					System.out.println(resultMapB.get(orderID_B).toString());
					break;

				case "C":
					orders = DBNinja.getCompletedOrders();
					if (orders.isEmpty()) {
						System.out.println("No orders to display, returning to menu.");
						break;
					}
					for (Order order : orders) {
						System.out.println(order.toSimplePrint());
					}
					System.out.println("Which order would you like to see in detail? Enter the number (-1 to exit): ");
					int orderID_C = Integer.parseInt(reader.readLine());
					if (orderID_C == -1) {
						break;
					}
					Map<Integer, Order> resultMapC = orders.stream().collect(Collectors.toMap(x -> x.getOrderID(), x -> x));
					resultMapC.get(orderID_C);
					System.out.println(resultMapC.get(orderID_C).toString());
					break;

				case "D":
					System.out.println("What is the date you want to restrict by? (FORMAT= YYYY-MM-DD)");
					String dateString = reader.readLine();
					try {
						LocalDate parsedDate = LocalDate.parse(dateString);
					} catch (DateTimeParseException e) {
						System.out.println("I don't understand that input, returning to menu");
						break;
					}
					orders = DBNinja.getOrdersByDate(dateString);
					if (orders.isEmpty()) {
						System.out.println("No orders to display, returning to menu.");
						break;
					}
					for (Order order : orders) {
						System.out.println(order.toSimplePrint());
					}
					System.out.println("Which order would you like to see in detail? Enter the number (-1 to exit): ");
					int orderID_D = Integer.parseInt(reader.readLine());
					if (orderID_D == -1) {
						break;
					}
					Map<Integer, Order> resultMapD = orders.stream().collect(Collectors.toMap(x -> x.getOrderID(), x -> x));
					resultMapD.get(orderID_D);
					System.out.println(resultMapD.get(orderID_D).toString());
					break;

				default:
					System.out.println("I don't understand that input... returning to menu...");
			}
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// User Input Prompts...


	}


	// When an order is completed, we need to make sure it is marked as complete
	public static void MarkOrderAsComplete() throws SQLException, IOException {
		/*
		 * All orders that are created through java (part 3, not the orders from part 2) should start as incomplete
		 *
		 * When this method is called, you should print all of the "opoen" orders marked
		 * and allow the user to choose which of the incomplete orders they wish to mark as complete
		 *
		 */
		ArrayList<Order> orders = DBNinja.getOrders(true);
		if(!orders.isEmpty()) {
			for (Order order : orders) {
				System.out.println(order.toSimplePrint());
			}


			System.out.println("Which order would you like mark as complete? Enter the OrderID: ");
			Integer orderId = Integer.parseInt(reader.readLine());
			Order order = null;


			for (Order o:orders) {
				if (o.getOrderID() == orderId) {
					order = o;

				}
			}

			if(order==null){
				System.out.println("Incorrect entry, not an option");
				return;
			}

			DBNinja.completeOrder(order);
		} else{
			System.out.println("There are no open orders currently... returning to menu...");

		}

	}

	public static void ViewInventoryLevels() throws SQLException, IOException {
		/*
		 * Print the inventory. Display the topping ID, name, and current inventory
		 */
		DBNinja.printInventory();


	}


	public static void AddInventory() throws SQLException, IOException {
		/*
		 * This should print the current inventory and then ask the user which topping (by ID) they want to add more to and how much to add
		 */
		ArrayList<Topping> toppings = null;
		System.out.println("Current Inventory Levels:");
		Menu.ViewInventoryLevels();

		System.out.println("Which topping do you want to add inventory to? Enter the number: ");
		int toppingID = Integer.parseInt(reader.readLine());

		toppings=DBNinja.getToppingList();

		Topping t= null;
		for(Topping topping:toppings){
			if(topping.getTopID()==toppingID){
				t=topping;
			}
		}
		if(t==null){
			System.out.println("Incorrect entry, not an option");
			return;
		}
		System.out.println("How many units would you like to add? ");
		double quantity = Float.parseFloat(reader.readLine());
		DBNinja.addToInventory(t,quantity);


		// User Input Prompts...


	}

	// A method that builds a pizza. Used in our add new order method
	public static Pizza buildPizza(int orderID) throws SQLException, IOException {

		/*
		 * This is a helper method for first menu option.
		 *
		 * It should ask which size pizza the user wants and the crustType.
		 *
		 * Once the pizza is created, it should be added to the DB.
		 *
		 * We also need to add toppings to the pizza. (Which means we not only need to add toppings here, but also our bridge table)
		 *
		 * We then need to add pizza discounts (again, to here and to the database)
		 *
		 * Once the discounts are added, we can return the pizza
		 */
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String timestamp=dtf.format(now);
		String choice = "";
		boolean validInp = false;
		String regex = "^([YyNn]?)$";
		Pizza pi=null;

		ArrayList<Integer> toppingList = new ArrayList<>();
		boolean[] isToppingDouble = new boolean[17];
		regex = "^[1-4]?$";
		validInp = false;
		int s=1;

			System.out.println("What size is the pizza?");
			System.out.println("1."+DBNinja.size_s);
			System.out.println("2."+DBNinja.size_m);
			System.out.println("3."+DBNinja.size_l);
			System.out.println("4."+DBNinja.size_xl);
			System.out.println("Enter the corresponding number: ");
			s = Integer.parseInt(reader.readLine());;

			try {


				String size = null;
				String crust = null;
				if (s == 1)
					size = "Small";
				else if (s == 2)
					size = "Medium";
				else if (s == 3)
					size = "Large";
				else if (s == 4)
					size = "XLarge";

				regex = "^[1-4]$";
				validInp = false;
				int ch = 1;

					System.out.println("What crust for this pizza?");
					System.out.println("1." + DBNinja.crust_thin);
					System.out.println("2." + DBNinja.crust_orig);
					System.out.println("3." + DBNinja.crust_pan);
					System.out.println("4." + DBNinja.crust_gf);
					System.out.println("Enter the corresponding number: ");
					ch = Integer.parseInt(reader.readLine());



				if (ch == 1)
					crust = "Thin";
				else if (ch == 2)
					crust = "Original";
				else if (ch == 3)
					crust = "Pan";
				else if (ch == 4)
					crust = "Gluten-Free";

				double basepricecustomer = DBNinja.getBaseCustPrice(size, crust);
				double basepricebusiness = DBNinja.getBaseBusPrice(size, crust);
				System.out.println(basepricebusiness);


				int pizzaId = DBNinja.getMaxPizzaID();

				pi = new Pizza(0, size, crust, orderID, "Processing", timestamp, basepricecustomer, basepricebusiness);
				DBNinja.addPizza(pi);
				pi.setPizzaID(pizzaId + 1);
				int toppingFlag = 1;
				ArrayList<Topping> toppingsList = new ArrayList<Topping>();
				while (toppingFlag != -1) {
					regex = "^(-1|1[0-7]|[1-9])$";
					validInp = false;
					int toppingID = -1;
					while (!validInp) {
						System.out.println("Available Toppings:");
						ViewInventoryLevels();
						System.out.println("Which topping do you want to add? Enter the TopID. Enter -1 to stop adding toppings: ");
						choice = reader.readLine();
						validInp = CheckRegax(regex, choice);
						if (validInp) {
							validInp = true;
							toppingID = Integer.parseInt(choice);
						} else
							System.out.println("Provide only valid input");

					}
					if (toppingID != -1) {
						Topping t = null;
						toppingsList = DBNinja.getToppingList();
						for (Topping topping : toppingsList) {
							if (topping.getTopID() == toppingID) {
								t = topping; // Return the matching topping object

							}
						}
						System.out.println("Do you want to add extra topping? Enter y/n");
						String damt = reader.readLine();
						if (damt.equals("y") || damt.equals("Y")) {
							pi.addToppings(t, true);
							DBNinja.applyTopping(pi, t, true);


						} else {
							pi.addToppings(t, false);
							DBNinja.applyTopping(pi, t, false);


						}

					} else
						toppingFlag = -1;
				}

				ArrayList<Integer> discountList = new ArrayList<Integer>();

				choice = "N";
				validInp = false;
				regex = "^([YyNn]?)$";
				while (!validInp) {
					System.out.println("Do you want to add discounts to this Pizza? Enter y/n?");
					choice = reader.readLine();
					validInp = CheckRegax(regex, choice);
					if (validInp)
						validInp = true;
					else
						System.out.println("Provide only valid input");
				}
				if (choice.equals("Y") || choice.equals("y")) {

					System.out.println("Getting discount list...");
					int discountflag = 1;
					while (discountflag != -1) {
						ArrayList<Discount> disc = new ArrayList<Discount>();
						Discount d = null;
						disc = DBNinja.getDiscountList();
						for (Discount discount : disc) {
							System.out.println(discount.toString());
						}
						System.out.println("Which Pizza Discount do you want to add? Enter the DiscountID. Enter -1 to stop adding Discounts: ");
						int DiscountID = Integer.parseInt(reader.readLine());
						double custPrice1 = pi.getCustPrice();
						if (DiscountID != -1) {
							for (Discount discount : disc) {
								if (discount.getDiscountID() == DiscountID) {
									d = discount;
								}
							}
							if (d.isPercent()) {
								pi.setCustPrice(custPrice1 - ((custPrice1 * d.getAmount()) / 100));
							} else {
								pi.setCustPrice(custPrice1 - d.getAmount());
								;
							}

//						discountList.add(DiscountID);
							DBNinja.applyDiscountToPizza(pi, d);


						} else
							discountflag = -1;
					}

				}


				Pizza ret = null;

				// User Input Prompts...


			}catch (IOException e){
				System.out.println("Exception"+e.getMessage());
			}



		return pi;
	}


	public static void PrintReports() throws SQLException, NumberFormatException, IOException {
		/*
		 * This method asks the use which report they want to see and calls the DBNinja method to print the appropriate report.
		 *
		 */
		System.out.println("Which report do you wish to print? Enter\n(a) ToppingPopularity\n(b) ProfitByPizza\n(c) ProfitByOrderType:");

		String option = reader.readLine();

		if (option.equals("a")) {
			DBNinja.printToppingPopReport();
		} else if (option.equals("b")) {
			DBNinja.printProfitByPizzaReport();
		} else if (option.equals("c")) {
			DBNinja.printProfitByOrderType();
		} else {
			System.out.println("I don't understand that input... returning to menu...");
		}


		// User Input Prompts...


	}

	//Prompt - NO CODE SHOULD TAKE PLACE BELOW THIS LINE
	// DO NOT EDIT ANYTHING BELOW HERE, THIS IS NEEDED TESTING.
	// IF YOU EDIT SOMETHING BELOW, IT BREAKS THE AUTOGRADER WHICH MEANS YOUR GRADE WILL BE A 0 (zero)!!

	public static void PrintMenu() {
		System.out.println("\n\nPlease enter a menu option:");
		System.out.println("1. Enter a new order");
		System.out.println("2. View Customers ");
		System.out.println("3. Enter a new Customer ");
		System.out.println("4. View orders");
		System.out.println("5. Mark an order as completed");
		System.out.println("6. View Inventory Levels");
		System.out.println("7. Add Inventory");
		System.out.println("8. View Reports");
		System.out.println("9. Exit\n\n");
		System.out.println("Enter your option: ");
	}

	/*
	 * autograder controls....do not modiify!
	 */

	public final static String autograder_seed = "6f1b7ea9aac470402d48f7916ea6a010";


	private static void autograder_compilation_check() {

		try {
			Order o = null;
			Pizza p = null;
			Topping t = null;
			Discount d = null;
			Customer c = null;
			ArrayList<Order> alo = null;
			ArrayList<Discount> ald = null;
			ArrayList<Customer> alc = null;
			ArrayList<Topping> alt = null;
			double v = 0.0;
			String s = "";

			DBNinja.addOrder(o);
			DBNinja.addPizza(p);
			DBNinja.applyTopping(p, t, false);
			DBNinja.applyDiscountToPizza(p, d);
			DBNinja.useOrderDiscount(o, d);
			DBNinja.addCustomer(c);
			DBNinja.completeOrder(o);
			alo = DBNinja.getOrders(false);
			o = DBNinja.getLastOrder();
			alo = DBNinja.getOrdersByDate("01/01/1999");
			ald = DBNinja.getDiscountList();
			d = DBNinja.findDiscountByName("Discount");
			alc = DBNinja.getCustomerList();
			c = DBNinja.findCustomerByPhone("0000000000");
			alt = DBNinja.getToppingList();
			t = DBNinja.findToppingByName("Topping");
			DBNinja.addToInventory(t, 1000.0);
			v = DBNinja.getBaseCustPrice("size", "crust");
			v = DBNinja.getBaseBusPrice("size", "crust");
			DBNinja.printInventory();
			DBNinja.printToppingPopReport();
			DBNinja.printProfitByPizzaReport();
			DBNinja.printProfitByOrderType();
			s = DBNinja.getCustomerName(0);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}


}

