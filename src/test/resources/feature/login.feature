Feature: api login

  Background: 
    Given connection to client 'test_service'
    And delete username 'jean.dupond@gmail.com'

#  Scenario: create login
#    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    Then login authorization status is 201
#    And login authorization 'jean.dupond@gmail.com' exists
#    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user' with scopes 'login:update login:read login:delete'
#    And connection status is 200
#    And login authorization 'jean.dupond@gmail.com' is
#      """
#      {
#          "disabled": false,
#          "accountLocked": false,
#          "roles": []
#          
#      }
#      """
#
#  Scenario: create multi login
#    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    And delete username 'jean.dupont@gmail.com'
#    And create authorization login 'jean.dupont@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    And login authorization 'jean.dupond@gmail.com' exists
#    And login authorization 'jean.dupont@gmail.com' exists
#    When connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user' with scopes 'login:update login:read login:delete'
#    Then connection status is 200
#    And login authorization 'jean.dupond@gmail.com' is
#      """
#      {
#          "disabled": false,
#          "accountLocked": false,
#          "roles": []
#      }
#      """
#    And connection with username 'jean.dupont@gmail.com' and password 'mdp' to client 'test_service_user'
#    And login authorization 'jean.dupont@gmail.com' is
#      """
#      {
#          "disabled": false,
#          "accountLocked": false,
#          "roles": []
#      }
#      """
#
#  Scenario: delete login
#    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
#    When delete login 'jean.dupond@gmail.com'
#    Then login authorization status is 204
#    But login authorization 'jean.dupond@gmail.com' not exists
#    But login service 'jean.dupond@gmail.com' not exists
#
#  Scenario: create login fails because username already exists
#    When create authorization login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    And create authorization login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    Then login authorization status is 403
#
#  Scenario: access login is forbidden
#    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    And login authorization status is 201
#    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
#    And connection status is 200
#    When get authorization login 'jean.dupont@gmail.com' for application 'test'
#    Then login authorization status is 403
#
#  Scenario: copy username
#    Given delete username 'jean.dupont@gmail.com'
#    And create authorization login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    And login authorization status is 201
#    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
#    And connection status is 200
#    When copy authorization login for application 'test'
#      """
#      {
#          "fromUsername": "jean.dupond@gmail.com",
#          "toUsername": "jean.dupont@gmail.com"
#      }
#      """
#    Then login authorization status is 201
#    And connection with username 'jean.dupont@gmail.com' and password 'mdp' to client 'test_service_user'
#    And connection status is 200
#    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
#    And connection status is 200
#
#  Scenario: copy username fails because username already exists
#    Given delete username 'jean.dupont@gmail.com'
#    And create authorization login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    And create authorization login 'jean.dupont@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
#    And connection status is 200
#    When copy authorization login for application 'test'
#      """
#      {
#          "fromUsername": "jean.dupond@gmail.com",
#          "toUsername": "jean.dupont@gmail.com"
#      }
#      """
#    Then login authorization status is 400
#    And login authorization error is
#      """
#      [{
#          "path": "/toUsername",
#          "code": "username",
#          "message": "[jean.dupont@gmail.com] already exists"
#      }]
#      """
#
#  Scenario: copy username fails because username not found
#    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
#    And connection status is 200
#    And delete username 'jean.dupond@gmail.com'
#    When copy authorization login for application 'test'
#      """
#      {
#          "fromUsername": "jean.dupond@gmail.com",
#          "toUsername": "jean.dupont@gmail.com"
#      }
#      """
#    Then login authorization status is 400
#    And login authorization error is
#      """
#      [{
#          "path": "/fromUsername",
#          "code": "not_found",
#          "message": "[jean.dupond@gmail.com] not found"
#      }]
#      """
#
#  Scenario: disable login
#    Given create authorization login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp"
#      }
#      """
#    And login authorization status is 201
#    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
#    And connection status is 200
#    When put login 'jean.dupond@gmail.com' for application 'test'
#      """
#      {
#          "password": "mdp",
#          "disabled": true
#      }
#      """
#    Then login authorization status is 204
#    And connection with username 'jean.dupond@gmail.com' and password 'mdp' to client 'test_service_user'
#    And connection status is 400
#    And connection error is
#      """
#      {
#          "error": "invalid_grant",
#          "error_description": "User is disabled"
#      }
#      """
