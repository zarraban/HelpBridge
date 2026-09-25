package com.example.help_bridge.modulithTest;

import com.example.help_bridge.HelpBridgeApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithTest {

    @Test
    void verifyModularity(){
        ApplicationModules.of(HelpBridgeApplication.class).verify();
    }
}
