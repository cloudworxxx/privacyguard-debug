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

package org.sandrop.webscarab.httpclient;

import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.NtlmMessage;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;


import org.sandrop.webscarab.model.HttpUrl;
import org.sandrop.webscarab.model.NamedValue;
import org.sandrop.webscarab.model.Request;
import org.sandrop.webscarab.model.Response;
import org.sandrop.webscarab.util.Glob;

/** Creates a new instance of URLFetcher
 * @author rdawes
 */
public class URLFetcher implements HTTPClient {

    // These represent the SSL classes required to connect to the server.
    private String _keyFingerprint = null;
    private SSLContextManager _sslContextManager = null;

    private final Logger _logger = Logger.getLogger(getClass().getName());

    private String _httpProxy = "";
    private int _httpProxyPort = -1;
    private String _httpsProxy = "";
    private int _httpsProxyPort = -1;
    private String[] _noProxy = new String[0];

    private String _localDomainName = null;
    
    private Socket _socket = null;
    private boolean _direct = false;
    private Response _response = null;

    // these represent an already connected socket, and the end point thereof.
    private InputStream _in = null;
    private OutputStream _out = null;
    private String _host = null;
    private int _port = 0;
    private long _lastRequestTime = 0;

    private int _timeout = 0;
    private int _connectTimeout = 10000;

    private Authenticator _authenticator = null;
    private String _authCreds = null;
    private String _proxyAuthCreds = null;
    
    private static Map<String, String> _proxyAuthCredsBasic;
    private static Map<String, InetAddress> _cachedLocalAddresses;
    
    private static final boolean LOGD = false;

    /** Creates a new instance of URLFetcher
     */
    public URLFetcher() {
        _logger.setLevel(Level.FINEST);
        if (_proxyAuthCredsBasic == null){
            _proxyAuthCredsBasic = new HashMap<String, String>();
        }
        if (_cachedLocalAddresses == null){
            _cachedLocalAddresses = new HashMap<String, InetAddress>();
        }
    }
    
    public static void cleanCachedBasicCredentials(){
        _proxyAuthCredsBasic = new HashMap<String, String>();
        _cachedLocalAddresses = new HashMap<String, InetAddress>();
    }
    
    public void setLocalDomainName(String domainName){
        _localDomainName = domainName;
    }

    /** Tells URLFetcher which HTTP proxy to use, if any
     * @param proxy The address or name of the proxy server to use for HTTP requests
     * @param proxyport The port on the proxy server to connect to
     */
    public void setHttpProxy(String proxy, int proxyport) {
        _httpProxy = proxy;
        if (_httpProxy == null) _httpProxy = "";
        _httpProxyPort = proxyport;
    }

    /** Tells URLFetcher which HTTPS proxy to use, if any
     * @param proxy The address or name of the proxy server to use for HTTPS requests
     * @param proxyport The port on the proxy server to connect to
     */
    public void setHttpsProxy(String proxy, int proxyport) {
        _httpsProxy = proxy;
        if (_httpsProxy == null) _httpsProxy = "";
        _httpsProxyPort = proxyport;
    }

    /** Accepts an array of hostnames or domains for which no proxy should be used.
     * if the hostname begins with a period ("."), than all hosts in that domain will
     * ignore the configured proxies
     * @param noproxy An array of hosts or domains for which no proxy should be used.
     * Domains must start with a period (".")
     */
    public void setNoProxy(String[] noproxy) {
        if (noproxy == null) {
            _noProxy = new String[0];
        } else if (noproxy.length == 0) {
            _noProxy = noproxy;
        } else {
            _noProxy = new String[noproxy.length];
            System.arraycopy(noproxy, 0, _noProxy, 0, noproxy.length);
        }
    }

    public void setSSLContextManager(SSLContextManager sslContextManager) {
        _sslContextManager = sslContextManager;
    }

    public void setTimeouts(int connectTimeout, int readTimeout) {
        _connectTimeout = connectTimeout;
        _timeout = readTimeout;
    }

    public void setAuthenticator(Authenticator authenticator) {
        _authenticator = authenticator;
    }

