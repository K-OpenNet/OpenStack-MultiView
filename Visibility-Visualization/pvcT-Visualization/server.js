/**
 * Module dependencies.
 */

var express = require('express');
var BoxProvider = require('./MultiView-DataAPI').BoxProvider;
var http = require("http");
var app = express();
//var app = module.exports = express.createServer();
var client = require('socket.io').listen(8080).sockets;
var host = "103.22.221.56";

app.configure(function(){
  app.set('views', __dirname + '/views');
  app.set('view engine', 'jade');
//  app.use(express.bodyParser());
  app.use(express.json());
  app.use(express.urlencoded());
  
  app.use(express.methodOverride());
  app.use(require('stylus').middleware({ src: __dirname + '/public' }));
  app.use(app.router);
  app.use(express.static(__dirname + '/public'));
});

app.configure('development', function(){
});
app.configure('production', function(){
  app.use(express.errorHandler()); 
});

//Define Application Routes
var resourceProvider = new ResourceProvider();
// Route for Resource-Centric View
app.get('/resourcecentricviewops', function(req, res){
    var boxList         = null;
    var switchList      = null;
    var instanceList    = null;
    var workloadList     = 0;
    var ovsBridgeStatus = null;
    var pPathStatus     = null;
    resourceProvider.getpBoxList( function(error,boxobj)
    {
	boxList = boxobj;
    //console.log( boxList);
	showView();
    })
    resourceProvider.getvSwitchList(function(error, switchobj)
    {
    	switchList = switchobj;
        //console.log(switchList);
	showView();
    })

    resourceProvider.getvBoxList(function(error, instanceobj)
    {
        instanceList = instanceobj;
        //console.log(instanceList);
        showView();
    })

    /*resourceProvider.getworkloadList(function(error, serviceobj)
    {
        workloadList = serviceobj;
        console.log(workloadList);
        showView();
    })*/

    /*resourceProvider.getpPathStatus(function(error, pathobj)
    {
        pPathStatus = pathobj;
        console.log(pPathStatus);
        showView();
    })*/

    resourceProvider.getovsBridgeStatus(function(error, bridgestatusobj)
    {
        ovsBridgeStatus = bridgestatusobj;
        //console.log(ovsBridgeStatus);
        showView();
    })

    function showView()
    {
        if(boxList !== null && switchList !== null && instanceList !== null && workloadList !==null &&  ovsBridgeStatus !== null)
	{
		    console.log('Resource-Centric View Rendering');
			res.render('resourcecentricviewops.jade',{locals: {
            boxList         : JSON.stringify(boxList),
			switchList      : JSON.stringify(switchList),
			instanceList    : JSON.stringify(instanceList),
			workloadList     : JSON.stringify(workloadList),
//			pPathStatus    : JSON.stringify(pPathStatus),
			ovsBridgeStatus : JSON.stringify(ovsBridgeStatus)
        	},
        	title: 'Resource-Centric Topological View'}
    		)
	}
    }
});

//Route for Flow Rules View
app.get('/flowrulesviewops', function(req, res){
    console.log('Flow Rules and Statistics View Rendering');
    //res.render('flowrulesviewops.jade', {title: 'Flow-Centric View'})
    var boxList         = null;
    var switchList      = null;
    var instanceList    = null;
    var workloadList     = 0;
    var ovsBridgeStatus = null;
    var pPathStatus     = null;
    resourceProvider.getpBoxList( function(error,boxobj)
    {
        boxList = boxobj;
        console.log( boxList);
        showView();
    })
    resourceProvider.getvSwitchList(function(error, switchobj)
    {
        switchList = switchobj;
        console.log(switchList);
        showView();
    })

    resourceProvider.getvBoxList(function(error, instanceobj)
    {
        instanceList = instanceobj;
        console.log(instanceList);
        showView();
    })

	resourceProvider.getovsBridgeStatus(function(error, bridgestatusobj)
    {
        ovsBridgeStatus = bridgestatusobj;
        console.log(ovsBridgeStatus);
        showView();
    })

    function showView()
    {
        if(boxList !== null && switchList !== null && instanceList !== null && workloadList !==null &&  ovsBridgeStatus !== null)
        {
                console.log('Flow Rules and Statistics View Rendering');
                res.render('flowrulesviewops.jade',{locals: {
                        boxList         : JSON.stringify(boxList),
                        switchList      : JSON.stringify(switchList),
                        instanceList    : JSON.stringify(instanceList),
                        workloadList     : JSON.stringify(workloadList),
//                      pPathStatus    : JSON.stringify(pPathStatus),
                        ovsBridgeStatus : JSON.stringify(ovsBridgeStatus)
                },
                title: 'Flow Rules and Statistics View Rendering'}
                )
        }
    }
});

