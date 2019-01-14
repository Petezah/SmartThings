/**
 *  Hass.io (Connect)
 *
 *  Copyright 2019 Peter Dunshee
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Hass.io (Connect)",
    namespace: "Petezah",
    author: "Peter Dunshee",
    description: "Connect to Hass.io devices",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    page(name: "hassioInstance", title: "Use devices from this Hass.io instance", nextPage: "hassioDiscovery", uninstall: true) {
        section("Instance settings") {
            input "instanceIp", "text", required: true
        }
    }
    page(name: "hassioDiscovery", title:"Hass.io Device Setup", install: true, uninstall: true)
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def uninstalled() {
	def devices = getChildDevices()
	log.trace "deleting ${devices.size()} Sonos"
	devices.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}

def initialize() {
	// remove location subscription aftwards
	unsubscribe()
	state.subscribe = false

	unschedule()
	//scheduleActions()

	if (selectedDevices) {
		addHassioDevices()
	}
}

def addHassioDevices() {
	def deviceInfos = getDevicesDiscoveredInfo()
	def runSubscribe = false
	selectedDevices.each { device ->
    	def dni = device.entity_id
		def d = getChildDevice(device.entity_id)
		if(!d) {
			def newDevice = deviceInfos.find { (it.entity_id) == device.entity_id }
			log.trace "newDevice = $newDevice"
			log.trace "dni = $dni"
			d = addChildDevice("Petezah", "Hass.io Device", dni, newDevice?.value.hub, [label:"${newDevice?.value.name} Hass.io Device"])
			log.trace "created ${d.displayName} with id $dni"

			//d.setModel(newPlayer?.value.model)
			//log.trace "setModel to ${newPlayer?.value.model}"

			runSubscribe = true
		} else {
			log.trace "found ${d.displayName} with id $dni already exists"
		}
	}
}

//PAGES
def hassioDiscovery()
{
	log.trace "Instance set to $instanceIp"
/*
	discoverHassioDevices()
    def numFound = 0
    return dynamicPage(name:"hassioDiscovery", title:"Discovery Started!", nextPage:"", refreshInterval:refreshInterval) {
			section("Please wait while we discover your Hass.io devices. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
				input "selectedDevices", "enum", required:false, title:"Select Devices (${numFound} found)", multiple:true, options:options
			}
		}
*/
		int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
		state.refreshCount = refreshCount + 1
		def refreshInterval = 3

		def options = getDevicesDiscovered() ?: []

		def numFound = options.size() ?: 0

		//sonos discovery request every 5 //25 seconds
		//if((refreshCount % 8) == 0) {
			discoverHassioDevices()
		//}

		//setup.xml request every 3 seconds except on discoveries
		//if(((refreshCount % 1) == 0) && ((refreshCount % 8) != 0)) {
		//	verifySonosPlayer()
		//}

		return dynamicPage(name:"hassioDiscovery", title:"Discovery Started!", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
			section("Please wait while we discover your Hass.io devices. Discovery can take five minutes or more, so sit back and relax! Select your devices below once discovered.") {
				input "selectedDevices", "enum", required:false, title:"Select Devices (${numFound} found)", multiple:true, options:options
			}
		}
}

def parse(description) 
{
	log.trace "hassio parse got $description"
}

Map getDevicesDiscoveredInfo() {
	state.devices = state.devices ?: [:]
    return state.devices
}

Map getDevicesDiscovered() {
    def devs = getDevicesDiscoveredInfo()
	def map = [:]
	devs.each {
    	//log.trace it
		def value = "${it.attributes.friendly_name}"
		def key = it.entity_id
		map["${key}"] = value
	}
	map
}

private discoverHassioDevices()
{
    log.trace "Trying to discover devices"
	sendHubCommand(
    	new physicalgraph.device.HubAction(
        	"""GET /api/states HTTP/1.1\r\nHOST: $instanceIp:8123\r\n\r\n""", 
            physicalgraph.device.Protocol.LAN, 
            "${instanceIp}:8123", 
            [callback: calledBackHandler]))
}

void calledBackHandler(physicalgraph.device.HubResponse hubResponse) {
	log.trace "hassio parse got $hubResponse"
    def body = hubResponse.json
    //log.trace "got devices: $body"
    state.devices = body
    
}
