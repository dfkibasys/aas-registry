package org.eclipse.basyx.aas.registry.util.path;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerationTarget {

	private String packageName;

	private String className;

}
