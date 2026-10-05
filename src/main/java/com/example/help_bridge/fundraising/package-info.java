@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"users", "users::exception", "users::*"},
        type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package com.example.help_bridge.fundraising;
