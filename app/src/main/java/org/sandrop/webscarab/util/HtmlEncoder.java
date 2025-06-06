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

package org.sandrop.webscarab.util;

import java.util.HashMap;
import java.util.Map;

public class HtmlEncoder
{
    static Map<Object, Object> e2i = new HashMap<Object, Object>();
    static Map<Object, Object> i2e = new HashMap<Object, Object>();

    // html entity list
    private static final Object[][] entities =
    {
        {"quot", Integer.valueOf(34)}, // " - double-quote
        {"amp", Integer.valueOf(38)}, // & - ampersand
        {"lt", Integer.valueOf(60)}, // < - less-than
        {"gt", Integer.valueOf(62)}, // > - greater-than
        {"nbsp", Integer.valueOf(160)}, // non-breaking space
        {"copy", Integer.valueOf(169)}, // � - copyright
        {"reg", Integer.valueOf(174)}, // � - registered trademark
        {"Agrave", Integer.valueOf(192)}, // � - uppercase A, grave accent
        {"Aacute", Integer.valueOf(193)}, // � - uppercase A, acute accent
        {"Acirc", Integer.valueOf(194)}, // � - uppercase A, circumflex accent
        {"Atilde", Integer.valueOf(195)}, // � - uppercase A, tilde
        {"Auml", Integer.valueOf(196)}, // � - uppercase A, umlaut
        {"Aring", Integer.valueOf(197)}, // � - uppercase A, ring
        {"AElig", Integer.valueOf(198)}, // � - uppercase AE
        {"Ccedil", Integer.valueOf(199)}, // � - uppercase C, cedilla
        {"Egrave", Integer.valueOf(200)}, // � - uppercase E, grave accent
        {"Eacute", Integer.valueOf(201)}, // � - uppercase E, acute accent
        {"Ecirc", Integer.valueOf(202)}, // � - uppercase E, circumflex accent
        {"Euml", Integer.valueOf(203)}, // � - uppercase E, umlaut
        {"Igrave", Integer.valueOf(204)}, // � - uppercase I, grave accent
        {"Iacute", Integer.valueOf(205)}, // � - uppercase I, acute accent
        {"Icirc", Integer.valueOf(206)}, // � - uppercase I, circumflex accent
        {"Iuml", Integer.valueOf(207)}, // � - uppercase I, umlaut
        {"ETH", Integer.valueOf(208)}, // � - uppercase Eth, Icelandic
        {"Ntilde", Integer.valueOf(209)}, // � - uppercase N, tilde
        {"Ograve", Integer.valueOf(210)}, // � - uppercase O, grave accent
        {"Oacute", Integer.valueOf(211)}, // � - uppercase O, acute accent
        {"Ocirc", Integer.valueOf(212)}, // � - uppercase O, circumflex accent
        {"Otilde", Integer.valueOf(213)}, // � - uppercase O, tilde
        {"Ouml", Integer.valueOf(214)}, // � - uppercase O, umlaut
        {"Oslash", Integer.valueOf(216)}, // � - uppercase O, slash
        {"Ugrave", Integer.valueOf(217)}, // � - uppercase U, grave accent
        {"Uacute", Integer.valueOf(218)}, // � - uppercase U, acute accent
        {"Ucirc", Integer.valueOf(219)}, // � - uppercase U, circumflex accent
        {"Uuml", Integer.valueOf(220)}, // � - uppercase U, umlaut
        {"Yacute", Integer.valueOf(221)}, // � - uppercase Y, acute accent
        {"THORN", Integer.valueOf(222)}, // � - uppercase THORN, Icelandic
        {"szlig", Integer.valueOf(223)}, // � - lowercase sharps, German
        {"agrave", Integer.valueOf(224)}, // � - lowercase a, grave accent
        {"aacute", Integer.valueOf(225)}, // � - lowercase a, acute accent
        {"acirc", Integer.valueOf(226)}, // � - lowercase a, circumflex accent
        {"atilde", Integer.valueOf(227)}, // � - lowercase a, tilde
        {"auml", Integer.valueOf(228)}, // � - lowercase a, umlaut
        {"aring", Integer.valueOf(229)}, // � - lowercase a, ring
        {"aelig", Integer.valueOf(230)}, // � - lowercase ae
        {"ccedil", Integer.valueOf(231)}, // � - lowercase c, cedilla
        {"egrave", Integer.valueOf(232)}, // � - lowercase e, grave accent
        {"eacute", Integer.valueOf(233)}, // � - lowercase e, acute accent
        {"ecirc", Integer.valueOf(234)}, // � - lowercase e, circumflex accent
        {"euml", Integer.valueOf(235)}, // � - lowercase e, umlaut
        {"igrave", Integer.valueOf(236)}, // � - lowercase i, grave accent
        {"iacute", Integer.valueOf(237)}, // � - lowercase i, acute accent
        {"icirc", Integer.valueOf(238)}, // � - lowercase i, circumflex accent
        {"iuml", Integer.valueOf(239)}, // � - lowercase i, umlaut
        {"igrave", Integer.valueOf(236)}, // � - lowercase i, grave accent
        {"iacute", Integer.valueOf(237)}, // � - lowercase i, acute accent
        {"icirc", Integer.valueOf(238)}, // � - lowercase i, circumflex accent
        {"iuml", Integer.valueOf(239)}, // � - lowercase i, umlaut
        {"eth", Integer.valueOf(240)}, // � - lowercase eth, Icelandic
        {"ntilde", Integer.valueOf(241)}, // � - lowercase n, tilde
        {"ograve", Integer.valueOf(242)}, // � - lowercase o, grave accent
        {"oacute", Integer.valueOf(243)}, // � - lowercase o, acute accent
        {"ocirc", Integer.valueOf(244)}, // � - lowercase o, circumflex accent
        {"otilde", Integer.valueOf(245)}, // � - lowercase o, tilde
        {"ouml", Integer.valueOf(246)}, // � - lowercase o, umlaut
        {"oslash", Integer.valueOf(248)}, // � - lowercase o, slash
        {"ugrave", Integer.valueOf(249)}, // � - lowercase u, grave accent
        {"uacute", Integer.valueOf(250)}, // � - lowercase u, acute accent
        {"ucirc", Integer.valueOf(251)}, // � - lowercase u, circumflex accent
        {"uuml", Integer.valueOf(252)}, // � - lowercase u, umlaut
        {"yacute", Integer.valueOf(253)}, // � - lowercase y, acute accent
        {"thorn", Integer.valueOf(254)}, // � - lowercase thorn, Icelandic
        {"yuml", Integer.valueOf(255)}, // � - lowercase y, umlaut
        {"euro", Integer.valueOf(8364)},// Euro symbol
    };
    
    
    static {
        for(int i=0; i<entities.length; i++)
            e2i.put(entities[i][0], entities[i][1]);
        for(int i=0; i<entities.length; i++)
            i2e.put(entities[i][1], entities[i][0]);
    }
    
