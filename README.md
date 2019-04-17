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

It should then be listening on port 9000

    browse http://localhost:9000/agent-status-change

### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
