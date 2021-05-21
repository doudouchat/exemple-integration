Feature: api put account

  Background: 
    Given connection to client 'test'
    And delete username 'jean.dupond@gmail.com'
    And create account for application 'test' and version 'v1'
      """
      {
          "optin_mobile": true,
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "addresses": {
             "job": {
                 "city": "Paris",
                 "street": "rue de la paix"
             },
             "home": {
                 "city": "Lyon",
                 "street": "rue de la poste"
             }
          },
          "civility": "Mr",
          "mobile": "0610203040",
          "cgus": [
             {
               "code": "code_1",
               "version": "v0"
             }
          ],
          "email": "jean.dupond@gmail.com",
          "lastname": "Dupont"
      }
      """
    And create login for application 'test' and version 'v1' and last id
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 200

  Scenario: put account
    When put account for application 'test' and version 'v1'
      """
      {
         "addresses": {
             "home": {
                 "street": "rue de la poste",
                 "city": "Paris"
             },
             "job": {
                 "street": "5th avenue",
                 "city": "New-York"
             }
         },
         "birthday": "1967-06-15",
         "cgus": [
             {
                 "code": "code_1",
                 "version": "v0"
             },
             {
                 "code": "code_1",
                 "version": "v1"
             }
         ],
         "email": "jean.dupond@gmail.com",
         "firstname": "Jean",
         "lastname": "Dupond",
         "mobile": "0610203040",
         "optin_mobile": true
      }
      """
    Then account status is 204
    And account exists
    And account 'creation_date' exists
    And account 'update_date' exists
    And account 'id' exists
    And account is
      """
      {
          "addresses": {
              "home": {
                  "street": "rue de la poste",
                  "city": "Paris"
              },
              "job": {
                  "street": "5th avenue",
                  "city": "New-York"
              }
          },
          "birthday": "1967-06-15",
          "cgus": [
              {
                  "code": "code_1",
                  "version": "v0"
              },
              {
                  "code": "code_1",
                  "version": "v1"
              }
          ],
          "email": "jean.dupond@gmail.com",
          "firstname": "Jean",
          "lastname": "Dupond",
          "mobile": "0610203040",
          "optin_mobile": true
      }
      """
