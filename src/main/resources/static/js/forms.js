(() => {
    const login = document.getElementById('login-form');
    if (login) {
        login.addEventListener('submit', () => {
            document.getElementById('username').value =
                document.getElementById('accountType').value + ':' + document.getElementById('email').value.trim();
        });
    }

    const assistanceType = document.getElementById('assistanceType');
    const otherTypeField = document.getElementById('otherTypeField');
    if (assistanceType && otherTypeField) {
        const toggleOtherType = () => {
            const isOther = assistanceType.value === 'OTHER';
            otherTypeField.style.display = isOther ? '' : 'none';
            document.getElementById('otherAssistanceType').setAttribute('aria-required', String(isOther));
        };
        assistanceType.addEventListener('change', toggleOtherType);
        toggleOtherType();
    }

    document.querySelectorAll('form[data-submit-once]').forEach(form => {
        const buttons = [...form.querySelectorAll('button[type="submit"]')];
        const status = form.querySelector('[data-submit-status]');
        const reset = () => {
            delete form.dataset.submitting;
            form.removeAttribute('aria-busy');
            buttons.forEach(button => { button.disabled = false; });
            if (status) status.hidden = true;
        };
        form.addEventListener('submit', event => {
            if (event.defaultPrevented) return;
            if (form.dataset.submitting) {
                event.preventDefault();
                return;
            }
            form.dataset.submitting = 'true';
            form.setAttribute('aria-busy', 'true');
            buttons.forEach(button => { button.disabled = true; });
            if (status) status.hidden = false;
        });
        window.addEventListener('pageshow', reset);
    });

    const invalidField = document.querySelector('[aria-invalid="true"]');
    if (invalidField && !invalidField.closest('[style*="display:none"]')) invalidField.focus();
})();
