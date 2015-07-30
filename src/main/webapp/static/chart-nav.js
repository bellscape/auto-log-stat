function navli(subul,subi,object){
    $(".navul").css("color","#777");
    $(".navul").css("background-color","");
    $(".navallli").css("display","none");

    $("#subli_"+subi).css("display","");
    $("#navul_"+subi).css("background-color","#e5e5e5");
    $("#navul_"+subi).css("color","#555");
}

function obj2list(obj) {
	var list = [];
	$.each(obj, function(k, v) {
		list.push(k);
	});
	list.sort();
	return list;
}

function data2meta(data) {
	var ks = {};
	var ts = {};
	var rows = {};
	$.each(data, function(i, v) {
		ks[v[0]] = true;
		ts[v[1]] = true;
		var row = rows[v[1]] || (rows[v[1]] = {});
		row[v[0]] = v[2];
	});
	return [ obj2list(ks), obj2list(ts), rows ];
}

function data2table(data) {
	var pair = data2meta(data);
	var ks = pair[0];
	var ts = pair[1];
	var rows = pair[2];

	// console.log(k);
	// console.log(t);
	// console.log(rows);

	var html = [];
	html.push('<table class="table table-bordered table-hover table-condensed">');
	// push thead
	html.push('<tr><th>#</th>');
	$.each(ks, function(i, v) {
		html.push('<th>' + v + '</th>');
	});
	html.push('</tr>');
	// push tbody
	$.each(ts, function(i, t) {
		html.push('<tr><th>' + t + '</th>');
		var row = rows[t];
		$.each(ks, function(i, k) {
			html.push('<td>' + (row[k] || '') + '</td>');
		});
		html.push('</tr>');
	});
	html.push('</table>');
	return html.join('');
}

/* ------------------------- buttons ------------------------- */

setTimeout(function() {

	$('#btn-inverse').click(function() {
		$('.chart').each(function() {
			var id = $(this).attr('id');
			$.each(window[id].series, function(i, s) {
				if (s.visible)
					s.hide();
				else
					s.show();
			});
		});
	});

	if (location.search.indexOf('unmax=on') < 0) {
		$('#btn-unmax').click(function() {
			var url = location.href;
			url += (url.indexOf('?') >= 0 ? '&' : '?') + 'unmax=on';
			location = url;
		});
	} else {
		$('#btn-unmax').toggleClass('active').click(
			function() {
				var url = location.href;
				url = url.replace('unmax=on', '').replace('?&', '?')
					.replace(/[?&]$/, '');
				location.href = url;
			});
	}

	if (location.search.indexOf('nocache=on') < 0) {
		$('#btn-nocache').show().click(function() {
			var url = location.href;
			url += (url.indexOf('?') >= 0 ? '&' : '?') + 'nocache=on';
			location.href = url;
		});
	} else {
		$('#btn-nocache').click(function() {
			location.reload();
		});
	}

}, 500);

$(function() {
	$('#btn-showdebug').click(function() {
		$('#showdebug').toggle();
	});

	$('#btn-showtable').click(function() {
		$('.chart').each(function() {
			var id = $(this).attr('id');
			var data = window[id + "_data"];
			var $table = $('#' + id + '_table');

			var html = data2table(data);
			$table.html(html).show();
		});
		$('#btn-showtable').hide();
	});
});
