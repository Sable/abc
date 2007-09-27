package abc.tm.weaving.weaver.tmanalysis.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polyglot.util.Position;
import soot.Local;
import soot.SootMethod;
import abc.main.Main;
import abc.tm.weaving.aspectinfo.TraceMatch;
import abc.tm.weaving.weaver.tmanalysis.query.ShadowRegistry;
import abc.tm.weaving.weaver.tmanalysis.query.SymbolShadowWithPTS;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.matching.AdviceApplication;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.matching.StmtAdviceApplication;

/**
 * A symbol shadow represents a static point in the program where the state
 * machine may make a transition with a certain symbol. Each Shadow belongs to a container
 * method and has a uniqueShadowId. Further, it has a mapping from
 * tracematch variables to Soot {@link Local}s (<i>advice actuals</i>)
 * that bind those tracematch variables at the shadow. 
 *
 * @author Eric Bodden
 */
public class SymbolShadow implements ISymbolShadow {
	
	protected final String symbolName;
	
    protected final SootMethod container;

    protected final TraceMatch owner;
		
	protected final Map<String,Local> tmFormalToAdviceLocal;

	protected final String uniqueShadowId;
	
	protected final Position pos;

	protected static Map<String,SymbolShadow> uniqueIdToShadow;

	private final int hashCode;
	
	SymbolShadow(String symbolName,
			Map<String, Local> tmVarToAdviceLocal, int shadowId, SootMethod container, TraceMatch owner) {
		this.symbolName = symbolName;
		this.tmFormalToAdviceLocal = tmVarToAdviceLocal;
		this.owner = owner;
		this.uniqueShadowId = Naming.uniqueShadowID(owner.getName(),symbolName,shadowId).intern();
		this.hashCode = computeHashCode();
		if(uniqueIdToShadow==null) {
		    uniqueIdToShadow = new HashMap<String, SymbolShadow>();
		}
		
        if(container.getName().indexOf("$body_real")>-1) {
            //have to compensate for renaming of method "body" to "body_real"
            String name = container.getName().substring(0, container.getName().lastIndexOf("$body_real")) +"$body";
            this.container = container.getDeclaringClass().getMethodByName(name);
        } else {
            this.container = container;
        }

        pos = computePosition(shadowId);
		
		//if a shadow is already associated with the same shadow ID it should better equal the new one
		SymbolShadow existingShadow = uniqueIdToShadow.get(this.uniqueShadowId);
		if(existingShadow!=null) {
		    assert existingShadow.deepEquals(existingShadow);
		}
		uniqueIdToShadow.put(this.uniqueShadowId, this);
	}

    private Position computePosition(int shadowId) {
        GlobalAspectInfo gai = Main.v().getAbcExtension().getGlobalAspectInfo();
        MethodAdviceList adviceList = gai.getAdviceList(container);
        List<AdviceApplication> applications = adviceList.allAdvice();
        for (AdviceApplication aa : applications) {
            if(aa.shadowmatch.shadowId==shadowId) {
                if(aa instanceof StmtAdviceApplication) {
                    StmtAdviceApplication stmtAA = (StmtAdviceApplication) aa;
                    return stmtAA.statementPosition();
                }
            }
        }
        return null;
    }

    /** 
	 * {@inheritDoc}
	 */
	public String getSymbolName() {
		return symbolName;
	}

	/** 
	 * {@inheritDoc}
	 */
	public TraceMatch getOwner() {
		return owner;
	}

