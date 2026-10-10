package com.example.help_bridge.fundraising.fundraiser.spi;

/**
 * Port implemented by the volunteer module, so assignments can be validated
 * without the fundraiser module depending on volunteer classes.
 */
public interface VolunteerDirectory {

    /**
     * @throws RuntimeException a "not found" domain exception if the volunteer does not exist or is inactive
     */
    void requireActiveVolunteer(Long volunteerId);
}
