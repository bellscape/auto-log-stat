/* ------------------------- $.tmpl ------------------------- */

//ref: http://stackoverflow.com/questions/24816/escaping-html-strings-with-jquery
var entityMap = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': '&quot;', "'": '&#39;', "/": '&#x2F;' };
function escapeHtml(string) {
	return String(string).replace(/[&<>"'\/]/g, function(s) {
		return entityMap[s];
	});
}

// $.tmpl
// ref: http://ejohn.org/blog/javascript-micro-templating/
//         xxxxx_____________..!!!..___xxxxx___..!!!..________xxxxx
// p.push('xxxxx',escapeHtml({{abc}}),'xxxxx');{%...%}p.push('xxxxx');
$.tmpl = function(str, data) {
	data = data || {};

	// inject me.*, for templates like {{me.field||""}}
	// cannot inject property for raw string, so...
	if (typeof data == 'string')
		data = new String(data);
	data.me = data;

	var fn = new Function("obj",
		"var p=[],print=function(){p.push.apply(p,arguments);};"
			+ "with(obj){p.push('"
			+ str.replace(/[\r\t\n]/g, " ").replace(/'/g, '"')
			.split('{%').join("');")
			.split('%}').join("p.push('")
			.split('{{{').join("',")
			.split('}}}').join(",'")
			.split('{{').join("',escapeHtml(")
			.split('}}').join("),'")
			+ "');}return p.join('');");
	var html = fn(data);

	delete data.me; // inject self.*
	return html;
};


$.replace = function (str, to) {
	return str.replace(/{{}}/g, to);
};



/* ------------------------- alert ------------------------- */

$.alert = function() {
	var $alert;
	var msg;
	var timeout;
	$.each(arguments, function(_, arg) {
		switch ($.type(arg)) {
			case 'object':
				$alert = arg;
				break;
			case 'string':
				msg = arg;
				break;
			case 'number':
				timeout = arg;
		}
	});

	$alert = $alert || $('#alert');
	timeout = timeout || 3000;
	if (msg) {
		$alert.text(msg).fadeOut().fadeIn();
		var rand = Math.round(Math.random() * 100);
		$alert.data('rand', rand);
		setTimeout(function() {
			if (rand == $alert.data('rand')) {
				$alert.fadeOut();
				$alert.data('rand', -1);
			}
		}, timeout)
	} else {
		window.alert_rand = -1;
		$alert.html('').hide();
	}
};

/* ------------------------- config ------------------------- */

/** trace (for debug only) */
window.trace = window.log = function() {
	window.console && console.log && console.log(arguments.length == 1 ? arguments[0] : arguments);
};

jQuery.fx.speeds._default = 200;

// override default alert
if ($('#alert').size()) {
	window.alert = function(msg, timeout) {
		$.alert($('#alert'), '' + msg, timeout);
		// bell(2013-7): msg需要转为string，否则alert(123)时，123会被理解为timeout
	}
}
