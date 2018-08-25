package net.androgames.level.config;

//import net.androgames.level.R;

import com.byagowi.persiancalendar.R;

/*
 *  This file is part of Level (an Android Bubble Level).
 *  <https://github.com/avianey/Level>
 *
 *  Copyright (C) 2014 Antoine Vianey
 *
 *  Level is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Level is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Level. If not, see <http://www.gnu.org/licenses/>
 */
public enum DisplayType {

    ANGLE(R.string.angle, R.string.angle_summary, "00.0", "88.8", 99.9f),
    INCLINATION(R.string.inclination, R.string.inclination_summary, "000.0", "888.8", 999.9f),
    ROOF_PITCH(R.string.roof_pitch, R.string.roof_pitch_summary, "00.000", "88.888", 99.999f);

    private int label;
    private int summary;
    private float max;
    private String displayFormat;
    private String displayBackgroundText;

    DisplayType(int label, int summary, String displayFormat, String displayBackgroundText, float max) {
        this.label = label;
        this.max = max;
        this.summary = summary;
        this.displayFormat = displayFormat;
        this.displayBackgroundText = displayBackgroundText;
    }

    public float getMax() {
        return max;
    }

    public int getSummary() {
        return summary;
    }

    public int getLabel() {
        return label;
    }

    public String getDisplayFormat() {
        return displayFormat;
    }

    public String getDisplayBackgroundText() {
        return displayBackgroundText;
    }

}
