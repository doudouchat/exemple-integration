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
    Given create account
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
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    When get account for application 'default' and version 'v1'
    Then account is denied
    
  Scenario: disconnection
    Given create account
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
          "password": "mdp",
          "roles" : ["ROLE_ACCOUNT"]
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
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
    Then get account
    And account is unauthorized
