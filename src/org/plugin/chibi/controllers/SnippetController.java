package org.plugin.chibi.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chibi.generator.CBComparer;
import chibi.generator.CBMethodParser;
import chibi.gumtreediff.tree.ITree;
import chibi.model.CBMethod;
import chibi.model.ICBWarning;

public class SnippetController {
	// private static final String snippetCodeClass =
	// "platform:/plugin/org.plugin.chibi/snippets/Snippet.java";

	public ComparisonResult compareByMethod(String classBody, String methodSignature, String className,
			String classFullPath) {
		List<CBMethod> snippetsFound = new ArrayList<CBMethod>();
		List<ICBWarning> warningsFound = new ArrayList<ICBWarning>();
		CBMethod cbMethod = CBMethodParser.parse(classBody, className).stream()
				.filter(n -> n.getSignature().contains(methodSignature)).findFirst().orElse(null);

		if (cbMethod != null) {
			cbMethod.setClassFullPath(classFullPath);
			try {

				// List<CBMethod> snippets =
				// SMethodParser.parse(Util.readFileContents(snippetCodeClass),
				// snippetCodeClass);
				List<CBMethod> snippets = CBMethodParser.parse("/Snippet.sp");

				for (CBMethod cbSnippet : snippets) {
					ITree snippetTree = CBComparer.tree(cbSnippet.getSource());
					ITree methodCodeTree = CBComparer.tree(cbMethod.getSource());
					CBComparer comparer = new CBComparer();
					double difference = comparer.check(methodCodeTree, snippetTree);

					System.out.println("Difference found is: " + String.format("%.2f", difference * 100) + "%");

					if (difference < CBComparer.threshold) {
						snippetsFound.add(cbSnippet);

						warningsFound = comparer.checkWarnings(snippetTree, methodCodeTree,
								cbSnippet.getStartLineNumber(), className, cbMethod.getMethodName());

						for (ICBWarning cbWarning : warningsFound) {
							System.out.println(cbWarning);
						}
					}

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ComparisonResult result = new ComparisonResult(cbMethod, snippetsFound, warningsFound);
		return result;

	}

	public List<ComparisonResult>  compareByProject(List<String> classesSource, List<String> classesName, List<String> classesFullPath) {
		List<ComparisonResult> results = new ArrayList<ComparisonResult>();

		try {
			List<CBMethod> snippets = CBMethodParser.parse("/Snippet.sp");

			for (int i = 0; i < classesSource.size(); i++) {
				List<CBMethod> methodsByClass = CBMethodParser.parse(classesSource.get(i), classesName.get(i));

				for (CBMethod cbMethod : methodsByClass) {
					List<ICBWarning> warningsFound = new ArrayList<ICBWarning>();
					List<CBMethod> snippetsFound = new ArrayList<CBMethod>();
					
					cbMethod.setClassFullPath(classesFullPath.get(i));

					for (CBMethod cbSnippet : snippets) {
						ITree snippetTree = CBComparer.tree(cbSnippet.getSource());
						ITree methodCodeTree = CBComparer.tree(cbMethod.getSource());
						CBComparer comparer = new CBComparer();
						double difference = comparer.check(methodCodeTree, snippetTree);
						System.out.println("Difference found is: " + String.format("%.2f", difference * 100) + "%");

						if (difference < CBComparer.threshold) {
							snippetsFound.add(cbSnippet);

							warningsFound = comparer.checkWarnings(snippetTree, methodCodeTree,
									cbSnippet.getStartLineNumber(), classesName.get(i), cbMethod.getMethodName());

							for (ICBWarning cbWarning : warningsFound) {
								System.out.println(cbWarning);
							}

							ComparisonResult result = new ComparisonResult(cbMethod, snippetsFound,
									warningsFound);
							results.add(result);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return results;
	}
}
