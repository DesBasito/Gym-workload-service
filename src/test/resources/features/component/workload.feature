Feature: Trainer Workload Management

  Scenario: Successfully add training workload for a new trainer
    Given no workload exists for trainer "John.Doe"
    When I POST a workload request for trainer "John.Doe" firstName "John" lastName "Doe" action "ADD" duration 60 date "2025-06-01"
    Then the response status is 200
    And the workload for trainer "John.Doe" in year 2025 month 6 is 60 minutes

  Scenario: Successfully add workload for existing trainer accumulates duration
    Given a workload exists for trainer "Jane.Smith" with 60 minutes in year 2025 month 6
    When I POST a workload request for trainer "Jane.Smith" firstName "Jane" lastName "Smith" action "ADD" duration 30 date "2025-06-15"
    Then the response status is 200
    And the workload for trainer "Jane.Smith" in year 2025 month 6 is 90 minutes

  Scenario: Successfully delete training from workload
    Given a workload exists for trainer "Mike.Johnson" with 90 minutes in year 2025 month 7
    When I POST a workload request for trainer "Mike.Johnson" firstName "Mike" lastName "Johnson" action "DELETE" duration 90 date "2025-07-10"
    Then the response status is 200
    And the workload for trainer "Mike.Johnson" in year 2025 month 7 is 0 minutes

  Scenario: Add workload with missing username returns 400
    When I POST a workload request with missing username
    Then the response status is 400

  Scenario: Add workload with missing first name returns 400
    When I POST a workload request with missing firstName
    Then the response status is 400

  Scenario: Add workload with negative duration returns 400
    When I POST a workload request for trainer "John.Doe" firstName "John" lastName "Doe" action "ADD" duration -10 date "2025-06-01"
    Then the response status is 400

  Scenario: Add workload without authentication returns 403
    When I POST an unauthenticated workload request for trainer "John.Doe"
    Then the response status is 403

  Scenario: Get workload for existing trainer
    Given a workload exists for trainer "Sarah.Connor" with 45 minutes in year 2025 month 8
    When I GET workload for trainer "Sarah.Connor"
    Then the response status is 200
    And the response contains trainer username "Sarah.Connor"

  Scenario: Get workload for non-existent trainer returns 404
    Given no workload exists for trainer "Ghost.Trainer"
    When I GET workload for trainer "Ghost.Trainer"
    Then the response status is 404

  Scenario: Get workload without authentication returns 403
    When I GET workload unauthenticated for trainer "John.Doe"
    Then the response status is 403

  Scenario: Get all trainer workloads
    Given workloads exist for trainers "Alice.Trainer" and "Bob.Trainer"
    When I GET all workloads
    Then the response status is 200
    And the response is a JSON array with at least 2 entries

  Scenario: Get all workloads without authentication returns 403
    When I GET all workloads unauthenticated
    Then the response status is 403