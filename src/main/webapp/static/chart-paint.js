/* ------------------------- colors ------------------------- */

// colors from highchart
$.color = { blue: '#4572A7', red: '#AA4643', green: '#89A54E',
	purple: '#80699B', cyan: '#3D96AE', orange: '#DB843D',
	cane: '#92A8CD', brown: '#A47D7C', grass: '#B5CA92'};
$.color.pv = '#CCBE93';
// $.color.pv = '#3A87AD';

/* ------------------------- Date.format ------------------------- */

Date.prototype.format = function(format) {
	/*
	 * eg:format="yyyy-MM-dd hh:mm:ss";
	 */
	var o = {
		"M+": this.getMonth() + 1, // month
		"d+": this.getDate(), // day
		"h+": this.getHours(), // hour
		"m+": this.getMinutes(), // minute
		"s+": this.getSeconds(), // second
		"q+": Math.floor((this.getMonth() + 3) / 3), // quarter
		"S": this.getMilliseconds() // millisecond
	};
	if (/(y+)/.test(format)) {
		format = format.replace(RegExp.$1, (this.getFullYear() + "")
			.substr(4 - RegExp.$1.length));
	}
	for (var k in o) {
		if (new RegExp("(" + k + ")").test(format)) {
			format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k]
				: ("00" + o[k]).substr(("" + o[k]).length));
		}
	}
	return format;
};

/* ------------------------- chart.data source ------------------------- */

$.chart = {};

$.chart.formatLabel = function(s) {
	if (typeof s === 'string') {
		return s.replace('snapshot', 'snap');
	}
	return s;
};

// Highcharts���ܵ�ʱ���ʽ��Date.UTC
$.chart.formatTime = function(s) {
	if (typeof s === 'number') {
		s = '' + s;
		// 20120129
		if (/^\d{8}$/.test(s)) {
			return Date.UTC(
				s.substring(0, 4),
				s.substring(4, 6) - 1,
				s.substring(6, 8)) - 8 * 3600 * 1000;
		}
	}
	if (typeof s === 'string') {
		if (/^\d+-\d+-\d+ \d+:\d+$/.test(s)) {
			var parts = s.split(/\D+/);
			return Date.UTC(
				parts[0],
				parts[1] - 1,
				parts[2],
				parts[3],
				parts[4]) - 8 * 3600 * 1000;
		}
		// 20120129/21/53
		if (/^\d{8}\/\d{1,2}\/\d{1,2}$/.test(s)) {
			var parts = s.split('/');
			return Date.UTC(
				parts[0].substring(0, 4),
				parts[0].substring(4, 6) - 1,
				parts[0].substring(6, 8),
				parts[1],
				parts[2]) - 8 * 3600 * 1000;
		}
		// 20120129/21
		if (/^\d{8}\/\d{1,2}$/.test(s)) {
			return Date.UTC(
				s.substring(0, 4),
				s.substring(4, 6) - 1,
				s.substring(6, 8),
				s.substring(9)) - 8 * 3600 * 1000;
		}
		// 20120129
		if (/^\d{8}$/.test(s)) {
			return Date.UTC(
				s.substring(0, 4),
				s.substring(4, 6) - 1,
				s.substring(6, 8)) - 8 * 3600 * 1000;
		}
		// Feb 12, 2012 12:00:00 AM
		if (/^[A-Z].*M/.test(s)) {
			var d = new Date(s);
			return Date.UTC(
				d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate(),
				d.getUTCHours(), d.getUTCMinutes(), d.getUTCSeconds());
		}
	}
	return s;
};

