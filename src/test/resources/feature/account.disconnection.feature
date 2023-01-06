Feature: api account security

  Background: 
    Given connection to client 'test_service' and scopes
      |account:create|login:create|ROLE_APP|
    And delete username 'jean.dupond@gmail.com'
    
  Scenario: disconnection
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
    And account is
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
    When disconnection
    Then account is unauthorized