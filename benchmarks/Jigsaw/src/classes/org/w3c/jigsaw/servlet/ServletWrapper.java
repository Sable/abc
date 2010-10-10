// ServletWrapper.java
// $Id: ServletWrapper.java,v 1.77 2005/06/08 12:55:30 ylafon Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;

import org.w3c.tools.timers.EventHandler;
import org.w3c.tools.timers.EventManager;

import org.w3c.util.ArrayDictionary;
import org.w3c.util.EmptyEnumeration;
import org.w3c.util.ThreadCache;

import org.w3c.jigsaw.http.Reply;
import org.w3c.jigsaw.http.Request;
import org.w3c.jigsaw.http.httpd;

import org.w3c.tools.resources.Attribute;
import org.w3c.tools.resources.AttributeHolder;
import org.w3c.tools.resources.AttributeRegistry;
import org.w3c.tools.resources.FramedResource;
import org.w3c.tools.resources.InvalidResourceException;
import org.w3c.tools.resources.IntegerAttribute;
import org.w3c.tools.resources.LongAttribute;
import org.w3c.tools.resources.ObjectAttribute;
import org.w3c.tools.resources.PropertiesAttribute;
import org.w3c.tools.resources.Resource;
import org.w3c.tools.resources.ResourceReference;
import org.w3c.tools.resources.ServerInterface;
import org.w3c.tools.resources.StringAttribute;

import org.w3c.www.http.HTTP;

/**
 * @author Alexandre Rafalovitch <alex@access.com.au>
 * @author Anselm Baird-Smith <abaird@w3.org>
 * @author Benoit Mahe <bmahe@w3.org>
 * @author Yves Lafon <ylafon@w3.org>
 */

