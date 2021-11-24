package org.eclipse.basyx.aas.registry.model;

import java.util.Objects;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@Document(indexName = "shell-descriptors")
@ToString
@EqualsAndHashCode
public class AssetAdministrationShellDescriptorDecorator extends AssetAdministrationShellDescriptor {

    @Delegate
    private final AssetAdministrationShellDescriptor delegate;

    public AssetAdministrationShellDescriptorDecorator(AssetAdministrationShellDescriptor delegate) {
        this.delegate = delegate;
    }
    
    @Id
    public String getId() {
    	return Optional.ofNullable(delegate.getIdentification()).map(Identifier::getId).orElse(null);
    }
    
    @Id
    public void setId(String id) {
    	Identifier identifier = delegate.getIdentification();
    	if (identifier == null) {
    		identifier = new Identifier();
    		delegate.setIdentification(identifier);
    	}
    	identifier.setId(id);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(delegate);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssetAdministrationShellDescriptorDecorator other = (AssetAdministrationShellDescriptorDecorator) obj;
		return Objects.equals(delegate, other.delegate);
	}
       

}