    public Authenticator getAuthenticator() {
        return _authenticator;
    }

    /** Can be used by a calling class to fetch a request without spawning an additional
     * thread. This is appropriate when the calling class is already running in an
     * independant thread, and must wait for the response before continuing.
     * @return the retrieved response
     * @param request the request to retrieve.
     */
    public Response fetchResponse(Request request) throws IOException {
        if (_response != null) {
            _response.flushContentStream(); // flush the content stream, just in case it wasn't read
            _response.clean();
            _response = null;
        }
        if (request == null) {
            _logger.severe("Asked to fetch a null request");
            return null;
        }
        HttpUrl url = request.getURL();
        if (url == null) {
            _logger.severe("Asked to fetch a request with a null URL");
            return null;
        }

        // if the previous auth method was not "Basic", force a new connection
        // we try to keep previous auth header so on basic we send it on first request
//        if (_authCreds != null){
//            if(_authCreds.startsWith("Basic")){
//                if (request.getHeader("Authorization") != null){
//                    _authCreds = request.getHeader("Authorization");
//                }
//            }else{
//                _lastRequestTime = 0;
//                _authCreds = request.getHeader("Authorization");
//            }
//            
//        }
        
        if (_authCreds != null && !_authCreds.startsWith("Basic"))
            _lastRequestTime = 0;
        
        if (_proxyAuthCreds != null){
            if(_proxyAuthCreds.startsWith("Basic")){
                if (request.getHeader("Proxy-Authorization") != null){
                    _proxyAuthCreds = request.getHeader("Proxy-Authorization");
                }
            }else{
                _lastRequestTime = 0;
                _proxyAuthCreds = request.getHeader("Proxy-Authorization");
            }
            
        }
        
        if (!_direct &&  _proxyAuthCreds == null && _proxyAuthCredsBasic.containsKey(_httpProxy + _httpProxyPort)){
            _proxyAuthCreds = _proxyAuthCredsBasic.get(_httpProxy + _httpProxyPort);
        }
        
        // Get any provided credentials from the request
        _authCreds = request.getHeader("Authorization");
//        _proxyAuthCreds = request.getHeader("Proxy-Authorization");
        
        String keyFingerprint = request.getHeader("X-SSLClientCertificate");
        request.deleteHeader("X-SSLClientCertificate");
        if (keyFingerprint == null && _keyFingerprint == null) {
            // no problem
        } else if (keyFingerprint != null && keyFingerprint.equals(_keyFingerprint)) {
            // no problem
        } else {
            // force a new connection, and change the fingerprint
            _keyFingerprint = keyFingerprint;
            _lastRequestTime = 0;
        }

        String status;

        String oldProxyAuthHeader = null;
        if (_proxyAuthCreds == null && _authenticator!= null && useProxy(url))
            _proxyAuthCreds = _authenticator.getProxyCredentials(url.toString().startsWith("https") ? _httpsProxy : _httpProxy, null);
        String proxyAuthHeader = constructAuthenticationHeader(null, _proxyAuthCreds, url.getPath(), request.getMethod());

        String oldAuthHeader = null;
        if (_authCreds == null && _authenticator!= null)
            _authCreds = _authenticator.getCredentials(url, null);
        String authHeader = constructAuthenticationHeader(null, _authCreds, url.getPath(), request.getMethod());

        int tries = 0;
        do {
            // make sure that we have a "clean" request each time through
            request.deleteHeader("Authorization");
            request.deleteHeader("Proxy-Authorization");

            _response = null;
            connect(url, true);
            if (_response != null) { // there was an error opening the socket
                return _response;
            }

            if (authHeader != null) {
                request.setHeader("Authorization", authHeader);
                if (authHeader.startsWith("NTLM") || authHeader.startsWith("Negotiate")) {
                    if (request.getVersion().equals("HTTP/1.0")) {
                        // we have to explicitly tell the server to keep the connection alive for 1.0
                        request.setHeader("Connection", "Keep-Alive");
                    } else {
                        request.deleteHeader("Connection");
                    }
                }
            }
            // depending on whether we are connected directly to the server, or via a proxy
            if (_direct) {
                request.writeDirect(_out);
            } else {
                if (proxyAuthHeader != null) {
                    request.setHeader("Proxy-Authorization", proxyAuthHeader);
                    if (proxyAuthHeader.startsWith("NTLM") || proxyAuthHeader.startsWith("Negotiate")) {
                        if (request.getVersion().equals("HTTP/1.0")) {
                            // we have to explicitly tell the server to keep the connection alive for 1.0
                            request.setHeader("Connection", "Keep-Alive");
                        } else {
                            request.deleteHeader("Connection");
                        }
                    }
                }
                request.write(_out);
            }
            _out.flush();
            _logger.finest("Request : \n" + request);

            _response = new Response();
            _response.setRequest(request);

            // test for spurious 100 header from IIS 4 and 5.
            // See http://mail.python.org/pipermail/python-list/2000-December/023204.html
            _logger.fine("Reading the response");
            do {
                _response.read(_in);
                status = _response.getStatus();
            } while (status.equals("100"));

            {
                StringBuffer buff = new StringBuffer();
                buff.append(_response.getStatusLine()).append("\n");
                NamedValue[] headers = _response.getHeaders();
                if (headers != null)
                    for (int i=0; i< headers.length; i++)
                        buff.append(headers[i].getName()).append(": ").append(headers[i].getValue()).append("\n");
                _logger.finest("Response:\n" + buff);
            }

            if (status.equals("407")) {
                if ( _proxyAuthCredsBasic.containsKey(_httpProxy + _httpProxyPort) && proxyAuthHeader != null &&proxyAuthHeader.startsWith("Basic")){
                    _proxyAuthCredsBasic.remove(_httpProxy + _httpProxyPort);
                }
                
                _response.flushContentStream();
                oldProxyAuthHeader = proxyAuthHeader;
                String[] challenges = _response.getHeaders("Proxy-Authenticate");
                if (_proxyAuthCreds == null && _authenticator != null) {
                    _proxyAuthCreds = _authenticator.getProxyCredentials(_httpProxy, challenges);
                }
                proxyAuthHeader = constructAuthenticationHeader(challenges, _proxyAuthCreds, url.getPath(), request.getMethod());
                if (oldProxyAuthHeader != null && oldProxyAuthHeader.equals(proxyAuthHeader)) {
                    _logger.info("No possible authentication");
                    proxyAuthHeader = null;
                }
            }

            if (status.equals("401")) {
                _response.flushContentStream();
                oldAuthHeader = authHeader;
                String[] challenges = _response.getHeaders("WWW-Authenticate");
                if (_authCreds == null && _authenticator != null)
                    _authCreds = _authenticator.getCredentials(url, challenges);
                _logger.finer("Auth creds are " + _authCreds);
                authHeader = constructAuthenticationHeader(challenges, _authCreds, url.getPath(), request.getMethod());
                _logger.finer("Auth header is " + authHeader);
                if (oldAuthHeader != null && oldAuthHeader.equals(authHeader)) {
                    _logger.info("No possible authentication");
                    authHeader = null;
                }
            }

            // if the request method is HEAD, we get no contents, EVEN though there
            // may be a Content-Length header.
            if (request.getMethod().equals("HEAD")) _response.setNoBody();

            _logger.info(request.getURL() +" : " + _response.getStatusLine());

            String connection = _response.getHeader("Proxy-Connection");
            if ("close".equalsIgnoreCase(connection)) {
                _in = null;
                _out = null;
                // do NOT close the socket itself, since the message body has not yet been read!
            } else {
                connection = _response.getHeader("Connection");
                String version = _response.getVersion();
                if (version.equals("HTTP/1.0") && "Keep-alive".equalsIgnoreCase(connection)) {
                    _lastRequestTime = System.currentTimeMillis();
                    _response.setSocket(_socket);
                } else if (version.equals("HTTP/1.1") && (connection == null || !connection.equalsIgnoreCase("Close"))) {
                    _lastRequestTime = System.currentTimeMillis();
                    if (status.equals("101")) _response.setNoBody();
                    _response.setSocket(_socket);
                } else {
                    _logger.info("Closing connection!");
                    _in = null;
                    _out = null;
                    // do NOT close the socket itself, since the message body has not yet been read!
                }
            }
            tries ++;
        } while (tries < 3 && ((status.equals("401") && authHeader != null) || (status.equals("407") && proxyAuthHeader != null)));

        if (_authCreds != null)
            request.setHeader("Authorization", _authCreds);
        if (_proxyAuthCreds != null) 
            request.setHeader("Proxy-Authorization", _proxyAuthCreds);
        if (_keyFingerprint != null)
            request.setHeader("X-SSLClientCertificate", _keyFingerprint);

        if (!_direct && !status.equals("407") && !status.equals("401") && _proxyAuthCreds != null && _proxyAuthCreds.startsWith("Basic") && !_proxyAuthCredsBasic.containsKey(_httpProxy + _httpProxyPort) ){
            _proxyAuthCredsBasic.put(_httpProxy + _httpProxyPort, proxyAuthHeader);
        }
        return _response;
    }
    
