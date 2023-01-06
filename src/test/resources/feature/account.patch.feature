Feature: api patch account

  Background: 
    Given connection to client 'test_service' and scopes
      |account:create|login:create|ROLE_APP|
    And delete username 'jean.dupond@gmail.com'
    And account
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
    And create authorization login 'jean.dupond@gmail.com'
      """
      {
          "password": "mdp"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user' and application 'test' and scopes
      |account:read|account:update|login:head|login:create|login:update|

  Scenario: patch account
    When patch account
      """
      [
         {
           "op": "replace",
           "path": "/lastname",
           "value": "Dupond"
         },
         {
           "op": "replace",
           "path": "/addresses/job",
           "value": {"city" : "New-York", "street": "5th avenue"}
         },
         {
           "op": "replace",
           "path": "/addresses/home/city",
           "value": "Paris"
         },
         {
           "op": "remove",
           "path": "/civility"
         },
         {
           "op": "add",
           "path": "/cgus/0",
           "value": {"code" : "code_1", "version": "v1"}
         }
      ]
      """
    Then account is
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
    And account property 'creation_date' exists
    And account property 'update_date' exists
    
  Scenario: change email
    Given delete username 'jean.dupont@gmail.com'
    When patch account
      """
      [
         {
           "op": "replace",
           "path": "/email",
           "value": "jean.dupont@gmail.com"
         }
      ]
      """
    And move authorization login from 'jean.dupond@gmail.com' to 'jean.dupont@gmail.com'
    And account 'jean.dupont@gmail.com' exists
    And connection to client 'test_service' and scopes
      |ROLE_APP|
    And connection with username 'jean.dupont@gmail.com' and password 'mdp' to client 'test_service_user' and application 'test' and scopes
      |account:read|login:head|
    Then account is
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
          "email": "jean.dupont@gmail.com",
          "lastname": "Dupont"
      }
      """
    And account 'jean.dupond@gmail.com' not exists
    
  Scenario: patch account fails because birthday is incorrect
    When patch account
      """
      [
         {
           "op": "replace",
           "path": "/birthday",
           "value": "2019-02-30"
         }
      ]
      """
    Then account error only contains
      """
      {
          "path": "/birthday",
          "code": "format"
      }
      """

  Scenario: patch account fails because creation_date is incorrect
    When patch account
      """
      [
         {
           "op": "replace",
           "path": "/creation_date",
           "value": "2019-02-30T19:16:40Z"
         }
      ]
      """
    Then account error contains 2 errors
    And account error contains
      """
      {"path":"/creation_date","code":"format"}
      """
    And account error contains
      """
      {"path":"/creation_date","code":"readOnly"}
      """

  Scenario: patch account fails because creation_date is readonly
    When patch account
      """
      [
         {
           "op": "replace",
           "path": "/creation_date",
           "value": "2019-02-25T19:16:40Z"
         }
      ]
      """
    Then account error only contains
      """
      {
          "path": "/creation_date",
          "code": "readOnly"
      }
      """

  Scenario: patch account fails because creation_date is required
    When patch account
      """
      [
         {
           "op": "remove",
           "path": "/creation_date"
         }
      ]
      """
    Then account error only contains
      """
      {
          "path": "/creation_date",
          "code": "required"
      }
      """

  Scenario: patch account fails because a property is unknown
    When patch account
      """
      [
         {
           "op": "add",
           "path": "/unknown",
           "value": "nc"
         }
      ]
      """
    Then account error only contains
      """
      {
          "path": "/unknown",
          "code": "additionalProperties"
      }
      """

  Scenario: patch account fails because an address is incomplete
    When patch account
      """
      [
         {
           "op": "add",
           "path": "/addresses/job",
           "value": {"city": "Paris"}
         }
      ]
      """
    Then account error only contains
      """
      {
          "path": "/addresses/job/street",
          "code": "required"
      }
      """

  Scenario: patch account fails because two many addresses
    When patch account
      """
      [
         {
           "op": "add",
           "path": "/addresses/holidays_1",
           "value": {"city": "Paris", "street": "1 rue de la paix"}
         },
         {
           "op": "add",
           "path": "/addresses/holidays_2",
           "value": {"city": "Paris", "street": "2 rue de la paix"}
         },
         {
           "op": "add",
           "path": "/addresses/holidays_3",
           "value": {"city": "Paris", "street": "3 rue de la paix"}
         }
      ]
      """
    Then account error only contains
      """
      {
          "path": "/addresses",
          "code": "maxProperties"
      }
      """

  Scenario: patch account fails because username already exists
    Given connection to client 'test_service' and scopes
      |account:create|login:create|ROLE_APP|
    And delete username 'jean.dupont@gmail.com'
    And account
      """
      {
          "birthday": "1967-06-15",
          "firstname": "Jean",
          "email": "jean.dupont@gmail.com",
          "lastname": "Dupont"
      }
      """
    And create authorization login 'jean.dupont@gmail.com'
      """
      {
          "password": "mdp"
      }
      """
    And connection with username 'jean.dupont@gmail.com' and password 'mdp' to client 'test_service_user' and application 'test' and scopes
      |account:update|
    When patch account
      """
      [
         {
           "op": "replace",
           "path": "/email",
           "value": "jean.dupond@gmail.com"
         }
      ]
      """
    Then account error only contains
      """
      {
          "code": "username",
          "message": "[jean.dupond@gmail.com] already exists"
      }
      """

  Scenario: change email fails because username already exists
    Given delete username 'jean.dupont@gmail.com'
    And create authorization login 'jean.dupont@gmail.com'
      """
      {
          "password": "mdp"
      }
      """
    And patch account
      """
      [
         {
           "op": "replace",
           "path": "/email",
           "value": "jean.dupont@gmail.com"
         }
      ]
      """
    When move authorization login from 'jean.dupond@gmail.com' to 'jean.dupont@gmail.com'
    Then authorization error only contains
      """
      {
          "path": "/toUsername",
          "code": "username",
          "message": "[jean.dupont@gmail.com] already exists"
      }
      """

  Scenario: change email fails because username is not found
    Given delete username 'jean.dupont@gmail.com'
    And patch account
      """
      [
         {
           "op": "replace",
           "path": "/email",
           "value": "jean.dupont@gmail.com"
         }
      ]
      """
    And delete username 'jean.dupond@gmail.com'
    When move authorization login from 'jean.dupond@gmail.com' to 'jean.dupont@gmail.com'
    Then authorization error only contains
      """
      {
         "path":"/fromUsername",
         "code":"not_found",
         "message":"[jean.dupond@gmail.com] not found"
      }      
      """