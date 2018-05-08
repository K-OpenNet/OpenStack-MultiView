$(document).ready(function() {
  var path = window.location.pathname;
  var n = path.lastIndexOf('/');
  var url = path.substring(0,n) + "/json" + window.location.search;

  var maxPoints = 5 * 60;
  var step = 1000;
  var times;
  var values;
  var widget = $('#stripchart').stripchart();

  function updateChart(data) {
      if(!data || data.length == 0) return;

      var now = (new Date()).getTime(); 
      if(!times) {
         times = [];
         values = [];
         var t = now;
         for(var i = 0; i < maxPoints; i++) {
           t = t - step;
           times.unshift(t);
         }
         for(var j = 0; j < data.length; j++) {
           var series = new Array(times.length);
           for(var k = 0; k < times.length; k++) series[k] = 0;
           values.push(series);
         }
      }

      labels = [];
      times.push(now);
      var tmin = now - (maxPoints * 1.04 * step);
      var nshift = 0; 
      while(times.length >= maxPoints || times[0] < tmin) {
          times.shift();
          nshift ++;
      }
      for(var l = 0; l < data.length; l++) {
	  labels.push(data[l].metricName);
	  var series = values[l];
          var val = data[l].metricValue;
          if(!$.isNumeric(val)) val = 0;
	  series.push(val);
	  for(var s = 0; s < nshift; s++) series.shift();
      }

      widget.stripchart("draw",{times:times,values:values,legend:{labels:labels}});
  }

  $(window).resize(function() {
     widget.stripchart("draw",{times:times,values:values,legend:{labels:labels}});
  });

  (function poll() {
      $.ajax({
	      url: url,
              dataType: "json",
	      success: function(data) {
		  updateChart(data);
		  setTimeout(poll, step);
	      },
	      error: function(result,status,errorThrown) {
                  setTimeout(poll, 5000);
	      }
     });
  })();
})
   
