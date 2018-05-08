// Copyright (c) InMon Corp. 2014 ALL RIGHTS RESERVED

(function($) {
    $.fn.chart = function(settings, db) {
	var $chartDiv = $(this);
	var db = db;

	var defaults = {
	    type  : 'trend',
	    step: false,
	    stack: false,
	    mirror: false,
	    includeMillis:false,
	    sep: ',',
	    keyName: function(key) { return key; },
	    includeOther:true,
	    otherStr: '-other-'
	};
	var option = $.extend(defaults,settings);
	var clickable = 'topn' === option.type;
	var widget = $(this).stripchart({clickable:clickable,includeMillis:option.includeMillis,stack:option.stack,mirror:option.mirror,step:option.step});
	var selectedTime = 0;
	if(clickable) {
	    $chartDiv.bind('stripchartclick', function(e,pt) {
		    var times = db.trend.times;
		    var tmin = times[0];
		    var tmax = times[times.length - 1];
		    var t  = ((tmax - tmin) * pt.x) + tmin;
		    if(t < tmin || t > tmax) selectedTime = 0;
		    else {
			var mindiff = tmax - tmin;
			var mint = tmin;
			for(var i = 0; i < times.length; i++) {
			    var diff = Math.abs(t - times[i]);
			    if(diff < mindiff) {
				mindiff = diff;
				mint = times[i];
			    }
			}
			selectedTime = mint;
		    }
		    drawChart();
		});
	} 

	function drawChart() {
	    if(!db.trend) return;
	    var val,cdata,idx,topn,lines,labels,legend,sortedKeys,k,keyToIdx,seriesOptions,key,line,i,cvals,entry,colors;
	
	    cdata = {times:db.trend.times};
	    if(option.units) cdata['units'] = option.units;
            if(option.ymargin) cdata['ymargin'] = option.ymargin;
	    if(option.hrule) {
		cdata.hrule = [];
		for(i = 0; i < option.hrule.length; i++) {
		    if(option.hrule[i].value) cdata.hrule.push(option.hrule[i]);
		    else if(option.hrule[i].name && db.trend.values) {
			val = db.trend.values[option.hrule[i].name];
			if(val) {   
			    cdata.hrule.push({
				    color:option.hrule[i].color,
				    scale:option.hrule[i].scale,
				    value:db.trend.values[option.hrule[i].name]
			     });
			}
		    } 
		}
	    }
	    switch(option.type) {
	    case "trend":
		if(option.colors) cdata.colors = option.colors;
		cdata.values = [];
		for(i = 0; i < option.metrics.length; i++) {
		    cdata.values.push(db.trend.trends[option.metrics[i]]);
		}
		if(option.legend) cdata.legend = {labels:option.legend};
		widget.stripchart("draw", cdata);
		break;
	    case "topn":
		if(selectedTime >= db.trend.times[0]) {
		    for(idx = 0; idx < db.trend.times.length; idx++) {
			if(db.trend.times[idx] >= selectedTime) break;
		    }
		} else {
		    selectedTime = 0;
		    idx = db.trend.times.length - 1;
		}
		cdata.selectedIdx = idx;

		topn = db.trend.trends[option.metric];
		lines = [];
		labels = [];
		if(selectedTime >= db.trend.times[0]) {
		    for(idx = 0; idx < db.trend.times.length; idx++) {
			if(db.trend.times[idx] >= selectedTime) break;
		    }
		} else {
		    selectedTime = 0;
		    idx = db.trend.times.length -1;
		}

		legend = topn[idx];
		sortedKeys = [];
		for(k in legend) {
		    if(option.otherStr !== k) sortedKeys.push(k);
		}
		sortedKeys.sort(function(a,b) { return legend[b] - legend[a]; });
		sortedKeys.push("hidden");

		keyToIdx = {};
		seriesOptions = [];
		for(k in sortedKeys) {
		    key = sortedKeys[k];
		    line = new Array(db.trend.times.length);
		    for(i = 0; i < line.length; i++) line[i] = 0;
		    lines[k] = line;
		    keyToIdx[sortedKeys[k]] = k;
		    if("hidden" !== key) labels[k] = $.map(key.split(option.sep),option.keyName);
		}
		for(i = 0; i < db.trend.times.length; i++) {	
		    entry = topn[i];
		    for(key in entry) {
			val = (entry[key] || 0); 
			if(key in keyToIdx) lines[keyToIdx[key]][i] = val;
			else {
			    if(option.includeOther || key !== option.otherStr) {
				if(option.stack) lines[sortedKeys.length - 1][i] += val;
				else lines[sortedKeys.length - 1][i] = Math.max(lines[sortedKeys.length - 1][i],val);
			    }
			}
		    }
		}
		cvals = option.colors || $.inmon.stripchart.prototype.options.colors;
		colors = [];
		for(i = 0; i < labels.length; i++) {
		    colors.push(cvals[i % cvals.length]);
		}
		colors.push(option.otherColor || 'darkgray');
		cdata.colors = colors;
		cdata.values = lines;
		cdata.legend = {labels:labels,headings:option.legendHeadings,links:option.legendLinks};
		widget.stripchart("draw", cdata);
		break;
	    }
	}
	$(document).on('updateChart', function(e) {
		if($chartDiv.is(':visible')) drawChart();
        });
	return this;
    }
}(jQuery));
