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

package org.sandrop.webscarab.plugin.spider;

import org.sandrop.webscarab.model.HttpUrl;

/**
 *
 * @author  rdawes
 */
public class Link {
    
    private final HttpUrl _url;
    private final String _referer;
    
    /** Creates a new instance of Link */
    public Link(HttpUrl url, String referer) {
        _url = url;
        _referer = referer;
    }
    
    public HttpUrl getURL() {
        return _url;
    }
    
    public String getReferer() {
        return _referer;
    }
    
    public String toString() {
        return _url.toString() + " via " + _referer;
    }
}
