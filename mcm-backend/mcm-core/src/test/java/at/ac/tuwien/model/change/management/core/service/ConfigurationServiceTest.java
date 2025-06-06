package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.*;
import at.ac.tuwien.model.change.management.core.model.*;
import at.ac.tuwien.model.change.management.core.model.versioning.ModelDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.NodeDiff;
import at.ac.tuwien.model.change.management.core.model.versioning.RelationDiff;
import at.ac.tuwien.model.change.management.core.utils.ConfigurationContents;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAccessException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryAlreadyExistsException;
import at.ac.tuwien.model.change.management.git.exception.RepositoryDoesNotExistException;
import at.ac.tuwien.model.change.management.git.repository.ConfigurationRepository;
import at.ac.tuwien.model.change.management.git.repository.VersionControlRepository;
import at.ac.tuwien.model.change.management.testutil.assertion.ConfigurationAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfigurationServiceTest {

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private VersionControlRepository versionControlRepository;

    @Mock
    private GraphDBService graphDBService;

    @Mock
    private NameValidationService nameValidationService;

    @InjectMocks
    private ConfigurationServiceImpl configurationService;

    private static final String TEST_CONFIGURATION_NAME = "testConfiguration";
    private static final String TEST_CONFIGURATION_VERSION = "1.0.0";

    @Test
    public void testCreateConfiguration_emptyConfiguration_shouldCreateConfiguration() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        when(configurationRepository.saveConfiguration(configuration)).thenReturn(configuration);

        var createdConfiguration = configurationService.createConfiguration(configuration, false);

        Assertions.assertThat(createdConfiguration).isEqualTo(configuration);
        verify(configurationRepository).createConfiguration(configuration.getName());
        verify(configurationRepository).saveConfiguration(configuration);
    }

    @Test
    public void testCreateConfiguration_configurationWithVersion_shouldThrowConfigurationValidationException() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        Assertions.assertThatThrownBy(() -> configurationService.createConfiguration(configuration, false))
                .isInstanceOf(ConfigurationValidationException.class)
                .hasMessage("New configuration cannot have a version.");
    }

    @Test
    public void testCreateConfiguration_configurationWithDuplicateIDs_shouldThrowConfigurationValidationException() {
        var model1 = new Model();
        model1.setId("1");

        var model2 = new Model();
        model2.setId("1");

        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setModels(Set.of(model1, model2));


        Assertions.assertThatThrownBy(() -> configurationService.createConfiguration(configuration, false))
                .isInstanceOf(ConfigurationValidationException.class)
                .hasMessageContaining("duplicate element IDs");
    }

    @Test
    public void testCreateConfiguration_createThrowsRepositoryAlreadyExistsException_shouldThrowConfigurationAlreadyExistsException() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        when(configurationRepository.saveConfiguration(configuration)).thenThrow(new RepositoryAlreadyExistsException(TEST_CONFIGURATION_NAME));

        Assertions.assertThatThrownBy(() -> configurationService.createConfiguration(configuration, false))
                .isInstanceOf(ConfigurationAlreadyExistsException.class);
    }

    @Test
    public void testCreateConfiguration_createThrowsRepositoryAccessException_shouldThrowConfigurationCreateException() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        when(configurationRepository.saveConfiguration(configuration)).thenThrow(new RepositoryAccessException(TEST_CONFIGURATION_NAME));

        Assertions.assertThatThrownBy(() -> configurationService.createConfiguration(configuration, false))
                .isInstanceOf(ConfigurationCreateException.class);
    }

    @Test
    public void testCreateConfiguration_loadIntoGraphDBFalse_shouldNotLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        when(configurationRepository.saveConfiguration(configuration)).thenReturn(configuration);

        configurationService.createConfiguration(configuration, false);

        verifyNoInteractions(graphDBService);
    }


    @Test
    public void testCreateConfiguration_loadIntoGraphDBTrue_shouldLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        when(configurationRepository.saveConfiguration(configuration)).thenReturn(configuration);

        configurationService.createConfiguration(configuration, true);

        verify(graphDBService).loadConfiguration(configuration);
    }

    @Test
    public void testCreateConfiguration_loadIntoGraphDBDefaultBehavior_shouldLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        when(configurationRepository.saveConfiguration(configuration)).thenReturn(configuration);

        configurationService.createConfiguration(configuration);

        verify(graphDBService).loadConfiguration(configuration);
    }

    @Test
    public void testCreateConfiguration_saveThrowsException_shouldAttemptRollbackByDeletingConfiguration() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        doThrow(new RuntimeException()).when(configurationRepository).saveConfiguration(configuration);

        Assertions.assertThatThrownBy(() -> configurationService.createConfiguration(configuration, false))
                .isInstanceOf(RuntimeException.class);

        verify(configurationRepository).deleteConfiguration(TEST_CONFIGURATION_NAME);
    }

    @Test
    public void testCreateConfiguration_createThrowsException_shouldAttemptRollbackByDeletingConfiguration() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        doThrow(new RuntimeException()).when(configurationRepository).createConfiguration(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.createConfiguration(configuration, false))
                .isInstanceOf(RuntimeException.class);

        verify(configurationRepository).deleteConfiguration(TEST_CONFIGURATION_NAME);
    }

    @Test
    public void testCreateConfiguration_saveThrowsException_thenDeleteAgainThrowsException_shouldStillPropagateOriginalException() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        doThrow(new RepositoryAccessException("")).when(configurationRepository).saveConfiguration(configuration);
        doThrow(new RuntimeException()).when(configurationRepository).deleteConfiguration(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.createConfiguration(configuration, false))
                .isInstanceOf(ConfigurationCreateException.class)
                .cause()
                .isInstanceOf(RepositoryAccessException.class);

        verify(configurationRepository).deleteConfiguration(TEST_CONFIGURATION_NAME);
    }

    @Test
    public void testCreateConfiguration_nameValidationThrowsInvalidNameException_shouldThrowConfigurationValidationException() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        doThrow(new InvalidNameException("")).when(nameValidationService).validateRepositoryName(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.createConfiguration(configuration, false))
                .isInstanceOf(ConfigurationValidationException.class);
    }

    @Test
    public void testUpdateConfiguration_emptyConfiguration_shouldUpdateConfiguration() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.saveConfiguration(configuration)).thenReturn(configuration);

        var updatedConfiguration = configurationService.updateConfiguration(configuration, false);

        ConfigurationAssert.assertThat(updatedConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .containsSameElementsAs(configuration);
        verify(configurationRepository).saveConfiguration(configuration);
    }

    @Test
    public void testUpdateConfiguration_configurationWithDuplicateIDs_shouldThrowConfigurationValidationException() {
        var model1 = new Model();
        model1.setId("1");

        var model2 = new Model();
        model2.setId("1");

        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setModels(Set.of(model1, model2));

        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        Assertions.assertThatThrownBy(() -> configurationService.updateConfiguration(configuration, false))
                .isInstanceOf(ConfigurationValidationException.class)
                .hasMessageContaining("duplicate element IDs");
    }

    @Test
    public void testUpdateConfiguration_updateThrowsRepositoryDoesNotExistException_shouldThrowConfigurationDoesNotExistException() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.saveConfiguration(configuration)).thenThrow(new RepositoryDoesNotExistException(""));

        Assertions.assertThatThrownBy(() -> configurationService.updateConfiguration(configuration, false))
                .isInstanceOf(ConfigurationDoesNotExistException.class);
    }

    @Test
    public void testUpdateConfiguration_updateThrowsRepositoryAccessException_shouldThrowConfigurationUpdateException() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.saveConfiguration(configuration)).thenThrow(new RepositoryAccessException(TEST_CONFIGURATION_NAME));

        Assertions.assertThatThrownBy(() -> configurationService.updateConfiguration(configuration, false))
                .isInstanceOf(ConfigurationUpdateException.class);
    }

    @Test
    public void testUpdateConfiguration_loadIntoGraphDBFalse_shouldNotLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.saveConfiguration(configuration)).thenReturn(configuration);

        configurationService.updateConfiguration(configuration, false);

        verifyNoInteractions(graphDBService);
    }

    @Test
    public void testUpdateConfiguration_loadIntoGraphDBTrue_shouldLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.saveConfiguration(configuration)).thenReturn(configuration);

        configurationService.updateConfiguration(configuration, true);

        verify(graphDBService).loadConfiguration(configuration);
    }

    @Test
    public void testUpdateConfiguration_loadIntoGraphDBDefaultBehavior_shouldLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.saveConfiguration(configuration)).thenReturn(configuration);

        configurationService.updateConfiguration(configuration);

        verify(graphDBService).loadConfiguration(configuration);
    }

    @Test
    public void testUpdateConfiguration_nameValidationThrowsInvalidNameException_shouldThrowConfigurationValidationException() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        doThrow(new InvalidNameException("")).when(nameValidationService).validateRepositoryName(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.updateConfiguration(configuration, false))
                .isInstanceOf(ConfigurationValidationException.class);
    }

    @Test
    public void testUpdateConfiguration_versionWithCustomName_shouldEncodeAndDecodeCustomName() {
        var customName = "customName";
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, customName));

        when(configurationRepository.saveConfiguration(configuration)).thenReturn(configuration);
        when(nameValidationService.encodeVersionName(customName, true)).thenReturn(customName);
        when(nameValidationService.decodeVersionName(customName)).thenReturn(customName);

        configurationService.updateConfiguration(configuration, false);

        verify(nameValidationService).encodeVersionName(customName, true);
        verify(nameValidationService).decodeVersionName(customName);
    }

    @Test
    public void testDeleteConfiguration_shouldDeleteConfiguration() {
        configurationService.deleteConfiguration(TEST_CONFIGURATION_NAME);

        verify(configurationRepository).deleteConfiguration(TEST_CONFIGURATION_NAME);
    }

    @Test
    public void testDeleteConfiguration_deleteThrowsRepositoryAccessException_shouldThrowConfigurationDeleteException() {
        doThrow(new RepositoryAccessException(TEST_CONFIGURATION_NAME)).when(configurationRepository).deleteConfiguration(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.deleteConfiguration(TEST_CONFIGURATION_NAME))
                .isInstanceOf(ConfigurationDeleteException.class);
    }

    @Test
    public void testDeleteConfiguration_nameValidationThrowsInvalidNameException_shouldThrowConfigurationValidationException() {
        doThrow(new InvalidNameException("")).when(nameValidationService).validateRepositoryName(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.deleteConfiguration(TEST_CONFIGURATION_NAME))
                .isInstanceOf(ConfigurationValidationException.class);
    }

    @Test
    public void testGetConfigurationByName_existingConfiguration_shouldReturnConfiguration() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        when(configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME)).thenReturn(Optional.of(configuration));

        var retrievedConfiguration = configurationService.getConfigurationByName(TEST_CONFIGURATION_NAME, false);

        ConfigurationAssert.assertThat(retrievedConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .containsSameElementsAs(configuration);
        verify(configurationRepository).findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME);
    }

    @Test
    public void testGetConfigurationByName_nonExistingConfiguration_shouldThrowConfigurationNotFoundException() {
        when(configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> configurationService.getConfigurationByName(TEST_CONFIGURATION_NAME, false))
                .isInstanceOf(ConfigurationNotFoundException.class);
    }

    @Test
    public void testGetConfigurationByName_findByNameThrowsRepositoryAccessException_shouldThrowConfigurationGetException() {
        when(configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME)).thenThrow(new RepositoryAccessException(TEST_CONFIGURATION_NAME));

        Assertions.assertThatThrownBy(() -> configurationService.getConfigurationByName(TEST_CONFIGURATION_NAME, false))
                .isInstanceOf(ConfigurationGetException.class);
    }

    @Test
    public void testGetConfigurationByName_loadIntoGraphDBFalse_shouldNotLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        when(configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME)).thenReturn(Optional.of(configuration));

        configurationService.getConfigurationByName(TEST_CONFIGURATION_NAME, false);

        verifyNoInteractions(graphDBService);
    }

    @Test
    public void testGetConfigurationByName_loadIntoGraphDBTrue_shouldLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        when(configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME)).thenReturn(Optional.of(configuration));

        configurationService.getConfigurationByName(TEST_CONFIGURATION_NAME, true);

        verify(graphDBService).loadConfiguration(configuration);
    }

    @Test
    public void testGetConfigurationByName_loadIntoGraphDBDefaultBehavior_shouldNotLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        when(configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME)).thenReturn(Optional.of(configuration));

        configurationService.getConfigurationByName(TEST_CONFIGURATION_NAME);

        verifyNoInteractions(graphDBService);
    }

    @Test
    public void testGetConfigurationByName_nameValidationThrowsInvalidNameException_shouldThrowConfigurationValidationException() {
        doThrow(new InvalidNameException("")).when(nameValidationService).validateRepositoryName(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.getConfigurationByName(TEST_CONFIGURATION_NAME, false))
                .isInstanceOf(ConfigurationValidationException.class);
    }

    @Test
    public void testGetConfigurationByName_versionWithCustomName_shouldEncodeAndDecodeCustomName() {
        var customName = "customName";
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, customName));

        when(configurationRepository.findCurrentVersionOfConfigurationByName(TEST_CONFIGURATION_NAME)).thenReturn(Optional.of(configuration));
        when(nameValidationService.decodeVersionName(customName)).thenReturn(customName);

        configurationService.getConfigurationByName(TEST_CONFIGURATION_NAME, false);

        verify(nameValidationService).decodeVersionName(customName);
    }

    @Test
    public void testGetConfigurationVersion_existingConfigurationVersion_shouldReturnConfiguration() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        var retrievedConfiguration = configurationService.getConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false);

        ConfigurationAssert.assertThat(retrievedConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasVersion(TEST_CONFIGURATION_VERSION)
                .containsSameElementsAs(configuration);
        verify(configurationRepository).findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);
    }

    @Test
    public void testGetConfigurationVersion_nonExistingConfigurationVersion_shouldThrowConfigurationNotFoundException() {
        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.empty());
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        Assertions.assertThatThrownBy(() -> configurationService.getConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false))
                .isInstanceOf(ConfigurationNotFoundException.class);
    }

    @Test
    public void testGetConfigurationVersion_findSpecifiedVersionThrowsRepositoryAccessException_shouldThrowConfigurationGetException() {
        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenThrow(new RepositoryAccessException(TEST_CONFIGURATION_NAME));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        Assertions.assertThatThrownBy(() -> configurationService.getConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false))
                .isInstanceOf(ConfigurationGetException.class);
    }

    @Test
    public void testGetConfigurationVersion_loadIntoGraphDBFalse_shouldNotLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        configurationService.getConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false);

        verifyNoInteractions(graphDBService);
    }

    @Test
    public void testGetConfigurationVersion_loadIntoGraphDBTrue_shouldLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));
        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        configurationService.getConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, true);

        verify(graphDBService).loadConfiguration(configuration);
    }

    @Test
    public void testGetConfigurationVersion_loadIntoGraphDBDefaultBehavior_shouldNotLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));
        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        configurationService.getConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);

        verifyNoInteractions(graphDBService);
    }

    @Test
    public void testGetConfigurationVersion_nameValidationThrowsInvalidNameException_shouldThrowConfigurationValidationException() {
        doThrow(new InvalidNameException("")).when(nameValidationService).validateRepositoryName(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.getConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false))
                .isInstanceOf(ConfigurationValidationException.class);
    }

    @Test
    public void testGetConfigurationVersion_versionWithCustomName_shouldEncodeAndDecodeCustomName() {
        var customName = "customName";
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, customName));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, customName)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(customName, true)).thenReturn(customName);
        when(nameValidationService.decodeVersionName(customName)).thenReturn(customName);

        configurationService.getConfigurationVersion(TEST_CONFIGURATION_NAME, customName, false);

        verify(nameValidationService).encodeVersionName(customName, true);
        verify(nameValidationService).decodeVersionName(customName);
    }

    @Test
    public void testGetAllConfigurations_noConfigurations_shouldReturnEmptyList() {
        when(configurationRepository.findAllConfigurations()).thenReturn(List.of());

        var configurations = configurationService.getAllConfigurations();

        Assertions.assertThat(configurations).isEmpty();
        verify(configurationRepository).findAllConfigurations();
    }

    @Test
    public void testGetAllConfigurations_shouldReturnAllConfigurations() {
        var configuration1 = getEmptyConfiguration("configuration1");
        var configuration2 = getEmptyConfiguration("configuration2");
        when(configurationRepository.findAllConfigurations()).thenReturn(List.of(configuration1, configuration2));

        var configurations = configurationService.getAllConfigurations();

        Assertions.assertThat(configurations).containsExactlyInAnyOrder(configuration1, configuration2);
        verify(configurationRepository).findAllConfigurations();
    }

    @Test
    public void testGetAllConfigurations_findAllConfigurationsThrowsRepositoryAccessException_shouldThrowConfigurationGetException() {
        when(configurationRepository.findAllConfigurations()).thenThrow(new RepositoryAccessException(""));

        Assertions.assertThatThrownBy(() -> configurationService.getAllConfigurations())
                .isInstanceOf(ConfigurationGetException.class);
    }

    @Test
    public void testGetAllConfigurations_configurationWithCustomName_shouldDecodeCustomName() {
        var customName = "customName";
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, customName));

        when(configurationRepository.findAllConfigurations()).thenReturn(List.of(configuration));
        when(nameValidationService.decodeVersionName(customName)).thenReturn(customName);

        configurationService.getAllConfigurations();

        verify(nameValidationService).decodeVersionName(customName);
    }

    @Test
    public void testListConfigurationVersions_existingConfiguration_shouldReturnVersions() {
        var versions = List.of(
                new ConfigurationVersion("1.0.0", null, null),
                new ConfigurationVersion("1.1.0", null, null),
                new ConfigurationVersion("1.2.0", null, null)
        );
        when(configurationRepository.listConfigurationVersions(TEST_CONFIGURATION_NAME)).thenReturn(versions);

        var retrievedVersions = configurationService.listConfigurationVersions(TEST_CONFIGURATION_NAME);

        Assertions.assertThat(retrievedVersions).containsExactlyInAnyOrderElementsOf(versions);
        verify(configurationRepository).listConfigurationVersions(TEST_CONFIGURATION_NAME);
    }

    @Test
    public void testListConfigurationVersions_nonExistingConfiguration_shouldThrowConfigurationDoesNotExistException() {
        doThrow(new RepositoryDoesNotExistException("")).when(configurationRepository).listConfigurationVersions(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.listConfigurationVersions(TEST_CONFIGURATION_NAME))
                .isInstanceOf(ConfigurationDoesNotExistException.class);
    }

    @Test
    public void testListConfigurationVersions_findAllConfigurationsThrowsRepositoryAccessException_shouldThrowConfigurationGetException() {
        when(configurationRepository.listConfigurationVersions(TEST_CONFIGURATION_NAME)).thenThrow(new RepositoryAccessException(TEST_CONFIGURATION_NAME));

        Assertions.assertThatThrownBy(() -> configurationService.listConfigurationVersions(TEST_CONFIGURATION_NAME))
                .isInstanceOf(ConfigurationGetException.class);
    }

    @Test
    public void testListConfigurationVersions_nameValidationThrowsInvalidNameException_shouldThrowConfigurationValidationException() {
        doThrow(new InvalidNameException("")).when(nameValidationService).validateRepositoryName(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.listConfigurationVersions(TEST_CONFIGURATION_NAME))
                .isInstanceOf(ConfigurationValidationException.class);
    }

    @Test
    public void testListConfigurationVersions_versionWithCustomName_shouldDecodeCustomName() {
        var customName = "customName";
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, customName));

        when(configurationRepository.listConfigurationVersions(TEST_CONFIGURATION_NAME)).thenReturn(List.of(configuration.getVersion()));
        when(nameValidationService.decodeVersionName(customName)).thenReturn(customName);

        configurationService.listConfigurationVersions(TEST_CONFIGURATION_NAME);

        verify(nameValidationService).decodeVersionName(customName);
    }

    @Test
    public void testCompareConfigurationVersions_nonExistingConfiguration_shouldThrowConfigurationDoesNotExistException() {
        var oldVersion = "1.0.0";
        var newVersion = "1.1.0";
        var includeUnchanged = true;

        when(nameValidationService.encodeVersionName(oldVersion, true)).thenReturn(oldVersion);
        when(nameValidationService.encodeVersionName(newVersion, true)).thenReturn(newVersion);
        doThrow(new RepositoryDoesNotExistException("")).when(configurationRepository).compareConfigurationVersions(TEST_CONFIGURATION_NAME, oldVersion, newVersion, includeUnchanged);

        Assertions.assertThatThrownBy(() -> configurationService.compareConfigurationVersions(TEST_CONFIGURATION_NAME, oldVersion, newVersion, includeUnchanged))
                .isInstanceOf(ConfigurationDoesNotExistException.class);
    }

    @Test
    public void testCompareConfigurationVersions_existingConfiguration_shouldReturnDiffsInList() {
        var oldVersion = "1.0.0";
        var newVersion = "1.1.0";
        var includeUnchanged = true;


        var modelDiffs = List.of(new ModelDiff(new Model(), "UNCHANGED", "diff"), new ModelDiff(new Model(), "ADD", "diff"));
        var nodeDiffs = List.of(new NodeDiff(new Node(), "DELETE", "diff"), new NodeDiff(new Node(), "MODIFY", "diff"));
        var relationDiffs = List.of(new RelationDiff(new Relation(), "UNCHANGED", "diff"), new RelationDiff(new Relation(), "ADD", "diff"));

        var diffs = new ConfigurationContents<ModelDiff, NodeDiff, RelationDiff>();
        diffs.setModels(new HashSet<>(modelDiffs));
        diffs.setNodes(new HashSet<>(nodeDiffs));
        diffs.setRelations(new HashSet<>(relationDiffs));

        when(configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, oldVersion, newVersion, includeUnchanged)).thenReturn(diffs);
        when(nameValidationService.encodeVersionName(oldVersion, true)).thenReturn(oldVersion);
        when(nameValidationService.encodeVersionName(newVersion, true)).thenReturn(newVersion);

        var retrievedDiffs = configurationService.compareConfigurationVersions(TEST_CONFIGURATION_NAME, oldVersion, newVersion, includeUnchanged);
        Assertions.assertThat(retrievedDiffs).containsExactlyInAnyOrderElementsOf(
                combineLists(modelDiffs, nodeDiffs, relationDiffs)
        );
        verify(configurationRepository).compareConfigurationVersions(TEST_CONFIGURATION_NAME, oldVersion, newVersion, includeUnchanged);
    }

    @Test
    public void testCompareConfigurationVersions_compareConfigurationVersionsThrowsRepositoryAccessException_shouldThrowConfigurationComparisonException() {
        var oldVersion = "1.0.0";
        var newVersion = "1.1.0";
        var includeUnchanged = true;

        when(configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, oldVersion, newVersion, includeUnchanged)).thenThrow(new RepositoryAccessException(TEST_CONFIGURATION_NAME));
        when(nameValidationService.encodeVersionName(oldVersion, true)).thenReturn(oldVersion);
        when(nameValidationService.encodeVersionName(newVersion, true)).thenReturn(newVersion);

        Assertions.assertThatThrownBy(() -> configurationService.compareConfigurationVersions(TEST_CONFIGURATION_NAME, oldVersion, newVersion, includeUnchanged))
                .isInstanceOf(ConfigurationComparisonException.class);
    }

    @Test
    public void testCompareConfigurationVersions_nameValidationThrowsInvalidNameException_shouldThrowConfigurationValidationException() {
        var oldVersion = "1.0.0";
        var newVersion = "1.1.0";
        var includeUnchanged = true;

        doThrow(new InvalidNameException("")).when(nameValidationService).validateRepositoryName(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.compareConfigurationVersions(TEST_CONFIGURATION_NAME, oldVersion, newVersion, includeUnchanged))
                .isInstanceOf(ConfigurationValidationException.class);
    }

    @Test
    public void testCompareConfigurationVersions_versionsWithCustomName_shouldEncodeCustomNames() {
        var customOldVersion = "customOldVersion";
        var customNewVersion = "customNewVersion";
        var diffs = new ConfigurationContents<ModelDiff, NodeDiff, RelationDiff>();

        when(configurationRepository.compareConfigurationVersions(TEST_CONFIGURATION_NAME, customOldVersion, customNewVersion, true)).thenReturn(diffs);
        when(nameValidationService.encodeVersionName(customOldVersion, true)).thenReturn(customOldVersion);
        when(nameValidationService.encodeVersionName(customNewVersion, true)).thenReturn(customNewVersion);

        configurationService.compareConfigurationVersions(TEST_CONFIGURATION_NAME, customOldVersion, customNewVersion, true);

        verify(nameValidationService).encodeVersionName(customOldVersion, true);
        verify(nameValidationService).encodeVersionName(customNewVersion, true);
    }

    @Test
    public void testCheckoutConfigurationVersion_existingConfiguration_shouldReturnConfiguration() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        var retrievedConfiguration = configurationService.checkoutConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false);

        ConfigurationAssert.assertThat(retrievedConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasVersion(TEST_CONFIGURATION_VERSION)
                .containsSameElementsAs(configuration);

        verify(versionControlRepository).checkoutVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);
    }

    @Test
    public void testCheckoutConfigurationVersion_nonExistingConfigurationVersion_shouldThrowConfigurationDoesNotExistException() {
        doThrow(new RepositoryDoesNotExistException("")).when(versionControlRepository).checkoutVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);

        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        Assertions.assertThatThrownBy(() -> configurationService.checkoutConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false))
                .isInstanceOf(ConfigurationDoesNotExistException.class);
    }

    @Test
    public void testCheckoutConfigurationVersion_checkoutVersionThrowsRepositoryAccessException_shouldThrowConfigurationCheckoutException() {
        doThrow(new RepositoryAccessException(TEST_CONFIGURATION_NAME)).when(versionControlRepository).checkoutVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);

        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        Assertions.assertThatThrownBy(() -> configurationService.checkoutConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false))
                .isInstanceOf(ConfigurationCheckoutException.class);
    }

    @Test
    public void testCheckoutConfigurationVersion_loadIntoGraphDBFalse_shouldNotLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        configurationService.checkoutConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false);

        verifyNoInteractions(graphDBService);
    }

    @Test
    public void testCheckoutConfigurationVersion_loadIntoGraphDBTrue_shouldLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        configurationService.checkoutConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, true);

        verify(graphDBService).loadConfiguration(configuration);
    }

    @Test
    public void testCheckoutConfigurationVersion_loadIntoGraphDBDefaultBehavior_shouldNotLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        configurationService.checkoutConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);

        verifyNoInteractions(graphDBService);
    }

    @Test
    public void testCheckoutConfigurationVersion_nameValidationThrowsInvalidNameException_shouldThrowConfigurationValidationException() {
        doThrow(new InvalidNameException("")).when(nameValidationService).validateRepositoryName(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.checkoutConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false))
                .isInstanceOf(ConfigurationValidationException.class);
    }

    @Test
    public void testCheckoutConfigurationVersion_versionWithCustomName_shouldEncodeAndDecodeCustomName() {
        var customName = "customName";
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, customName));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, customName)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(customName, true)).thenReturn(customName);
        when(nameValidationService.decodeVersionName(customName)).thenReturn(customName);

        configurationService.checkoutConfigurationVersion(TEST_CONFIGURATION_NAME, customName, false);

        verify(nameValidationService).encodeVersionName(customName, true);
        verify(nameValidationService).decodeVersionName(customName);
    }

    @Test
    public void testResetConfigurationVersion_existingConfiguration_shouldReturnConfiguration() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        var retrievedConfiguration = configurationService.resetConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false);

        ConfigurationAssert.assertThat(retrievedConfiguration)
                .hasName(TEST_CONFIGURATION_NAME)
                .hasVersion(TEST_CONFIGURATION_VERSION)
                .containsSameElementsAs(configuration);

        verify(versionControlRepository).resetToVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);
    }

    @Test
    public void testResetConfigurationVersion_nonExistingConfigurationVersion_shouldThrowConfigurationDoesNotExistException() {
        doThrow(new RepositoryDoesNotExistException("")).when(versionControlRepository).resetToVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);

        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        Assertions.assertThatThrownBy(() -> configurationService.resetConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false))
                .isInstanceOf(ConfigurationDoesNotExistException.class);
    }

    @Test
    public void testResetConfigurationVersion_resetToVersionThrowsRepositoryAccessException_shouldThrowConfigurationResetException() {
        doThrow(new RepositoryAccessException(TEST_CONFIGURATION_NAME)).when(versionControlRepository).resetToVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);

        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        Assertions.assertThatThrownBy(() -> configurationService.resetConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false))
                .isInstanceOf(ConfigurationResetException.class);
    }

    @Test
    public void testResetConfigurationVersion_loadIntoGraphDBFalse_shouldNotLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        configurationService.resetConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false);

        verifyNoInteractions(graphDBService);
    }

    @Test
    public void testResetConfigurationVersion_loadIntoGraphDBTrue_shouldLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        configurationService.resetConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, true);

        verify(graphDBService).loadConfiguration(configuration);
    }

    @Test
    public void testResetConfigurationVersion_loadIntoGraphDBDefaultBehavior_shouldNotLoadIntoGraphDB() {
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, null));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(TEST_CONFIGURATION_VERSION, true)).thenReturn(TEST_CONFIGURATION_VERSION);

        configurationService.resetConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION);

        verifyNoInteractions(graphDBService);
    }

    @Test
    public void testResetConfigurationVersion_nameValidationThrowsInvalidNameException_shouldThrowConfigurationValidationException() {
        doThrow(new InvalidNameException("")).when(nameValidationService).validateRepositoryName(TEST_CONFIGURATION_NAME);

        Assertions.assertThatThrownBy(() -> configurationService.resetConfigurationVersion(TEST_CONFIGURATION_NAME, TEST_CONFIGURATION_VERSION, false))
                .isInstanceOf(ConfigurationValidationException.class);
    }

    @Test
    public void testResetConfigurationVersion_versionWithCustomName_shouldEncodeAndDecodeCustomName() {
        var customName = "customName";
        var configuration = getEmptyConfiguration(TEST_CONFIGURATION_NAME);
        configuration.setVersion(new ConfigurationVersion(TEST_CONFIGURATION_VERSION, null, customName));

        when(configurationRepository.findSpecifiedVersionOfConfigurationByName(TEST_CONFIGURATION_NAME, customName)).thenReturn(Optional.of(configuration));
        when(nameValidationService.encodeVersionName(customName, true)).thenReturn(customName);
        when(nameValidationService.decodeVersionName(customName)).thenReturn(customName);

        configurationService.resetConfigurationVersion(TEST_CONFIGURATION_NAME, customName, false);

        verify(nameValidationService).encodeVersionName(customName, true);
        verify(nameValidationService).decodeVersionName(customName);
    }

    private Configuration getEmptyConfiguration(String name) {
        var configuration = new Configuration();
        configuration.setName(name);
        return configuration;
    }

    @SafeVarargs
    private <T> List<T> combineLists(List<? extends T>... lists) {
        List<T> combined = new ArrayList<>();
        for (List<? extends T> l : lists) {
            combined.addAll(l);
        }
        return combined;
    }
}