// Route for Flow Path Tracing View
app.get('/flowtracingviewops/*', function(req, res){
	//Wait for 1 minute before requesting again
	req.connection.setTimeout(60*1000);
	
	console.log('Flow Path Tracing View Rendering');
    
	var tenantID=req.originalUrl;
	var vlanID=tenantID;
	
	tenantID=tenantID.substring(20, tenantID.indexOf("&"));
	vlanID=vlanID.substring(vlanID.indexOf("&")+1, vlanID.length);
	console.log(tenantID);
	console.log(vlanID);
	
    var boxList           = null;
    var switchList        = null;
    var instanceList      = null;
    var workloadList      = 0;
    var ovsBridgeStatus   = 0;
	var bridgevlanmapList = null;
    
	resourceProvider.getpBoxList( function(error,boxobj)
    {
        boxList = boxobj;
        showView();
    })
    resourceProvider.getvSwitchList(function(error, switchobj)
    {
        switchList = switchobj;
        showView();
    })

    resourceProvider.getTenantvBoxList(tenantID, function(error, instanceobj)
    {
        instanceList = instanceobj;
        showView();
    })

	resourceProvider.getbridgevlanmapList(vlanID, function(error, bridgevlanmapobj)
    {
       	bridgevlanmapList = bridgevlanmapobj;
       	showView();
    })

    function showView()
    {
        if(boxList !== null && switchList !== null && instanceList !== null && workloadList !==null &&  ovsBridgeStatus !== null && bridgevlanmapList !==null)
        {
                res.render('flowtracingviewops.jade',{locals: {
                        boxList           : JSON.stringify(boxList),
                        switchList        : JSON.stringify(switchList),
                        instanceList      : JSON.stringify(instanceList),
                        workloadList      : JSON.stringify(workloadList),
                        ovsBridgeStatus   : JSON.stringify(ovsBridgeStatus),
                        bridgevlanmapList : JSON.stringify(bridgevlanmapList)
                },
                title: 'Flow Tracing View Rendering'}
                )
        }
    }
});

// Route for Flows/Playground Measurements View
app.get('/flowmeasureviewops', function(req, res){
    console.log('Flow Measure View Rendering');
    //res.render('flowcentricviewops.jade', {title: 'Flow-Centric View'})
    var boxList         = null;
    var switchList      = null;
    var instanceList    = null;
    var workloadList     = 0;
    var ovsBridgeStatus = null;
    var pPathStatus     = null;
    resourceProvider.getpBoxList( function(error,boxobj)
    {
        boxList = boxobj;
        console.log( boxList);
        showView();
    })
    resourceProvider.getvSwitchList(function(error, switchobj)
    {
        switchList = switchobj;
        console.log(switchList);
        showView();
    })

    resourceProvider.getvBoxList(function(error, instanceobj)
    {
        instanceList = instanceobj;
        console.log(instanceList);
        showView();
    })

	resourceProvider.getovsBridgeStatus(function(error, bridgestatusobj)
    {
        ovsBridgeStatus = bridgestatusobj;
        console.log(ovsBridgeStatus);
        showView();
    })

    function showView()
    {
        if(boxList !== null && switchList !== null && instanceList !== null && workloadList !==null &&  ovsBridgeStatus !== null)
        {
                console.log('Flow Measure View Rendering');
                res.render('flowmeasureviewops.jade',{locals: {
                        boxList         : JSON.stringify(boxList),
                        switchList      : JSON.stringify(switchList),
                        instanceList    : JSON.stringify(instanceList),
                        workloadList     : JSON.stringify(workloadList),
//                      pPathStatus    : JSON.stringify(pPathStatus),
                        ovsBridgeStatus : JSON.stringify(ovsBridgeStatus)
                },
                title: 'Flow Measure View'}
                )
        }
    }
});

// Route for Flows/Box Measurements View
app.get('/flowboxviewops', function(req, res){
    console.log('Flow Box View Rendering');
    var boxList         = null;
    var switchList      = null;
    var instanceList    = null;
    var workloadList     = 0;
    var ovsBridgeStatus = null;
    var pPathStatus     = null;
    resourceProvider.getpBoxList( function(error,boxobj)
    {
        boxList = boxobj;
        console.log( boxList);
        showView();
    })
    resourceProvider.getvSwitchList(function(error, switchobj)
    {
        switchList = switchobj;
        console.log(switchList);
        showView();
    })

    resourceProvider.getvBoxList(function(error, instanceobj)
    {
        instanceList = instanceobj;
        console.log(instanceList);
        showView();
    })

	resourceProvider.getovsBridgeStatus(function(error, bridgestatusobj)
    {
        ovsBridgeStatus = bridgestatusobj;
        console.log(ovsBridgeStatus);
        showView();
    })

    function showView()
    {
        if(boxList !== null && switchList !== null && instanceList !== null && workloadList !==null &&  ovsBridgeStatus !== null)
        {
                console.log('Flow Box View Rendering');
                res.render('flowboxviewops.jade',{locals: {
                        boxList         : JSON.stringify(boxList),
                        switchList      : JSON.stringify(switchList),
                        instanceList    : JSON.stringify(instanceList),
                        workloadList     : JSON.stringify(workloadList),
                        ovsBridgeStatus : JSON.stringify(ovsBridgeStatus)
                },
                title: 'Flows/Box Measure View'}
                )
        }
    }
});

