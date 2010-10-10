// Install.java for 2.0.3
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

import java.util.*;
import java.io.*;
import org.w3c.jigsaw.http.httpd;
import org.w3c.jigsaw.pagecompile.PageCompileProp;
import org.w3c.www.protocol.http.cache.CacheFilter;
import org.w3c.www.protocol.http.cache.CacheStore;

public class Install {

    Properties http_props  = null;
    Properties admin_props  = null;
    Properties props = null;

    public final String CONFIG_DIR    = "config";
    public final String TRASH_DIR     = "trash";
    public final String CONFIGADM_DIR = "configadm";
    public final String HTTP_PROPS    = "http-server.props";
    public final String ADMIN_PROPS   = "admin-server.props";
    public final String SERVER_PROPS  = "server.props";
    public final String AUTH_DIR      = "auth";
    public final String INDEXERS_DIR  = "indexers";
    public final String JIGADM_ZIP    = "jigadm.zip";
    public final String JIGADMIN_ZIP  = "jigadmin.zip";
    public final String ICONS_DIR     = "icons";
    public final String STORES_DIR    = "stores";
    public final String CACHE_DIR     = "cache";
    public final String PC_DIR       = "compiledPages";
    public final String WWW_DIR       = "WWW";

    public final String comment = "Updated by Install";

    File config_dir       = null;
    File trash_dir        = null;
    File configadm_dir    = null;
    File http_props_file  = null;
    File admin_props_file = null;
    File www              = null;
    File root             = null;

    int warning = 0;

    //
    private File check(File file) {
	if (! file.exists()) {
	    warning++;
	    System.out.println("WARNING: "+file+" NOT FOUND!");
	    return null;
	}
	return file;
    }

    private void initialize () {
	root = check(root);
	trash_dir = check( new File(root, TRASH_DIR) );
	config_dir = check( new File(root, CONFIG_DIR) );
	www = check( new File (root, WWW_DIR) );
	if (config_dir != null) {
	    http_props_file = check( new File(config_dir, HTTP_PROPS) );
	    admin_props_file = check( new File(config_dir, ADMIN_PROPS) );
	    check( new File(config_dir, SERVER_PROPS) );
	    check( new File(config_dir, AUTH_DIR) );
	    check( new File(config_dir, INDEXERS_DIR) );
	    check( new File(config_dir, JIGADM_ZIP) );
            check( new File(config_dir, JIGADMIN_ZIP) );
	    check( new File(config_dir, STORES_DIR) );
	    check( new File(config_dir, ICONS_DIR) );
	}
	configadm_dir = check( new File(root, CONFIGADM_DIR) );
	if (configadm_dir != null) {
	    check ( new File(configadm_dir, STORES_DIR) );
	    check ( new File(configadm_dir, AUTH_DIR) );
	}
    }

    private void fail() {
	fail(null);
    }

    private void fail(String msg) {
	if (msg != null)
	    System.out.println("[Installation failed]: "+msg);
	else 
	    System.out.println("[Installation failed]");
	System.exit(1);
    }

    private Properties loadProps(File file) {
	Properties p = new Properties();
	FileInputStream in = null;
	try {
	    in = new FileInputStream (file);
	    p.load( in );
	} catch (Exception ex) {
	    fail(ex.getMessage());
	} finally { 
	    try { in.close(); } catch (Exception e) {}
	}
	return p;
    }

    private void saveProps(Properties p, File file) {
	FileOutputStream out = null;
	try {
	    out = new FileOutputStream (file);
	    p.save( out, comment );
	} catch (Exception ex) {
	    fail(ex.getMessage());
	} finally {
	    try { out.close(); } catch (Exception e) {}
	}
    }

    public void install() {
	initialize();
	if (root == null)
	    fail();
	if (http_props_file == null)
	    fail();
    
	//
	// http-server.props
	//
	http_props = loadProps(http_props_file);
	System.out.println("\t\t"+http_props.get(httpd.SERVER_SOFTWARE_P)+
			   " setup.\n");
	System.out.print("updating "+http_props_file+" ... ");
	try {
	    http_props.put(httpd.PROPS_P, http_props_file.getCanonicalPath());
	    http_props.put(httpd.ROOT_P, root.getCanonicalPath());
	    http_props.put(httpd.TRASHDIR_P, trash_dir.getCanonicalPath());
	    http_props.put(httpd.CONFIG_P, config_dir.getCanonicalPath());
	    http_props.put(httpd.ROOT_NAME_P, "root");
	    // cache directory
	    if (http_props.get(CacheStore.CACHE_DIRECTORY_P) != null) {
		File cacheD = new File(root, CACHE_DIR);
		if (! cacheD.exists())
		    cacheD.mkdir();
		http_props.put(CacheStore.CACHE_DIRECTORY_P, 
			       cacheD.getCanonicalPath()); 
	    }
	    // page compile directory
	    if (http_props.get("org.w3c.jigsaw.pagecompile.dir") != null) {
		File pcd = new File(root, PC_DIR);
		if (! pcd.exists())
		    pcd.mkdir();
		http_props.put("org.w3c.jigsaw.pagecompile.dir",
			       pcd.getCanonicalPath());
	    }
	    if (www != null)
		http_props.put(httpd.SPACE_P, www.getCanonicalPath());
	} catch (IOException ex ) {
	    fail(ex.getMessage());
	} finally {
	    try { 
		saveProps(http_props, http_props_file); 
	    } catch (Exception e) {}
	}
	System.out.println("done.");

	//
	// admin-server.props
	//
	if (admin_props_file == null)
	    fail();
	System.out.print("updating "+admin_props_file+" ...");
	admin_props = loadProps(admin_props_file);
	try {
	    admin_props.put(httpd.PROPS_P, 
			    admin_props_file.getCanonicalPath());
	    admin_props.put(httpd.ROOT_P, root.getCanonicalPath());
	    if (configadm_dir != null)
		admin_props.put(httpd.CONFIG_P, 
				configadm_dir.getCanonicalPath());
	    admin_props.put(httpd.ROOT_NAME_P, "root");
	} catch (IOException ex ) {
	    fail(ex.getMessage());
	} finally {
	    try { 
		saveProps(admin_props, admin_props_file); }
	    catch (Exception e) {
		System.out.println(e.getMessage());
	    }
	}
	//
	// Messages
	//
	System.out.println("done.");
	System.out.println("\nInstallation complete with "+
			   warning+" warning.\n");
	System.out.println("type 'java org.w3c.jigsaw.Main -root "+root+
			   "' to launch Jigsaw.\n");
	System.out.println("type 'java org.w3c.jigadmin.Main -root "+root+
			   " http://localhost:"+admin_props.get(httpd.PORT_P)+
			   "' to launch the new JigAdmin tool.");
        System.out.println("type 'java org.w3c.jigadm.Main -root "+root+
                           " http://localhost:"+admin_props.get(httpd.PORT_P)+
                           "' to launch the old JigAdmin tool (If you do not have JDK1.2)");
    }

    public Install () {
	props = System.getProperties();
	this.root = new File(props.getProperty("user.dir", null));
    }

    public static void main (String args[]) {
	(new Install()).install();
    }

}
