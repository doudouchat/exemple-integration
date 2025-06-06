Feature: api subscription

  Background: 
    Given connection to client 'test_service' and scopes
      |subscription:update|subscription:read|ROLE_APP|

  Scenario: create subscription
    Given delete subscription 'jean.dupond@gmail.com'
    When create subscription 'jean.dupond@gmail.com'
    Then subscription 'jean.dupond@gmail.com' is
      """
      { 
      	"email": "jean.dupond@gmail.com"
      }
      """
    And subscription event is
      """
      { 
      	"email": "jean.dupond@gmail.com"
      }
      """
    And subscription contains 'subscription_date'

  Scenario: update subscription
    Given delete subscription 'jean.dupond@gmail.com'
    When create subscription 'jean.dupond@gmail.com'
    And create subscription 'jean.dupond@gmail.com'
    Then subscription 'jean.dupond@gmail.com' is
      """
      { 
      	"email": "jean.dupond@gmail.com"
      }
      """
    And subscription event is
      """
      { 
      	"email": "jean.dupond@gmail.com"
      }
      """
    And subscription contains 'subscription_date'

  Scenario: get subscription fails because none subscription exists
    When delete subscription 'jean.dupond@gmail.com'
    Then subscription 'jean.dupond@gmail.com' is unknown

  Scenario: update subscription fails because email is incorrect
    When create subscription 'jean.dupond'
    Then subscription error only contains
      """
      {
          "path": "/email",
          "code": "format"
      }
      """