    /**
     *  Turns funky characters into HTML entity equivalents<p>
     *
     *  e.g. <tt>"bread" & "butter"</tt> => <tt>&amp;quot;bread&amp;quot; &amp;amp;
     *  &amp;quot;butter&amp;quot;</tt> . Update: supports nearly all HTML entities, including funky
     *  accents. See the source code for more detail. Adapted from
     *  http://www.purpletech.com/code/src/com/purpletech/util/Utils.java.
     *
     * @param  s1  Description of the Parameter
     * @return     Description of the Return Value
     */
    public static String encode( String s1 )
    {
        StringBuffer buf = new StringBuffer();
        
        int i;
        for ( i = 0; i < s1.length(); ++i )
        {
            char ch = s1.charAt( i );

            String entity = (String) i2e.get(Integer.valueOf(ch));
            
            if ( entity == null )
            {
                if ( ch > 128 )
                {
                    buf.append( "&#" + ( (int) ch ) + ";" );
                }
                else
                {
                    buf.append( ch );
                }
            }
            else
            {
                buf.append( "&" + entity + ";" );
            }
        }

        return buf.toString();
    }

    
    /**
     *  Given a string containing entity escapes, returns a string containing the actual Unicode
     *  characters corresponding to the escapes. Adapted from
     *  http://www.purpletech.com/code/src/com/purpletech/util/Utils.java.
     *
     * @param  s1  Description of the Parameter
     * @return     Description of the Return Value
     */
    public static String decode( String s1 )
    {
        StringBuffer buf = new StringBuffer();

        int i;
        for ( i = 0; i < s1.length(); ++i )
        {
            char ch = s1.charAt( i );
            
            if ( ch == '&' )
            {
                int semi = s1.indexOf( ';', i + 1 );
                if ( semi == -1 )
                {
                    buf.append( ch );
                    continue;
                }
                String entity = s1.substring( i + 1, semi );
                Integer iso;
                if ( entity.charAt( 0 ) == '#' )
                {
                    iso = Integer.valueOf(entity.substring(1));
                }
                else
                {
                    iso = (Integer) e2i.get( entity );
                }
                if ( iso == null )
                {
                    buf.append( "&" + entity + ";" );
                }
                else
                {
                    buf.append( (char) ( iso.intValue() ) );
                }
                i = semi;
            }
            else
            {
                buf.append( ch );
            }
        }

        return buf.toString();
    }
}
