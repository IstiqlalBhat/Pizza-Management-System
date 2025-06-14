DROP DATABASE IF EXISTS PIZZERIA;
CREATE DATABASE PIZZERIA; 
USE PIZZERIA;
CREATE TABLE customer
(
	CustomerID int AUTO_INCREMENT,
    CustomerFirstName varchar(255) NOT NULL,
    CustomerLastName varchar(255) NOT NULL,
    CustomerPhoneNo varchar(15) NOT NULL,
    CustomerEmailID varchar(320) UNIQUE,
    CustomerAptNo VARCHAR(10),
    CustomerStreet VARCHAR(255),
    CustomerCity VARCHAR(255),
    CustomerState VARCHAR(255),
    CustomerPinCode VARCHAR(10),
    PRIMARY KEY(CustomerID)    
); 


CREATE TABLE ordert
(
	OrdertID int AUTO_INCREMENT,
    OrdertCustomerID int NOT NULL,
    OrdertTimeStamp varchar(255) NOT NULL,
    OrdertCustomerPrice decimal(6,2) NOT NULL,
    OrdertBusinessPrice decimal(6,2) NOT NULL,
    OrdertType varchar(255) NOT NULL,
    IsCompleted boolean NOT NULL,
    PRIMARY KEY(OrdertID),
    FOREIGN KEY (OrdertCustomerID) REFERENCES customer(CustomerID)


); 


CREATE TABLE dinein
(
	DineInOrderID int,
    DineInTableNumber int NOT NULL,
    PRIMARY KEY(DineInOrderID),
    FOREIGN KEY(DineInOrderID) REFERENCES ordert(OrdertID)
);

CREATE TABLE pickup
(
	PickUpOrderID int,
    
    PRIMARY KEY(PickUpOrderID),
    FOREIGN KEY(PickUpOrderID) REFERENCES ordert(OrdertID)
);

CREATE TABLE delivery
(
	DeliveryOrderID int,
    PRIMARY KEY(DeliveryOrderID),
    FOREIGN KEY(DeliveryOrderID) REFERENCES ordert(OrdertID)

);

CREATE TABLE discount
(
	DiscountID int AUTO_INCREMENT,
    DiscountType varchar(50),
    DiscountName varchar(255) NOT NULL,
    IsPercent  boolean NOT NULL,
    DiscountValue decimal(6,2) NOT NULL,
    PRIMARY KEY(DiscountID)
);

CREATE TABLE orderdeals
(
	OrderDealsOrderID int,
    OrderDealsDiscountID int,
    PRIMARY KEY(OrderDealsOrderID, OrderDealsDiscountID),
    FOREIGN KEY(OrderDealsOrderID) REFERENCES ordert(OrdertID),
    FOREIGN KEY(OrderDealsDiscountID) REFERENCES discount(DiscountID)
);

CREATE TABLE basepizza
(
    BasePizzaSize varchar(255),
    BasePizzaCrustType varchar(255),
    BasePizzaCustomerPrice decimal(6,2) NOT NULL,
    BasePizzaBusinessPrice decimal(6,2) NOT NULL,
    PRIMARY KEY(BasePizzaSize,BasePizzaCrustType)
);

CREATE TABLE pizza
(
	PizzaID int AUTO_INCREMENT,
    PizzaOrderID int NOT NULL,
    PizzaSize varchar(255) NOT NULL,
    PizzaState varchar(255) NOT NULL, 
    PizzaCustomerPrice decimal(6,2) NOT NULL,
    PizzaBusinessPrice decimal(6,2) NOT NULL,
    PizzaCrustType varchar(255) NOT NULL,
    PRIMARY KEY(PizzaID),
    FOREIGN KEY(PizzaOrderID) REFERENCES ordert(OrdertID),
    FOREIGN KEY(PizzaSize,PizzaCrustType) REFERENCES basepizza(BasePizzaSize,BasePizzaCrustType)
);

CREATE TABLE specialpizzaoffer
(
	SpecialPizzaOfferPizzaID int ,
    SpecialPizzaOfferDiscountID int,
    PRIMARY KEY(SpecialPizzaOfferPizzaId, SpecialPizzaOfferDiscountID),
    FOREIGN KEY(SpecialPizzaOfferPizzaId) REFERENCES pizza(PizzaID),
    FOREIGN KEY(SpecialPizzaOfferDiscountID) REFERENCES discount(DiscountID)
);

CREATE TABLE topping
(
	ToppingID int auto_increment,
    ToppingName varchar(255) NOT NULL,
    ToppingCustomerPrice decimal(6,2) NOT NULL,
    ToppingBusinessPrice decimal(6,2) NOT NULL,
    ToppingQuantityForPersonal decimal(6,2) NOT NULL,
    ToppingQuantityForMediumUnits decimal(6,2) NOT NULL,
    ToppingQuantityForLargeUnits decimal(6,2) NOT NULL,
    ToppingQuantityForXLargeUnits decimal(6,2) NOT NULL,
    ToppingMinInvLvl decimal(6,2),
    ToppingCurrentInvLvl decimal(6,2) NOT NULL,
    PRIMARY KEY(ToppingID)
);

CREATE TABLE pizzatopping
(
	PizzaToppingPizzaID int,
    PizzaToppingToppingID int,
    PizzaToppingIsDouble boolean,
    PRIMARY KEY(PizzaToppingPizzaID, PizzaToppingToppingID),
    FOREIGN KEY(PizzaToppingPizzaID) REFERENCES pizza(PizzaID),
    FOREIGN KEY(PizzaToppingToppingID) REFERENCES topping(ToppingID) 
);