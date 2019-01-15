/*
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

metadata {
	definition (name: "Hass.io Device Presence", namespace: "Petezah", author: "Peter Dunshee") {
		capability "Presence Sensor"
		capability "Sensor"
        
        command "arrived"
        command "departed"
	}

	simulator {
		status "present": "presence: present"
		status "not present": "presence: not present"
	}

	tiles {
		standardTile("presence", "device.presence", width: 2, height: 2, canChangeBackground: true) {
			state("present", labelIcon:"st.presence.tile.present", backgroundColor:"#00A0DC")
			state("not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ffffff")
		}
		main "presence"
		details "presence"
	}
}

def parse(String description) {
    def pair = description.split(":")
    createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
	// TODO?
}

// handle commands

def arrived() {
    log.trace "Executing 'arrived'"
    sendEvent(name: "presence", value: "present")
}

def departed() {
    log.trace "Executing 'departed'"
    sendEvent(name: "presence", value: "not present")
}
