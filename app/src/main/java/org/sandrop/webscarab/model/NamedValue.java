/***********************************************************************
 *
 * This file is part of SandroProxy, 
 * For details, please see http://code.google.com/p/sandrop/
 *
 * Copyright (c) 2012 supp.sandrob@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Getting Source
 * ==============
 *
 * Source for this application is maintained at
 * http://code.google.com/p/sandrop/
 *
 * Software is build from sources of WebScarab project
 * For details, please see http://www.sourceforge.net/projects/owasp
 *
 */

package org.sandrop.webscarab.model;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author  rogan
 */
public class NamedValue {
    
    private final String _name;
    private final String _value;
    
    private static final Logger _logger = Logger.getLogger("org.owasp.webscarab.model.NamedValue");
    
    {
        _logger.setLevel(Level.INFO);
    }
    
    /** Creates a new instance of NamedValue */
    public NamedValue(String name, String value) {
        _name = name;
        _value = value;
    }
    
    public String getName() {
        return _name;
    }
    
    public String getValue() {
        return _value;
    }
    
    public String toString() {
        return _name + "='" + _value + "'";
    }
    
    public static NamedValue[] splitNamedValues(String source, String pairSeparator, String nvSeparator) {
        try {
            if (source == null || "".equals(source)) return new NamedValue[0];
            String[] pairs = source.split(pairSeparator);
            _logger.fine("Split \""+ source + "\" into " + pairs.length);
            NamedValue[] values = new NamedValue[pairs.length];
            for (int i=0; i<pairs.length; i++) {
                String[] nv = pairs[i].split(nvSeparator,2);
                if (nv.length == 2) { 
                    values[i] = new NamedValue(nv[0], nv[1]);
                } else if (nv.length == 1) {
                    values[i] = new NamedValue(nv[0], "");
                } else {
                    values[i] = null;
                }
            }
            return values;
        } catch (ArrayIndexOutOfBoundsException aioob) {
            _logger.warning("Error splitting \"" + source + "\" using '" + pairSeparator + "' and '" + nvSeparator + "'");
        }
        return new NamedValue[0];
    }
    
}
