Feature: api password

  Background: 
    Given connection to client 'test_service'
    And delete username 'jean.dupond@gmail.com'

  Scenario: change password
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
    When put login 'jean.dupond@gmail.com'
      """
      {
          "password": "mdp123"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp123' to client 'test_service_user'
    And account is
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """

  Scenario: forgotten password
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
    And connection to client 'test_admin'
    When new password for 'jean.dupond@gmail.com' from application 'admin'
    And put login 'jean.dupond@gmail.com' for application 'admin'
      """
      {
          "password": "mdp123"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp123' to client 'test_service_user'
    And account is
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
