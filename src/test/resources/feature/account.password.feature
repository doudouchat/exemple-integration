Feature: api password

  Background: 
    Given connection to client 'test_service'
    And delete username 'jean.dupond@gmail.com'

  Scenario: change password
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
    When put login 'jean.dupond@gmail.com'
      """
      {
          "password": "mdp123"
      }
      """
    And get access for username 'jean.dupond@gmail.com' and password 'mdp123'
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
    And connection to client 'test_admin'
    When new password for 'jean.dupond@gmail.com' from application 'admin'
    And put login 'jean.dupond@gmail.com' for application 'admin'
      """
      {
          "password": "mdp123"
      }
      """
    And get access for username 'jean.dupond@gmail.com' and password 'mdp123'
    And account is
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
