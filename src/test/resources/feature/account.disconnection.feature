Feature: api account security

  Background: 
    Given connection to client 'test_service'
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
    And get access for username 'jean.dupond@gmail.com' and password 'mdp'
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
    And get account
    Then account is denied