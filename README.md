# agent-status-change

[ ![Download](https://api.bintray.com/packages/hmrc/releases/agent-status-change/images/download.svg) ](https://bintray.com/hmrc/releases/agent-status-change/_latestVersion)

## Running the tests

    sbt test it:test

## Running the tests with coverage

    sbt clean coverageOn test it:test coverageReport

## Running the app locally

    sm --start AGENTS_STUBS AGENT_STATUS_CHANGE -f
    sm --stop AGENT_STATUS_CHANGE
    sbt run

It should then be listening on port 9424

    browse http://localhost:9424/agent-status-change

## Endpoints


### Terminate Agent Records
```markdown
DELETE /agent/:arn/terminate
```

Hard Delete on Agent-Client-Authorisation, Agent-Fi-Relationship and Agent-Mapping Mongo stores of all records for a given Agent Reference Number 

| Response | Reason |
| ---------| ------ |
| 200      |  Successfully Deleted All Records      |
| 400      |  Invalid Agent Reference Number Given      |
| 500      |  Failed to Terminate All or Some Records in mentioned Stores     |

### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
