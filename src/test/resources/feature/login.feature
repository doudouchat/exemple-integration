Feature: api login

  Background: 
    Given connection to client 'test'
    And delete username 'jean.dupond@gmail.com'

  Scenario: create login
    When create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "4a62b95a-3a0a-4104-baee-7bbce9249c6b"
      }
      """
    Then login status is 201
    And login 'jean.dupond@gmail.com' exists
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user' with scopes 'login:update login:read login:delete'
    And connection status is 200
    And login 'jean.dupond@gmail.com' is
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "4a62b95a-3a0a-4104-baee-7bbce9249c6b"
      }
      """

  Scenario: create multi login
    Given create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "404b2983-3cdc-4c58-8198-35e2fc9d09cf"
      }
      """
    And delete username 'jean.dupont@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupont@gmail.com",
          "password": "mdp",
          "id": "404b2983-3cdc-4c58-8198-35e2fc9d09cf"
      }
      """
    And login 'jean.dupond@gmail.com' exists
    And login 'jean.dupont@gmail.com' exists
    When connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user' with scopes 'login:update login:read login:delete'
    Then connection status is 200
    And login 'jean.dupont@gmail.com' is
      """
      {
          "username": "jean.dupont@gmail.com",
          "id": "404b2983-3cdc-4c58-8198-35e2fc9d09cf"
      }
      """
    And login 'jean.dupond@gmail.com' is
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "404b2983-3cdc-4c58-8198-35e2fc9d09cf"
      }
      """

  Scenario: delete login
    Given create login for application 'test' and version 'v1'
      """
      {
          "password": "mdp",
          "username": "jean.dupond@gmail.com",
          "id": "636325d4-4d23-419f-b92b-0090502bb965"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    When delete login 'jean.dupond@gmail.com'
    Then login status is 204
    But login 'jean.dupond@gmail.com' not exists

  Scenario: create login fails because username already exists
    When create login for application 'test' and version 'v1'
      """
      {
          "password": "mdp",
          "username": "jean.dupond@gmail.com",
          "id": "607a1829-8972-463e-9a91-bc55688edc13"
      }
      """
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "179d5fa8-dabd-4363-9668-cd295fc90a51"
      }
      """
    Then login status is 400
    And login error is
      """
      [{
          "path": "/username",
          "code": "username",
          "message": "[jean.dupond@gmail.com] already exists"
      }]
      """

  Scenario: change password
    Given create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "11326617-c0a2-49d0-a358-5c5785c7b855"
      }
      """
    And login status is 201
    And login 'jean.dupond@gmail.com' exists
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 200
    When patch login 'jean.dupond@gmail.com' for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/password",
           "value": "mdp123"
         }
      ]
      """
    Then login status is 204
    And connection with username 'jean.dupond@gmail.com' and password 'mdp123' to client 'test_user'
    And connection status is 200
    And login 'jean.dupond@gmail.com' is
      """
      {
          "username": "jean.dupond@gmail.com",
          "id": "11326617-c0a2-49d0-a358-5c5785c7b855"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 401

  Scenario: disconnection
    Given create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "11326617-c0a2-49d0-a358-5c5785c7b855"
      }
      """
    And login status is 201
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 200
    And get login 'jean.dupond@gmail.com' for application 'test' and version 'v1'
    And login status is 200
    When disconnection from application 'test'
    Then connection status is 204
    And get login 'jean.dupond@gmail.com' for application 'test' and version 'v1'
    And login status is 401

  Scenario: access login is forbidden
    Given create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "11326617-c0a2-49d0-a358-5c5785c7b855"
      }
      """
    And login status is 201
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 200
    When get login 'jean.dupont@gmail.com' for application 'test' and version 'v1'
    Then login status is 403

  Scenario: change username
    Given delete username 'jean.dupont@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "cd171aaa-a74b-41f5-9c31-d04e812c14bd"
      }
      """
    And login status is 201
    And login 'jean.dupond@gmail.com' exists
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 200
    When patch login by id cd171aaa-a74b-41f5-9c31-d04e812c14bd for application 'test' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/0/username",
           "value": "jean.dupont@gmail.com"
         }
      ]
      """
    Then login status is 204
    And connection with username 'jean.dupont@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 200
    And login 'jean.dupont@gmail.com' is
      """
      {
          "username": "jean.dupont@gmail.com",
          "id": "cd171aaa-a74b-41f5-9c31-d04e812c14bd"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 401

  Scenario: forgotten password
    Given create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "cd171aaa-a74b-41f5-9c31-d04e812c14bd"
      }
      """
    And login status is 201
    And login 'jean.dupond@gmail.com' exists
    And connection to client 'admin'
    When new password for 'jean.dupond@gmail.com' from application 'admin'
    Then connection status is 200
    And patch login 'jean.dupond@gmail.com' for application 'admin' and version 'v1'
      """
      [
         {
           "op": "replace",
           "path": "/password",
           "value": "mdp123"
         }
      ]
      """
    And login status is 204
    And connection with username 'jean.dupond@gmail.com' and password 'mdp123' to client 'test_user'
    And connection status is 200

  Scenario: copy username
    Given delete username 'jean.dupont@gmail.com'
    And create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "cd171aaa-a74b-41f5-9c31-d04e812c14bd"
      }
      """
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    When patch login by id cd171aaa-a74b-41f5-9c31-d04e812c14bd for application 'test' and version 'v1'
      """
      [
         {
           "op": "copy",
           "path": "/1",
           "from": "/0"
         },
         {
           "op": "replace",
           "path": "/1/username",
           "value": "jean.dupont@gmail.com"
         }
      ]
      """
    Then login status is 204
    And connection with username 'jean.dupont@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 200
    And logins cd171aaa-a74b-41f5-9c31-d04e812c14bd are
      """
      [
         {
           "username": "jean.dupond@gmail.com",
           "id": "cd171aaa-a74b-41f5-9c31-d04e812c14bd"
         },
         {
           "username": "jean.dupont@gmail.com",
           "id": "cd171aaa-a74b-41f5-9c31-d04e812c14bd"
         }
      ]
      """

  Scenario: disable login
    Given create login for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "11326617-c0a2-49d0-a358-5c5785c7b855"
      }
      """
    And login status is 201
    And login 'jean.dupond@gmail.com' exists
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 200
    When put login 'jean.dupond@gmail.com' for application 'test' and version 'v1'
      """
      {
          "username": "jean.dupond@gmail.com",
          "password": "mdp",
          "id": "11326617-c0a2-49d0-a358-5c5785c7b855",
          "disabled": true
      }
      """
    Then login status is 204
    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_user'
    And connection status is 400
    And connection error is
      """
      {
          "error": "invalid_grant",
          "error_description": "User is disabled"
      }
      """
