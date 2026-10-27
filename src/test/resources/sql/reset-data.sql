-- Очищає дані, створені тестом. Довідник assistance_type заповнює AssistanceTypeInitializer
-- під час старту контексту, тому з нього видаляються лише додані тестами коди.
TRUNCATE TABLE
    mailing_recipient,
    mailing_task,
    donors,
    evidences,
    fundraiser_assignment,
    fundraiser,
    request_documents,
    requests,
    verification_acts,
    volunteers,
    fund_representatives,
    funds,
    system_admins,
    users
    RESTART IDENTITY CASCADE;

DELETE FROM assistance_type
WHERE code NOT IN ('MILITARY', 'MEDICAL', 'SURGERY', 'HOUSING', 'FINANCIAL', 'OTHER');
