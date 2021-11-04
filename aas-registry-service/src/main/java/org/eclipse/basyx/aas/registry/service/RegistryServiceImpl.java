package org.eclipse.basyx.aas.registry.service;

import org.eclipse.basyx.aas.registry.event.RegistryEvent;
import org.eclipse.basyx.aas.registry.event.RegistryEventBuilder;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptorEnvelop;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RegistryServiceImpl implements RegistryService {

    @Autowired
    private AssetAdministrationShellDescriptorRepository aasDescriptorRepository;

    @Autowired
    private StreamBridge streamBridge;

    @Override
    public boolean existsAssetAdministrationShellDescriptorById(String aasIdentifier) {
        return aasDescriptorRepository.existsById(aasIdentifier);
    }

    @Override
    public boolean existsSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) {
        if (aasDescriptorRepository.existsById(aasIdentifier)) {
            AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = aasDescriptorRepository.findById(aasIdentifier).get().getAssetAdministrationShellDescriptor();
            assetAdministrationShellDescriptor.getSubmodelDescriptors().stream().filter(submodelDescriptor -> submodelDescriptor.getIdentification().getId().equals(submodelIdentifier)).findFirst().isPresent();
        }
        return false;
    }

    @Override
    public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors() {
        Iterable<AssetAdministrationShellDescriptorEnvelop> iterable = aasDescriptorRepository.findAll();
        List<AssetAdministrationShellDescriptor> result =
                StreamSupport.stream(iterable.spliterator(), false)
                        .map(AssetAdministrationShellDescriptorEnvelop::getAssetAdministrationShellDescriptor)
                        .collect(Collectors.toList());
        return result;
    }


    @Override
    public Optional<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById(String aasIdentifier) {
        if (aasDescriptorRepository.existsById(aasIdentifier)) {
            AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = aasDescriptorRepository.findById(aasIdentifier).get().getAssetAdministrationShellDescriptor();
            return Optional.of(assetAdministrationShellDescriptor);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public AssetAdministrationShellDescriptor registerAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor body) {
        AssetAdministrationShellDescriptorEnvelop result = aasDescriptorRepository.save(new AssetAdministrationShellDescriptorEnvelop(body));
        sendEvent(new RegistryEventBuilder()
                .setType(RegistryEvent.EventType.AAS_REGISTERED)
                .setId(result.getId())
                .setAssetAdministrationShellDescriptor(result.getAssetAdministrationShellDescriptor())
                .build());
        return result.getAssetAdministrationShellDescriptor();
    }

    @Override
    public void unregisterAssetAdministrationShellDescriptorById(String aasIdentifier) {
        if (aasDescriptorRepository.existsById(aasIdentifier)) {
            aasDescriptorRepository.deleteById(aasIdentifier);
            sendEvent(new RegistryEventBuilder()
                    .setType(RegistryEvent.EventType.AAS_UNREGISTERED)
                    .setId(aasIdentifier)
                    .build());
        }
    }

    @Override
    public List<SubmodelDescriptor> getAllSubmodelDescriptors(String aasIdentifier) {
        if (aasDescriptorRepository.existsById(aasIdentifier)) {
            AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = aasDescriptorRepository.findById(aasIdentifier).get().getAssetAdministrationShellDescriptor();
            return assetAdministrationShellDescriptor.getSubmodelDescriptors();
        } else {
            return null;
        }
    }

    @Override
    public Optional<SubmodelDescriptor> getSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) {
        if (aasDescriptorRepository.existsById(aasIdentifier)) {
            AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = aasDescriptorRepository.findById(aasIdentifier).get().getAssetAdministrationShellDescriptor();
            return assetAdministrationShellDescriptor.getSubmodelDescriptors().stream().filter(submodelDescriptor -> submodelDescriptor.getIdentification().getId().equals(submodelIdentifier)).findFirst();
        } else {
            return null;
        }
    }

    @Override
    public SubmodelDescriptor registerSubmodelDescriptor(String aasIdentifier, SubmodelDescriptor body) {
        if (aasDescriptorRepository.existsById(aasIdentifier)) {
            AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = aasDescriptorRepository.findById(aasIdentifier).get().getAssetAdministrationShellDescriptor();
            assetAdministrationShellDescriptor.getSubmodelDescriptors().removeIf(submodelDescriptor -> submodelDescriptor.getIdentification().getId().equals(body.getIdentification().getId()));
            assetAdministrationShellDescriptor.getSubmodelDescriptors().add(body);
            AssetAdministrationShellDescriptorEnvelop result = aasDescriptorRepository.save(new AssetAdministrationShellDescriptorEnvelop(assetAdministrationShellDescriptor));

            sendEvent(new RegistryEventBuilder()
                    .setType(RegistryEvent.EventType.SUBMODEL_REGISTERED)
                    .setId(body.getIdentification().getId())
                    .setAssetAdministrationShellDescriptor(result.getAssetAdministrationShellDescriptor())
                    .build());

            return body;
        } else {
            return null;
        }
    }

    @Override
    public void unregisterSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) {
        if (aasDescriptorRepository.existsById(aasIdentifier)) {
            AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = aasDescriptorRepository.findById(aasIdentifier).get().getAssetAdministrationShellDescriptor();
            assetAdministrationShellDescriptor.getSubmodelDescriptors().removeIf(submodelDescriptor -> submodelDescriptor.getIdentification().getId().equals(submodelIdentifier));
            AssetAdministrationShellDescriptorEnvelop result = aasDescriptorRepository.save(new AssetAdministrationShellDescriptorEnvelop(assetAdministrationShellDescriptor));

            sendEvent(new RegistryEventBuilder()
                    .setType(RegistryEvent.EventType.SUBMODEL_UNREGISTERED)
                    .setId(submodelIdentifier)
                    .setAssetAdministrationShellDescriptor(result.getAssetAdministrationShellDescriptor())
                    .build());
        }
    }

    private void sendEvent(RegistryEvent event) {
        streamBridge.send("aasRegistryBinding",event);
    }
}
