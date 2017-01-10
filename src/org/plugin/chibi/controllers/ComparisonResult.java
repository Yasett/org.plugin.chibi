package org.plugin.chibi.controllers;

import java.util.List;

import chibi.model.CBMethod;
import chibi.model.ICBWarning;

public class ComparisonResult {
	private CBMethod cbMethod;
	private List<CBMethod> snippetsFound;
	private List<ICBWarning> warningsFound;
	private boolean isCollapsed;
	
	public ComparisonResult(CBMethod cbMethod, List<CBMethod> snippetsFound, List<ICBWarning> warningsFound){
		this.cbMethod = cbMethod;
		this.snippetsFound = snippetsFound;
		this.warningsFound = warningsFound;
	}

	public CBMethod getCbMethod() {
		return cbMethod;
	}

	public void setCbMethod(CBMethod cbMethod) {
		this.cbMethod = cbMethod;
	}

	public List<CBMethod> getSnippetsFound() {
		return snippetsFound;
	}

	public void setSnippetsFound(List<CBMethod> snippetsFound) {
		this.snippetsFound = snippetsFound;
	}

	public List<ICBWarning> getWarningsFound() {
		return warningsFound;
	}

	public void setWarningsFound(List<ICBWarning> warningsFound) {
		this.warningsFound = warningsFound;
	}
	
	public String toString(){
		if (isCollapsed) {
			return "asdf";
		}
		else {
			return "";
		}
	}

	public boolean isCollapsed() {
		return isCollapsed;
	}

	public void setCollapsed(boolean isCollapsed) {
		this.isCollapsed = isCollapsed;
	}
}
