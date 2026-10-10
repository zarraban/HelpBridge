@ApplicationModule(
        allowedDependencies = {"users", "users::exception", "users::*"},
        type = ApplicationModule.Type.OPEN
)
package com.example.help_bridge.fundraising;

import org.springframework.modulith.ApplicationModule;
