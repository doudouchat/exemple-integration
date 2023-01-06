Feature: api account security

  Background: 
    Given connection to client 'test_service' and scopes
      |account:create|login:create|ROLE_APP|
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
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user' and application 'test' and scopes
      |account:read|
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
    When login with username 'jean.dupond@gmail.com' and password 'mdp123' to application 'test'
    Then login is unauthorized
