package edus2.domain;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static edus2.TestUtil.randomInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public abstract class EDUS2ConfigurationTest {
    private EDUS2Configuration configuration;

    public abstract EDUS2Configuration getConfiguration();
    
    @Before
    public void setup() {
        this.configuration = getConfiguration();
    }
    
    @Test
    public void getMinimumVideoHeight_shouldReturnEmpty_whenHeightNotSet() {
        // Act
        Optional<Integer> actual = configuration.getMinimumVideoHeight();
        
        // Assert
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void getMinimumVideoHeight_shouldReturnValue_whenValueSaved() {
        // Arrange
        int expected = randomInt();
        configuration.setMinimumVideoHeight(expected);

        // Act
        Optional<Integer> actual = configuration.getMinimumVideoHeight();

        // Assert
        assertEquals(Optional.of(expected), actual);
    }

    @Test
    public void getMinimumVideoWidth_shouldReturnEmpty_whenWidthNotSet() {
        // Act
        Optional<Integer> actual = configuration.getMinimumVideoWidth();

        // Assert
        assertFalse(actual.isPresent());
    }

    @Test
    public void getMinimumVideoWidth_shouldReturnValue_whenValueSaved() {
        // Arrange
        int expected = randomInt();
        configuration.setMinimumVideoWidth(expected);

        // Act
        Optional<Integer> actual = configuration.getMinimumVideoWidth();

        // Assert
        assertEquals(Optional.of(expected), actual);
    }

    @Test
    public void getHashedPassword_shouldReturnEmpty_whenNoPasswordSet() {
        // Act
        Optional<String> actual = configuration.getHashedPassword();

        // Assert
        assertFalse(actual.isPresent());
    }

    @Test
    public void getHashedPassword_shouldReturnHashedPassword_whenPasswordSet() {
        // Arrange
        configuration.setHashedPassword("hashedPassword");

        // Act
        Optional<String> actual = configuration.getHashedPassword();

        // Assert
        assertEquals(Optional.of("hashedPassword"), actual);
    }

    @Test
    public void getScanFileLocation_shouldReturnEmpty_whenNoLocationSpecified() {
        // Act
        Optional<String> actual = configuration.getScanFileLocation();

        // Assert
        assertFalse(actual.isPresent());
    }

    @Test
    public void getScanFileLocation_shouldReturnScanLocation_whenLocationSet() {
        // Arrange
        configuration.setScanFileLocation("file/scan/location.json");

        // Act
        Optional<String> actual = configuration.getScanFileLocation();

        // Assert
        assertEquals(Optional.of("file/scan/location.json"), actual);
    }
}