// HighCharts需要的格式：[{name:'',data:[[time,num], ...]}, ...]
// 假定sql为select label, time, value
$.chart.formatResult = function(rows, params) {
	var entryCache = {};
	var entries = [];
	var formatTime = $.chart.formatTime;
	var formatLabel = params && params.__formatLabel || $.chart.formatLabel;
	for (var i = 0; i < rows.length; i++) {
		var row = rows[i];
		var label = formatLabel(row[0]);
		var time = formatTime(row[1]);
		var num = row[2];

		var entry = entryCache[label];
		if (!entry) {
			entry = { name: label, data: [] };
			entryCache[label] = entry;
			entries.push(entry);
		}
		entry.data.push([time, num]);
	}
	return entries;
};

/* ------------------------- util.chart params ------------------------- */

// HighCharts params
Highcharts.setOptions({
	chart: {
		type: 'spline',
		backgroundColor: '#eff2e2', borderColor: '#989b8b', borderWidth: 1 },
	credits: { enabled: false },
	global: {
		useUTC: false // 修正时区问题
	},
	lang: {
		shortMonths: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12']
	},
	title: { text: '' },
	tooltip: { formatter: function() {
		return new Date(this.x).format('yyyy-M-d/hh:mm')
			+ '<br><span style="color:' + this.series.color + ';font-weight:bold">' + this.series.name + '</span>: <b>' + this.y + '</b>';
	}},
	xAxis: { type: 'datetime',
		dateTimeLabelFormats: { // %Y %b
			second: '%H:%M:%S',
			minute: '%H:%M',
			hour: '%b-%e/%H:%M',
			day: '%b-%e',
			week: '%b-%e',
			month: '%b \'%y',
			year: '%Y'
		},
		gridLineWidth: 1,
		gridLineColor: '#ddd'},
	yAxis: { title: { text: '' } }
});

/* 未考虑currLen,targetLen较小情况 */
/*function trimData(data, currLen, targetLen) {
 var target = [];
 var mask = currLen - 1;
 var len = targetLen - 1;
 for (var i = 0; i < len; i++) {
 target.push(data[Math.round(mask*i/len)]);
 }
 target.push(data[data.length - 1]);
 return target;
 }*/

function fillChartParams($div, data, params) {
	// assign $div.id
	var id = $div.attr('id');
	if (!id || id == '') {
		id = 'chart' + Math.floor(Math.random() * 1000000);
		$div.attr('id', id);
	}

	params = $.extend(true, {
		chart: { renderTo: id },
		series: data
	}, params);

	// get avg(len(data))
	var avgLen = 0;
	for (var i = 0; i < data.length; i++) {
		var len = data[i].data && data[i].data.length || 0;
		/*if (len > 200) {
		 data[i].data = trimData(data[i].data, len, 200);
		 len = data[i].data.length;
		 }*/
		avgLen += len;
	}
	avgLen /= data.length;

	if (avgLen > 100) {
		params = $.extend(true, {
			plotOptions: {
				spline: { marker: { enabled: false } },
				area: { marker: { enabled: false } },
				areaspline: { marker: { enabled: false } }
			}
		}, params);
	}
	/*if (avgLen <= 12 && data.length <= 3) {
	 params = $.extend(true, {
	 plotOptions: {
	 line: { dataLabels: { enabled:true } },
	 spline: { dataLabels: { enabled:true } },
	 area: { dataLabels: { enabled:true } },
	 areaspline: { dataLabels: { enabled:true } }
	 }
	 }, params);
	 }*/

	// 如果有unmax参数，取消所有的yAxis[*].max/min
	if (location.search.indexOf('unmax=on') >= 0) {
		if (params.yAxis) {
			$.each(params.yAxis, function() {
				if (this.min || this.min === 0)
					this.min = null;
				if (this.max)
					this.max = null;
			});
			if (params.yAxis.min || params.yAxis.min === 0)
				params.yAxis.min = null;
			if (params.yAxis.max)
				params.yAxis.max = null;
		}
	}

	return params
}

$.fn.timeChart = function(rows, params) {
	var $this = this;
	data = $.chart.formatResult(rows, params);
	params = fillChartParams($this, data, params);
	window[$this.attr('id')] = new Highcharts.Chart(params);
};

