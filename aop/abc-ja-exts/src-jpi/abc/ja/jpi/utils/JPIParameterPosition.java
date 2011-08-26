package abc.ja.jpi.utils;

import abc.ja.jpi.jrag.ParameterDeclaration;

public class JPIParameterPosition {
	
	private Integer position;
	private Integer parentPosition;
	private ParameterDeclaration parameter;
	private boolean accessed;
	
	public JPIParameterPosition(Integer position, Integer parentPosition, ParameterDeclaration parameter) {
		super();
		this.position = position;
		this.parentPosition = parentPosition;
		this.parameter = parameter;
		this.accessed = false;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public Integer getParentPosition() {
		return parentPosition;
	}

	public void setParentPosition(Integer parentPosition) {
		this.parentPosition = parentPosition;
	}

	public ParameterDeclaration getParameter() {
		return parameter;
	}

	public void setParameter(ParameterDeclaration parameter) {
		this.parameter = parameter;
	}

	public void setAccessed() {
		this.accessed = true;
		
	}

	public boolean getAccessed() {
		return this.accessed;
	}	
	
}