	/** 
	 * {@inheritDoc}
	 */
	public Set<String> getBoundTmFormals() {
		return tmFormalToAdviceLocal.keySet();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public Collection<Local> getAdviceLocals() {
		return tmFormalToAdviceLocal.values();
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public Local getAdviceLocalForVariable(String tracematchVariable){
		assert getBoundTmFormals().contains(tracematchVariable);
		return tmFormalToAdviceLocal.get(tracematchVariable);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public Map<String,Local> getTmFormalToAdviceLocal() {
		return new HashMap<String,Local>(tmFormalToAdviceLocal);
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public boolean isEnabled() {
		return ShadowRegistry.v().isEnabled(getUniqueShadowId());
	}

	/** 
	 * {@inheritDoc}
	 */
	public String getUniqueShadowId() {
		return uniqueShadowId;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	public String getLocationId() {
		return Naming.locationID(uniqueShadowId);
	}
	
    /** 
	 * {@inheritDoc}
	 */
    public SootMethod getContainer() {
        return container;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    /**
     * Computes the (constant) hashCode.
     */
    protected int computeHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result
				+ ((symbolName == null) ? 0 : symbolName.hashCode());
		result = prime
				* result
				+ ((tmFormalToAdviceLocal == null) ? 0 : tmFormalToAdviceLocal
						.hashCode());
		result = prime * result
				+ ((uniqueShadowId == null) ? 0 : uniqueShadowId.hashCode());
		return result;
	}

	/** 
	 * Computes equals() based on the unique shadow ID. 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SymbolShadow other = (SymbolShadow) obj;
		//the unique shadow ID should uniquely identify equal shadows
		if(uniqueShadowId.equals(other.uniqueShadowId)) {
		    assert deepEquals(obj);
		    return true;
		} else {
		    return false;
		}
	}

    /** 
     * Used as a sanity check: Computes equals() deeply.
     */
    private boolean deepEquals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SymbolShadow other = (SymbolShadow) obj;
        if (owner == null) {
            if (other.owner != null)
                return false;
        } else if (!owner.equals(other.owner))
            return false;
        if (symbolName == null) {
            if (other.symbolName != null)
                return false;
        } else if (!symbolName.equals(other.symbolName))
            return false;
        if (tmFormalToAdviceLocal == null) {
            if (other.tmFormalToAdviceLocal != null)
                return false;
        } else if (!tmFormalToAdviceLocal.equals(other.tmFormalToAdviceLocal))
            return false;
        if (uniqueShadowId == null) {
            if (other.uniqueShadowId != null)
                return false;
        } else if (!uniqueShadowId.equals(other.uniqueShadowId))
            return false;
        return true;
    }

    /**
	 * {@inheritDoc}
	 */
	public String toString() {
		String ret =
		    "symbol:     " + symbolName + "\n" +
			"tracematch: " + owner.getName()+ "\n" +
			"variables:  " + tmFormalToAdviceLocal + "\n" +
			"shadow:     " + uniqueShadowId;
		
		if(pos!=null) {
		    ret += "\nposition:   " + pos;
		}
		
		return ret;
	}

	/**
	 * For a given set of {@link SymbolShadowWithPTS}s, returns the set of their
	 * unique shadow IDs.
	 * @param shadows a set of {@link SymbolShadowWithPTS}s
	 * @return the set of their shadow IDs
	 * @see Naming#uniqueShadowID(String, int)
	 * @see Naming#uniqueShadowID(String, String, int)
	 */
	public static Set<String> uniqueShadowIDsOf(Set<ISymbolShadow> shadows) {
		Set ids = new HashSet();
		for (Iterator<ISymbolShadow> shadowIter = shadows.iterator(); shadowIter.hasNext();) {
            ISymbolShadow shadow = shadowIter.next();
			ids.add(shadow.getUniqueShadowId());
		}
		return Collections.unmodifiableSet(ids);
	}
	
	/**
	 * Returns the symbol shadow associated with that ID.
	 * There can exist multiple such shadows with the same ID but they should all be equal
	 * in terms of equals/hashCode.
	 */
	public static SymbolShadow getSymbolShadowForUniqueID(String uniqueShadowID) {
	    SymbolShadow symbolShadow = uniqueIdToShadow.get(uniqueShadowID);
	    assert symbolShadow!=null;
        return symbolShadow;
	}
	
	/**
	 * Resets the association between unique shadow IDs and shadows. 
	 */
	public static void reset() {
	    uniqueIdToShadow=null;
	}

    /** 
     * {@inheritDoc}
     */
    public boolean isArtificial() {
        return getSymbolName().equals("newDaCapoRun");
    }
}