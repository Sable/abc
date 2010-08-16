package tests;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.TestCase;
import AST.ASTNode;
import AST.ClassMonitor;
import AST.FieldDeclaration;
import AST.FieldMonitor;
import AST.Monitor;
import AST.MonitorAction;
import AST.Program;
import AST.SimpleSet;
import AST.TypeDecl;

public class LockRefactoringTests extends TestCase {
	final static String BASE = "/home/xiemaisi/RefactoringForConcurrency";
	public static int involves_library = 0,
					  ambiguous_action = 0,
					  not_modifiable = 0;
	
	public LockRefactoringTests(String name) {
		super(name);
	}

	private static void refactorLocks(Program p) {
		Collection<MonitorAction> mas;
		Collection<MonitorAction> impossible = new HashSet<MonitorAction>();
		int total = 0;
		int fm = 0, cm = 0, tm = 0;
		for(MonitorAction ma : p.monitorActions()) {
			if(ma.fromSource()) {
				total++;
				Monitor m = ma.getAcquiredMonitor();
				if(m instanceof FieldMonitor) {
					++fm;
				} else if(m instanceof ClassMonitor) {
					++cm;
				} else {
					++tm;
				}
			}
		}
		System.out.println("FM: "+fm+"; CM: "+cm+"; TM: "+tm);
		involves_library = ambiguous_action = not_modifiable = 0;
		while(!(mas=p.monitorActions()).isEmpty()) {
			Iterator<MonitorAction> iter = mas.iterator();
			MonitorAction ma = null;
			while(iter.hasNext()) {
				ma = iter.next();
				if(impossible.contains(ma))
					continue;
				if(ma.fromSource() && ma.replaceWithReentrantLock()) {
					break;
				} else {
					impossible.add(ma);
				}
			}
			p.eliminate(ASTNode.LOCKED_NAMES);
			if(!iter.hasNext())
				break;
		}
		System.out.println("involves library: " + involves_library + "; ambiguous action: " + ambiguous_action + "; not modifiable: " + not_modifiable);
	}
	
	public void test_elevator2() {
		Program p = CompileHelper.compileAllJavaFilesUnder(BASE+"/elevator2/src");
		//Program p_new = CompileHelper.compileAllJavaFilesUnder(BASE+"/elevator2-ReentrantLock/src");
		refactorLocks(p);
		//assertEquals(p_new.toString(), p.toString());
	}
	
	public void test_tsp2() {
		Program p = CompileHelper.compileAllJavaFilesUnder(BASE+"/tsp2/src");
		//Program p_new = CompileHelper.compileAllJavaFilesUnder(BASE+"/tsp2-ReentrantLock/src");
		refactorLocks(p);
		//assertEquals(p_new.toString(), p.toString());
	}
	
	public void test_Hanoi() {
		Program p = CompileHelper.compileAllJavaFilesUnder(BASE+"/Hanoi/src");
		Program p_new = CompileHelper.compileAllJavaFilesUnder(BASE+"/Hanoi-ReentrantLock/src");
		refactorLocks(p);
		assertEquals(p_new.toString(), p.toString());
	}
	
	public void test_jmeter() throws Exception {
		Program p = CompileHelper.buildProjectFromClassPathFile(new File(BASE+"/jmeter/.classpath"));
		refactorLocks(p);
	}
	
	public void test_jtopas() {
		Collection<String> files = CompileHelper.findAllJavaFiles(new File(BASE+"/jtopas_0.6"));
		files.add(BASE+"/jtopas_0.6/lib/junit.jar");
		Program p = CompileHelper.compile(files.toArray(new String[]{}));
		Collection<String> new_files = CompileHelper.findAllJavaFiles(new File(BASE+"/jtopas_0.6-ReentrantLock"));
		new_files.add(BASE+"/jtopas_0.6-ReentrantLock/lib/junit.jar");
		Program new_p = CompileHelper.compile(new_files.toArray(new String[]{}));
		refactorLocks(p);
		//assertEquals(new_p.toString(), p.toString());
	}
	
	public void test_hsqldb() throws Exception {
		Program p = CompileHelper.buildProjectFromClassPathFile(new File(BASE+"/hsqldb-2.0.0/.classpath"));
		refactorLocks(p);
	}
	
