# Basic-Ble-Writer
Plugin for Tasker that allows the user to write arbitrary binary arrays to Bluetooth Low Energy Devices.

Within tasker the action is called "Basic BLE Write"

Binary Arrays can be written to devices provided the user can supply:
* The device address in the format FF:FF:FF:FF:FF:FF (where "F" stands for any hex character)
* The service address/GUID: FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF
* The characteristic address/GUID: FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF.

Values must be written in hex pairs, odd numbers of characters should fail to send, but I havent confirmed it.

I wrote this to aide me in the construction of my very own, very simple, BLE device, so the functionality fits my use case.  

I will likely add type selection for the value to be written in the near future.

This Module could really really really use tasker variable interpolation, but I don't have the time at this moment to sort that out.  

This code may be modified and used freely by other products and projects, excepting in the case where this project makes up 50% or more of said project or product: Lets work together on this to make it better for everyone!