public class ServletWrapper extends FramedResource 
    implements ServletConfig
{

    static ThreadCache threadCache = null;

    static {
	threadCache = new ThreadCache("servlet-runner");
	threadCache.setCachesize(5);
	threadCache.setGrowAsNeeded(true);
	threadCache.setIdleTimeout(86400000);
	threadCache.initialize();
    }

    protected class TimeoutManager implements EventHandler {

	private Object timer     = null;
	private httpd  server    = null;

	/**
	 * Handle timer events. 
	 * @param data The timer closure.
	 * @param time The absolute time at which the event was triggered.
	 * @see org.w3c.tools.timers.EventManager
	 * @see org.w3c.tools.timers.EventHandler
	 */
	public void handleTimerEvent(Object data, long time) {
	    synchronized (this) {
		timer = null;
	    }
            // FIXME, each servlet instance available should have its 
	    // individual timeout manager.
            // Thus, resources could be released as required. This mechanism
	    // would opt for a more fine grained servlet instance management.
	    destroyServlet();
	}

	private synchronized void setTimer(long ms) {
	    if ( timer != null ) {
		server.timer.recallTimer(timer) ;
		timer = null ;
	    }
	    timer = server.timer.registerTimer(ms, this, null);
	}

	protected void restart() {
	    start();
	}

	protected void start() {
	    long timeout = getServletTimeout();
	    if (timeout != -1)
		setTimer(timeout);
	}

	protected synchronized void stop() {
	    if ( timer != null ) {
		server.timer.recallTimer(timer);
		timer = null;
	    }
	}
	
	TimeoutManager(httpd server) {
	    this.server = server;
	}

    }

    // used to pass the runner as a state
    public static final String RUNNER = "org.w3c.jigsaw.servlet.runner";
    // used to signal the end of the servlet in the Reply
    public static final String ENDED = "org.w3c.jigsaw.servlet.ended";

    protected TimeoutManager timeoutManager = null;

    // protected int connections = 0;

    protected final static boolean debug = false;

    /**
     * Attributes index - The servlet class name.
     */
    protected static int ATTR_SERVLET_CLASS = -1 ;
    /**
     * Attributes index - The servlet timeout
     */
    protected static int ATTR_SERVLET_TIMEOUT = -1 ;
    /**
     * Attributes index - The servlet maxinstances for single thread model
     * servlet instance pool size limitation, tk, 20.10.2001
     */
    protected static int ATTR_SERVLET_INSTANCEMAX = -1 ;
    /**
     * Attribute index - The init parameters for that servlet.
     */
    protected static int ATTR_PARAMETERS = 1;
    /**
     * Attribute index - Our parent-inherited servlet context.
     */
    protected static int ATTR_SERVLET_CONTEXT = -1;
    /**
     * Attribute index - Our parent-inherited session context.
     */
    protected static int ATTR_SESSION_CONTEXT = -1;

    static {
	Attribute a   = null ;
	Class     cls = null ;
	try {
	    cls = Class.forName("org.w3c.jigsaw.servlet.ServletWrapper") ;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}
	// The servlet class attribute.
	a = new StringAttribute("servlet-class"
				, null
				, Attribute.EDITABLE | Attribute.MANDATORY) ;
	ATTR_SERVLET_CLASS = AttributeRegistry.registerAttribute(cls, a) ;
	// This servlet init parameters
	a = new PropertiesAttribute("servlet-parameters"
				    , null
				    , Attribute.EDITABLE);
	ATTR_PARAMETERS = AttributeRegistry.registerAttribute(cls, a);
	// Our servlet context:
	a = new ObjectAttribute("servlet-context",
				"org.w3c.jigsaw.servlet.JigsawServletContext",
				null,
				Attribute.DONTSAVE);
	ATTR_SERVLET_CONTEXT = AttributeRegistry.registerAttribute(cls, a);
	// Our session context:
	a = new ObjectAttribute("session-context",
			     "org.w3c.jigsaw.servlet.JigsawHttpSessionContext",
				null,
				Attribute.DONTSAVE);
	ATTR_SESSION_CONTEXT = AttributeRegistry.registerAttribute(cls, a);
	// The servlet timeout:
	a = new LongAttribute("servlet-timeout",
			      null,
			      Attribute.EDITABLE);
	ATTR_SERVLET_TIMEOUT = AttributeRegistry.registerAttribute(cls, a);
	// The servlet maxinstances:
	a = new IntegerAttribute("servlet-instancemax",
				 null,
				 Attribute.EDITABLE);
	ATTR_SERVLET_INSTANCEMAX = AttributeRegistry.registerAttribute(cls, a);
    }

    /** 
     * Using a limited pool of servlet instances instead of a single 
     * instance, tk, 22.10.2001
     * protected Servlet servlet = null;
     */
    protected ServletPool servletPool = new ServletPool(); 
    
    /**
     * Is our servler initialized ?
     */
    protected boolean inited = false;

    /**
     * The Path where we can find the servlet class file.
     */
    public File getServletDirectory() {
	ResourceReference rr = getParent();
	if (rr != null) {
	    try {
		Resource parent = rr.lock();
		if (parent.definesAttribute("directory"))
		    return (File) parent.getValue("directory", null);
	    } catch(InvalidResourceException ex) {
		ex.printStackTrace();
	    } finally {
		rr.unlock();
	    }
	}
	return null;
    }

    /**
     * Servlet stub implementation - Get an init parameter value.
     */
    public synchronized String getInitParameter(String string) {
	ArrayDictionary d = getServletParameters();
	String          v = (d != null) ? (String) d.get(string) : null;
	return v;
    }

    /**
     * Servlet stub implementation - Get all init parameters.
     */
    public synchronized Enumeration getInitParameterNames() {
	// Convert init parameters to hashtable:
	ArrayDictionary d = getServletParameters();
	return (d != null) ? d.keys() : new EmptyEnumeration();
    }

    /**
     * Servlet stub implementation - Get that servlet context.
     */
    public ServletContext getServletContext() { 
	ServletContext ctxt = 
	    (ServletContext) getValue(ATTR_SERVLET_CONTEXT, null);
	return ctxt;
    }

    public JigsawHttpSessionContext getSessionContext() {
	return (JigsawHttpSessionContext) getValue(ATTR_SESSION_CONTEXT, null);
    }

    protected long getServletTimeout() {
	long timeout = getLong(ATTR_SERVLET_TIMEOUT, 0);
	if (timeout == 0) {
	    JigsawServletContext ctxt = 
		(JigsawServletContext) getServletContext();
	    timeout = ctxt.getServletTimeout();
	}
	return timeout;
    }

    protected int getInstanceMax() { 
        // added for single thread model 
	// servlet instance pool size limitation, tk, 20.10.2001
	int instancemax = getInt(ATTR_SERVLET_INSTANCEMAX, 1);
	if (instancemax < 1) {
	    JigsawServletContext ctxt = 
		(JigsawServletContext) getServletContext();
	    instancemax = ctxt.getServletInstanceMax();
	}
        if (instancemax < 1) {
	    return 1;
        } else {
	    return instancemax;
	}
    }

    protected void invalidateAllSession() {
	if (debug) {
	    System.out.println("Invalidate All Session...");
	}
	JigsawHttpSessionContext ctxt = getSessionContext();
	Enumeration _enum = ctxt.getIds();
	while (_enum.hasMoreElements()) {
	    ctxt.getSession((String)_enum.nextElement()).invalidate();
	}
    }

    /**
     * Check the servlet class, ans try to initialize it.
     * @exception ClassNotFoundException if servlet class can't be found.
     * @exception ServletException if servlet can't be initialized.
     */
    protected void checkServlet() 
	throws ClassNotFoundException, ServletException
    {
	synchronized(servletPool) { 
	    // tk, 20.10.2001, synchronized loading is necessary at 
	    // least for the non-SingleThreadModel
	    boolean classmodified = 
		getLocalServletLoader().classChanged(getServletClass());
	    
	    if ((!inited) ||
		classmodified ||
		// (servlet.getClass() != 
		//    getLocalServletLoader().loadClass(getServletClass()))
		(servletPool.getLoadedClass() != 
		 getLocalServletLoader().loadClass(getServletClass()))
		) {
		inited = launchServlet();
	    }
	}
    }

    protected boolean isInited() {
	return inited;
    }

    /**
     * Check and eventually load the servlet we are wrapping.
     * This method was added for replacing getServlet() during access checks
     * in order to do perform checks without accessing a servlet instance.
     * @return true if and only if the sevlet has successfully been loaded
     */
    public boolean isServletLoaded() {
	try {
	    checkServlet();
	} catch (Exception ex) {
	    if (debug) {
		ex.printStackTrace();
	    }
	}
	return inited;
    }
    
    /**
     * Helper class for managing a limited pool of servlet instances, 
     * tk, 20.10.2001
     * For the SingleThreadModel instance are created as required and 
     * retained up to a specified limit.
     * For the non-SingleThreadModel one instance only is created 
     * (in accordance with the servlet spec).
     * The first instance initializes the pool with its pars-pro-toto
     * class attributes.
     */
    private class ServletPool {
        
        // maximum pool size
        private int maximum = 1;
        
        // current pool capacity
        private int capacity = 0;
        
        // current pool usage level
        private int usage = 0;
        
        // list of servlet instances
        private Servlet[] pool = null;

        // indicator for SingleThreadModel
        private boolean singleThreaded = false;
        
        // common class of the servlet pool
        private Class loadedClass = null;

        
        /**
         * method for exporting the class common to all loaded servlet 
	 * instances
         * @return common loaded servlet class
         */
        protected Class getLoadedClass() {
            return loadedClass;
        }
        
        /**
         * method for adding a fresh servlet instance to the pool 
	 * (using external synchronization)
         * @param servlet the instance to be added to the pool
         */
        protected void add(Servlet servlet) {
            if ((pool == null)||(loadedClass == null)) {
		singleThreaded = (servlet instanceof SingleThreadModel);
		if (singleThreaded) maximum = getInstanceMax(); 
		else maximum = 1;
		loadedClass = servlet.getClass();
		pool = new Servlet[maximum];
            }
            if (capacity < maximum) {
		pool[capacity++] = servlet;
            }
        }
        
        /**
         * non-blocking method for removing a servlet instance from the
	 * pool (using external synchronization)
         * @return a removed servlet instance or null if the pool is empty
         */
        protected Servlet remove() {
	    if (capacity > usage) {
                Servlet servlet = pool[--capacity];
                if (capacity < 1) {
		    pool = null;
		    loadedClass = null;
		    singleThreaded = false;
		    maximum = 1;
                }
                return servlet;
	    }
	    else return null;
        }
        
        /**
         * blocking method for accessing a servlet instance from the pool
         * @exception ServletException thrown if the pool is not properly
	 * initialized
         */
        protected synchronized Servlet takeServlet() throws ServletException {
            if ((!inited)||(capacity < 1)||
		(pool == null)||(loadedClass == null)) {
		throw new ServletException("Accessing servlet without"+
					   " initialization.");
            }
            if (singleThreaded) {
		try {
		    while (true) {
			if (usage < capacity) {
			    return pool[usage++];
			} else {
			    if (capacity < maximum) {
				if (launchServlet(loadedClass)) {
				    notifyAll();
				    Thread.currentThread().yield(); 
                                    // give previous waiters a chance
				} else {
				    wait();
				}
			    } else {
				wait();
			    }
			}
		    }
		}
		catch (InterruptedException ex) {
		    throw new ServletException("Waiting for a free servlet"+
					       " instance interrupted.");
		}
            } else {
		// One instance only is used in non single thread case 
		// (cf. servlet api for details)
		usage++;
		return pool[0];
            }
        }
        
        /**
         * method for releasing a servlet instance into the pool after work
         * @param servlet the instance to be returned to the pool
         * @exception ServletException thrown if pool is not properly 
	 * initialized
         */
        protected synchronized void releaseServlet(Servlet servlet) 
	    throws ServletException 
	{
            if ((!inited)||(capacity < 1)||(pool == null)||
		   (loadedClass == null)) {
		throw new ServletException("Releasing servlet without"+
					   " initialization.");
            }
            if (usage > 0) {
		if (singleThreaded) {
		    pool[--usage] = servlet;
		    notifyAll();
		} else {
		    // In this case the servlet instance is shared, i.e. 
		    // we have a counting semaphore only.
		    usage--;
		}
            } else {
		throw new ServletException("Incorrect servlet release"+
					   " occurred.");
	    }
        }
        
        /**
         * method for referencing a servlet instance of the pool
         * This method was added for backward compatibility in order to 
	 * support the deprecated getServlet() method,
         * which is structurally not applicable to the pool mechanism, i.e. 
	 * it always returns null in
         * case of the SingleThreadModel (in accordance with the current 
	 * servlet spec)
         * @return a servlet reference or null
         */
        protected synchronized Servlet getRepresentative() {
            if ((!inited)||(capacity < 1)||(loadedClass == null)) {
		return null;
	    } else {
		if (singleThreaded) {
		    // FIXME, here we could also return pool[0],
		    // which is defined but probably taken.
		    // However, using pool[0] in a normal manner might 
		    // cause strange behavior 
		    // due to its single threaded design aspect.
		    return null;
		} else {
		    return pool[0];
		}
            }
        }
    }    
    
    protected class ServletRunner implements Runnable, EventHandler {
	Servlet srServlet = null;
	JigsawHttpServletRequest srReq = null;
	JigsawHttpServletResponse srResp = null;
	Thread t = null;
	private Object stimer = null;
	private httpd server = (httpd) getServer();
	/**
	 * Handle timer events. 
	 * @param data The timer closure.
	 * @param time The absolute time at which the event was triggered.
	 * @see org.w3c.tools.timers.EventManager
	 * @see org.w3c.tools.timers.EventHandler
	 */
	public void handleTimerEvent(Object data, long time) {
	    signalTimeout();
	}

	protected void signalTimeout() {
	    synchronized (this) {
		stimer = null;
	    }
	    // as the request timeouted, we interrupt the runner's thread
	    // (as this thread may be the cause of the timeout)
	    synchronized (this) {
		if (t != null) {
		    if (debug)
			System.out.println("Killing " +t);
		    t.interrupt();
		}
	    }
	}

	public void run() {
	    // name modified in order to keep track of who is running
	    // which instance, tk, 21.10.2001
	    synchronized (this) {
		t = Thread.currentThread();
	    }
	    stimer = server.timer.registerTimer(server.getRequestTimeOut(), 
						this, null);
	    String ts = t.getName();
	    t.setName("ServletRunner<" + ts +":"+ srServlet.hashCode() + ">");
	    // synchronization object
	    Object o = null;
	    try {
		Reply reply = srResp.getReply();
		if (reply != null) {
		    o = reply.getState(JigsawHttpServletResponse.MONITOR);
		}
		srServlet.service(srReq , srResp);
		// processing done, release the servlet
                try {
		    servletPool.releaseServlet(srServlet);
		} finally {
		    srServlet = null;
                }
		// and remove the timer
		if ( stimer != null ) {
		    server.timer.recallTimer(stimer);
		    stimer = null;
		}
		srResp.flushStream(true);
	    } catch (UnavailableException uex) {
		String message = null;
		srResp.setStatus(HTTP.SERVICE_UNAVAILABLE);
		if (uex.isPermanent()) {
		    message = "<h2>The servlet is permanently "+
			"unavailable :</h2>"+
			"Details: <b>"+uex.getMessage()+"</b>";
		    try {
			srResp.sendError(HTTP.SERVICE_UNAVAILABLE, message);
		    } catch (IOException ioex) {
			// not much to do now...
		    }
		} else {
		    int delay = uex.getUnavailableSeconds();
		    if (delay > 0) {
			message = "<h2>The servlet is temporarily "+
			    "unavailable :</h2>"+
			    "Delay : "+delay+
			    " seconds<br><br>Details: <b>"+
			    uex.getMessage()+"</b>";
			srResp.getReply().setRetryAfter(delay);
			try {
			    srResp.sendError(HTTP.SERVICE_UNAVAILABLE,message);
			} catch (IOException ioex) {
			    // not much to do now...
			}
		    } else {
			message = "<h2>The servlet is temporarily "+
			    "unavailable :</h2>"+
			    "Details: <b>"+uex.getMessage()+
			    "</b>";
			try {		
			    srResp.sendError(HTTP.SERVICE_UNAVAILABLE,message);
			} catch (IOException ioex) {
			    // not much to do now...
			}
		    }
		}
	    } catch (Exception ex) {
		if (debug) {
		    ex.printStackTrace();
		}
		if (srResp.isStreamObtained()) {
		    try {
			srResp.flushStream(false);
			OutputStream os = srResp.getRawOutputStream();
			if (os != null) {
			    synchronized (os) {
				PrintWriter pw = new PrintWriter(os);
				ex.printStackTrace(pw);
				pw.flush();
			    }
			}
			srResp.flushStream(true);
		    } catch (IOException ioex) {}
		} else {
		    try {
			srResp.sendError(HTTP.INTERNAL_SERVER_ERROR,
					 "Servlet has thrown exception:" + 
					 ex.toString());
		    } catch (IOException ioex) {
			// no stream to write on, fail silently
		    }
		}
	    } finally {
		if ( stimer != null ) {
		    server.timer.recallTimer(stimer);
		    stimer = null;
		}
                if (srServlet != null) {
		    try {
			servletPool.releaseServlet(srServlet);
		    }
		    catch (ServletException ex) {
			// ignore
		    }
                }
		// release the monitor waiting for the end of the reply setup
		if (o != null) {
		    synchronized (o) {
			o.notifyAll();
		    }
		}
		// revert name to original value
		t.setName(ts);
		srServlet = null;
		srReq = null;
		// adds the END state
		Reply r = srResp.getReply();
		if (r != null) {
		    r.setState(ENDED, new Object());
		}
		srResp = null;
	    }
	}
	
	ServletRunner(Servlet servlet,
		      JigsawHttpServletRequest request,
		      JigsawHttpServletResponse response) {
	    srServlet = servlet;
	    srReq = request;
	    srResp = response;
 	}
    }

    protected void service(Request request, Reply reply)
	throws ServletException, IOException
	{
	    JigsawHttpServletResponse jRes = null;
	    JigsawHttpServletRequest jReq = null;
	    /* modified due to servlet pooling, tk, 20.10.2001
	       if (servlet instanceof SingleThreadModel) {
	       synchronized (this) {
	       jRes = new JigsawHttpServletResponse(request, reply);
	       jReq = new JigsawHttpServletRequest(servlet, 
	       request, 
	       jRes,
	       getSessionContext());
	       jRes.setServletRequest(jReq);
	       try {
	       connections++;
	       // FIXME we should reuse a thread rather than
	       // reallocating one for every hit
	       ServletRunner runner = new ServletRunner(servlet, jReq, jRes);
	       reply.setState(RUNNER, runner);
	       runner.start();
	       } finally {
	       connections--;
	       }
	       }
	       } else {
	    */
	    jRes = new JigsawHttpServletResponse(request, reply);
            Servlet servlet = servletPool.takeServlet(); 
            // accessing a fresh or sharable instance, tk, 21.10.2001
	    // jReq = new JigsawHttpServletRequest(servlet,
	    JigsawServletContext jco = 
		                    (JigsawServletContext) getServletContext();
	    jReq = new JigsawHttpServletRequest(servlet, 
						jco,
						request, 
						jRes,
						getSessionContext());
	    jRes.setServletRequest(jReq);
	    // try {
  	    //       connections++;
	    // FIXME we should reuse a thread rather than
	    // reallocating one for every hit
	    // reallocating one for every hit
	    ServletRunner runner = new ServletRunner(servlet, jReq, jRes);
	    reply.setState(RUNNER, runner);
	    threadCache.getThread(runner, true);
//	    runner.start();
	    // } finally {
	    //     connections--;
	    // }
	    /* } */
	    timeoutManager.restart();
	}

    /**
     * Get the class name of the wrapped servlet.
     * @return The class name for the servlet if attribute is defined. 
     * Otherwise the class name is deduced from the resource identifier.
     */

    public String getServletClass()
	{
	    String sclass =  getString(ATTR_SERVLET_CLASS, null);
	    if (sclass == null) {
		String ident = getIdentifier();
		if (ident.endsWith(".class")) {
		    sclass = ident;
		}
	    }
	    return sclass;
	}

    /**
     * Get the init parameters for our wrapped servlet.
     * @return An ArrayDictionary instance if the attribute is defined, 
     * <strong>false</strong> otherwise.
     */

    public ArrayDictionary getServletParameters() {
	return (ArrayDictionary) getValue(ATTR_PARAMETERS, null);
    }

    protected void setValueOfSuperClass(int idx, Object value) {
	super.setValue(idx, value);
    }

    /**
     * Catch assignements to the servlet class name attribute.
     * <p>When a change to that attribute is detected, the servlet is
     * automatically reinitialized.
     */

    public void setValue(int idx, Object value) {
	super.setValue(idx, value);
	if (((idx == ATTR_SERVLET_CLASS) && (value != null)) ||
	    (idx == ATTR_PARAMETERS)) {
	    try {
                synchronized(servletPool) { 
                    // synchronization added, tk, 20.10.2001
    		    inited = launchServlet();
                }
	    } catch (Exception ex) {
		String msg = ("unable to set servlet class \""+
			      getServletClass()+
			      "\" : "+
			      ex.getMessage());
		getServer().errlog(msg);
	    }
	} if (idx == ATTR_SERVLET_TIMEOUT) {
	    timeoutManager.restart();
	}
    }

    /**
     * Destroy the servlet we are wrapping.
     */
    protected void destroyServlet() {
	// if ((servlet != null) && (connections < 1)) {
	synchronized(servletPool) {
	    Servlet servlet = servletPool.remove();
	    while (servlet != null) {
		servlet.destroy();
		// servlet = null;
		servlet = servletPool.remove();
	    }
	    inited = (servletPool.getLoadedClass() != null);
	}
	// }
    }

    /**
     * Get the servlet we are wrapping.
     * @return A servlet instance, if the servlet is alredy running, 
     * <strong>null</strong> otherwise.
     */
    public Servlet getServlet() {
	try {
	    checkServlet();
	} catch (Exception ex) {
	    if (debug)
		ex.printStackTrace();
	}
	return servletPool.getRepresentative();
    }
    
    /**
     * Initialize our servlet from the given (loaded) class.
     * @param cls The servlet loaded main class.
     * @return A boolean, <strong>true</strong> if servlet was successfully
     * initialised, <strong>false</strong> otherwise.
     * @exception ServletException if servlet can't be initialized.
     */

    protected boolean launchServlet(Class cls) 
	throws ServletException
	{
	    if (debug) {
		System.out.println("launching Servlet: "+getServletName());
	    }
	    Servlet servlet = null;
	    try {
		servlet = (Servlet) cls.newInstance();
		servlet.init((ServletConfig) this);
		timeoutManager.restart();
		// modified for servlet pool and executed in a context 
		// synchronized on the servlet pool, tk, 20.10.2001
		if (servlet != null) {
		    servletPool.add(servlet);
		    return true;
		} else {
		    return false;
		}
	    } catch (IllegalAccessException ex) {
		String msg = ("Illegal access during servlet instantiation, "+
			      ex.getClass().getName()+": "+
			      ex.getMessage());
		if ( debug ) {
		    ex.printStackTrace();
		}
		getServer().errlog(this, msg);
		// return false;
		throw new ServletException(msg, ex);
	    } catch (InstantiationException iex) {
		String msg = ("unable to instantiate servlet, "+
			      iex.getClass().getName()+": "+
			      iex.getMessage());
		if ( debug ) {
		    iex.printStackTrace();
		}
		getServer().errlog(this, msg);
		// return false;
		throw new ServletException(msg, iex);
	    } catch (ServletException nex) {
		String msg = ("Error while initializing servlet");
		getServer().errlog(this, msg);
		if ( debug ) {
		    nex.printStackTrace();
		}
		getServer().errlog(this, msg);
		// return false;
		throw nex;
	    } catch (Exception oex) {
		String msg = ("Error while loading servlet");
		getServer().errlog(this, msg);
		if ( debug ) {
		    oex.printStackTrace();
		}
		getServer().errlog(this, msg);
		// return false;
        throw new ServletException(msg, oex);
        }
	}

    /**
     * Check if the Servletclass wrapped is a Servlet class without
     * initializing it. (not the same than checkServlet).
     * used by the ServletIndexer.
     * @see org.w3c.jigsaw.servlet.ServletIndexer
     * @return A boolean.
     */
    protected boolean isWrappingAServlet() {
	String clsname = getServletClass();
	if ( clsname == null ) {
	    return false;
	}
	Class c = null;
	try {
	    c = getLocalServletLoader().loadClass(clsname, true);
	    Object o = c.newInstance();
	    return (o instanceof Servlet);
	} catch (Exception ex) {
	    return false;
	}
    }

    /**
     * Launch the servlet we are wrapping.
     * <p>This method either succeed, or the wrapper resource itself will fail
     * to initialize, acting as transparently as possible (in some sense).
     * @return A boolean, <strong>true</strong> if servlet launched.
     * @exception ClassNotFoundException if servlet class can't be found.
     * @exception ServletException if servlet can't be initialized.
     */

    protected boolean launchServlet() 
	throws ClassNotFoundException, ServletException
	{
	    // Get and check the servlet class:
	    // if ( servlet != null )
	    destroyServlet();
	    if (inited) { 
		String msg = "relaunching servlet failed due to incomplete \""
		    + getServletClass() + "\" cleanup.";
		getServer().errlog(this, msg); 
		return false;
	    } else {
		// Load appropriate servlet class:
		String clsname = getServletClass();
		if ( clsname == null ) {
		    getServer().errlog(this, "no servlet class attribute"+
				       " defined.");
		    return false;
		} else {
		    Class c = null;
		    try {
			if (getLocalServletLoader().classChanged(clsname)) {
			    createNewLocalServletLoader(true);
			    invalidateAllSession();
			}
			c = getLocalServletLoader().loadClass(clsname, true);
		    } catch (ClassNotFoundException ex) {
			String msg = ("unable to find servlet class \""+
				      getServletClass()+"\"");
			getServer().errlog(msg);
			// re throw the exception
			throw ex;
		    }
		    return (c != null) ? launchServlet(c) : false;
		}
	    }
	}
    
    public boolean acceptUnload() {
	// return (servlet == null);
        return (!inited);
    }
    
    public void notifyUnload() {
	if (timeoutManager != null) {
	    timeoutManager.stop();
	}
	destroyServlet();
    }

    /** 
     * Get or create a suitable LocalServletLoader instance to load 
     * that servlet.
     * @return A LocalServletLoader instance.
     */
    // singleton synchronized already performed at the servlet context.
    // protected synchronized AutoReloadServletLoader getLocalServletLoader() {
    protected AutoReloadServletLoader getLocalServletLoader() {
	JigsawServletContext ctxt = (JigsawServletContext) getServletContext();
	return ctxt.getLocalServletLoader();
    }

    protected AutoReloadServletLoader createNewLocalServletLoader(boolean 
								  keepold) 
    {
	JigsawServletContext ctxt = (JigsawServletContext) 
		                                      getServletContext();
	return ctxt.createNewLocalServletLoader(keepold);
    }

    /**
     * Returns the name of this servlet instance.
     * The name may be provided via server administration, assigned in the 
     * web application deployment descriptor, or for an unregistered (and thus
     * unnamed) servlet instance it will be the servlet's class name.
     * @return the name of the servlet instance
     */
    public String getServletName() {
	return getIdentifier();
    }

    /**
     * Initialize this servlet wrapper resource.
     * After the wrapper itself is inited, it performs the servlet 
     * initialzation.
     * @param values The default attribute values.
     */
    public void initialize(Object values[]) {
	super.initialize(values);
	// connections = 0;
	
	if (getServletContext() != null) {
	    timeoutManager = new TimeoutManager((httpd)getServer());
	    timeoutManager.start();
	}

	try {
	    registerFrameIfNone("org.w3c.jigsaw.servlet.ServletWrapperFrame",
				"servlet-wrapper-frame");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