    public Socket getConnectedSocket(HttpUrl url, boolean makeHandshake) throws IOException{
        _socket = null;
        connect(url, makeHandshake);
        return _socket;
    }
    
    private InetSocketAddress getSocketAddress(String host, int port) throws UnknownHostException {
        InetSocketAddress hostSocketAddress = null;
        InetAddress address = null;
        UnknownHostException hostException = null;
        try {
            if (_cachedLocalAddresses.containsKey(host)){
                address = _cachedLocalAddresses.get(host);
            }else{
                address = InetAddress.getByName(host);
            }

        } catch (UnknownHostException e) {
            hostException = e;
            if (_localDomainName != null && _localDomainName.length() > 0){
                String hostDomainName = host + "." + _localDomainName;
                address = InetAddress.getByName(hostDomainName);
                try{
                    _cachedLocalAddresses.put(host, address);
                }catch(Exception ex){
                }
            }
            e.printStackTrace();
        }
        if (address != null){
            hostSocketAddress = new InetSocketAddress(address, port);
        }else{
            if (hostException != null){
                throw hostException;
            }
        }
        return hostSocketAddress;
    }

    private void connect(HttpUrl url, boolean makeSslHandshake) throws IOException {
        if (! invalidSocket(url)) return;
        _logger.fine("Opening a new connection");
        _socket = new Socket(java.net.Proxy.NO_PROXY);
        _socket.setSoTimeout(_timeout);
        _direct = true;

        // We record where we are connected to, in case we might reuse this socket later
        _host = url.getHost();
        _port = url.getPort();
        boolean ssl = url.getScheme().equalsIgnoreCase("https");

        if (useProxy(url)) {
            if (!ssl) {
                _logger.fine("Connect to " + _httpProxy + ":" + _httpProxyPort);
                _socket.connect(getSocketAddress(_httpProxy, _httpProxyPort), _connectTimeout);
                _in = _socket.getInputStream();
                _out = _socket.getOutputStream();
                _direct = false;
            } else {
                _socket.connect(getSocketAddress(_httpsProxy, _httpsProxyPort), _connectTimeout);
                _in = _socket.getInputStream();
                _out = _socket.getOutputStream();
                String oldAuthHeader = null;
                String connectMethod = "CONNECT";
                if (_proxyAuthCreds == null && _proxyAuthCredsBasic.containsKey(_httpsProxy+_httpsProxyPort)){
                    _proxyAuthCreds = _proxyAuthCredsBasic.get(_httpsProxy+_httpsProxyPort);
                }
                String authHeader = constructAuthenticationHeader(null, _proxyAuthCreds, _host + ":" + _port, connectMethod);
                String status;
                int loopCountMax = 10;
                int loopCount = 0;
                boolean startNewConnection = false;
                do {
                    // we create new connection if we receive response header to do so
                    if (startNewConnection){
                        
                        // clean up previous socket objects
                        if (_in != null) _in.close();
                        if (_out != null) _out.close();
                        if (_socket != null) _socket.close();
                        _in = null; _out = null; _socket = null;
                        
                        // creating new socket to proxy
                        _socket = new Socket();
                        _socket.setSoTimeout(_timeout);
                        _socket.connect(getSocketAddress(_httpsProxy, _httpsProxyPort), _connectTimeout);
                        _in = _socket.getInputStream();
                        _out = _socket.getOutputStream();
                        startNewConnection = false;
                    }
                    _out.write((connectMethod + " " + _host + ":" + _port + " HTTP/1.0\r\n").getBytes());
                    // setting headers to alive connection on http 1.0
                    _out.write(("Proxy-Connection: " + "Keep-Alive\r\n").getBytes());
                    _out.write(("Connection: " + "Keep-Alive\r\n").getBytes());
                    if (authHeader != null) {
                        _out.write(("Proxy-Authorization: " + authHeader + "\r\n").getBytes());
                    }
                    _out.write("\r\n".getBytes());
                    _out.flush();
                    _logger.fine("Sent CONNECT, reading Proxy response");
                    Response response = new Response();
                    response.read(_in);
                    _logger.fine("Got proxy response " + response.getStatusLine());
                    status = response.getStatus();
                    if (status.equals("407")) {
                        response.flushContentStream();
                        oldAuthHeader = authHeader;
                        String[] challenges = response.getHeaders("Proxy-Authenticate");
                        if (_proxyAuthCreds == null && _authenticator != null)
                            _proxyAuthCreds = _authenticator.getProxyCredentials(_httpsProxy, challenges);
                        if (_proxyAuthCreds == null) {
                            _response = response;
                            return;
                        }
                        authHeader = constructAuthenticationHeader(challenges, _proxyAuthCreds, _host + ":" + _port, connectMethod);
                        if (authHeader == null || oldAuthHeader != null && oldAuthHeader.equals(authHeader)) {
                            _response = response;
                            return;
                        }
                        String[] headerConnection = response.getHeaders("Connection");
                        String[] headerProxyConnection = response.getHeaders("Proxy-Connection");
                        if(headerConnection != null && headerConnection.length > 0){
                            String val = headerConnection[0];
                            if (val != null && val.trim().equalsIgnoreCase("close")){
                                startNewConnection = true;
                            }
                        }
                        if(headerProxyConnection != null && headerProxyConnection.length > 0){
                            String val = headerProxyConnection[0];
                            if (val != null && val.trim().equalsIgnoreCase("close")){
                                startNewConnection = true;
                            }
                        }
                        String httpVersion = response.getVersion();
                        if (httpVersion != null && headerConnection == null && headerProxyConnection == null && httpVersion.trim().equalsIgnoreCase("HTTP/1.0")){
                            startNewConnection = true;
                        }
                    }
                    loopCount++;
                } while (status.equals("407") && authHeader != null && loopCount < loopCountMax);
                if (loopCount >= loopCountMax){
                    _logger.finest("HTTPS CONNECT looping count reached " + loopCountMax);
                    _proxyAuthCredsBasic.remove(_httpsProxy+_httpsProxyPort);
                }else{
                    _logger.fine("HTTPS CONNECT successful");
                }
            }
        } else {
            _logger.fine("Connect to " + _host + ":" + _port );
            _socket.connect(getSocketAddress(_host, _port), _connectTimeout);
        }

        if (ssl && makeSslHandshake) {
            // if no fingerprint is specified, get the default one
            if (_keyFingerprint == null)
                _keyFingerprint = _sslContextManager.getDefaultKey();
            _logger.fine("Key fingerprint is " + _keyFingerprint);
            // get the associated context manager
            SSLContext sslContext = _sslContextManager.getSSLContext(_keyFingerprint);
            if (sslContext == null)
                throw new IOException("No SSL cert found matching fingerprint: " + _keyFingerprint);
            // Use the factory to create a secure socket connected to the
            // HTTPS port of the specified web server.
            try {
                SSLSocketFactory factory = sslContext.getSocketFactory();
                SSLSocket sslsocket=(SSLSocket)factory.createSocket(_socket,_socket.getInetAddress().getHostName(),_socket.getPort(),true);
                // sslsocket.setEnabledProtocols(new String[] {"SSLv3"});
                sslsocket.setUseClientMode(true);
                _socket = sslsocket;
                _socket.setSoTimeout(_timeout);
            } catch (IOException ioe) {
                _logger.severe("Error layering SSL over the existing socket: " + ioe);
                throw ioe;
            }
            _logger.fine("Finished negotiating SSL");
        }
        _in = _socket.getInputStream();
        _out = _socket.getOutputStream();
    }

