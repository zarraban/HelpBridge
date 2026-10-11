@ApplicationModule(
        allowedDependencies = {"fundraising", "fundraising::exception", "fundraising::*"},
        type = ApplicationModule.Type.OPEN
)
package com.example.help_bridge.users;

import org.springframework.modulith.ApplicationModule;
