# jira-reader

Reads messages from JIRA projects and streams updates to Kafka.

This service uses the JIRA API to check for messages that have been udpated after a given point in time. Recieved messages are streamed to a Kafka topic, expecting processing to occur downstream.  using the JIRA ID as a key.

## Installation

Download from https://github.com/clingen-data-model/jira-reader

## Usage

Either deployed in a containerized environment (see Dockerfile), or as a standalone JAR To run as a jar: 

    $ java -jar jira-reader.jar

## Options

Options are passed as environment variables. The following variables are required to be set:

```
JIRA_READER_USER # Username for JIRA access
JIRA_READER_PASSWORD # Password for JIRA access
JIRA_READER_HOST # JIRA host
JIRA_READER_PROJECT # JIRA project
JIRA_READER_TYPES # semicolon delimited list of types to read
KAFKA_HOST # Kafka host
KAFKA_USER # Kafka username
KAFKA_PASSWORD # Kafka password
JIRA_READER_TOPIC # Kafka topic to publish to
JIRA_READER_BUCKET # GCS bucket to store last-queried date to
JIRA_READER_DEFAULT_START_DATE # Default date to start query from, in absence of 
```


## License

Copyright © 2019 

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
