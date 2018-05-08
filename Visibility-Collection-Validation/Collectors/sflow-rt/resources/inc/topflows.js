$(document).ready(function() {
  var path = window.location.pathname;
  var n = path.lastIndexOf('/');
  var m = path.lastIndexOf('/',n-1);
  var specname = path.substring(m+1,n);
  var url = path.substring(0,n) + "/json" + window.location.search;
  var step = 2000;

  var spec;
  function getSpec() {
    $.ajax({
      url: '../../../flow/' + specname + '/json',
      dataType: "json",
      success: function(data) {
        spec = data;
        poll();
      },
      error: function() {
        setTimeout(getSpec,5000);
      }
    });
  }  

  var suffixes = ["\uD835\uDF07","\uD835\uDC5A",,"K","M","G","T","P","E"];
  function formatValue(value,includeMillis) {
      if(value == 0) return "0.000";

      var i = 2;
      var divisor = 1;

      if(includeMillis) {
         i = 0;
         divisor = 0.000001;
      }

      var absval = Math.abs(value);
      while(i < suffixes.length) {
         if((absval /divisor) < 1000) break;
         divisor *= 1000;
         i++;
      }
      var scaled = Math.round(absval * 1000 / divisor) / 1000;
      return scaled.toFixed(3) + (suffixes[i] ? suffixes[i] : "");
   }
  
  function updateTable(data) {
      if(!data) return;

      var sep = spec.fs;

      var table = "<table>";
      table += "<thead>";
      if(spec.keys) {
        var headers = spec.keys.split(',');
        for(var h = 0; h < headers.length; h++) {
          table += '<th>' + headers[h] + '</th>';
        }
      }
      table += '<th>' + spec.value + '</th>';
      table += "</thead>";
      table += '<tbody>';
      for(var i = 0; i < data.length; i++) {
         table += '<tr class="' + (i % 2 == 0 ? 'even' : 'odd') + '">';
         if(spec.keys) {
           var keys = data[i].key.split(sep);
           for(var k = 0; k < keys.length; k++) {
              table += "<td>" + keys[k] + "</td>";
           }
         }
         table += '<td class="alignr">' + formatValue(data[i].value) + '</td>';
         table += "</tr>";
      }
      table += '</tbody>';
      table += '</table>';
      $("#content").empty().append(table);
  }

  function poll() {
      $.ajax({
	      url: url,
	      success: function(data) {
		  updateTable(data);
		  setTimeout(poll, step);
	      },
	      error: function(result,status,errorThrown) {
		  setTimeout(getSpec, 5000);
	      },
	      dataType: "json",
           });
   };

   getSpec();
})
   
