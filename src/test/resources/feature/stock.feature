Feature: api stock

  Background: 
    Given get access for username 'admin' and password 'admin123' to client 'test_back_user'

  Scenario: increase stock
    When increase of 5 for product 'product1' from store 'store'
    And increase of 20 for product 'product1' from store 'store'
    And increase of -12 for product 'product1' from store 'store'
    Then stock of product 'product1' from store 'store' is 13

  Scenario: descrease stock fails because stock is insufficient
    When increase of 5 for product 'product1' from store 'store'
    And increase of -10 for product 'product1' from store 'store'
    Then stock of product 'product1' from store 'store' is 5, is insufficient for -10

  Scenario: get stock fails because none stock exists
    When get stock of product 'product1' from store 'store'
    Then stock of product 'product1' from store 'store' is unknown
