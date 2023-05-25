# agent-status-change

A backend microservice responsible for handling the suspension status of agents using the Agent Services Account.

## Running the tests

    sbt test it:test

## Running the tests with coverage

    sbt clean coverageOn test it:test coverageReport

## Running the app locally

    sm --start AGENT_AUTHORISATION -r
    sm --stop AGENT_STATUS_CHANGE
    sbt run

It should then be listening on port 9424

    browse http://localhost:9424/agent-status-change

## Endpoints


### Terminate Agent Records
```markdown
DELETE /agent/:arn/terminate
```

#### Example:
```markdown
http://localhost:9424/agent-status-change/agent/TARN000001/terminate
```

Hard Delete on Agent-Client-Authorisation, Agent-Client-Relationships, Agent-Fi-Relationship and Agent-Mapping Mongo stores of all records for a given ARN aka Agent Reference Number. 

| Response | Reason |
| ---------| ------ |
| 200      |  Successfully Deleted All Records for given Agent Reference Number     |
| 400      |  Invalid Agent Reference Number Given      |
| 500      |  Failed to Terminate All or Some Records in mentioned Stores     |

### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
