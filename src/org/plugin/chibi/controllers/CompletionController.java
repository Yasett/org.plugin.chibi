package org.plugin.chibi.controllers;

import java.util.ArrayList;
import java.util.List;

import chibi.completion.CBImportsResolver;
import chibi.model.CBMethod;

public class CompletionController {

	public String getImports(String fullSource, CBMethod snippet){
		StringBuilder sbImports = new StringBuilder();
		List<String> lstImports = new ArrayList<String>();
		
		String[] lines = fullSource.split("\n");
		
		List<String> lstImportLines = new ArrayList<String>();
		for (int i = 0; i < lines.length; i++) {			
			if (lines[i].contains("class")){
				break;
			}
			else{
				if (lines[i].contains("import ")){
					lstImportLines.add(lines[i]);					
				}
			}
		}
		
		CBImportsResolver cbImportsResolver = new CBImportsResolver();
		lstImports = cbImportsResolver.getImports(lstImportLines, snippet.getSource(), snippet.getCbClass().getLstImports());
		
		for (String strImport : lstImports) {
			sbImports.append("import ");
			sbImports.append(strImport);
			sbImports.append(";\n");
		}
		
		return sbImports.toString();
	}
}
