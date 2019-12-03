# jira-reader

Reads messages from JIRA projects and streams updates to Kafka.

This service uses the JIRA API to check for messages that have been udpated after a given point in time. Recieved messages are streamed to a Kafka topic, expecting processing to occur downstream.  using the JIRA ID as a key.

## Installation

Download from https://github.com/clingen-data-model/jira-reader

## Usage

FIXME: explanation

    $ java -jar jira-reader-0.1.0-standalone.jar [args]

## Options



## Examples



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
