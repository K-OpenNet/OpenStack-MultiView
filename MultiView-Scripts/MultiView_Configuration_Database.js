#Update This Script Before Installing MultiView Software

use smartxdb
show collections

#Insert MultiView Users Data into Collection
db.configuration-multiview-users.ensureIndex({username:1, password: 1},{unique:true})
db.configuration-multiview-users.insert( { username: "admin", password: "admin", role: "operator" } )
db.configuration-multiview-users.insert( { username: "demo", password: "demo", role: "developer" } )

#Insert Physical Boxes Data into Collection <Insert for all boxes>
db.configuration-pbox-list.ensureIndex({box:1, boxID: 1},{unique:true})
db.configuration-pbox-list.insert( { box: "boxName", boxID: "SmartXBoxGIST", management_ip: "", management_ip_status: "", data_ip: "", data_ip_status: "", control_ip: "", control_ip_status: "", ovs_vm1: "", ovs_vm2: "", active_ovs_vm: "" } )

#Insert OVS Bridges Topology Data into Collection
db.configuration-vswitch-list.ensureIndex({type:1, bridge: 1},{unique:true})
db.configuration-vswitch-list.insert( { type: "B**", bridge: "brcap", topologyorder: "1" } )
db.configuration-vswitch-list.insert( { type: "B**", bridge: "brdev", topologyorder: "2" } )
db.configuration-vswitch-list.insert( { type: "B**", bridge: "brvlan", topologyorder: "3" } )
db.configuration-vswitch-list.insert( { type: "B**", bridge: "br-ex", topologyorder: "3" } )
db.configuration-vswitch-list.insert( { type: "B**", bridge: "br-int", topologyorder: "4" } )

#Insert OVS Bridges Status Data into Collection <Insert For all boxes>
db.configuration-vswitch-status.ensureIndex({bridge:1, box: 1},{unique:true})
db.configuration-vswitch-status.insert( { bridge: "brcap", box: "boxName", status: "RED" } )
db.configuration-vswitch-status.insert( { bridge: "brdev", box: "boxName", status: "RED" } )
db.configuration-vswitch-status.insert( { bridge: "brvlan", box: "boxName", status: "RED" } )
db.configuration-vswitch-status.insert( { bridge: "br-ex", box: "boxName", status: "RED" } )
db.configuration-vswitch-status.insert( { bridge: "br-int", box: "boxName", status: "RED" } )