// Route for Workload View
app.get('/servicecentricviewops', function(req, res){
    console.log('Workload-Centric View Rendering');
    res.render('servicecentricviewops.jade', {title: 'Workload Centric View'})
});

// Route for Flow Rules View
app.get('/opsflowrules/*', function(req, res){
    var configList = null;
    var statList = null;
    var boxID=req.originalUrl;
    boxID=boxID.substring(14,boxID.length);
    resourceProvider.getOpsSDNConfigList(boxID, function(error,configobj)
    {
       	configList = configobj;
       	showView();
    })
    resourceProvider.getOpsSDNStatList(boxID, function(error,statobj)
    {
        statList = statobj;
        console.log(statList);
        showView();
    })
    function showView()
    {
       	if(configList !== null && statList !== null)
       	{
        	console.log('Operator Controller Flow Rules');
		console.log(statList);
		res.render('opssdncontconfig', { title: 'Operator Controller Flow Rules', configList: configList, statList: statList });
               // res.render('opssdncontconfig.jade',{locals: {
               //        	configList : JSON.stringify(configList),
               // },
               // title: 'Operator Controller Flow Rules'}
               // )
        }
    }    
});

// Route for Flow Statistics View
app.get('/opsflowstat', function(req, res){
    var statList = null;
    resourceProvider.getOpsSDNStatList( function(error,statobj)
    {
        statList = statobj;
        console.log(statList);
        showView();
    })
    function showView()
    {
        if(statList !== null)
        {
                console.log('Operator Controller Flow Stats');
                res.render('opssdncontstat', { title: 'Operator Controller Flow Statistics', statList: statList });
        }
    }
});

// Route for Tenant-Vlan Mappings View
app.get('/tenantvlanmapops', function(req, res){
	var tenantList = null;
    //var tenantID=req.originalUrl;
    //tenantID=tenantID.substring(14, tenantID.length);
    //resourceProvider.gettenantvlanmapList(tenantID, function(error, tenantObj)
	resourceProvider.gettenantvlanmapList(function(error, tenantObj)
    {
       	tenantList = tenantObj;
       	showView();
    })
    
    function showView()
    {
       	if(tenantList !== null)
       	{
        	console.log('Tenant-Vlan Flow Path Tracing');
			console.log(tenantList);
			res.render('tenantvlanmapops', { title: 'Tenant Vlan Mappings View', tenantList: tenantList });
		}
    }    
});

// Route for Login View
app.get('/', function(req, res){
    res.render('login.jade', {title: 'MultiView Web Application Login'})
});

// Route for Menu View
app.get('/menu', function(req, res){
       	console.log('Menu Rendering');
	res.render('menu.jade',{locals: {}, title: 'MultiView Menu'})
});

app.get('/login', function(req, res){
    res.render('login.jade',{ title: 'MultiView Login'})
});

// Web Autentication & Validation
client.on('connection', function (socket) {
    socket.on('login', function(login_info){
        var this_user_name = login_info.user_name,
            this_user_password = login_info.user_password;
        if (this_user_name === '' || this_user_password === '') {
                socket.emit('alert', 'You must fill in both fields');
        } else {
            resourceProvider.getUsers(function (err, listusers){
                if(err) throw err;
                var found = false,
                    location =-1;
                  if (listusers.length) {
                        for (var i in listusers) {
                            if (listusers[i].username === this_user_name) {
                                found = true;
                                if (listusers[i].password === this_user_password) {
                                    //todo: get priority and send to menu page.
                                    if(listusers[i].role === 'operator'){
                                        socket.emit('redirect', 'operator');
                                    }
                                    else{
                                        socket.emit('redirect', 'developer');
                                    }
                                } else {
                                    socket.emit('alert', 'Please retry password');
                                }
                                break;
                            }
                        }

                        if (!found) {
                            socket.emit('alert', 'Sorry, We could not find you.');
                        }
                    }
            })
        }
    });
});

app.set('domain', '0.0.0.0')
app.listen(3011);
console.log("Express Server Running...");
//console.log("Express server listening on port %d in %s mode", app.address().port, app.settings.env);
