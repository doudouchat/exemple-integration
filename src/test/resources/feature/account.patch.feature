Feature: api patch account

  Background: 
    Given connection to client 'test_service'
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
    And create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And account status is 201
    And login authorization status is 201
    And create service login for application 'test' and last id
      """
      {
          "username": "jean.dupond@gmail.com"
      }
      """
    And login service status is 201
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200

  Scenario: patch account
    When patch account for application 'test' and version 'v1'
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
    Then account status is 204
    And account exists
    And account 'creation_date' exists
    And account 'update_date' exists
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

  Scenario: patch account fails because birthday is incorrect
    When patch account for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/birthday",
           "value": "2019-02-30"
         }
      ]
      """
    Then account status is 400
    And account error is
      """
      [{
          "path": "/birthday",
          "code": "format"
      }]
      """

  Scenario: patch account fails because creation_date is incorrect
    When patch account for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/creation_date",
           "value": "2019-02-30T19:16:40Z"
         }
      ]
      """
    Then account status is 400
    And account error contains 2 errors
    And account error contains
      """
      {"path":"/creation_date","code":"format"}
      """
    And account error contains
      """
      {"path":"/creation_date","code":"readOnly"}
      """

  Scenario: patch account fails because creation_date is readonly
    When patch account for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/creation_date",
           "value": "2019-02-25T19:16:40Z"
         }
      ]
      """
    Then account status is 400
    And account error is
      """
      [{
          "path": "/creation_date",
          "code": "readOnly"
      }]
      """

  Scenario: patch account fails access is forbidden
    When patch account 5aa2387a-cf87-4163-902e-69a6706708b8 for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/birthday",
           "value": "2000-02-01"
         }
      ]
      """
    Then account status is 403
