package edus2.adapter.repository.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edus2.domain.EDUS2Configuration;
import edus2.domain.SystemIdentifier;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.String.format;

public class FileEDUS2Configuration extends FileRepository implements EDUS2Configuration {

    private Gson gson;
    private String filePath;

    public FileEDUS2Configuration(String saveFilePath) {
        this.filePath = Paths.get(saveFilePath).toString();
        this.gson = new GsonBuilder().create();
        ensureConfigurationFileExists();
    }

    FileEDUS2Configuration(String fileDirectory, String fileName) {
        this.filePath = fileDirectory + "\\" + fileName;
        this.gson = new GsonBuilder().create();
        ensureConfigurationFileExists();
    }

    private void ensureConfigurationFileExists() {
        try {
            EDUS2ConfigurationDto dto = getDto();
            if (dto.systemIdentifier == null) {
                dto.systemIdentifier = SystemIdentifier.ofRandom().getSystemIdentifier();
                saveToFile(gson.toJson(dto), filePath);
            }
        } catch (MissingConfigurationFileException e) {
            EDUS2ConfigurationDto dto = new EDUS2ConfigurationDto();
            dto.systemIdentifier = SystemIdentifier.ofRandom().getSystemIdentifier();
            saveToFile(gson.toJson(dto), filePath);
        }
    }

    @Override
    public Optional<Integer> getMinimumVideoHeight() {
        return Optional.ofNullable(getDto().minimumVideoHeight);
    }

    @Override
    public Optional<Integer> getMinimumVideoWidth() {
        return Optional.ofNullable(getDto().minimumVideoWidth);
    }

    @Override
    public Optional<String> getHashedPassword() {
        return Optional.ofNullable(getDto().hashedPassword);
    }

    @Override
    public Optional<String> getSaveFileLocation() {
        return Optional.ofNullable(getDto().saveFileLocation);
    }

    @Override
    public Optional<File> getDefaultScenarioDirectory() {
        return Optional.ofNullable(getDto().defaultScenarioDirectory).map(File::new);
    }

    @Override
    public SystemIdentifier getSystemIdentifier() {
        return SystemIdentifier.of(getDto().systemIdentifier);
    }

    @Override
    public boolean displayedPhoneHomeWarning() {
        return getDto().shownPhoneHomeWarning;
    }

    @Override
    public void setMinimumVideoHeight(int minimumVideoHeight) {
        EDUS2ConfigurationDto dto = getDto();
        dto.minimumVideoHeight = minimumVideoHeight;
        saveToFile(gson.toJson(dto), filePath);
    }

    @Override
    public void setMinimumVideoWidth(int minimumVideoWidth) {
        EDUS2ConfigurationDto dto = getDto();
        dto.minimumVideoWidth = minimumVideoWidth;
        saveToFile(gson.toJson(dto), filePath);
    }

    @Override
    public void setHashedPassword(String hashedPassword) {
        EDUS2ConfigurationDto dto = getDto();
        dto.hashedPassword = hashedPassword;
        saveToFile(gson.toJson(dto), filePath);
    }

    @Override
    public void setSaveFileLocation(String saveFileLocation) {
        EDUS2ConfigurationDto dto = getDto();
        dto.saveFileLocation = saveFileLocation;
        saveToFile(gson.toJson(dto), filePath);
    }

    @Override
    public void setDefaultScenarioDirectory(File defaultScenarioDirectory) {
        EDUS2ConfigurationDto dto = getDto();
        dto.defaultScenarioDirectory = defaultScenarioDirectory.getAbsolutePath();
        saveToFile(gson.toJson(dto), filePath);
    }

    @Override
    public void tripDisplayedPhoneHomeWarning() {
        EDUS2ConfigurationDto dto = getDto();
        dto.shownPhoneHomeWarning = true;
        saveToFile(gson.toJson(dto), filePath);
    }

    private EDUS2ConfigurationDto getDto() {
        Optional<String> fileContents = readFileContents(filePath);
        return fileContents
                .map(s -> gson.fromJson(s, EDUS2ConfigurationDto.class))
                .orElseThrow(() -> new MissingConfigurationFileException(format("Could not read configuration file from %s", filePath)));
    }

    private static class EDUS2ConfigurationDto {
        Integer minimumVideoHeight;
        Integer minimumVideoWidth;
        String hashedPassword;
        String saveFileLocation;
        String defaultScenarioDirectory;
        String systemIdentifier;
        boolean shownPhoneHomeWarning;
    }
}
