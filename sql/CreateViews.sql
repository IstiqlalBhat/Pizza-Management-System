USE PIZZERIA;
CREATE OR REPLACE VIEW ToppingPopularity AS 
SELECT 
  topping.ToppingName as Topping, 
  count(pizzatopping.PizzatoppingToppingID) + sum(COALESCE(pizzatopping.PizzatoppingIsDouble, 0)) as ToppingCount
FROM 
  pizzatopping 
RIGHT JOIN 
  topping ON pizzatopping.PizzatoppingToppingID = topping.ToppingID
GROUP BY 
  topping.ToppingName
ORDER BY 
  ToppingCount DESC;

CREATE OR REPLACE VIEW ProfitByPizza AS 
SELECT 
  basepizza.BasePizzaSize AS 'Size',
  basepizza.BasePizzaCrustType AS 'Crust',
  ROUND(SUM(pizza.PizzaCustomerPrice - pizza.PizzaBusinessPrice), 2) AS 'Profit',
  DATE_FORMAT(MAX(ordert.OrdertTimeStamp), '%c/%Y') AS 'OrderMonth' 
FROM basepizza 
JOIN pizza ON basepizza.BasePizzaSize = pizza.PizzaSize 
AND basepizza.BasePizzaCrustType = pizza.PizzaCrustType
JOIN ordert ON pizza.PizzaOrderID = ordert.OrdertID
GROUP BY basepizza.BasePizzaSize, basepizza.BasePizzaCrustType
ORDER BY Profit DESC;



CREATE OR REPLACE VIEW ProfitByOrderType AS
SELECT OrdertType AS CustomerType, DATE_FORMAT (OrdertTimeStamp, '%c/%Y') AS OrderMonth ,
SUM(OrdertCustomerPrice) AS TotalOrderPrice, SUM(OrdertBusinessPrice) AS TotalOrderCost, SUM(OrdertCustomerPrice)-SUM(OrdertBusinessPrice) AS Profit FROM ordert GROUP BY CustomerType,OrderMonth
UNION
SELECT '', 'Grand Total',SUM(OrdertCustomerPrice), SUM(OrdertBusinessPrice), SUM(OrdertCustomerPrice)-SUM(OrdertBusinessPrice) FROM ordert;




select * from ToppingPopularity;
select * from ProfitByPizza;
select * from ProfitByOrderType;
