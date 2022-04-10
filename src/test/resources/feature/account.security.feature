Feature: api account security

  Background: 
    Given connection to client 'test_service'
    And delete username 'jean.dupond@gmail.com'

  Scenario: create account fails because application not exists
    When create account for application 'default' and version 'v1'
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
    Then account is denied

  Scenario: get account fails because application not exists
    Given account
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
    And create authorization login 'jean.dupond@gmail.com'
      """
      {
          "password": "mdp"
      }
      """
    And get access for username 'jean.dupond@gmail.com' and password 'mdp'
    When get account for application 'default' and version 'v1'
    Then account is denied

  Scenario: connection fails because password is incorrect
    Given account
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
    And create authorization login 'jean.dupond@gmail.com'
      """
      {
          "password": "mdp"
      }
      """
    When connection with username 'jean.dupond@gmail.com' and password 'mdp123'
    Then connection is unauthorized