    private boolean useProxy(HttpUrl url) {
        String host = url.getHost();
        boolean ssl = url.getScheme().equalsIgnoreCase("https");

        if (ssl && "".equals(_httpsProxy)) {
            return false;
        } else if (!ssl && "".equals(_httpProxy)) {
            return false;
        } else {
            for (int i=0; i<_noProxy.length; i++) {
                if (_noProxy[i].startsWith(".") && host.endsWith(_noProxy[i])) {
                    return false;
                } else if (_noProxy[i].equals("<local>") && host.indexOf('.') < 0) {
                    return false;
                } else if (host.equals(_noProxy[i])) {
                    return false;
                } else {
                    try {
                        if (host.matches(Glob.globToRE(_noProxy[i]))) return false;
                    } catch (Exception e) {
                        // fail silently
                    }
                }
            }
        }
        return true;
    }

    private boolean invalidSocket(HttpUrl url) {
        if (_host == null || _in == null) return true; // _out may be null if we are testing
        // the right host
        if (url.getHost().equals(_host)) {
            int urlport = url.getPort();
            // and the right port
            if (urlport == _port) {
                // in the last 1 second, it could still be valid
                long now = System.currentTimeMillis();
                if (now - _lastRequestTime > 1000) {
                    _logger.fine("Socket has expired (" + (now - _lastRequestTime) + "), open a new one!");
                    return true;
                } else if (_socket.isOutputShutdown() || _socket.isClosed()) {
                    _logger.fine("Existing socket is closed");
                    return true;
                } else {
                    _logger.fine("Existing socket is valid, reusing it!");
                    return false;
                }
            } else {
                _logger.fine("Previous request was to a different port");
            }
        } else {
            _logger.fine("Previous request was to a different host");
        }
        return true;
    }

