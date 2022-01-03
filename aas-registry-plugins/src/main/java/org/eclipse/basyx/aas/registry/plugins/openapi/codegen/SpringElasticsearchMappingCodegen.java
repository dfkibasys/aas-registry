package org.eclipse.basyx.aas.registry.plugins.openapi.codegen;

import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.codegen.v3.generators.java.SpringCodegen;

public class SpringElasticsearchMappingCodegen extends SpringCodegen {

	
	@Override
	public String getName() {
		return "springElasticsearchMapping";
	}
}
