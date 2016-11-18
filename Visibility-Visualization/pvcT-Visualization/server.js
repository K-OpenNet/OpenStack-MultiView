/**
 * Module dependencies.
 */

var express = require('express');
//var ArticleProvider = require('./resource-mongodb').ArticleProvider;
//var RouteProvider = require('./route-mongodb').RouteProvider;
var BoxProvider = require('./MultiView-DataAPI').BoxProvider;
//var UserProvider = require('./user-mongodb').UserProvider;
var http = require("http");
var app = express();
//var app = module.exports = express.createServer();
var client = require('socket.io').listen(8080).sockets;
// Configuration

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
//MultiView Data Integration & View Access
var resourceProvider = new ResourceProvider();
app.get('/resourcecentricviewops', function(req, res){
    var boxList         = null;
    var switchList      = null;
    var instanceList    = null;
    var serviceList     = 0;
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

    /*resourceProvider.getServiceList(function(error, serviceobj)
    {
        serviceList = serviceobj;
        console.log(serviceList);
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
        console.log(ovsBridgeStatus);
        showView();
    })

    function showView()
    {
        if(boxList !== null && switchList !== null && instanceList !== null && serviceList !==null &&  ovsBridgeStatus !== null)
	{
        	console.log('Resource-Centric View Rendering');
    		res.render('resourcecentricviewops.jade',{locals: {
                	boxList         : JSON.stringify(boxList),
			switchList      : JSON.stringify(switchList),
			instanceList    : JSON.stringify(instanceList),
			serviceList     : JSON.stringify(serviceList),
//			pPathStatus    : JSON.stringify(pPathStatus),
			ovsBridgeStatus : JSON.stringify(ovsBridgeStatus)
        	},
        	title: 'Resource-Centric Topological View'}
    		)
	}
    }
});

//var resourceProvider = new ResourceProvider();
app.get('/flowcentricviewops', function(req, res){
    console.log('Flow-Centric View Rendering');
    //res.render('flowcentricviewops.jade', {title: 'Flow-Centric View'})
    var boxList         = null;
    var switchList      = null;
    var instanceList    = null;
    var serviceList     = 0;
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
        if(boxList !== null && switchList !== null && instanceList !== null && serviceList !==null &&  ovsBridgeStatus !== null)
        {
                console.log('Flow-Centric View Rendering');
                res.render('flowcentricviewops.jade',{locals: {
                        boxList         : JSON.stringify(boxList),
                        switchList      : JSON.stringify(switchList),
                        instanceList    : JSON.stringify(instanceList),
                        serviceList     : JSON.stringify(serviceList),
//                      pPathStatus    : JSON.stringify(pPathStatus),
                        ovsBridgeStatus : JSON.stringify(ovsBridgeStatus)
                },
                title: 'Flow-Centric Topological View'}
                )
        }
    }
});

app.get('/servicecentricviewops', function(req, res){
    console.log('Workload-Centric View Rendering');
    res.render('servicecentricviewops.jade', {title: 'Workload Centric View'})
});

//var resourceProvider = new ResourceProvider();
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

//var userProvider = new UserProvider();
app.get('/', function(req, res){
    res.render('login.jade', {title: 'MultiView Login'})
});

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

app.get('/menu', function(req, res){
       	console.log('Menu Rendering');
	res.render('menu.jade',{locals: {}, title: 'MultiView Menu'})
});

app.get('/login', function(req, res){
    res.render('login.jade',{ title: 'MultiView Login'})
});

app.set('domain', '0.0.0.0')
app.listen(3006);
console.log("Express Server Running...");
//console.log("Express server listening on port %d in %s mode", app.address().port, app.settings.env);