    private String constructAuthenticationHeader(String[] challenges, String credentials, String url, String method) {
        /* credentials string looks like:
         * Basic BASE64(username:password)
         * or
         * Digest BASE64(username:password
         * or)
         * NTLM BASE64(domain\ username:password)
         */
        // _logger.info("Constructing auth header from " + credentials);
        if (credentials == null)
            return null;
        if (credentials.startsWith("Basic")) {
            return credentials;
        }
        if (credentials.startsWith("Digest") && challenges != null && challenges.length > 0){
            // digest handshake
            String proxyAuthHeader = challenges[0];
            int i = proxyAuthHeader.indexOf(' ');
            String authParameters = proxyAuthHeader.substring(i + 1);
            String realm = "";
            String nonce = "";
            String QOP   = "";
            String algorithm = "";
            String opaque = "";
            boolean stale;
            if (authParameters != null) {
                int parPos;
                do {
                    parPos = authParameters.indexOf(',');
                    if (parPos < 0) {
                        // have only one parameter
                        // parseParameter(authParameters);
                    } else {
                        String tokenVal = authParameters.substring(0,parPos);
                        int tokenValPos = tokenVal.indexOf('=');
                        if (tokenValPos >= 0) {
                            String token = tokenVal.substring(0, tokenValPos).trim();
                            String value = AuthDigestManager.trimDoubleQuotesIfAny(tokenVal.substring(tokenValPos + 1).trim());
                            
                            if (token.equalsIgnoreCase(AuthDigestManager.REALM_TOKEN)) {
                                realm = value;
                            }
                            if (token.equalsIgnoreCase(AuthDigestManager.NONCE_TOKEN)) {
                                nonce = value;
                            }
                            if (token.equalsIgnoreCase(AuthDigestManager.STALE_TOKEN)) {
                                if (value.equalsIgnoreCase("true")) {
                                    stale = true;
                                }
                            }
                            if (token.equalsIgnoreCase(AuthDigestManager.OPAQUE_TOKEN)) {
                                opaque = value;
                            }
                            if (token.equalsIgnoreCase(AuthDigestManager.QOP_TOKEN)) {
                                QOP = value.toLowerCase();
                            }
                            if (token.equalsIgnoreCase(AuthDigestManager.ALGORITHM_TOKEN)) {
                                algorithm = value.toLowerCase();
                            }
                            authParameters = authParameters.substring(parPos + 1);
                        }
                    }
                } while (parPos >= 0);
            }
            String userNamePassword = credentials.substring(credentials.indexOf(" ") + 1);
            userNamePassword = new String(Base64.decode(userNamePassword)); // decode the base64
            String username = userNamePassword.substring(0, userNamePassword.indexOf(":"));
            String password = userNamePassword.substring(username.length() + 1);
            return "Digest " + AuthDigestManager.computeDigestAuthResponse(username, password, realm, nonce, QOP, algorithm, opaque, method, url);
        }
        if (challenges != null) {
            for (int i=0; i<challenges.length; i++) {
                if (LOGD) _logger.fine("Challenge: " + challenges[i]);
                if (challenges[i].startsWith("NTLM") && credentials.startsWith("NTLM")) {
                    return attemptNegotiation(challenges[i], credentials);
                }
                if (challenges[i].startsWith("Negotiate") && credentials.startsWith("Negotiate")) {
                    if (LOGD) _logger.fine("Attempting 'Negotiate' Authentication");
                    return attemptNegotiation(challenges[i], credentials);
                }
                if (LOGD) _logger.info("Can't do auth for " + challenges[i]);
            }
        }
        return credentials;
    }
    
