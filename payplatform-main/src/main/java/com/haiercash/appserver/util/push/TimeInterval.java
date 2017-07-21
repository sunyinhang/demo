package com.haiercash.appserver.util.push;

import org.json.JSONObject;

public class TimeInterval {
	public TimeInterval(int startHour, int startMin, int endHour, int endMin) {
		this.m_startHour = startHour;
		this.m_startMin = startMin;
		this.m_endHour = endHour;
		this.m_endMin = endMin;
	}

	public int getM_startHour() {
		return m_startHour;
	}

	public void setM_startHour(int m_startHour) {
		this.m_startHour = m_startHour;
	}

	public int getM_startMin() {
		return m_startMin;
	}

	public void setM_startMin(int m_startMin) {
		this.m_startMin = m_startMin;
	}

	public int getM_endHour() {
		return m_endHour;
	}

	public void setM_endHour(int m_endHour) {
		this.m_endHour = m_endHour;
	}

	public int getM_endMin() {
		return m_endMin;
	}

	public void setM_endMin(int m_endMin) {
		this.m_endMin = m_endMin;
	}

	public boolean isValid() {
		if (this.m_startHour >= 0 && this.m_startHour <= 23 && this.m_startMin >= 0 && this.m_startMin <= 59
				&& this.m_endHour >= 0 && this.m_endHour <= 23 && this.m_endMin >= 0 && this.m_endMin <= 59)
			return true;
		else
			return false;
	}

	public JSONObject toJsonObject() {
		JSONObject json = new JSONObject();
		JSONObject js = new JSONObject();
		JSONObject je = new JSONObject();
		js.put("hour", String.valueOf(m_startHour));
		js.put("min", String.valueOf(m_startMin));
		je.put("hour", String.valueOf(m_endHour));
		je.put("min", String.valueOf(m_endMin));
		json.put("start", js);
		json.put("end", je);
		return json;
	}

	private int m_startHour;
	private int m_startMin;
	private int m_endHour;
	private int m_endMin;
}
