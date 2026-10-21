package com.example.help_bridge.fundraising.request.config;

import com.example.help_bridge.fundraising.request.entity.AssistanceType;
import com.example.help_bridge.fundraising.request.repository.AssistanceTypeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AssistanceTypeInitializer implements ApplicationRunner {

    private static final List<AssistanceType> DEFAULT_TYPES = List.of(
            new AssistanceType("MILITARY", "Військова потреба", 1),
            new AssistanceType("MEDICAL", "Лікування", 2),
            new AssistanceType("SURGERY", "Операція", 3),
            new AssistanceType("HOUSING", "Втрата/пошкодження житла", 4),
            new AssistanceType("FINANCIAL", "Фінансова допомога", 5),
            new AssistanceType("OTHER", "Інше", 6)
    );

    private final AssistanceTypeRepository assistanceTypeRepository;

    public AssistanceTypeInitializer(AssistanceTypeRepository assistanceTypeRepository) {
        this.assistanceTypeRepository = assistanceTypeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        DEFAULT_TYPES.stream()
                .filter(type -> !assistanceTypeRepository.existsById(type.getCode()))
                .forEach(assistanceTypeRepository::save);
    }
}