    private String attemptNegotiation(String challenge, String credentials) {
        String authMethod = null;
        String authorization = null;
        if (challenge.startsWith("NTLM")) {
            if (challenge.length() == 4) {
                authMethod = "NTLM";
            }
            if (challenge.indexOf(' ') == 4) {
                authMethod = "NTLM";
                authorization = challenge.substring(5).trim();
            }
        } else if (challenge.startsWith("Negotiate")) {
            if (challenge.length() == 9) {
                authMethod = "Negotiate";
            }
            if (challenge.indexOf(' ') == 9) {
                authMethod = "Negotiate";
                authorization = challenge.substring(10).trim();
            }
        }
        if (authMethod == null) return null;
        NtlmMessage message = null;
        if (authorization != null) {
            try {
                message = new Type2Message(Base64.decode(authorization));
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return null;
            }
        }
        // reconnect();
        int flags = NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2 | NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN | NtlmFlags.NTLMSSP_NEGOTIATE_NTLM | NtlmFlags.NTLMSSP_REQUEST_TARGET | NtlmFlags.NTLMSSP_NEGOTIATE_OEM | NtlmFlags.NTLMSSP_NEGOTIATE_UNICODE;
        if (message == null) {
            message = new Type1Message(flags, null, null);
        } else {
            credentials = credentials.substring(authMethod.length()+1); // strip off the "NTLM " or "Negotiate "
            credentials = new String(Base64.decode(credentials)); // decode the base64
            String domain = credentials.substring(0, credentials.indexOf("\\"));
            String user = credentials.substring(domain.length()+1, credentials.indexOf(":"));
            String password = credentials.substring(domain.length()+user.length()+2);
            Type2Message type2 = (Type2Message) message;
            flags ^= NtlmFlags.NTLMSSP_NEGOTIATE_OEM;
            message = new Type3Message(type2, password, domain, user, null, flags);
        }
        return authMethod + " " + Base64.encode(message.toByteArray());
    }

}