	/*
	 * Almost nothing can be refactored right away:
	 *   1. field org.apache.lucene.util.cache.Cache.SynchronizedCache.mutex cannot be proved to be a dedicated lock field, since
	 *      it is initialised to "this" in one constructor, and to a constructor parameter in another (though that constructor
	 *      is not used)
	 *   2. field org.apache.lucene.util.cache.SimpleMapCache.SynchronizedSimpleMapCache.mutex cannot be proved to be a dedicated
	 *      lock field, since it is initialised to this
	 *   3. in org.apache.lucene.search.FieldCacheImpl.Cache.get, there is a synchronization on a variable value of type Object which,
	 *      however, has just been tested to be an instance of CreationPlaceHolder
	 *   4. there are several synchronisations on Vector objects, which cannot be refactored due to synchronised methods in class Vector;
	 *      similar for several other collection classes
	 *   5. subclass MergeThread of Thread with synchronised methods which cannot be refactored due to synchronised methods in class Thread;
	 *      similar for subclass SegmentInfos of Vector
	 *   
	 * After manually fixing these, all but 16 monitor actions (out of 374) can be refactored. 
	 */
	public void test_lucene() {
		Program p = compileLucene();
		String old_p = p.toString();
		refactorLocks(p);
		assertEquals(old_p, p.toString());
	}
	
	public void test_lucene_fields() {
		Program p = compileLucene();
		assertTrue(nonLeaking(p, "ConcurrentMergeScheduler", "allInstances"));
		assertTrue(nonLeaking(p, "IndexWriter", "synced"));
		assertTrue(nonLeaking(p, "CloseableThreadLocal", "hardRefs"));
		assertTrue(nonLeaking(p, "NativeFSLock", "LOCK_HELD"));
		assertTrue(nonLeaking(p, "FilterManager", "cache"));
		assertTrue(nonLeaking(p, "FieldCacheImpl.Cache", "readerCache"));
		assertTrue(nonLeaking(p, "DefaultAttributeFactory", "attClassImplMap"));
		//assertTrue(nonLeaking(p, "DirectoryReader", "normsCache"));
	}
	
	private boolean nonLeaking(Program p, String tp, String f) {
		TypeDecl td = p.findType(tp);
		assertNotNull(td);
		SimpleSet fs = td.localFields(f);
		assertTrue(fs.isSingleton());
		FieldDeclaration fd = (FieldDeclaration)fs.iterator().next();
		return !fd.refEscapes();
	}

	private Program compileLucene() {
		String lucene_root = BASE+"/lucene-3.0.1";
		Collection<String> files = CompileHelper.findAllJavaFiles(new File(lucene_root+"/src"));
		Program p = CompileHelper.compile(files.toArray(new String[]{}));
		return p;
	}
	
	public void test_cewolf() throws Exception {
		Program p = CompileHelper.buildProjectFromClassPathFile(new File(BASE+"/cewolf1.1/.classpath"));
		String old_p = p.toString();
		refactorLocks(p);
		//assertEquals(old_p, p.toString());
	}
	
	public void test_xalan() throws Exception {
		Program p = CompileHelper.buildProjectFromClassPathFile(new File(BASE+"/trunk/concurrency-benchmarks/xalan-j_2_4_1/.classpath"));
		String old_p = p.toString();
		refactorLocks(p);
		//assertEquals(old_p, p.toString());
	}
	
	public void test_hadoop() throws Exception {
		Program p = CompileHelper.buildProjectFromClassPathFile(new File(BASE+"/hadoop-core/.classpath"));
		String old_p = p.toString();
		refactorLocks(p);
		//assertEquals(old_p, p.toString());
	}
	
	public void test_cassandra() throws Exception {
		Program p = CompileHelper.buildProjectFromClassPathFile(new File(BASE+"/cassandra-0.63/.classpath"));
		refactorLocks(p);
	}
	
	public void test_jgroups() throws Exception {
		Program p = CompileHelper.buildProjectFromClassPathFile(new File("/home/xiemaisi/workspace/jgroups/.classpath"));
		refactorLocks(p);
	}
	
	public void test() {
		Program p = CompileHelper.compile("/home/xiemaisi/JastAdd/Sandbox/src/p/A.java");
	}
}
