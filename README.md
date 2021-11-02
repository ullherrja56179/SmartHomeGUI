# SmartHomeGUI

Small Test Project to build a GUI for SmartHome in Kotlin.
It should be able to add new devices via MQTT or Zigebee2Mqtt.
This requires parsing the published JSON-Messages to add new devices.

For the First stance only Zigbee Devices will be added to see in parsing works correctly (as i only have one bulp so far).

* In the GUI everything that can be changed/configured shall be displayed. For now this is everything that is present in the exposes-section of the json/Mqtt-Message.
* For each Expose there will be an Expose-Object created. There are: BinaryExpose, NumbericExpose and EnumExpose (are they the same or almost the same for other Device types as well?). A Device contains an Expose-Object of the type specified by the JSON.
* Predefined values shall be displayed in something like a dropdown menu.
