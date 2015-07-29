package stat.web.entity;

public enum TimeType {

	d("天"), h("小时"), m("5分");

	public final String label;
	TimeType(String label) {
		this.label = label;
	}

	public static TimeType of(String s) {
		for (TimeType type : TimeType.values()) {
			if (type.name().equals(s))
				return type;
		}
		return m;
	}

}
