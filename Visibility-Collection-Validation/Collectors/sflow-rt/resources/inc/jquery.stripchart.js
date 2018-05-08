// Copyright (c) 2014-2016 InMon Corp. ALL RIGHTS RESERVED

(function($) {
    var suffixes = ["\uD835\uDF07","\uD835\uDC5A","","K","M","G","T","P","E"];

    var colorNames = {
	"aliceblue":"#f0f8ff","antiquewhite":"#faebd7","aqua":"#00ffff",
        "aquamarine":"#7fffd4","azure":"#f0ffff","beige":"#f5f5dc",
        "bisque":"#ffe4c4","black":"#000000","blanchedalmond":"#ffebcd",
        "blue":"#0000ff","blueviolet":"#8a2be2","brown":"#a52a2a",
        "burlywood":"#deb887","cadetblue":"#5f9ea0","chartreuse":"#7fff00",
        "chocolate":"#d2691e","coral":"#ff7f50","cornflowerblue":"#6495ed",
        "cornsilk":"#fff8dc","crimson":"#dc143c","cyan":"#00ffff",
        "darkblue":"#00008b","darkcyan":"#008b8b","darkgoldenrod":"#b8860b",
        "darkgray":"#a9a9a9","darkgreen":"#006400","darkkhaki":"#bdb76b",
        "darkmagenta":"#8b008b","darkolivegreen":"#556b2f",
        "darkorange":"#ff8c00","darkorchid":"#9932cc","darkred":"#8b0000",
        "darksalmon":"#e9967a","darkseagreen":"#8fbc8f",
        "darkslateblue":"#483d8b","darkslategray":"#2f4f4f",
        "darkturquoise":"#00ced1","darkviolet":"#9400d3","deeppink":"#ff1493",
        "deepskyblue":"#00bfff","dimgray":"#696969","dodgerblue":"#1e90ff",
	"firebrick":"#b22222","floralwhite":"#fffaf0","forestgreen":"#228b22",
        "fuchsia":"#ff00ff","gainsboro":"#dcdcdc","ghostwhite":"#f8f8ff",
        "gold":"#ffd700","goldenrod":"#daa520","gray":"#808080",
        "green":"#008000","greenyellow":"#adff2f", "honeydew":"#f0fff0",
        "hotpink":"#ff69b4","indianred":"#cd5c5c","indigo":"#4b0082",
        "ivory":"#fffff0","khaki":"#f0e68c","lavender":"#e6e6fa",
        "lavenderblush":"#fff0f5","lawngreen":"#7cfc00","lemonchiffon":"#fffacd",
	"lightblue":"#add8e6","lightcoral":"#f08080","lightcyan":"#e0ffff",
        "lightgoldenrodyellow":"#fafad2","lightgrey":"#d3d3d3",
        "lightgreen":"#90ee90","lightpink":"#ffb6c1","lightsalmon":"#ffa07a",
        "lightseagreen":"#20b2aa","lightskyblue":"#87cefa",
        "lightslategray":"#778899","lightsteelblue":"#b0c4de",
        "lightyellow":"#ffffe0","lime":"#00ff00","limegreen":"#32cd32",
        "linen":"#faf0e6","magenta":"#ff00ff","maroon":"#800000",
        "mediumaquamarine":"#66cdaa","mediumblue":"#0000cd",
        "mediumorchid":"#ba55d3","mediumpurple":"#9370d8",
        "mediumseagreen":"#3cb371","mediumslateblue":"#7b68ee",
	"mediumspringgreen":"#00fa9a","mediumturquoise":"#48d1cc",
        "mediumvioletred":"#c71585","midnightblue":"#191970",
        "mintcream":"#f5fffa","mistyrose":"#ffe4e1","moccasin":"#ffe4b5",
	"navajowhite":"#ffdead","navy":"#000080","oldlace":"#fdf5e6",
        "olive":"#808000","olivedrab":"#6b8e23","orange":"#ffa500",
        "orangered":"#ff4500","orchid":"#da70d6","palegoldenrod":"#eee8aa",
	"palegreen":"#98fb98","paleturquoise":"#afeeee",
        "palevioletred":"#d87093","papayawhip":"#ffefd5","peachpuff":"#ffdab9",
        "peru":"#cd853f","pink":"#ffc0cb","plum":"#dda0dd",
        "powderblue":"#b0e0e6","purple":"#800080","red":"#ff0000",
        "rosybrown":"#bc8f8f","royalblue":"#4169e1","saddlebrown":"#8b4513",
        "salmon":"#fa8072","sandybrown":"#f4a460","seagreen":"#2e8b57",
        "seashell":"#fff5ee","sienna":"#a0522d","silver":"#c0c0c0",
	"skyblue":"#87ceeb","slateblue":"#6a5acd","slategray":"#708090",
        "snow":"#fffafa","springgreen":"#00ff7f","steelblue":"#4682b4",
        "tan":"#d2b48c","teal":"#008080","thistle":"#d8bfd8","tomato":"#ff6347",
        "turquoise":"#40e0d0","violet":"#ee82ee","wheat":"#f5deb3",
        "white":"#ffffff","whitesmoke":"#f5f5f5","yellow":"#ffff00",
        "yellowgreen":"#9acd32"
    };

    function colorToHex(col) {
       var vals,r,g,b;
       if(col[0] === '#') return col;
       vals = /rgb *\( *([0-9]{1,3}) *, *([0-9]{1,3}) *, *([0-9]{1,3}) *\)/.exec(col);
       if(vals && vals.length == 4) {
         r = Math.round(parseFloat(vals[1]));
         g = Math.round(parseFloat(vals[2]));
         b = Math.round(parseFloat(vals[3]));
         return "#"
                + (r + 0x10000).toString(16).substring(3).toUpperCase() 
                + (g + 0x10000).toString(16).substring(3).toUpperCase()
                + (b + 0x10000).toString(16).substring(3).toUpperCase();
        }
        col = col.toLowerCase();
        if(colorNames.hasOwnProperty(col)) {
           return colorNames[col];
        }
        return null;
    }

    function colorLuminance(col, lum) {
        var hex,rgb,i,c;
        hex = colorToHex(col);
        if(!hex) return col;
 
	hex = String(hex).replace(/[^0-9a-f]/gi, '');
	if(hex.length < 6) hex = hex[0]+hex[0]+hex[1]+hex[1]+hex[2]+hex[2];
	lum = lum || 0;
	rgb = "#";
	for (i = 0; i < 3; i++) {
	    c = parseInt(hex.substr(i*2,2), 16);
	    c = Math.round(Math.min(Math.max(0, c + (c * lum)), 255)).toString(16);
	    rgb += ("00"+c).substr(c.length);
	}
	return rgb;
    }

    function darkenColor(hex) {
	return colorLuminance(hex,-0.2);
    }

    function maxValue(values,sumFlag,mirrorFlag) {
	var maxUp = 0;
	var maxSumUp = 0;
	var maxDown = 0;
	var maxSumDown = 0;
	var i, sumup, sumdown, maxup, maxdown, s, val, even;
	for(i = 0; i < values[0].length; i++) {
	    sumup = 0;
	    maxup = 0;
	    sumdown = 0;
	    maxdown = 0;
	    for(s = 0; s < values.length; s++) {
		val = values[s][i];
		if(mirrorFlag) {
		    if(s%2) {
			sumdown += val;		    
			if(val > maxdown) maxdown = val;
		    } else {
			sumup += val;		    
			if(val > maxup) maxup = val;
		    }
		} else {
		    sumup += val;
		    if(val > maxup) maxup = val;
		}
	    }
	    if(maxup > maxUp) maxUp = maxup;
	    if(sumup > maxSumUp) maxSumUp = sumup;
	    if(mirrorFlag) {
		if(maxdown > maxDown) maxDown = maxdown;
		if(sumdown > maxSumDown) maxSumDown = sumdown;
	    }
	}
	var maxUp = sumFlag ? maxSumUp : maxUp;
	var maxDown = sumFlag ? maxSumDown : maxDown;
	return {up: maxUp, down:maxDown};
    }

    function drawLine(ctx, startx, starty, endx, endy, style) {
	ctx.beginPath();
	ctx.moveTo(startx, starty);
	ctx.lineTo(endx, endy); 
	ctx.strokeStyle = style;
	ctx.lineWidth = 1;
	ctx.stroke();
    }

    function valueTickSpacing(maxVal,base2) {
	var stepsizes = [1,2,5,10,20,50,100,200,500];
	var i, steps, stepsize, tsteps, j;
	var divisor = base2 ? 1/(1024*1024) : 0.000001;
	var factor = base2 ? 1024 : 1000;
	var range = maxVal.up + maxVal.down;

	for(i = 0; i < suffixes.length; i++) {
	    if((range / divisor) < factor) break;
	    divisor *= factor;
	}

	steps = Math.floor(range/divisor);
	if(steps < 5 && i > 0) {
	    divisor /= factor;
	    i--;
	    steps = Math.floor(range / divisor);
	} 
	stepsize = 1;
	tsteps;
	for(var j = 0; j < stepsizes.length; j++) {
	    stepsize = stepsizes[j];
	    tsteps = steps / stepsize; 
	    if(Math.floor(tsteps) < 8) break;
	}
	return stepsize * divisor;
    }

    function valueStr(value,includeMillis,base2) {
	if (value === 0) return value;
	var i = 2;
	var divisor = 1;
	var factor = base2 ? 1024 : 1000;
	var absVal, scaled;
	if (includeMillis) {
	    i = 0;
	    divisor = base2 ? 1/(1024*1024) : 0.000001;
	}
	absVal = Math.abs(value);
	while (i < suffixes.length) {
	    if ((absVal / divisor) < factor) break;
	    divisor *= factor;
	    i++;
	}
	scaled = Math.round(absVal * factor / divisor) / factor;
	return scaled + suffixes[i];
    }

    function drawValueAxis(ctx,h,w,maxVal,option,valueAxisLabel) {
	var stepsize = valueTickSpacing(maxVal,option.base2);
	var rOffset = 5;
	var yZero,vscale,vstep,meas,maxLabel,yval,s,i,lval,label,font,fontArgs,bottom;
	var range = maxVal.up + maxVal.down;

	ctx.textAlign = 'right';
	ctx.textBaseline = 'middle';
	ctx.fillStyle = "#000";	

	vscale = (h - option.bottomInset - option.topInset) / range;
	yZero = vscale * maxVal.up + option.topInset;
	ctx.fillText("0",option.leftInset - rOffset, yZero);
	drawLine(ctx,option.leftInset, yZero, w - option.rightInset, yZero, "#ccc");

	vstep = vscale * stepsize;

	meas = ctx.measureText("0");
	maxLabel = meas.width;

	// up axis
	yval = yZero - vstep;
	s = 1;
	while(yval > option.topInset) {
	    lval = s * stepsize;
	    label = valueStr(lval,option.includeMillis,option.base2);
	    meas = ctx.measureText(label);
	    maxLabel = Math.max(maxLabel,meas.width);
	    ctx.fillText(label, option.leftInset - rOffset, yval);
	    drawLine(ctx, option.leftInset, yval, w - option.rightInset, yval, "#ccc");
	    s++;
	    yval -= vstep;
	}

	// down axis
	yval = yZero + vstep;
	s = 1;
	bottom = h - option.bottomInset;
	while(yval < bottom) {
	    lval = s * stepsize;
	    label = valueStr(lval,option.includeMillis,option.base2);
	    meas = ctx.measureText(label);
	    maxLabel = Math.max(maxLabel,meas.width);
	    ctx.fillText(label,option.leftInset - rOffset, yval);
	    drawLine(ctx, option.leftInset, yval, w - option.rightInset, yval, "#ccc");
	    s++;
	    yval += vstep;
	}
	drawLine(ctx, option.leftInset, option.topInset, option.leftInset, h - option.bottomInset, "#000");
	// add units
	if(valueAxisLabel) {
	    font = ctx.font;
	    fontArgs = font.split(' ');
	    ctx.font = '12px ' + fontArgs[fontArgs.length - 1];
	    ctx.rotate(-Math.PI/2);
	    ctx.textAlign = "center";
	    ctx.textBaseline = "bottom";
	    ctx.fillText(valueAxisLabel, -option.topInset - ((h - option.topInset - option.bottomInset) / 2), Math.max(10,option.leftInset - maxLabel - rOffset - 5));
	    ctx.rotate(Math.PI/2);
	    ctx.font = font;
	}
	return {yZero:yZero, vScale: vscale};
    }

    function timeStr(ms,includeSeconds) {
	var date = new Date(ms);
	var sec = date.getSeconds().toString();
	if(sec.length === 1) sec = "0"+sec;
	var min = date.getMinutes().toString();
	if(min.length === 1) min = "0"+min;
	var hrs = date.getHours().toString();
	if(hrs.length === 1) hrs = "0"+hrs;
	return includeSeconds ? hrs+":"+min+":"+sec : hrs + ":" + min;
    }

    function drawTimeAxis(ctx,h,w,maxVal,option,times,step,selectedIdx) {	
	var stepsizes = [60000,120000,300000,600000,900000,1200000,1800000,3600000,7200000,10800000,14400000,21600000,43200000,86400000];
	var yBase = h - option.bottomInset;
	var tmin = times[0];
	var tmax = times[times.length - 1];
        if(step) tmax += (tmax - tmin) / Math.max(times.length - 1,1);
	var tdelta = tmax - tmin;
   
	var tOffset = 5;
	var height = h - yBase - tOffset - 2;
	ctx.textAlign = 'center';
	ctx.textBaseline = 'middle';
	var tMiddle = yBase + tOffset + height / 2;

	var i,stepsize,includeSeconds,d,tick,tscale,xPos,markerPos,label,meas,radius,width,x,y;
	for(i = 0; i < stepsizes.length; i++) {
	    stepsize = stepsizes[i];
	    if(tdelta / stepsize < 10) break;
	}
	includeSeconds = stepsize < 300000;

	d = new Date(tmin);
	d.setMilliseconds(0);
	d.setSeconds(0);
	if(stepsize >= 60000) d.setMinutes(0);
	if(stepsize >= 3600000) d.setHours(0);
	
	tick = d.getTime();
	tscale  = (w - option.leftInset - option.rightInset) / tdelta;
	while(tick < tmax) {
	    if(tick >= tmin) {
		xPos = option.leftInset + (tscale * (tick - tmin));
	  
		ctx.fillStyle = "#000";
		drawLine(ctx, xPos, yBase, xPos, yBase + 4, "#000");
		drawLine(ctx, xPos, yBase, xPos, option.topInset, "#ccc");
		ctx.fillText(timeStr(tick,includeSeconds), xPos, tMiddle);
	    }
	    tick += stepsize;
	}

	drawLine(ctx,option.leftInset,yBase,w - option.rightInset,yBase,"#000");

	if(times && times[selectedIdx]) {
	    markerPos = option.leftInset + (tscale * (times[selectedIdx] - tmin));
	    ctx.fillStyle = "#000";
	    drawLine(ctx, markerPos, yBase, markerPos, yBase + 4, "#000");
	    drawLine(ctx, markerPos, yBase, markerPos, option.topInset, "#ccc");
	    label = timeStr(times[selectedIdx],includeSeconds);
	    meas = ctx.measureText(label);
	    ctx.strokeStyle = "#000";
	    ctx.fillStyle = "#fff";
	    radius = 2;
	    width = meas.width + 4;
	    x = markerPos - width / 2;
	    y = yBase + tOffset;
	    ctx.beginPath();
	    ctx.moveTo(x + radius, y);
	    ctx.lineTo(x + width - radius, y);
	    ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
	    ctx.lineTo(x + width, y + height - radius);
	    ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
	    ctx.lineTo(x + radius, y + height);
	    ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
	    ctx.lineTo(x, y + radius);
	    ctx.quadraticCurveTo(x, y, x + radius, y);
	    ctx.closePath();
	    ctx.stroke();
	    ctx.fill();
	    ctx.fillStyle = "#000";
	    ctx.fillText(label,markerPos,tMiddle);
	}
	return { tScale: tscale, tMin: tmin, tMax: tmax };
    }

    function drawStackedSeries(ctx,h,w,maxVal,yZero,vscale,tmin,tmax,tscale,option,values,times,step,mirror,colors) {
	var s,series,yPos,xPos,i,val,c,col;
	for(s = values.length; --s >= 0;) {
	    series = values[s];
	    if(mirror) {
		col = colors[Math.floor(s / 2) % colors.length];
		if(s % 2) ctx.fillStyle=darkenColor(col);
		else ctx.fillStyle=col;
	    } else {
		ctx.fillStyle=colors[s % colors.length];
	    }
	    ctx.beginPath();
	    for(i = 0; i < series.length; i++) {
		val = series[i];
		if(mirror) {
		    if(s > 1) {
			c = s - 2;
			while(c >= 0) {
			    val += values[c][i];
			    c -= 2;
			}
		    }
		} else {
		    if(s > 0) {
			for(c = 0; c < s; c++) val += values[c][i];
		    }
		}
		val *= vscale;
		yPos = mirror ? (s % 2 ? yZero + val : yZero - val) : yZero - val;
		xPos = option.leftInset + (tscale * (times[i] - tmin));
		if(i === 0) {
		    ctx.moveTo(xPos,yZero);
		    ctx.lineTo(xPos,yPos);
		}
		else ctx.lineTo(xPos,yPos);
                if(step) {
                  xPos = option.leftInset + (tscale * ((times[i+1] || tmax) - tmin));
                  ctx.lineTo(xPos,yPos);
                }
	    }
	    ctx.lineTo(xPos,yZero);
	    ctx.closePath();
	    ctx.fill();
	}
	if(mirror && maxVal.down) drawLine(ctx,option.leftInset,yZero,w - option.rightInset,yZero,"#000");
    }

    function drawSeries(ctx,h,w,maxVal,yZero,vscale,tmin,tmax,tscale,option,values,times,step,mirror,colors) {
	var s, i, val, series, xPos, yPos, upFlag, col;
	for(s = values.length; --s >= 0;) {
	    series = values[s];
	    if(mirror) {
		col = colors[Math.floor(s / 2) % colors.length];
		if(s % 2) ctx.strokeStyle=darkenColor(col);
		else ctx.strokeStyle=col;
	    } else {
		ctx.strokeStyle=colors[s % colors.length];
	    }
	    ctx.lineWidth=2;
	    ctx.beginPath();
	    for(i = 0; i < series.length; i++) {
		val = series[i] * vscale;
		yPos = mirror ? (s % 2 ? yZero + val : yZero - val) : yZero - val;
		xPos = option.leftInset + (tscale * (times[i] - tmin));
		if(i === 0) ctx.moveTo(xPos,yPos);
		else ctx.lineTo(xPos,yPos);
                if(step) {
                  xPos = option.leftInset + (tscale * ((times[i+1] || tmax) - tmin));
                  ctx.lineTo(xPos,yPos);
                }
	    }
	    ctx.stroke(); 
	}
	if(mirror && maxVal.down) drawLine(ctx,option.leftInset,yZero,w - option.rightInset,yZero,"#000");
    }

    function drawHrule(ctx,h,w,maxVal,yZero,vscale,option,hrule) {
	var i,hval,yval;
	for(i = 0; i < hrule.length; i++) {
	    hval = hrule[i].value;
	    if(hval < maxVal.up) {
		yval = yZero - (hval * vscale);
		drawLine(ctx, option.leftInset, yval, w - option.rightInset, yval, hrule[i].color);
	    }		
	    if(hval < maxVal.down) {
		yval = yZero + (hval * vscale);
		drawLine(ctx, option.leftInset, yval, w - option.rightInset, yval, hrule[i].color);
	    }
	}
    }

    function addLegend(chart,h,w,option,legendInfo,values,mirror,colors) {
        var legend,i,r,c,row,col,links;
	legend = '<table>';
	if(legendInfo.headings) {
	    legend += '<thead><tr><th></th>';
	    for(i = 0; i < legendInfo.headings.length; i++) legend += '<th>'+legendInfo.headings[i] + '</th>';
	    legend += '</tr></thead>';
	}
	function createLink(idx,val) {
	    if(!legendInfo.links) return val;
            var link = legendInfo.links instanceof Array ? legendInfo.links[idx] : legendInfo.links;
	    if(!link) return val;
	    link = link.replace(/\{(.*?)\}/g, function(match,tok) {return val});
	    return '<a href="' + link + '">' + val + '</a>';
	}
	if(legendInfo.labels) {
	    legend += '<tbody>';
	    for(r = 0; r < legendInfo.labels.length; r++) {
		legend += '<tr>';
                if(mirror && legendInfo.labels.length === values.length) {
                   col = colors[Math.floor(r / 2)];
                   if(r % 2) col = darkenColor(col); 
                } else col = colors[r % colors.length];
		legend += '<td><div class="swatch" style="background:' + col + '"></div></td>';
		if(legendInfo.labels[r] instanceof Array) {
		    row = legendInfo.labels[r];
		    for(c = 0; c < row.length; c++) legend += '<td>' + createLink(c,row[c]) + '</td>';
		}
		else legend += '<td>' + createLink(r,legendInfo.labels[r]) + '</td>';
		legend += '</tr>';
	    }
	    legend += '</tbody>';
	}
	legend += '</table>';
	if(chart._legend) chart._legend.remove();
	var pos = chart._canvas.position();
	chart._legend = $(legend).css('position','absolute').css('top',pos.top).css('left',pos.left+option.leftInset).appendTo(chart.element);
    }

    $.widget('inmon.stripchart', {
	    options: { 
		bottomInset: 20,
		leftInset: 50,
		topInset: 5,
		rightInset: 22,
		clickable: false,
		includeMillis: false, 
	        base2:false,
                step:false,
		stack: false,
		mirror: false,
		colors: [
		   '#3366cc','#dc3912','#ff9900','#109618','#990099','#0099c6',
                   '#dd4477','#66aa00','#b82e2e','#316395','#994499','#22aa99',
                   '#aaaa11','#6633cc','#e67300','#8b0707','#651067','#329262',
                   '#5574a6','#3b3eac','#b77322','#16d620','#b91383','#f4359e',
                   '#9c5935','#a9c413','#2a778d','#668d1c','#bea413','#0c5922',
                   '#743411'
                ]
	    },
	    _create: function() {
                var option;
		this.element.addClass('stripchart');
		this._canvas = $('<canvas/>').appendTo(this.element);
		option = this.options;
		if(this.options.clickable) {
		    this._canvas.addClass("clickable").click(function(e) {
                            var $canvas,off;
			    $canvas = $(this);
			    off = $canvas.offset();
			    var x = (e.pageX - off.left - option.leftInset) / ($canvas.width() - option.leftInset - option.rightInset);
			    var y = 1 - (e.pageY - off.top - option.topInset) / ($canvas.height() - option.topInset - option.bottomInset);
			    $canvas.trigger('stripchartclick',{x:x,y:y});
		     }); 
		}
	    },
	    _destroy: function() {
		if(this.options.clickable) this._canvas.removeClass('clickable').unbind('click');
		this.element.removeClass('stripchart');
		this.element.empty();
		delete this._canvas;
		delete this._legend;
	    },
	    draw: function(cdata) {
                var canvas,ctx,h,w,ratio,colors,step,stack,mirror,maxVal,i,hval;
		canvas = this._canvas[0];
		if(!canvas || !canvas.getContext) return;

		ctx = canvas.getContext('2d');
		h = this._canvas.height();
		w = this._canvas.width();
		ratio = window.devicePixelRatio;
		if(ratio && ratio > 1) {
		    canvas.height = h * ratio;
		    canvas.width = w * ratio;
		    ctx.scale(ratio,ratio);
		}
		else {
		    canvas.height = h;
		    canvas.width = w;
		}
                ctx.font = '10px sans-serif';

                colors = cdata.colors || this.options.colors;
                step = this.options.step;
		stack = this.options.stack;
		mirror = this.options.mirror;
		maxVal = maxValue(cdata.values,stack,mirror);
		if(cdata.hrule) {
		    for(i = 0; i < cdata.hrule.length; i++) {
			if(cdata.hrule[i].scale) {
			    hval = cdata.hrule[i].value;
			    if(maxVal.up < hval) maxVal.up = hval;
			    if(mirror && maxVal.down < hval) maxVal.down = hval;
			}
		    }
		}
		if(maxVal.up === 0 && maxVal.down === 0) maxVal.up = 1;
		maxVal.up *= 1.0 + (cdata.ymargin || 0.2);
		maxVal.down *= 1.0 + (cdata.ymargin || 0.2);
		var tMetrics = drawTimeAxis(ctx,h,w,maxVal,this.options,cdata.times,step,cdata.selectedIdx);
		var vMetrics = drawValueAxis(ctx,h,w,maxVal,this.options,cdata.units);
		ctx.save();
		ctx.rect(this.options.leftInset,this.options.topInset,w-this.options.rightInset-this.options.leftInset,h-this.options.bottomInset-this.options.topInset);
		ctx.clip();
		if(stack) drawStackedSeries(ctx,h,w,maxVal,vMetrics.yZero,vMetrics.vScale,tMetrics.tMin,tMetrics.tMax,tMetrics.tScale,this.options,cdata.values,cdata.times,step,mirror,colors);
		else drawSeries(ctx,h,w,maxVal,vMetrics.yZero,vMetrics.vScale,tMetrics.tMin,tMetrics.tMax,tMetrics.tScale,this.options,cdata.values,cdata.times,step,mirror,colors);
		if(cdata.hrule) drawHrule(ctx,h,w,maxVal,vMetrics.yZero,vMetrics.vScale,this.options,cdata.hrule);
		ctx.restore();
		if(cdata.legend) addLegend(this,h,w,this.options,cdata.legend,cdata.values,mirror,colors);
	    }
	});
})(jQuery);
