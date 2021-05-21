Feature: api login

  Background: 
    Given connection to client 'test_service'
    And delete username 'jean.dupond@gmail.com'

  Scenario: create login
    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And create service login for application 'test'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "0bdb83b1-ddba-470d-a2a0-3c06091e909f"
      }
      """
    Then login service status is 201
    And login authorization status is 201
    And login authorization 'jean.dupond@gmail.com' exists
    And login service 'jean.dupond@gmail.com' exists
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user' with scopes 'login:update login:read login:delete'
    And connection status is 200
    And login authorization 'jean.dupond@gmail.com' is
      """
      {
          "disabled": false,
          "accountLocked": false,
          "roles": []
          
      }
      """
    And login service 'jean.dupond@gmail.com' is
      """
      {
          "id": "0bdb83b1-ddba-470d-a2a0-3c06091e909f"
      }
      """

  Scenario: create multi login
    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And create service login for application 'test'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "404b2983-3cdc-4c58-8198-35e2fc9d09cf"
      }
      """
    And delete username 'jean.dupont@gmail.com'
    And create authorization login 'jean.dupont@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And create service login for application 'test'
      """
      {
          "username": "jean.dupont@gmail.com",
          "id": "404b2983-3cdc-4c58-8198-35e2fc9d09cf"
      }
      """
    And login authorization 'jean.dupond@gmail.com' exists
    And login service 'jean.dupond@gmail.com' exists
    And login authorization 'jean.dupont@gmail.com' exists
    And login service 'jean.dupont@gmail.com' exists
    When connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user' with scopes 'login:update login:read login:delete'
    Then connection status is 200
    And login service 'jean.dupont@gmail.com' is
      """
      {
          "id": "404b2983-3cdc-4c58-8198-35e2fc9d09cf"
      }
      """
    And login service 'jean.dupond@gmail.com' is
      """
      {
          "id": "404b2983-3cdc-4c58-8198-35e2fc9d09cf"
      }
      """
    And login authorization 'jean.dupond@gmail.com' is
      """
      {
          "disabled": false,
          "accountLocked": false,
          "roles": []
      }
      """
    And connection with username 'jean.dupont@gmail.com' and password 'mdp' to client 'test_service_user'
    And login authorization 'jean.dupont@gmail.com' is
      """
      {
          "disabled": false,
          "accountLocked": false,
          "roles": []
      }
      """

  Scenario: delete login
    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And create service login for application 'test'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "636325d4-4d23-419f-b92b-0090502bb965"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    When delete login 'jean.dupond@gmail.com'
    Then login authorization status is 204
    And login service status is 204
    But login authorization 'jean.dupond@gmail.com' not exists
    But login service 'jean.dupond@gmail.com' not exists

  Scenario: create login fails because username already exists
    When create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And create service login for application 'test'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "58ee6563-63a1-448a-b845-7369c86e6c19"
      }
      """
    And create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And create service login for application 'test'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "58ee6563-63a1-448a-b845-7369c86e6c19"
      }
      """
    Then login authorization status is 403
    And login service status is 400
    And login service error is
      """
      [{
          "path": "/username",
          "code": "username",
          "message": "[jean.dupond@gmail.com] already exists"
      }]
      """

  Scenario: change password
    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And login authorization status is 201
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200
    When put login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp123"
      }
      """
    Then login authorization status is 204
    And connection with username 'jean.dupond@gmail.com' and password 'mdp123' to client 'test_service_user'
    And connection status is 200
    And login authorization 'jean.dupond@gmail.com' is
      """
      {
          "disabled": false,
          "accountLocked": false,
          "roles": []
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 401

  Scenario: disconnection
    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp",
          "roles" : ["ROLE_ACCOUNT"]
      }
      """
    And create service login for application 'test'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "11326617-c0a2-49d0-a358-5c5785c7b855"
      }
      """
    And login authorization status is 201
    And login service status is 201
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200
    And get service login 'jean.dupond@gmail.com' for application 'test'
    And login service status is 200
    And get authorization login 'jean.dupond@gmail.com' for application 'test'
    And login authorization status is 200
    When disconnection from application 'test'
    Then connection status is 204
    And get service login 'jean.dupond@gmail.com' for application 'test'
    And login service status is 401
    And get authorization login 'jean.dupond@gmail.com' for application 'test'
    And login authorization status is 403

  Scenario: access login is forbidden
    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And create service login for application 'test'
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "71df872d-7b1f-4d14-bc71-79b76e9a6485"
      }
      """
    And login service status is 201
    And login authorization status is 201
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200
    When get service login 'jean.dupont@gmail.com' for application 'test'
    And get authorization login 'jean.dupont@gmail.com' for application 'test'
    Then login service status is 403
    And login authorization status is 403

  Scenario: change username
    Given delete username 'jean.dupont@gmail.com'
    And create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And login authorization status is 201
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200
    When copy authorization login for application 'test'
      """
      {
          "fromUsername": "jean.dupond@gmail.com",
          "toUsername": "jean.dupont@gmail.com"
      }
      """
    And delete login 'jean.dupond@gmail.com'
    Then login authorization status is 204
    And connection with username 'jean.dupont@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 401

  Scenario: forgotten password
    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And login authorization status is 201
    And connection to client 'test_admin'
    When new password for 'jean.dupond@gmail.com' from application 'admin'
    Then connection status is 200
    And put login 'jean.dupond@gmail.com' for application 'admin'
      """
      {
          "password": "mdp123"
      }
      """
    And login authorization status is 204
    And connection with username 'jean.dupond@gmail.com' and password 'mdp123' to client 'test_service_user'
    And connection status is 200

  Scenario: copy username
    Given delete username 'jean.dupont@gmail.com'
    And create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And login authorization status is 201
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200
    When copy authorization login for application 'test'
      """
      {
          "fromUsername": "jean.dupond@gmail.com",
          "toUsername": "jean.dupont@gmail.com"
      }
      """
    Then login authorization status is 201
    And connection with username 'jean.dupont@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200

  Scenario: copy username fails because username already exists
    Given delete username 'jean.dupont@gmail.com'
    And create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And create authorization login 'jean.dupont@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200
    When copy authorization login for application 'test'
      """
      {
          "fromUsername": "jean.dupond@gmail.com",
          "toUsername": "jean.dupont@gmail.com"
      }
      """
    Then login authorization status is 400
    And login authorization error is
      """
      [{
          "path": "/toUsername",
          "code": "username",
          "message": "[jean.dupont@gmail.com] already exists"
      }]
      """

  Scenario: copy username fails because username not found
    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200
    And delete username 'jean.dupond@gmail.com'
    When copy authorization login for application 'test'
      """
      {
          "fromUsername": "jean.dupond@gmail.com",
          "toUsername": "jean.dupont@gmail.com"
      }
      """
    Then login authorization status is 400
    And login authorization error is
      """
      [{
          "path": "/fromUsername",
          "code": "not_found",
          "message": "[jean.dupond@gmail.com] not found"
      }]
      """

  Scenario: disable login
    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp"
      }
      """
    And login authorization status is 201
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 200
    When put login 'jean.dupond@gmail.com' for application 'test'
      """
      {
          "password": "mdp",
          "disabled": true
      }
      """
    Then login authorization status is 204
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
    And connection status is 400
    And connection error is
      """
      {
          "error": "invalid_grant",
          "error_description": "User is disabled"
      }
      """
