		var map;
		var control;
		var currGpx;

		$.urlParam = function (name) {
			var results = new RegExp('[\?&]' + name + '=([^&#]*)')
				.exec(window.location.search);

			return (results !== null) ? results[1] || 0 : false;
		}
		$("#image").on('click', function(){
            leafletImage(map, function(err, canvas) {
                // now you have canvas
                // example thing to do with that canvas:
                var img = document.createElement('img');
                var dimensions = map.getSize();
                img.width = dimensions.x;
                img.height = dimensions.y;
                img.src = canvas.toDataURL();
                document.getElementById('images').innerHTML = '';
                document.getElementById('images').appendChild(img);
            });
		})

		$(document).ready(function (e) {
			$("#downloadMapCard").hide();
			$("#downloadGpxCard").hide();
			$("#distanceCard").hide();
			var mapid = document.getElementById('map').getAttribute('data-map-target');
			map = L.map(mapid, {preferCanvas: true});
			var osm = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
				attribution: 'Map data &copy; <a href="http://www.osm.org">OpenStreetMap</a>'
			});
			//
			var ollie = L.tileLayer('https://tiler5.oobrien.com/streeto/{z}/{x}/{y}.png', {
				attribution: 'Map data &copy; <a href="http://www.oomap.co.uk">Open Orienteering Map</a>'
			});
			var sat = L.gridLayer.googleMutant({
				type: 'satellite',
				maxZoom: 18,
			});


			var baseMaps = {
				"Street Map": osm,
				"Satellite": sat,
				"OO Map": ollie
			};
			var overlays = {};

			L.control.layers(baseMaps, overlays).addTo(map);
			osm.addTo(map)
			L.control.scale().addTo(map);
            L.control.browserPrint().addTo(map)

			map.setView([53.2356556, -1.4255804], 13);
			var qBox = $.urlParam("gpx");
			if (qBox) {
				redrawLeaflet(qBox)
			}

		})

		$("#colStartLabel, #colDistLabel").change(function () {
			var newStart = $("#colStartLabel option:selected")[0].value
			var newDist = $("#colDistLabel option:selected")[0].value
			if (newDist != "" && newStart != "") {
				showDownloadGPX(newStart + "_" + newDist)
				showDownloadMap(newStart + "_" + newDist)
				redrawLeaflet(newStart)
			} else {
				$("#downloadMapCard").hide();
				$("#downloadGpxCard").hide();
				$("#distanceCard").hide();
			}
		})

		function showDist(length) {
			$("#distanceId").text((length / 1000).toFixed(2) + "K");
			$("#distanceCard").show()
		}

		function showDownloadGPX(course) {
			$("#downloadGpxId")[0].href = "./gpx/" + course + ".gpx";
			$("#downloadGpxCard").show();
		}

		function showDownloadMap(course) {
			$("#downloadMapId")[0].href = "./map/" + course + ".pdf";
			$("#downloadMapCard").show();
		}

		function redrawLeaflet(gpx) {
			if (currGpx)
				currGpx.removeFrom(map);
			display_gpx(gpx);
		}

		function display_gpx(url) {
			currGpx = new L.GPX(url, {
				async: true,
				gpx_options: {
					parseElements: ['track', 'waypoint']
				},
				marker_options: {
					wptIconUrls: {
						'': 'http://github.com/mpetazzoni/leaflet-gpx/raw/master/pin-icon-start.png'
					},
					startIconUrl: 'http://github.com/mpetazzoni/leaflet-gpx/raw/master/pin-icon-end.png',
					endIconUrl: 'http://github.com/mpetazzoni/leaflet-gpx/raw/master/pin-icon-end.png',
					shadowUrl: 'http://github.com/mpetazzoni/leaflet-gpx/raw/master/pin-shadow.png',
				},
			}).on('addpoint', function(e) {
                console.log('Added ' + e.point_type + ' point: ' + e.point.toString());
              }).on('loaded', function (e) {
				var gpx = e.target;
				map.fitBounds(gpx.getBounds());
				showDist(gpx.get_distance());
			}).on('error', function(e) {
                console.log('Error loading file: ' + e.err);
              }).addTo(map);
		}
